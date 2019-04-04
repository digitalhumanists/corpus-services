package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.ReportItem;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.exmaralda.folker.utilities.TimeStringFormatter;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Ozzy
 */
public class CalculateAnnotatedTime extends Checker implements CorpusFunction {

    String annotLoc = "";
    //HashMap<String, HashMap<String, String>> eventMap; // hash map for holding events of annotation tiers
    HashMap<String, HashMap<String, String>> tierMap; // all the annotation tiers of all the exb files of the corpus

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) throws JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, annotLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, annotLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, annotLoc + ": Unknown file reading error");
        }
        return stats;   
    }

    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException {
        Report stats = new Report(); //create a new report
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        String transcriptName;
        if (doc.getElementsByTagName("transcription-name").getLength() > 0) {   // check if transcript name exists for the exb file
            transcriptName = doc.getElementsByTagName("transcription-name").item(0).getTextContent(); // get transcript name
        } else {
            transcriptName = "No Name Transcript";
        }
        stats.addNote("calculate-annotated-time", "Annotation Tiers of " + transcriptName);
        HashMap<String, HashMap<String, String>> eventMap = new HashMap<>();
        //initialise the hash map only the first time when this function is called
        if (tierMap == null) {
            tierMap = new HashMap<>();
        }
        NodeList tiers = doc.getElementsByTagName("tier"); // get all tiers of the transcript
        NodeList items = doc.getElementsByTagName("tli"); // get all timeline items of the transcript
        HashMap<String, Float> timelineItems = getTimelineItems(items);
        HashMap<String, String> tierH = new HashMap<>();
        for (int i = 0; i < tiers.getLength(); i++) { // loop for dealing with each tier
            Element tier = (Element) tiers.item(i);
            if (tier.getAttribute("type").equals("a")) {
                HashMap<String, String> eventH = new HashMap<>();
                String tierDisplay = tier.getAttribute("display-name");
                float tierDuration = 0;
                NodeList events = tier.getElementsByTagName("event");
                for (int j = 0; j < events.getLength(); j++) {  // 
                    float eventDuration = 0;
                    Element event = (Element) events.item(j);
                    String eventLabel = event.getTextContent();
                    String eventStart = event.getAttribute("start");
                    String eventEnd = event.getAttribute("end");
                    tierDuration += timelineItems.get(eventEnd) - timelineItems.get(eventStart);
                    eventDuration = timelineItems.get(eventEnd) - timelineItems.get(eventStart);
                    float secondsLeft = eventDuration % 60;
                    int minutes = (int) Math.floor(eventDuration / 60);
                    String MM = (String) (minutes < 10 ? "0" + minutes : minutes);
                    String SS = (String) (secondsLeft < 10 ? "0" + Float.toString(secondsLeft) : Float.toString(secondsLeft));
                    if (SS.length() > 5) {
                        SS = SS.substring(0, 5);
                    }
                    eventH.put(eventLabel, MM + ":" + SS);
                }
                if (!eventH.isEmpty()) {
                    eventMap.put(tierDisplay, eventH);
                }
                float secondsLeft = tierDuration % 60;
                int minutes = (int) Math.floor(tierDuration / 60);
                String MM = (String) (minutes < 10 ? "0" + minutes : minutes);
                String SS = (String) (secondsLeft < 10 ? "0" + Float.toString(secondsLeft) : Float.toString(secondsLeft));
                if (SS.length() > 5) {
                    SS = SS.substring(0, 5);
                }
                tierH.put(tierDisplay, MM + ":" + SS);
                report.addNote("calculate-annotated-time", tierDisplay + "  " + MM + ":" + SS);
            }
        }
        report.addNote("calculate-annotated-time", "Labels per Tier");
        Set perTier = eventMap.keySet();
        for(Object per: perTier){
            String tierName = (String) per;
            report.addNote("calculate-annotated-time", tierName);
            HashMap map = new HashMap(eventMap.get(tierName));
            Set perMap = map.keySet();
            for(Object obj: perMap){
                String label = (String) obj;
                report.addNote("calculate-annotated-time", label + "    " + map.get(label));
            }
        }
        tierMap.put(transcriptName, tierH);  // finally add the annotations of the transcript
        return stats;
    }

    public HashMap<String, Float> getTimelineItems(NodeList items) {
        HashMap<String, Float> h = new HashMap<>();
        for (int i = 0; i < items.getLength(); i++) { // loop for dealing with each timeline item
            Element item = (Element) items.item(i);
            String itemID = item.getAttribute("id");
            Float time = new Float(item.getAttribute("time"));
            h.put(itemID, time);
        }
        return h;
    }

    /**
     * Fix is not supported for this functionality.
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
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IAAFunctionality.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

}
