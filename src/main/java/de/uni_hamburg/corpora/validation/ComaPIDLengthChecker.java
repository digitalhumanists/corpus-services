/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;


import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.cli.Option;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * A class that can load coma data and check for potential problems with HZSK
 * repository depositing.
 */
public class ComaPIDLengthChecker extends Checker implements CommandLineable, StringChecker, CorpusFunction {

    ValidatorSettings settings;
    final String COMA_PID_LENGTH = "coma-pid-length";
    String comaLoc = "";

    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    public Report check(String data) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(data);
        } catch(ParserConfigurationException pce) {
            stats.addException(pce, comaLoc + ": Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, comaLoc + ": Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, comaLoc + ": Unknown file reading error");
        }
        return stats;
    }


    private Report exceptionalCheck(String data)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(data));
        NodeList keys = doc.getElementsByTagName("Key");
        String corpusPrefix = "";
        String corpusVersion = "";
        for (int i = 0; i < keys.getLength(); i++) {
            Element keyElement = (Element)keys.item(i);
            if (keyElement.getAttribute("Name").equals("HZSK:corpusprefix")) {
                corpusPrefix = keyElement.getTextContent();
            } else if
                (keyElement.getAttribute("Name").equals("HZSK:corpusversion")) {
                corpusVersion = keyElement.getTextContent();
            }
        }
        
        Report stats = new Report();
        if (corpusPrefix.equals("")) {
            stats.addWarning(COMA_PID_LENGTH + "-config", comaLoc + ": " +
                        "Missing <Key name='HZSK:corpusprefix'>.",
                        "PID length cannot be estimated accurately. " +
                        "Add that key in coma.");
            corpusPrefix = "muster";
        } else {
            stats.addCorrect(COMA_PID_LENGTH + "-config", comaLoc + ": " +
                    "HZSK corpus prefix OK: " + corpusPrefix);
        }
        if (corpusVersion.equals("")) {
            stats.addWarning(COMA_PID_LENGTH + "-config", comaLoc + ": " +
                        "Missing <Key name='HZSK:corpusversion'>.",
                        "PID length cannot be estimated accurately. " +
                        "Add that key in coma.");
            corpusVersion = "0.0";
        } else {
            stats.addCorrect(COMA_PID_LENGTH + "-config", comaLoc + ": " +
                    "HZSK corpus version OK: " + corpusVersion);
        }
        
        //iterate <Communication>
        NodeList communications = doc.getElementsByTagName("Communication");
        for (int i = 0; i < communications.getLength(); i++) {
            Element communication = (Element)communications.item(i);
            String communicationName = communication.getAttribute("Name");
            String fedoraPID = new String("communication:" + corpusPrefix +
                    "-" + corpusVersion +
                    "_" + communicationName);
            
            //just strip some characters at the end to make a suggestion
            String shortenedCommuniationName;
            if(communicationName.length()>39){
                shortenedCommuniationName = communicationName.substring(0, 40);
            } else {
                shortenedCommuniationName = communicationName;
            }
            
            //test length of Fedora PID and report
            if (fedoraPID.length() >= 64) {
                stats.addCritical(COMA_PID_LENGTH, comaLoc + ": " +
                    "Fedora PID would be too long (max. 64) for communication name (" + fedoraPID.length() + " chars): " + fedoraPID );
                    // + " You could shorten it to: " + shortenedCommuniationName + ", or change the corpus prefix");
            } else {
                stats.addCorrect(COMA_PID_LENGTH, comaLoc + ": " +
                    "Following PID will be generated for this " +
                    "communication in Fedora: " + fedoraPID);
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("ComaPIDLengthChecker",
                "Checks Exmaralda .coma file for ID's that violate Fedora's " +
                "PID limits", "If input is a directory, performs recursive " +
                "check from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking COMA files for references...");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            try {
                comaLoc = f.getName();
                String s = TypeConverter.InputStream2String(new FileInputStream(f));
                stats = check(s);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return stats;
    }

    public static void main(String[] args) {
        ComaPIDLengthChecker checker = new ComaPIDLengthChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }
    
    /**
    * Default check function which calls the exceptionalCheck function so that the
    * primal functionality of the feature can be implemented, and additionally 
    * checks for parser configuration, SAXE and IO exceptions.
    */   
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch(ParserConfigurationException pce) {
            stats.addException(pce,  COMA_PID_LENGTH, cd, "Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, COMA_PID_LENGTH, cd, "Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, COMA_PID_LENGTH, cd, "Unknown file reading error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, COMA_PID_LENGTH, cd, "Unknown XPath error");
        } catch (TransformerException ex) {
            stats.addException(ex, COMA_PID_LENGTH, cd, "Unknown Transformer error");
        }
        return stats;
    }
    
    /**
    * Main feature of the class: Checks Exmaralda .coma file for 
    * ID's that violate Fedora's PID limits.
    */  
     private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException{
        Report stats = new Report();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString()));
        NodeList keys = doc.getElementsByTagName("Key");
        String corpusPrefix = "";
        String corpusVersion = "";
        for (int i = 0; i < keys.getLength(); i++) {
            Element keyElement = (Element)keys.item(i);
            if (keyElement.getAttribute("Name").equalsIgnoreCase("HZSK:corpusprefix")) {
                corpusPrefix = keyElement.getTextContent();
            } else if
                (keyElement.getAttribute("Name").equalsIgnoreCase("HZSK:corpusversion")) {
                corpusVersion = keyElement.getTextContent();
            }
        }
        if (corpusPrefix.equals("")) {
            stats.addWarning(COMA_PID_LENGTH, cd,
                        "Missing <Key name='HZSK:corpusprefix'>." +
                        "PID length cannot be estimated accurately. " +
                        "Add that key in coma.");
            corpusPrefix = "muster";
        } else {
            stats.addCorrect(COMA_PID_LENGTH,cd,
                    "HZSK corpus prefix OK: " + corpusPrefix);
        }
        if (corpusVersion.equals("")) {
            stats.addWarning(COMA_PID_LENGTH, cd,
                        "Missing <Key name='HZSK:corpusversion'>." +
                        "PID length cannot be estimated accurately. " +
                        "Add that key in coma.");
            corpusVersion = "0.0";
        } else {
            stats.addCorrect(COMA_PID_LENGTH, cd, 
                    "HZSK corpus version OK: " + corpusVersion);
        }
        NodeList communications = doc.getElementsByTagName("Communication");
        for (int i = 0; i < communications.getLength(); i++) {
            Element communication = (Element)communications.item(i);
            String communicationName = communication.getAttribute("Name");
            String fedoraPID = "communication: " + corpusPrefix +
                    "-" + corpusVersion +
                    "_" + communicationName;
            String shortenedCommuniationName;
            if(communicationName.length()>39){
                shortenedCommuniationName = communicationName.substring(0, 40);
            } else {
                shortenedCommuniationName = communicationName;
            }
            if (fedoraPID.length() >= 64) {
                stats.addCritical(COMA_PID_LENGTH, cd,
                            "Communication is too long for Fedora PID" +
                            "generation: " + fedoraPID +
                            " It must be shortened, e.g. use: " +
                            shortenedCommuniationName + ", or change " +
                            "the corpus prefix");
            } else {
                stats.addCorrect(COMA_PID_LENGTH, cd,
                            "Following PID will be generated for this " +
                            "communication in Fedora: " + fedoraPID);
            }
        }
        return stats;
     }
    
    /**
    * No fix is applicable for this feature.
    */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(COMA_PID_LENGTH,
                "Communication IDs which do not comply with Fedora PID cannot be fixed automatically");
        return report;
    }

    /**
    * Default function which determines for what type of files (basic transcription, 
    * segmented transcription, coma etc.) this feature can be used.
    */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ComaPIDLengthChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

}
