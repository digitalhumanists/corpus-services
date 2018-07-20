package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
 * The class that calculates IAA for Exb files; only cares for annotation
 * labels, assuming that transcription structure and text remains the same.
 * Checks and puts them in the error lists if different versions of the same
 * file have different annotations for the same event/token. Moreover, this
 * functionality includes the inter-annotator agreement; percentage of overlapping 
 * choices between the annotators.
 */
public class IAAFunctionality extends Checker implements CorpusFunction {

    String annotLoc = "";
    HashMap<String, Collection<String>> annotations; // hash map for holding annotations of exb files
    private int noOfAnnotations=0;     // total no of annotations
    private int noOfDifferentAnnotations=0; // total number of different annotations between different two different versions 
    
    
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
     * the EXB file.
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
        if(annotations == null){
            annotations = new HashMap<String, Collection<String>>();
        }
        if (!annotations.containsKey(transcriptName)) {
            Collection<String> c = new ArrayList<String>();
            for (int i = 0; i < tiers.getLength(); i++) { // loop for dealing with each tier
                Element tier = (Element) tiers.item(i);
                if (tier.getAttribute("type").equals("a")) {     // if it is an annotation tier
                    NodeList events = tier.getElementsByTagName("event");
                    for (int j = 0; j < events.getLength(); j++) {
                        Element event = (Element) events.item(j);
                        c.add(event.getTextContent());
                    }
                }
            }
            annotations.put(transcriptName, c);
            //c.clear();
        } else {
            int annotationCounter = 0;
            for (int i = 0; i < tiers.getLength(); i++) { // loop for dealing with each tier
                Element tier = (Element) tiers.item(i);
                List list = new ArrayList(annotations.get(transcriptName));
                if (tier.getAttribute("type").equals("a")) {     // if it is an annotation tier
                    NodeList events = tier.getElementsByTagName("event");
                    String tierID = tier.getAttribute("id");
                    for (int j = 0; j < events.getLength(); j++) {
                        Element event = (Element) events.item(j);
                        String eventStart = event.getAttribute("start");
                        if (!list.get(annotationCounter).equals(event.getTextContent())) {
                            stats.addWarning("iaa-functionality", "Exb file " + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1)
                                    + " is containing a different annotation for the same event (" + eventStart
                                    + ") in its tier " + tierID + " from another version of the same file! This version "
                                    + "has the annotation: " + event.getTextContent() + ", while the other version has the annotation: "
                                    + list.get(annotationCounter));
                            exmaError.addError("iaa-functionality", cd.getURL().getFile(), tierID, eventStart, false,
                                    "Exb file " + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1)
                                    + " is containing a different annotation for the same event (" + eventStart
                                    + ") in its tier " + tierID + " from another version of the same file! This version "
                                    + "has the annotation: " + event.getTextContent() + ", while the other version has the annotation: "
                                    + list.get(annotationCounter));
                            noOfDifferentAnnotations++;
                        }
                        annotationCounter++;
                    }
                }
            }
            noOfAnnotations = annotationCounter;
            float iaa = (noOfAnnotations-noOfDifferentAnnotations)/(float)noOfAnnotations;   // inter-annotator measure
            System.out.println("The percentage of overlapping annotations between two versions of "
            + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1) + " is: " + 100*iaa);
            stats.addNote("iaa-functionality", "The percentage of overlapping annotations between two versions of "
            + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1) + " is: " + 100*iaa);
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
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IAAFunctionality.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
}
