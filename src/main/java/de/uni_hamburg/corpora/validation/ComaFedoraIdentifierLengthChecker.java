/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;


import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import java.io.IOException;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
public class ComaFedoraIdentifierLengthChecker extends Checker implements CorpusFunction {

    String comaLoc = "";

    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    

    public ComaFedoraIdentifierLengthChecker() {
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
            stats.addException(pce,  function, cd, "Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown file reading error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown XPath error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown Transformer error");
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
            stats.addWarning(function, cd,
                "Missing Key[@name='HZSK:corpusprefix']. " +
                "PID length cannot be estimated accurately. " +
                "Add that key in coma.");
            corpusPrefix = "muster";
        } else {
            stats.addCorrect(function,cd,
                "HZSK corpus prefix OK: " + corpusPrefix);
        }
        if (corpusVersion.equals("")) {
            stats.addWarning(function, cd,
                "Missing Key[@name='HZSK:corpusversion']. " +
                "PID length cannot be estimated accurately. " +
                "Add that key in coma.");
            corpusVersion = "0.0";
        } else {
            stats.addCorrect(function, cd, 
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
                stats.addCritical(function, cd,
                    "Fedora PID would be too long (max. 64) for communication name (" + fedoraPID.length() + " chars): " + fedoraPID );
                    // + " You could shorten it to: " + shortenedCommuniationName + ", or change the corpus prefix");
            } else {
                stats.addCorrect(function, cd,
                    "Fedora PID can be generated for communication: " + fedoraPID);
            }
        }
        
        return stats;
     }
    
    /**
    * No fix is applicable for this feature.
    */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(function,
            "Communication IDs which do not comply with Fedora PID cannot be fixed automatically. ");
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
            report.addException(ex, " usable class not found");
        }
        return IsUsableFor;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class loads coma data and check for potential "
                + "problems with HZSK repository depositing; it checks the Exmaralda "
                + ".coma file for ID's that violate Fedora's PID limits. ";
        return description;
    }

    @Override
    public Report check(Corpus c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report function(CorpusData cd, Boolean fix) throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
