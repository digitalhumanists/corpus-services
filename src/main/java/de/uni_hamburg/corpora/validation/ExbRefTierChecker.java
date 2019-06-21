package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class that checks reference tiers in exb files and finds out whether or not
 * the order of the numbering and speaker reference are correct. If there are
 * mistakes in the ref tiers, it corrects them thanks to its fix function.
 */
public class ExbRefTierChecker extends Checker implements CorpusFunction {

    String tierLoc = "";
    final String ertc = "exb-ref-tier-checker";

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, tierLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, tierLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, tierLoc + ": Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, tierLoc + ": Unknown file reading error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, tierLoc + ": Unknown file reading error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; checks reference tiers in exb files
     * and finds out whether or not the order of the numbering and speaker
     * reference are correct. Issues appropriate warnings with regard to the
     * mistakes.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, TransformerException, XPathExpressionException {
        Report stats = new Report(); // create a new report for the transcript
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
        ArrayList<Element> refTiers = new ArrayList();
        ArrayList<String> speakerNames = new ArrayList();
        for (int i = 0; i < tiers.getLength(); i++) { // loop for dealing with each tier
            Element tier = (Element) tiers.item(i);
            String category = tier.getAttribute("category"); // get category  
            String speakerName = tier.getAttribute("speaker"); // get speaker name
            if (category.equals("ref")) {
                refTiers.add(tier);
                speakerNames.add(speakerName);
            }
        }
        if (refTiers.size() == 0) { //when there is no reference tier present 
            stats.addWarning(ertc, "There is no reference tier present in transcript "
                    + transcriptName);
            exmaError.addError(ertc, cd.getURL().getFile(), "", "", false, "There is no reference "
                    + "tier present in transcript " + transcriptName);
        } else if (refTiers.size() == 1) {  //when there is only one speaker ref present
            NodeList events = refTiers.get(0).getElementsByTagName("event");
            String tierId = refTiers.get(0).getAttribute("id");
            int order = 1;
            for (int i = 0; i < events.getLength(); i++) {  // reference events
                Element event = (Element) events.item(i);
                String eventStart = event.getAttribute("start");
                String eventEnd = event.getAttribute("end");
                String wholeRef = event.getTextContent();
                if (wholeRef.contains("(") && wholeRef.contains(".")) {
                    int end = wholeRef.indexOf("(") - 1;
                    int start = wholeRef.substring(0, end).lastIndexOf(".") + 1;
                    int numbering = Integer.parseInt(wholeRef.substring(start, end));
                    if (order != numbering) {
                        stats.addCritical(ertc, "False numbering of the event starting "
                                + eventStart + " ending " + eventEnd + " in the reference"
                                + " tier with id " + tierId + "in the exb file with "
                                + "transcript name " + transcriptName + ". The event"
                                + " has to be numbered as " + order + " instead of "
                                + numbering + ".");
                        System.out.println("False numbering of the event starting "
                                + eventStart + " ending " + eventEnd + " in the reference"
                                + " tier with id " + tierId + "in the exb file with "
                                + "transcript name " + transcriptName + ". The event"
                                + " has to be numbered as " + order + " instead of "
                                + numbering + ".");
                        exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, false,
                                "False numbering of the event starting "
                                + eventStart + " ending " + eventEnd + " in the reference"
                                + " tier with id " + tierId + "in the exb file with "
                                + "transcript name " + transcriptName + ". The event"
                                + " has to be numbered as " + order + " instead of "
                                + numbering + ".");
                    }
                    order++;
                } else {
                    stats.addCritical(ertc, "Unknown format of numbering of the "
                            + "reference tier events in transcript " + transcriptName);
                    exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, false,
                            "Unknown format of numbering of the "
                            + "reference tier events in transcript " + transcriptName);
                    break;
                }
            }
        } else {  // when there are multiple speakers present
            for (int i = 0; i < refTiers.size(); i++) {
                NodeList events = refTiers.get(i).getElementsByTagName("event");
                String tierId = refTiers.get(i).getAttribute("id");
                String tierSpeaker = refTiers.get(i).getAttribute("speaker");
                int order = 1;
                for (int j = 0; j < events.getLength(); j++) {  // reference events
                    Element event = (Element) events.item(j);
                    String eventStart = event.getAttribute("start");
                    String eventEnd = event.getAttribute("end");
                    String wholeRef = event.getTextContent();
                    if (wholeRef.contains("(") && wholeRef.contains(".")) {
                        int end = wholeRef.indexOf("(") - 1;
                        int start = wholeRef.substring(0, end).lastIndexOf(".") + 1;
                        int numbering = Integer.parseInt(wholeRef.substring(start, end));
                        int refEnd = start - 1;
                        int refStart = -1;
                        String speakerCode = null;
                        if (wholeRef.substring(0, refEnd).contains(".")) {
                            refStart = wholeRef.substring(0, refEnd).lastIndexOf(".") + 1;
                            speakerCode = wholeRef.substring(refStart, refEnd);
                        }
                        if (order != numbering) {
                            stats.addCritical(ertc, "False numbering of the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            System.out.println("False numbering of the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, false,
                                    "False numbering of the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                        }
                        order++;
                        if (speakerCode != null) {
                            if (!speakerCode.equals(tierSpeaker)) {
                                stats.addCritical(ertc, "False speaker code for the event starting "
                                        + eventStart + " ending " + eventEnd + " in the reference"
                                        + " tier with id " + tierId + "in the exb file with "
                                        + "transcript name " + transcriptName + ". The speaker code"
                                        + " of the event has to be " + tierSpeaker + " instead of "
                                        + speakerCode + ".");
                                System.out.println("False speaker code for the event starting "
                                        + eventStart + " ending " + eventEnd + " in the reference"
                                        + " tier with id " + tierId + "in the exb file with "
                                        + "transcript name " + transcriptName + ". The speaker code"
                                        + " of the event has to be " + tierSpeaker + " instead of "
                                        + speakerCode + ".");
                                exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, false,
                                        "False speaker code for the event starting "
                                        + eventStart + " ending " + eventEnd + " in the reference"
                                        + " tier with id " + tierId + "in the exb file with "
                                        + "transcript name " + transcriptName + ". The speaker code"
                                        + " of the event has to be " + tierSpeaker + " instead of "
                                        + speakerCode + ".");
                            }
                        } else {
                            stats.addCritical(ertc, "There is no speaker code for the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            System.out.println("There is no speaker code for the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, false,
                                    "There is no speaker code for the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                        }
                    } else {
                        stats.addCritical(ertc, "Unknown format of numbering of the "
                                + "reference tier events in transcript " + transcriptName);
                        exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, false,
                                "Unknown format of numbering of the reference tier "
                                + "events in transcript " + transcriptName);
                        break;
                    }
                }
            }
        }
        return stats; // return all the warnings
    }

    /**
     * Method for correcting the mistakes in the reference tiers.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        Report stats = new Report(); // create a new report for the transcript
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ExbRefTierChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document doc = null;
        try {
            doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        } catch (TransformerException ex) {
            Logger.getLogger(ExbRefTierChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ExbRefTierChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ExbRefTierChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        String transcriptName;
        if (doc.getElementsByTagName("transcription-name").getLength() > 0) {   // check if transcript name exists for the exb file
            transcriptName = doc.getElementsByTagName("transcription-name").item(0).getTextContent(); // get transcript name
        } else {
            transcriptName = "No Name Transcript";
        }
        NodeList tiers = doc.getElementsByTagName("tier"); // get all tiers of the transcript      
        ArrayList<Element> refTiers = new ArrayList();
        ArrayList<String> speakerNames = new ArrayList();
        for (int i = 0; i < tiers.getLength(); i++) { // loop for dealing with each tier
            Element tier = (Element) tiers.item(i);
            String category = tier.getAttribute("category"); // get category  
            String speakerName = tier.getAttribute("speaker"); // get speaker name
            if (category.equals("ref")) {
                refTiers.add(tier);
                speakerNames.add(speakerName);
            }
        }
        if (refTiers.size() == 0) { //when there is no reference tier present 
            stats.addWarning(ertc, "There is no reference tier present in transcript "
                    + transcriptName);
            exmaError.addError(ertc, cd.getURL().getFile(), "", "", false,
                    "There is no reference tier present in transcript "
                    + transcriptName);
        } else if (refTiers.size() == 1) {  //when there is only one speaker ref present
            NodeList events = refTiers.get(0).getElementsByTagName("event");
            String tierId = refTiers.get(0).getAttribute("id");
            int order = 1;
            for (int i = 0; i < events.getLength(); i++) {  // reference events
                Element event = (Element) events.item(i);
                String eventStart = event.getAttribute("start");
                String eventEnd = event.getAttribute("end");
                String wholeRef = event.getTextContent();
                if (wholeRef.contains("(") && wholeRef.contains(".")) {
                    int end = wholeRef.indexOf("(") - 1;
                    int start = wholeRef.substring(0, end).lastIndexOf(".") + 1;
                    String no = wholeRef.substring(start, end);
                    int numbering = Integer.parseInt(no);
                    if (order != numbering) {
                        stats.addCritical(ertc, "False numbering of the event starting "
                                + eventStart + " ending " + eventEnd + " in the reference"
                                + " tier with id " + tierId + "in the exb file with "
                                + "transcript name " + transcriptName);
                        System.out.println("False numbering of the event starting "
                                + eventStart + " ending " + eventEnd + " in the reference"
                                + " tier with id " + tierId + "in the exb file with "
                                + "transcript name " + transcriptName);
                        exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, true,
                                "False numbering of the event starting "
                                + eventStart + " ending " + eventEnd + " in the reference"
                                + " tier with id " + tierId + "in the exb file with "
                                + "transcript name " + transcriptName);
                        String correctNo = String.format("%0" + no.length() + "d", order);
                        String correctRef = wholeRef.substring(0, start) + correctNo + wholeRef.substring(end);
                        event.setTextContent(correctRef);
                        stats.addNote(ertc, "The event has been renumbered as "
                                + correctNo);
                    }
                    order++;
                } else {
                    stats.addCritical(ertc, "Unknown format of numbering of the "
                            + "reference tier events in transcript " + transcriptName);
                    exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, false,
                            "Unknown format of numbering of the "
                            + "reference tier events in transcript " + transcriptName);
                    break;
                }
            }
        } else {  // when there are multiple speakers present
            for (int i = 0; i < refTiers.size(); i++) {
                NodeList events = refTiers.get(i).getElementsByTagName("event");
                String tierId = refTiers.get(i).getAttribute("id");
                String tierSpeaker = refTiers.get(i).getAttribute("speaker");
                int order = 1;
                for (int j = 0; j < events.getLength(); j++) {  // reference events
                    Element event = (Element) events.item(j);
                    String eventStart = event.getAttribute("start");
                    String eventEnd = event.getAttribute("end");
                    String wholeRef = event.getTextContent();
                    if (wholeRef.contains("(") && wholeRef.contains(".")) {
                        int end = wholeRef.indexOf("(") - 1;
                        int start = wholeRef.substring(0, end).lastIndexOf(".") + 1;
                        int numbering = Integer.parseInt(wholeRef.substring(start, end));
                        String no = wholeRef.substring(start, end);
                        int refEnd = start - 1;
                        int refStart = -1;
                        String speakerCode = null;
                        if (wholeRef.substring(0, refEnd).contains(".")) {
                            refStart = wholeRef.substring(0, refEnd).lastIndexOf(".") + 1;
                            speakerCode = wholeRef.substring(refStart, refEnd);
                        }
                        if (order != numbering) {
                            stats.addCritical(ertc, "False numbering of the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            System.out.println("False numbering of the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, true,
                                    "False numbering of the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            String correctNo = String.format("%0" + no.length() + "d", order);
                            String correctRef = wholeRef.substring(0, start) + correctNo + wholeRef.substring(end);
                            event.setTextContent(correctRef);
                            stats.addNote(ertc, "The event has been renumbered as "
                                    + correctNo);
                            System.out.println("The event has been renumbered as "
                                    + correctNo);
                        }
                        order++;
                        if (speakerCode != null) {
                            if (!speakerCode.equals(tierSpeaker)) {
                                stats.addCritical(ertc, "False speaker code for the event starting "
                                        + eventStart + " ending " + eventEnd + " in the reference"
                                        + " tier with id " + tierId + "in the exb file with "
                                        + "transcript name " + transcriptName + ". The speaker code"
                                        + " of the event has to be " + tierSpeaker + " instead of "
                                        + speakerCode + ".");
                                System.out.println("False speaker code for the event starting "
                                        + eventStart + " ending " + eventEnd + " in the reference"
                                        + " tier with id " + tierId + "in the exb file with "
                                        + "transcript name " + transcriptName + ". The speaker code"
                                        + " of the event has to be " + tierSpeaker + " instead of "
                                        + speakerCode + ".");
                                exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, true,
                                        "False speaker code for the event starting "
                                        + eventStart + " ending " + eventEnd + " in the reference"
                                        + " tier with id " + tierId + "in the exb file with "
                                        + "transcript name " + transcriptName);
                                String correctRef = event.getTextContent().substring(0, refStart) + tierSpeaker + event.getTextContent().substring(refEnd);
                                event.setTextContent(correctRef);
                                stats.addNote(ertc, "The speaker code has been changed as "
                                        + tierSpeaker);
                                System.out.println("The speaker code has been changed as "
                                        + tierSpeaker);
                            }
                        } else {
                            stats.addCritical(ertc, "There is no speaker code for the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            System.out.println("There is no speaker code for the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, true,
                                    "There is no speaker code for the event starting "
                                    + eventStart + " ending " + eventEnd + " in the reference"
                                    + " tier with id " + tierId + "in the exb file with "
                                    + "transcript name " + transcriptName);
                            String correctRef = event.getTextContent().substring(0, start - 1) + "." + tierSpeaker + event.getTextContent().substring(refEnd);
                            event.setTextContent(correctRef);
                            stats.addNote(ertc, "The speaker code has been defined as "
                                    + tierSpeaker);
                            System.out.println("The speaker code has been defined as "
                                    + tierSpeaker);
                        }
                    } else {
                        stats.addCritical(ertc, "Unknown format of numbering of the "
                                + "reference tier events in transcript " + transcriptName);
                        exmaError.addError(ertc, cd.getURL().getFile(), tierId, eventStart, false,
                                "Unknown format of numbering of the "
                                + "reference tier events in transcript " + transcriptName);
                        break;
                    }
                }
            }
        }
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ExbRefTierChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        StreamResult result = new StreamResult(new FileOutputStream(cd.getURL().getPath()));
        DOMSource source = new DOMSource(doc);
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Logger.getLogger(ExbRefTierChecker.class.getName()).log(Level.SEVERE, null, ex);
        }

        return stats; // return all the warnings
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
            Logger.getLogger(ExbRefTierChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
}
