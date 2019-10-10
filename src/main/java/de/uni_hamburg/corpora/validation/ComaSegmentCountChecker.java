package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
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
 * A class that checks whether there are more than one segmentation algorithms
 * used in the corpus. If that is the case, it issues warnings.
 */
public class ComaSegmentCountChecker extends Checker implements CorpusFunction {

    String comaLoc = "";
    String cscc = "ComaSegmentCountChecker";

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
            stats.addException(pce, cscc, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, cscc, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, cscc, cd, "Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, cscc, cd, "Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, cscc, cd, "Transformer Exception");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, cscc, cd, "XPath Exception");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; checks the coma file whether or not
     * there are more than one segmentation algorithms used in the corpus.
     * Issues warnings and returns report which is composed of errors.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        NodeList communications = doc.getElementsByTagName("Communication"); // divide by Communication tags
        ArrayList<String> algorithmNames = new ArrayList<>(); // array for holding algorithm names
        Report stats = new Report(); //create a new report
        for (int i = 0; i < communications.getLength(); i++) { //iterate through communications
            Element communication = (Element) communications.item(i);
            NodeList transcriptions = communication.getElementsByTagName("Transcription"); // get transcriptions of current communication     
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
                        if (key.getAttribute("Name").contains("#") && key.getAttribute("Name").contains(":")) {
                            //String text = key.getAttribute("Name");
                            int colonIndex = key.getAttribute("Name").lastIndexOf(':');
                            int hashIndex = key.getAttribute("Name").indexOf('#');
                            algorithmNames.add(key.getAttribute("Name").substring(hashIndex + 2, colonIndex)); 
                        }
                        }
                        break;                    
                }
                }
            }
        }
        System.out.println(algorithmNames);
        String algorithmName = "";
        if (!algorithmNames.isEmpty()) {
            algorithmName = algorithmNames.get(0);
            boolean error = false;
            for (int i = 1; i < algorithmNames.size(); i++) { // check if coma file contains different seg algorithms
                if (!algorithmName.equals(algorithmNames.get(i))) {
                    error = true;
                    System.err.println("Coma file contains different segmentation algorithms: " + algorithmNames.get(i));
                    stats.addCritical(cscc, cd, "More than one segmentation algorithm: " + algorithmNames.get(i) + " and " + algorithmName);
                    break;
                }
            }
            if (!error) {
                stats.addCorrect(cscc, cd, "Only segmentation " + algorithmNames.get(1));
            }
        } else {
            stats.addWarning(cscc, cd, "No segment counts added yet. Use Coma > Maintenance > Update segment counts to add them. ");
        }
        return stats; // return the report with warnings
    }

    /**
     * This feature does not have fix functionality yet.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        return check(cd);
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
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
