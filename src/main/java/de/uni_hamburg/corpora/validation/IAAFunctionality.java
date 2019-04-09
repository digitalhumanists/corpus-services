package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The class that calculates IAA according to Krippendorff's alpha for Exb files; 
 * only cares for annotation labels, assuming that transcription structure and 
 * text remains the same.Checks and puts them in the error lists if different versions 
 * of the same file have different annotations for the same event/token. Moreover, this
 * functionality includes the inter-annotator agreement: percentage of overlapping 
 * choices between the annotators.
 */

public class IAAFunctionality extends Checker implements CorpusFunction {

    String annotLoc = "";
    HashMap<String, HashMap<String, String>> annotations; // hash map for holding annotations of exb files
    HashMap<String, Collection<String>> distinctAnnotations; // hash map for storing distinct annots for each transcription file
    HashMap<String, HashMap<String, String>> annotationsTwo; // hash map for holding annotations of second exb files
    HashMap<String, Integer> noOfSubCategories; // hash map for holding number of subcategories for every category
    HashMap<String, String> subCategoryToCategory; // hash map for holding parent categories for sub categories
    private int noOfAnnotations = 0;     // total no of annotations
    private int noOfDifferentAnnotations = 0; // total number of different annotations between different two different versions 

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) throws JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, annotLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, annotLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, annotLoc + ": Unknown file reading error");
        }
        return stats;
    }


    /**
     * Main functionality of the feature; check if there is any mismatch between
     * the annotations of the same event/token between the different versions of
     * the EXB file, calculate the percentage of overlapping annotations and
     * inter annotator agreement according to Krippendorff's alpha.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException {
        Report stats = new Report(); //create a new report
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        String transcriptName;
        if (doc.getElementsByTagName("transcription-name").getLength() > 0) {   // check if transcript name exists for the exb file
            transcriptName = doc.getElementsByTagName("transcription-name").item(0).getTextContent(); // get transcript name
        } else {
            transcriptName = "No Name Transcript";
        }
        NodeList tiers = doc.getElementsByTagName("tier"); // get all tiers of the transcript
        //initialise the hash map only the first time when this function is called
        if (annotations == null) {
            annotations = new HashMap<>();
        }
        if (distinctAnnotations == null) {
            distinctAnnotations = new HashMap<>();
        }
        //if annotations hash map doesn't contain the transcript's name it means 
        //that it is the first time a version of this file is encountered.
        if (!annotations.containsKey(transcriptName)) {
            Collection<String> c = new ArrayList<>();    // collection for adding annotations into hash map
            HashMap<String, String> h = new HashMap<>();
            for (int i = 0; i < tiers.getLength(); i++) { // loop for dealing with each tier
                Element tier = (Element) tiers.item(i);
                if (tier.getAttribute("type").equals("a") && !tier.getAttribute("category").equals("c")) {     // if it is an annotation tier
                    NodeList events = tier.getElementsByTagName("event");
                    String tierID = tier.getAttribute("id");
                    for (int j = 0; j < events.getLength(); j++) {  // annotation events
                        Element event = (Element) events.item(j);
                        String eventStart = event.getAttribute("start");
                        String eventEnd = event.getAttribute("end");
                        if(!c.contains(event.getTextContent()))      // if annot not already added to the list
                            c.add(event.getTextContent());
                        String key = tierID+"-"+eventStart+"-"+eventEnd;
                        h.put(key, event.getTextContent());
                    }
                }
            }
            if(!h.isEmpty())
                annotations.put(transcriptName, h);  // finally add the annotations of the transcript
            if(!c.isEmpty())
                distinctAnnotations.put(transcriptName, c);
        } else {     // another version of this transcript has already been encountered
            //initialise the hash map only the first time when another version of any transcript is encountered
            if (annotationsTwo == null) {
                annotationsTwo = new HashMap<>();
            }
            int annotationCounter = 0;       // counter for number of annotations
            noOfDifferentAnnotations = 0;
            HashMap<String, String> h = new HashMap<>();
            for (int i = 0; i < tiers.getLength(); i++) { // loop for dealing with each tier
                Element tier = (Element) tiers.item(i);
                HashMap map = new HashMap(annotations.get(transcriptName));
                if (tier.getAttribute("type").equals("a") && !tier.getAttribute("category").equals("c")) {     // if it is an annotation tier
                    NodeList events = tier.getElementsByTagName("event");
                    String tierID = tier.getAttribute("id");
                    for (int j = 0; j < events.getLength(); j++) {
                        Element event = (Element) events.item(j);
                        String eventStart = event.getAttribute("start");
                        String eventEnd = event.getAttribute("end");
                        String key = tierID+"-"+eventStart+"-"+eventEnd;
                        h.put(key, event.getTextContent());
                        annotationCounter++;
                        // check if the event's annotation in one version is same with the same event's annotation in the other version  
                        if (map.containsKey(key)) {
                            if (!map.get(key).equals(event.getTextContent())) {
                                stats.addWarning("iaa-functionality", "Exb file " + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1)
                                        + " is containing a different annotation for the same event (" + eventStart
                                        + ") in its tier " + tierID + " from another version of the same file! This version "
                                        + "has the annotation: " + event.getTextContent() + ", while the other version has the annotation: "
                                        + map.get(key));
                                exmaError.addError("iaa-functionality", cd.getURL().getFile(), tierID, eventStart, false,
                                        "Exb file " + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1)
                                        + " is containing a different annotation for the same event (" + eventStart
                                        + ") in its tier " + tierID + " from another version of the same file! This version "
                                        + "has the annotation: " + event.getTextContent() + ", while the other version has the annotation: "
                                        + map.get(key));
                                noOfDifferentAnnotations++;  // increase the counter for number of different annotations
                            }
                        }else{
                            noOfDifferentAnnotations++;
                        }
                    }
                }
            }
            if(!h.isEmpty())
                annotationsTwo.put(transcriptName, h);  // finally add the annotations of the transcript
            List list = new ArrayList(distinctAnnotations.get(transcriptName));
            float dE = 0;      // expected disagreement for Krippendorff's alpha
            noOfAnnotations = annotationCounter;
            int partOfDenominator = noOfAnnotations*2; //noOfItems*noOfCoders (number of coders = 2 since there are two versions(annotators))
            for (Object event : list) {       // go through every distinct annotation
                String ev = (String) event;
                int eventOccurrence = Collections.frequency(annotations.get(transcriptName).values(), ev);
                int eventOccurrenceTwo = Collections.frequency(annotationsTwo.get(transcriptName).values(), ev);
                int totalFirstEv = eventOccurrence + eventOccurrenceTwo;
                for (Object eventIn : list){
                    String evIn = (String) eventIn;
                    // if it is the same annotation with the upper loop skip to the next one
                    // as the distance is 0 - that cancels out the other operations down there.
                    if(ev.equals(evIn))      
                        continue;                  
                    int secEventOccurrence = Collections.frequency(annotations.get(transcriptName).values(), evIn);
                    int secEventOccurrenceTwo = Collections.frequency(annotationsTwo.get(transcriptName).values(), evIn);
                    int totalSecEv = secEventOccurrence + secEventOccurrenceTwo;
                    dE = dE + (totalFirstEv * totalSecEv) / (float) (partOfDenominator * (partOfDenominator-1));
                }
            }      
            float iaa = (noOfAnnotations - noOfDifferentAnnotations) / (float) noOfAnnotations;   // inter-annotator measure
            float dZero = noOfDifferentAnnotations / (float) noOfAnnotations;      // observed disagreement for Krippendorff's alpha
            float alpha = 1 - ((dZero)/(float)(dE)); // Krippendorff's alpha
            System.out.println("The percentage of overlapping annotations between two versions of "
                    + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1) + " is " + 100 * iaa + "%");
            System.out.println("Inter annotator agreement between two versions of "
                    + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1)
                    + " according to Krippendorff's alpha is " + alpha);
            stats.addNote("iaa-functionality", "The percentage of overlapping annotations between two versions of "
                    + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1) + " is " + 100 * iaa + "%");
            stats.addNote("iaa-functionality", "Inter annotator agreement between two versions of "
                    + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1)
                    + " according to Krippendorff's alpha is " + alpha);
        }
        return stats;
    }

    /**
     * Fix is not yet supported for this functionality.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IAAFunctionality.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
}