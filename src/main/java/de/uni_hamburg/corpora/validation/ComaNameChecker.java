package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
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
 * A class that checks whether or not there is a mismatch between
 * basic and segmented names, basic and segmented file names, plus their
 * NSLinks for each communication in the coma file.
 */
public class ComaNameChecker extends Checker implements CorpusFunction{
    
    String comaLoc = "";
    
    /**
    * Default check function which calls the exceptionalCheck function so that the
    * primal functionality of the feature can be implemented, and additionally 
    * checks for parser configuration, SAXE and IO exceptions.
    */   
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, comaLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, comaLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, comaLoc + ": Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, comaLoc + ": Unknown file reading error");
        }
        return stats;
    }
    
    /**
    * Main functionality of the feature; issues warnings
    * with respect to mismatches between basic and segmented names, 
    * basic and segmented file names, plus their NSLinks for each communication 
    * in the coma file and add those warnings to the report which it returns.
    */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
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
            for (int j = 0; j < transcriptions.getLength(); j++) {   // iterate through transcriptions 
                Element transcription = (Element) transcriptions.item(j);
                NodeList keys =  transcription.getElementsByTagName("Key");  // get keys of current transcription
                boolean segmented = false;   // flag for distinguishing basic file from segmented file 
                for (int k = 0; k < keys.getLength(); k++){  // look for the key with "segmented" attribute 
                    Element key = (Element) keys.item(k);
                    if (key.getAttribute("Name").equals("segmented")){
                        String seg = key.getTextContent();
                        if (seg.equals("true"))      // check if transcription is segmented or not
                            segmented = true;        // if segmented transcription then turn the flag true
                        break;
                    }
                }
                if (!segmented){ // get name, file name and nslink of basic transcription
                    basicTranscriptName = transcription.getElementsByTagName("Name").item(0).getTextContent();
                    basicFileName = transcription.getElementsByTagName("Filename").item(0).getTextContent();
                    basicNSLink = transcription.getElementsByTagName("NSLink").item(0).getTextContent();
                }
                else{  // get name, file name and nslink of segmented transcription
                    segmentedTranscriptName = transcription.getElementsByTagName("Name").item(0).getTextContent();
                    segmentedFileName = transcription.getElementsByTagName("Filename").item(0).getTextContent();
                    segmentedNSLink = transcription.getElementsByTagName("NSLink").item(0).getTextContent();
                }
            }
            if(!basicTranscriptName.equals(segmentedTranscriptName)){ // check for mismatch between names
                // issue a warning if necessary
                System.err.println("Basic transcription name and segmented transcription name do not match "
                        + "for communication " + communicationName + ", id: " + communicationID + ".");
                stats.addWarning("coma-name-checker", "Name mismatch "
                         + "for communication " + communicationName + ", id: " + communicationID + ".");
            }
            // check for mismatch between file names, issue a warning if necessary
            if(!basicFileName.substring(0, basicFileName.indexOf(".")).equals(segmentedFileName.substring(0, segmentedFileName.indexOf("_")))){
                System.err.println("Basic file name and segmented file name do not match "
                        + "for communication " + communicationName + ", id: " + communicationID + ".");
                stats.addWarning("coma-name-checker", "File name mismatch "
                         + "for communication " + communicationName + ", id: " + communicationID + ".");
            }
            // check for mismatch between nslinks, issue a warning if necessary
            if(!basicNSLink.substring(0, basicNSLink.indexOf(".")).equals(segmentedNSLink.substring(0, segmentedNSLink.indexOf("_")))){
                System.err.println("Basic NSLink and segmented NSLink do not match "
                        + "for communication " + communicationName + ", id: " + communicationID + ".");
                stats.addWarning("coma-name-checker", "NSLink mismatch "
                         + "for communication " + communicationName + ", id: " + communicationID + ".");
            }
        }
        return stats; // return the report with warnings
    }
    
    /**
    * Fixing the conflicts in coma file with regards to this feature is not supported yet.
    */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
    * Default function which determines for what type of files (basic transcription, 
    * segmented transcription, coma etc.) this feature can be used.
    */
    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TierChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
}
