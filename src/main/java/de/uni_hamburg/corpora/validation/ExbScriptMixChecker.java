/**
 * @file ExbScriptMixChecker.java
 *
 * Collection of checks for unintentionally mixed scripts (e.g.
 * Latin/Cyrillic) inside tokens.
 *
 * @author Timofey Arkhangelskiy <timofey.arkhangelskiy@uni-hamburg.de>
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.XMLData;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that checks for mixed scripts (e.g. Cyrillic/Latin) in the transcription.
 */
public class ExbScriptMixChecker extends Checker implements CorpusFunction {
    static final String CHECKER_NAME = "ScriptMixChecker";
    ArrayList<String> lsTiersToCheck = new ArrayList<>(
      Arrays.asList("tx", "mb", "mp", "ge"));
    static String sCharClassLat = "[a-zÀ-žḀ-ỹ]";
    static String sCharClassCyr = "[Ѐ-ԯ]";
    static String sCharClassGreek = "[΄-ϡϰ-Ͽἀ-῾]";
    Pattern rxLat = Pattern.compile(sCharClassLat, Pattern.CASE_INSENSITIVE);
    Pattern rxCyr = Pattern.compile(sCharClassCyr, Pattern.CASE_INSENSITIVE);
    Pattern rxGreek = Pattern.compile(sCharClassGreek, Pattern.CASE_INSENSITIVE);
    Map<String, Pattern> dictScripts = new HashMap<>();
    
    public ExbScriptMixChecker() {
        dictScripts.put("Cyrillic", rxCyr);
        dictScripts.put("Latin", rxLat);
        dictScripts.put("Greek", rxGreek);
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
            stats.addException(pce, CHECKER_NAME, cd, "Unknown parsing error");          
        } catch (SAXException saxe) {
            stats.addException(saxe, CHECKER_NAME, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, CHECKER_NAME, cd, "Unknown parsing error");
        } catch (TransformerException ex) {
            stats.addException(ex, CHECKER_NAME, cd, "Unknown parsing error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, CHECKER_NAME, cd, "Unknown parsing error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; checks text tiers in exb files
     * (their names are listed in lsTiersToCheck) and looks for words
     * that contain a mixture of characters in different scripts.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException,
                   TransformerException, XPathExpressionException {
        
        // test ref IDs without fixing errors
        Report stats = testScriptMix(cd, false);
        return stats; // return all the warnings
    }

    /**
     * Method for correcting the mistakes (not implemented now).
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        Report report = new Report();
        report.addCritical(CHECKER_NAME, cd, "Fixing option is not available");
        return report;
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
    
    
    private Report testScriptMix(CorpusData cd, Boolean fix) throws IOException, SAXException {
        //so this is easier this way :)
        Document doc = null;
        XMLData xml = (XMLData)cd; 
        doc = TypeConverter.JdomDocument2W3cDocument(xml.getJdom());
        Report stats = new Report(); // create a new report for the transcript
        
        //I would rather use the filename instead the transcription name, when you add the error using the cd 
        //object the filename gets added automatically to the created error list
//        String transcriptName;
//        if (doc.getElementsByTagName("transcription-name").getLength() > 0) {   // check if transcript name exists for the exb file
//            transcriptName = doc.getElementsByTagName("transcription-name").item(0).getTextContent(); // get transcript name
//        } else {
//            transcriptName = "Nameless Transcript";
//        }
        
        NodeList tiers = doc.getElementsByTagName("tier"); // get all tiers of the transcript      
        ArrayList<Element> relevantTiers = new ArrayList();
        for (int i = 0; i < tiers.getLength(); i++) {
            Element tier = (Element)tiers.item(i);
            String category = tier.getAttribute("category"); // get category so that we know is this is a relevant tier 
            if (lsTiersToCheck.contains(category)) {
                relevantTiers.add(tier);
            }
        }
        for (int i = 0; i < relevantTiers.size(); i++) {
            Element curTier = relevantTiers.get(i);
            NodeList events = curTier.getElementsByTagName("event");
            String tierId = curTier.getAttribute("id");
            String tierSpeaker = curTier.getAttribute("speaker");
            int order = 1;
            
            for (int j = 0; j < events.getLength(); j++) {  
                Element event = (Element)events.item(j);
                String eventStart = event.getAttribute("start");
                String eventEnd = event.getAttribute("end");
                String eventText = event.getTextContent();
                ArrayList<String> lsScriptsUsed = new ArrayList<>();
                for (Map.Entry<String, Pattern> entry : dictScripts.entrySet()) {
                    Pattern p = entry.getValue();
                    Matcher m = p.matcher(eventText);
                    if (m.find()) {
                        lsScriptsUsed.add(entry.getKey());
                    }
                }
                if (lsScriptsUsed.size() > 1) {
                    String eventRef = "event " + eventStart + "/" + eventEnd 
                            + ", tier '" + tierId + "'";
                    //Filename is added automatically so message can be shorter
                    String message = "Mixed scripts in \"" + eventText 
                            + "\" (" + String.join(", ", lsScriptsUsed) + "), " 
                            + eventRef;
                    stats.addCritical(CHECKER_NAME, cd, message);
                }
                /*
                else {
                    stats.addCorrect(CHECKER_NAME, cd, "ok");
                }
                */
            }
        }
        return stats;
    }
}
