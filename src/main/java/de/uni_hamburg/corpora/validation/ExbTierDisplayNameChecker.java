package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.ExmaErrorList;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
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
 * A class that can check out tiers and find out if there is a mismatch between
 * category, speaker abbreviation and display name for each tier.
 */
public class ExbTierDisplayNameChecker extends Checker implements CorpusFunction {

    String tierLoc = "";
    final String tdnc = "tier-displayname-checker";

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
            stats.addException(pce, tierLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, tierLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, tierLoc + ": Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, tierLoc + ": Unknown file reading error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, tierLoc + ": Unknown file reading error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; checks if there is a mismatch between
     * category, speaker abbreviation and display name for each tier. Issues
     * warnings with respect to mismatches in tiers and add those warnings to
     * the report. At last, it returns the report with all the warnings.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, TransformerException, XPathExpressionException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        String transcriptName;
        if (doc.getElementsByTagName("transcription-name").getLength() > 0) {   // check if transcript name exists for the exb file
            transcriptName = doc.getElementsByTagName("transcription-name").item(0).getTextContent(); // get transcript name
        } else {
            transcriptName = "No Name Transcript";
        }
        NodeList tiers = doc.getElementsByTagName("tier"); // get all tiers of the transcript
        NodeList speakers = doc.getElementsByTagName("speaker"); // get all speakers of the transcript 
        HashMap<String, String> speakerMap = new HashMap<String, String>(); // map for each speaker and its corresponding abbreviation
        Report stats = new Report(); // create a new report for the transcript
        for (int i = 0; i < speakers.getLength(); i++) { // put speakers and their abbreviations into the map
            Element speaker = (Element) speakers.item(i);
            speakerMap.put(speaker.getAttribute("id"), speaker.getElementsByTagName("abbreviation").item(0).getTextContent());
        }
        for (int i = 0; i < tiers.getLength(); i++) { // loop for dealing with each tier
            Element tier = (Element) tiers.item(i);
            String category = tier.getAttribute("category"); // get category  
            String speakerName = tier.getAttribute("speaker"); // get speaker name
            String displayName = tier.getAttribute("display-name"); // get display name
            String displayNameCategory = displayName;
            String displayNameSpeaker = "";
            int openingPar = -1;
            int closingPar = -1;
            if (!displayName.isEmpty()) { // if display name exists compare it with other attributes   
                if (displayName.contains("[") && displayName.contains("]")) { // check if display name contains brackets
                    openingPar = displayName.indexOf("[");
                    closingPar = displayName.indexOf("]");
                    displayNameCategory = displayName.substring(openingPar + 1, closingPar);
                    displayNameSpeaker = displayName.substring(0, openingPar - 1);
                } else if (displayName.contains("-")){
                    openingPar = displayName.lastIndexOf("-");
                    closingPar = displayName.length();
                    displayNameSpeaker = displayName.substring(openingPar + 1, closingPar);
                    displayNameCategory = displayName.substring(0, openingPar);
                }
                //System.out.println("Tier DisplayName " + displayName + " category " + category  + " displaycategory " + displayNameCategory  + " and speaker name " +  speakerName + " displayspeaker " + displayNameSpeaker);
                if (!speakerName.isEmpty() && !category.isEmpty()) { // if speaker name exists check if it complies with tier display name
                    if (category.equals(displayNameCategory) && speakerName.equals(displayNameSpeaker)) {
                        //everything is correct
                        System.out.println("Tier DisplayName " + displayName + " matches category " + category + " and speaker name " +  speakerName);
                        stats.addCorrect(tdnc, cd, "Tier DisplayName " + displayName + " matches category " + category + " and speaker name " +  speakerName);
                    } else if (category.equals(displayNameCategory) && displayNameSpeaker.isEmpty()){
                        System.out.println("Tier DisplayName " + displayName + " matches category " + category);
                    } else {
                     System.err.println("Speaker abbreviation and display name for tier do not match"
                                    + "for speaker " + speakerName + ", tier id " + tier.getAttribute("id")
                                    + " in transcription of " + transcriptName);
                            stats.addCritical(tdnc, cd, "Tier mismatch "
                                    + "for speaker " + speakerName + ", tier category " + category
                                    +", tier id " + tier.getAttribute("id")
                                    + " in transcription of " + transcriptName);
                            exmaError.addError(tdnc, cd.getURL().getFile(), tier.getAttribute("id"), "", false, "Error: Speaker abbreviation and display name for tier doES not match"
                                    + "for speaker " + speakerName + ", tier category " + category
                                    + ", tier id " + tier.getAttribute("id")
                                    + " in transcription of " + transcriptName);   
                    }
                }
            }
            else{
                stats.addWarning(tdnc, cd, "Display name is empty "
                                    + "for speaker " + speakerName + ", tier category " + category
                                    + ", tier id " + tier.getAttribute("id"));
                exmaError.addError(tdnc, cd.getURL().getFile(), tier.getAttribute("id"), "", false, "Error: Display name for tier is empty"
                                    + "for speaker " + speakerName + ", tier category " + category
                                    + ", tier id " + tier.getAttribute("id"));   
            }
        }
        return stats; // return all the warnings
    }

    /**
     * Fixing the errors in tiers is not supported yet.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            Class clSecond = Class.forName("de.uni_hamburg.corpora.UnspecifiedXMLData");
            IsUsableFor.add(cl);
            IsUsableFor.add(clSecond);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ExbTierDisplayNameChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
    
}