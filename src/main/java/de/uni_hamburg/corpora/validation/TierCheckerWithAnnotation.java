
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
 * The class that checks out that all annotations are from the annotation specification file 
 * and that there are no annotations in the coma file not in the annotation specification file.
 */

public class TierCheckerWithAnnotation extends Checker implements CorpusFunction{
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
    * Main functionality of the feature; compares the coma file with the corresponding
    * annotation specification file whether or not there is a conflict regarding 
    * the annotation tags that are used in the coma file.
    */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        NodeList communications = doc.getElementsByTagName("Communication"); // divide by Communication tags
        ArrayList<String> annotationsInComa = new ArrayList<String>(); // list for holding annotations in coma files
        Report stats = new Report(); //create a new report
        for (int i = 0; i < communications.getLength(); i++) { //iterate through communications
            Element communication = (Element) communications.item(i);
            NodeList transcriptions = communication.getElementsByTagName("Transcription"); // get transcriptions of current communication     
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
                if (segmented){ // get the names of the segmentation algorithms in the coma file
                    for (int k = 0; k < keys.getLength(); k++){  // look for the keys with algorithm 
                        Element key = (Element) keys.item(k);
                        if (key.getAttribute("Name").contains("Annotation type:")){
                            int colonIndex = key.getAttribute("Name").lastIndexOf(':');    
                            if(!annotationsInComa.contains(key.getAttribute("Name").substring(colonIndex+2)))
                                annotationsInComa.add(key.getAttribute("Name").substring(colonIndex+2));
                        }
                    }
                }
            }
        }
        
        // read the annotation specification file by giving file path, since we can't read files other than coma and exb yet
        File fXmlFile = new File("C:/Users/Ozzy/Desktop/AnnotationSpecDemoCorpus.xml");
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc2 = dBuilder.parse(fXmlFile);
        doc2.getDocumentElement().normalize();
        ArrayList<String> annotations = new ArrayList<String>();
        
        NodeList tags = doc2.getElementsByTagName("tag"); // divide by tags
        for (int i = 0; i < tags.getLength(); i++) { //iterate through tags
            Element tag = (Element) tags.item(i);
            annotations.add(tag.getAttribute("name"));
        }    
        for(int i = 0; i < annotationsInComa.size(); i++){ // iterate through annotations in the coma file
            if(!annotations.contains(annotationsInComa.get(i))){ // check if annotations not present in annotation spec file
                System.err.println("Coma file is containing annotation ("+ annotationsInComa.get(i) 
                        +") not specified by annotation spec file!");
                stats.addWarning("tier-checker-with-annotation", "annotation error: annotation ("
                        +annotationsInComa.get(i)+") not specified!");
            }
        }
        
        return stats; // return the report with warnings
    }
    
    /**
    * This feature does not have fix functionality yet.
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
            Logger.getLogger(TierCheckerWithAnnotation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
}
