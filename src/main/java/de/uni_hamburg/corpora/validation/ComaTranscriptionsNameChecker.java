package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.ComaData;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.XMLData;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class that checks whether or not there is a mismatch between basic and
 * segmented names, basic and segmented file names, plus their NSLinks for each
 * communication in the coma file.
 */
public class ComaTranscriptionsNameChecker extends Checker implements CorpusFunction {

    public ComaTranscriptionsNameChecker() {
        super("ComaTranscriptionsNameChecker");
    }

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
            stats.addException(pce, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, function, cd, "Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown file reading error.");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown file reading error.");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; issues warnings with respect to
     * mismatches between basic and segmented names, basic and segmented file
     * names, plus their NSLinks for each communication in the coma file and add
     * those warnings to the report which it returns.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException {
        Document doc = null;
        ComaData ccd = new ComaData();
        ccd = (ComaData) cd;
        doc = TypeConverter.JdomDocument2W3cDocument(ccd.getJdom()); // get the file as a document      
        NodeList communications = doc.getElementsByTagName("Communication"); // divide by Communication tags
        Report stats = new Report(); //create a new report

        for (int i = 0; i < communications.getLength(); i++) { //iterate through communications
            Element communication = (Element) communications.item(i);
            NodeList transcriptions = communication.getElementsByTagName("Transcription"); // get transcriptions of current communication
            String communicationID = communication.getAttribute("Id"); // get communication id to use it in the warning
            String communicationName = communication.getAttribute("Name"); // get communication name to use it in the warning

            String basicTranscriptName = "";
            String basicFileName = "";
            String basicNSLink = "";
            String segmentedTranscriptName = "";
            String segmentedFileName = "";
            String segmentedNSLink = "";
            if (transcriptions.getLength() > 0) {  // check if there is at least one transcription for the communication
                for (int j = 0; j < transcriptions.getLength(); j++) {   // iterate through transcriptions 
                    Element transcription = (Element) transcriptions.item(j);
                    NodeList keys = transcription.getElementsByTagName("Key");  // get keys of current transcription
                    boolean segmented = false;   // flag for distinguishing basic file from segmented file 
                    for (int k = 0; k < keys.getLength(); k++) {  // look for the key with "segmented" attribute 
                        Element key = (Element) keys.item(k);
                        if (key.getAttribute("Name").equals("segmented")) {
                            String seg = key.getTextContent();
                            if (seg.equals("true")) // check if transcription is segmented or not
                            {
                                segmented = true;        // if segmented transcription then turn the flag true
                            }
                            break;
                        }
                    }
                    if (!segmented) { // get name, file name and nslink of basic transcription
                        basicTranscriptName = transcription.getElementsByTagName("Name").item(0).getTextContent();
                        basicFileName = transcription.getElementsByTagName("Filename").item(0).getTextContent();
                        basicNSLink = transcription.getElementsByTagName("NSLink").item(0).getTextContent();
                    } else {  // get name, file name and nslink of segmented transcription
                        segmentedTranscriptName = transcription.getElementsByTagName("Name").item(0).getTextContent();
                        segmentedFileName = transcription.getElementsByTagName("Filename").item(0).getTextContent();
                        segmentedNSLink = transcription.getElementsByTagName("NSLink").item(0).getTextContent();
                    }
                }

                if (!basicTranscriptName.isEmpty() && !segmentedTranscriptName.isEmpty()) {
                    if (!basicTranscriptName.equals(segmentedTranscriptName)) { // check for mismatch between names
                        // issue a warning if necessary
                        System.err.println("Basic transcription name and segmented transcription name do not match "
                                + "for communication " + communicationName + ", id: " + communicationID + ".");
                        stats.addCritical(function, cd, "Transcript name mismatch exb: " + basicTranscriptName + " exs: " + segmentedTranscriptName
                                + " for communication " + communicationName + ".");
                    } else {
                        stats.addCorrect(function, cd, "Transcript name matches exb: " + basicTranscriptName + " exs: " + segmentedTranscriptName
                                + " for communication " + communicationName + ".");
                    }
                }
                if (!basicFileName.isEmpty() && !segmentedFileName.isEmpty()) {
                    // check for mismatch between file names, issue a warning if necessary
                    if (!basicFileName.substring(0, basicFileName.lastIndexOf(".")).equals(segmentedFileName.substring(0, segmentedFileName.lastIndexOf("_")))) {
                        System.err.println("Basic file name and segmented file name do not match "
                                + "for communication " + communicationName + ", id: " + communicationID + ".");
                        stats.addCritical(function, cd, "Basic file name mismatch exb: " + basicFileName.substring(0, basicFileName.lastIndexOf(".")) + " exs: " + segmentedFileName.substring(0, segmentedFileName.lastIndexOf("_"))
                                + " for communication " + communicationName + ".");
                    } else {
                        stats.addCorrect(function, cd, "Basic file name matches exb: " + basicFileName.substring(0, basicFileName.lastIndexOf(".")) + " exs: " + segmentedFileName.substring(0, segmentedFileName.lastIndexOf("_"))
                                + " for communication " + communicationName + ".");
                    }
                }
                if (!basicNSLink.isEmpty() && !segmentedNSLink.isEmpty()) {
                    // check for mismatch between nslinks, issue a warning if necessary
                    if (!basicNSLink.substring(0, basicNSLink.lastIndexOf(".")).equals(segmentedNSLink.substring(0, segmentedNSLink.lastIndexOf("_")))) {
                        System.err.println("Basic NSLink and segmented NSLink do not match "
                                + "for communication " + communicationName + ", id: " + communicationID + ".");
                        stats.addCritical(function, cd, "NSLink filename mismatch exb: " + basicNSLink.substring(0, basicNSLink.lastIndexOf(".")) + " exs: " + segmentedNSLink.substring(0, segmentedNSLink.lastIndexOf("_"))
                                + " for communication " + communicationName + ".");
                    } else {
                        stats.addCorrect(function, cd, "NSLink filename matches exb: " + basicNSLink.substring(0, basicNSLink.lastIndexOf(".")) + " exs: " + segmentedNSLink.substring(0, segmentedNSLink.lastIndexOf("_"))
                                + " for communication " + communicationName + ".");
                    }
                }
            } else {
                System.err.println("No transcriptions found "
                        + "for communication " + communicationName + ", id: " + communicationID + ".");
                stats.addCorrect(function, cd, "No transcript found to be compared "
                        + "for communication " + communicationName + ".");
            }

        }
        return stats; // return the report with warnings
    }

