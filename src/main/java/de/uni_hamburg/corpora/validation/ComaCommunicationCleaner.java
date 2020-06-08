package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.ComaData;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.XMLData;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class that checks whether or not the coma file contains superfluous Communication and Speaker elements (for publication).
 * If it does then these elements are removed.
 */
public class ComaCommunicationCleaner extends Checker implements CorpusFunction {

    String mode = "";
    String musterElementsPattern = "_(Muster|MUSTER)_";

    public ComaCommunicationCleaner() {
        //can fix
        super(true);
    }

    /**
     * One of the main functionalities of the feature; issues warnings if the
     * coma file contains apostrophe ’and add that warning to the report which
     * it returns.
     */
    @Override
    public Report function(CorpusData cd, Boolean fix) // check whether there's any illegal apostrophes '
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException {
        
        Report stats = new Report();         // create a new report
        
        Document doc = null;
        
        switch (mode) {
            case "inel":
                
                ComaData ccd = new ComaData();
                ccd = (ComaData) cd;
                doc = TypeConverter.JdomDocument2W3cDocument(ccd.getJdom()); // get the file as a document      
                
                //store IDs of all speakers for later
                ArrayList<String> speakerIDsToRemove = new ArrayList<String>();
                NodeList speakers = doc.getElementsByTagName("Speaker"); 
                for (int i = 0; i < speakers.getLength(); i++) { //iterate through speakers
                    Element speaker = (Element) speakers.item(i);
                    String speakerID = speaker.getAttribute("Id");
                    if(!speakerIDsToRemove.contains(speakerID)){
                        speakerIDsToRemove.add(speakerID);
                    }
                }
                 
                
                NodeList corpusData = doc.getElementsByTagName("CorpusData"); 
                NodeList communications = doc.getElementsByTagName("Communication"); // divide by Communication tags
                for (int i = 0; i < communications.getLength(); i++) { //iterate through communications
                    Element communication = (Element) communications.item(i);
                    String communicationName = communication.getAttribute("Name"); // get communication name to use it in the warning

                    NodeList transcriptions = communication.getElementsByTagName("Transcription"); // get transcriptions of current communication
                    // if there are no Transcription elements, then remove this Communication
                    if(transcriptions.getLength() == 0 || communicationName.matches(musterElementsPattern)){
                        communication.getParentNode().removeChild(communication);
                        if(fix){
                            if(communicationName.matches(musterElementsPattern)){
                                stats.addFix(function, cd, "Removed Communication "+communicationName+" because it was a 'Muster' Communication."); // fix report
                            } else{
                                stats.addFix(function, cd, "Removed Communication "+communicationName+" because there were no Transcription elements."); // fix report
                            }
                        } else{
                            if(communicationName.matches(musterElementsPattern)){
                                stats.addFix(function, cd, "Communication "+communicationName+" is a 'Muster' Communication."); // fix report
                            } else{
                                stats.addWarning(function, cd, "Communication "+communicationName+" has no Transcription elements."); // fix report
                            }
                        }
                    } else{
                        //remove speaker IDs from ArrayList
                        NodeList persons = communication.getElementsByTagName("Person"); 
                        for (int j = 0; j < persons.getLength(); j++) {
                            String personID = persons.item(j).getNodeValue(); 
                            if(speakerIDsToRemove.contains(personID)){
                                speakerIDsToRemove.remove(personID);
                            }
                            
                        }
                    }                    
                }
                
                
                //also remove Muster Speaker elements and remove Speaker elements which were exclusively linked in "empty" Communications
                for (int j = 0; j < speakers.getLength(); j++) {
                    Element speaker = (Element) speakers.item(j);

                    // test if it was only used in "empty" Communication 
                    String speakerID = speaker.getAttribute("Id");       
                    String speakerSigle = speaker.getElementsByTagName("Sigle").item(0).getNodeValue();                            
                    if(speakerIDsToRemove.contains(speakerID) || speakerSigle.matches("_(Muster|MUSTER)_")){
                        speaker.getParentNode().removeChild(speaker);
                        if(fix){
                            stats.addFix(function, cd, "Removed Speaker "+speakerSigle+" because it was a 'Muster' speaker."); // fix report
                        } else{
                            stats.addWarning(function, cd, "Speaker "+speakerSigle+" is a 'Muster' speaker.");
                        }
                    }
                }
                break;
            default:
                stats.addCritical(function, cd, "The value '"+mode+"' of the parameter 'mode' is not supported.");
        }
        
        if (fix) {
            //then save file
            CorpusIO cio = new CorpusIO();
            cd.updateUnformattedString(TypeConverter.W3cDocument2String(doc));
            XMLData xml = (XMLData) cd;
            org.jdom.Document jdomDoc = TypeConverter.W3cDocument2JdomDocument(doc);
            xml.setJdom(jdomDoc);
            cd = (CorpusData) xml;

            cd.updateUnformattedString(TypeConverter.JdomDocument2String(jdomDoc));
            cio.write(cd, cd.getURL());
        }
        
        return stats; // return the report with warnings
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

    public void setMode(String newMode){
        mode = newMode;
    }
    
    public void setMusterPattern(String pattern){
        musterElementsPattern = pattern;
    }
    
    /**
     * Default function which returns a two/three line description of what this
     * class is about.
     */
    @Override
    public String getDescription() {
        String description = "This function checks whether or not the coma file contains "
                + "superfluous Communication and Speaker elements (for publication).\n"
                + "If it does and fix is activated then these elements are removed.";
        return description;
    }

    @Override
    public Report function(Corpus c, Boolean fix) throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException {
        Report stats;
        cd = c.getComaData();
        stats = function(cd, fix);
        return stats;
    }
}
