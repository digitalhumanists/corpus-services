
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
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
 * A class that can check out tiers and find out if there is a mismatch between
 * category, speaker abbreviation and display name for each tier.
 */
public class TierChecker extends Checker implements CorpusFunction{
    
    String tierLoc = "";
            
    @Override
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
        }
        return stats;
    }
    
    private Report exceptionalCheck(CorpusData cd) 
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString()));
        String transcriptName = doc.getElementsByTagName("transcription-name").item(0).getTextContent();
        NodeList tiers = doc.getElementsByTagName("tier");
        NodeList speakers = doc.getElementsByTagName("speaker");
        HashMap<String, String> speakerMap = new HashMap<String, String>();
        Report stats = new Report();
        for (int i = 0; i < speakers.getLength(); i++){
            Element speaker = (Element) speakers.item(i);
            speakerMap.put(speaker.getAttribute("id"), speaker.getElementsByTagName("abbreviation").item(0).getTextContent());
        }
        for (int i = 0; i < tiers.getLength(); i++){
            Element tier = (Element) tiers.item(i);
            String category = tier.getAttribute("category");
            String displayName = tier.getAttribute("display-name");
            String speakerName = tier.getAttribute("speaker");
            if(!displayName.isEmpty()){
                int openingPar = displayName.indexOf("[");
                int closingPar = displayName.indexOf("]");
                String displayNameCategory = displayName.substring(openingPar+1, closingPar);
                if(!speakerName.isEmpty()){
                    String displayNameSpeaker = displayName.substring(0, openingPar-1);
                    if(!category.equals(displayNameCategory) || !displayNameSpeaker.equals(speakerMap.get(speakerName))){
                        System.err.println("Category or speaker abbreviation and display name for tier do not match"
                                + "for speaker " + speakerName + ", tier id " + tier.getAttribute("id") 
                                + " in transcription of " + transcriptName);
                        stats.addWarning("tier-checker", "Tier mismatch "
                                + "for speaker " + speakerName + ", tier id " + tier.getAttribute("id")
                                + " in transcription of " + transcriptName);
                    }
                }
                else{
                    if(!category.equals(displayNameCategory)){
                        System.err.println("Category and display name for tier do not match"
                                + "for speaker " + speakerName + ", tier id " + tier.getAttribute("id")
                                + " in transcription of " + transcriptName);
                        stats.addWarning("tier-checker", "Tier mismatch "
                                + "for speaker " + speakerName + ", tier id " + tier.getAttribute("id")
                                + " in transcription of " + transcriptName);
                    }
                }
            }    
        }
        return stats;
    }
    
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            Class clSecond = Class.forName("de.uni_hamburg.corpora.UnspecifiedXMLData");
            IsUsableFor.add(cl);
            IsUsableFor.add(clSecond);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TierChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
    
}