    /**
     * Fixing the conflicts in coma file with regards to this feature is not
     * supported yet.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {

        // fix transcription names (XPath: //Transcription/Name) which are unequals base of filename (XPath: //Transcription/Filename)
        Document doc = null;
        ComaData ccd = new ComaData();
        ccd = (ComaData) cd;
        doc = TypeConverter.JdomDocument2W3cDocument(ccd.getJdom()); // get the file as a document       
        NodeList communications = doc.getElementsByTagName("Communication"); // divide by Communication tags
        Report stats = new Report(); //create a new report

        for (int i = 0; i < communications.getLength(); i++) { //iterate through communications
            Element communication = (Element) communications.item(i);
            NodeList transcriptions = communication.getElementsByTagName("Transcription"); // get transcriptions of current communication
            String communicationID = communication.getAttribute("Id"); // get communication id to use it in the warning
            String communicationName = communication.getAttribute("Name"); // get communication name to use it in the warning

            String transcriptName = "";
            String fileName = "";
            if (transcriptions.getLength() > 0) {  // check if there is at least one transcription for the communication
                for (int j = 0; j < transcriptions.getLength(); j++) {   // iterate through transcriptions 
                    Element transcription = (Element) transcriptions.item(j);

                    transcriptName = transcription.getElementsByTagName("Name").item(0).getTextContent();
                    fileName = transcription.getElementsByTagName("Filename").item(0).getTextContent();
                    String baseFileName = fileName.replaceAll("(\\.exb|(_s)?\\.exs)$", "");

                    if (!transcriptName.equals(baseFileName)) {

                        // fix the transcription Name
                        transcription.getElementsByTagName("Name").item(0).setTextContent(baseFileName);
                        stats.addFix(function, cd, "Transcription/Name (" + transcriptName + ") changed to base file name (" + baseFileName + ").");

                    }
                }

            } else {
                String message = "No transcription found  for communication " + communicationName + ", id: " + communicationID + ".";
                System.err.println(message);
                stats.addCorrect(function, cd, message);
            }

        }
        //then save file
        CorpusIO cio = new CorpusIO();
        cd.updateUnformattedString(TypeConverter.W3cDocument2String(doc));
        XMLData xml = (XMLData) cd;
        org.jdom.Document jdomDoc = TypeConverter.W3cDocument2JdomDocument(doc);
        xml.setJdom(jdomDoc);
        cd = (CorpusData) xml;
        try {
            cd.updateUnformattedString(TypeConverter.JdomDocument2String(jdomDoc));
            cio.write(cd, cd.getURL());
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown Transformer Exception");
        } catch (ParserConfigurationException ex) {
            stats.addException(ex, function, cd, "Unknown Parser Exception");
        } catch (UnsupportedEncodingException ex) {
            stats.addException(ex, function, cd, "Unknown Encoding Exception");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown Xpath Exception");
        }
        return stats;
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "unknown class not found error");
        }
        return IsUsableFor;
    }

    /**
     * Default function which returns a two/three line description of what this
     * class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class checks whether or not there is a mismatch "
                + "between basic and segmented names, basic and segmented file names, "
                + "plus their NSLinks for each communication in the coma file.";
        return description;
    }
}
