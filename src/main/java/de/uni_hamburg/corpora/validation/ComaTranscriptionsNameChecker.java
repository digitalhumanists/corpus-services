package de.uni_hamburg.corpora.validation;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

    String checkname = "ComaTranscriptionsNameChecker";

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
            stats.addException(pce, checkname + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, checkname + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, checkname + ": Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, checkname + ": Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, checkname, cd, "Unknown file reading error.");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, checkname, cd, "Unknown file reading error.");
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
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
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
                        stats.addCritical(checkname, cd, "Transcript name mismatch exb: " + basicTranscriptName + " exs: " + segmentedTranscriptName
                                + " for communication " + communicationName + ".");
                    } else {
                         stats.addCorrect(checkname, cd, "Transcript name matches exb: " + basicTranscriptName + " exs: " + segmentedTranscriptName
                                + " for communication " + communicationName + ".");
                    }
                }
                if (!basicFileName.isEmpty() && !segmentedFileName.isEmpty()) {
                    // check for mismatch between file names, issue a warning if necessary
                    if (!basicFileName.substring(0, basicFileName.lastIndexOf(".")).equals(segmentedFileName.substring(0, segmentedFileName.lastIndexOf("_")))) {
                        System.err.println("Basic file name and segmented file name do not match "
                                + "for communication " + communicationName + ", id: " + communicationID + ".");
                        stats.addCritical(checkname, cd, "Basic file name mismatch exb: " + basicFileName.substring(0, basicFileName.lastIndexOf(".")) + " exs: " + segmentedFileName.substring(0, segmentedFileName.lastIndexOf("_"))
                                + " for communication " + communicationName + ".");
                    } else {
                        stats.addCorrect(checkname, cd, "Basic file name matches exb: " + basicFileName.substring(0, basicFileName.lastIndexOf(".")) + " exs: " + segmentedFileName.substring(0, segmentedFileName.lastIndexOf("_"))
                                + " for communication " + communicationName + ".");
                    }
                }
                if (!basicNSLink.isEmpty() && !segmentedNSLink.isEmpty()) {
                    // check for mismatch between nslinks, issue a warning if necessary
                    if (!basicNSLink.substring(0, basicNSLink.lastIndexOf(".")).equals(segmentedNSLink.substring(0, segmentedNSLink.lastIndexOf("_")))) {
                        System.err.println("Basic NSLink and segmented NSLink do not match "
                                + "for communication " + communicationName + ", id: " + communicationID + ".");
                        stats.addCritical(checkname, cd, "NSLink filename mismatch exb: " + basicNSLink.substring(0, basicNSLink.lastIndexOf(".")) + " exs: " + segmentedNSLink.substring(0, segmentedNSLink.lastIndexOf("_"))
                                + " for communication " + communicationName + ".");
                    } else {
                        stats.addCorrect(checkname, cd, "NSLink filename matches exb: " + basicNSLink.substring(0, basicNSLink.lastIndexOf(".")) + " exs: " + segmentedNSLink.substring(0, segmentedNSLink.lastIndexOf("_"))
                                + " for communication " + communicationName + ".");
                    }
                }
            } else {
                System.err.println("No transcriptions found "
                        + "for communication " + communicationName + ", id: " + communicationID + ".");
                stats.addCorrect(checkname, cd, "No transcript found to be compared "
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
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc = null; 
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        } catch (TransformerException ex) {
            Logger.getLogger(ComaTranscriptionsNameChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ComaTranscriptionsNameChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ComaTranscriptionsNameChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                    
                    if(!transcriptName.equals(baseFileName)){
                        
                        // fix the transcription Name
                        transcription.getElementsByTagName("Name").item(0).setTextContent(baseFileName);
                        
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
                            Logger.getLogger(ComaTranscriptionsNameChecker.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParserConfigurationException ex) {
                            Logger.getLogger(ComaTranscriptionsNameChecker.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(ComaTranscriptionsNameChecker.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathExpressionException ex) {
                            Logger.getLogger(ComaTranscriptionsNameChecker.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        stats.addCorrect(checkname, cd, "Transcription/Name ("+transcriptName+") changed to base file name ("+baseFileName+").");
                                                                        
                    }
                }

            } else {
                String message = "No transcription found  for communication " + communicationName + ", id: " + communicationID + ".";
                System.err.println(message);
                stats.addCorrect(checkname, cd, message);
            }
            
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
            Logger.getLogger(ComaTranscriptionsNameChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
