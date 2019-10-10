package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.Event;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.jdom.JDOMException;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author bau2401
 *
 * This class checks whether the annotations in exb files comply with annotation
 * specification panel.
 */
public class ExbAnnotationPanelCheck extends Checker implements CorpusFunction {

    ArrayList<String> allTagStrings;
    String tierLoc = "";

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
     * Main functionality of the feature; adds tiers from the annotation
     * specification file first, then checks if tiers in exb files are correct.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException {
        Report stats = new Report(); //create a new report
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document

        if (cd.getURL().toString().endsWith(".xml")) {          // when the annotation spec file is read
            allTagStrings = new ArrayList<String>();
            NodeList tags = doc.getElementsByTagName("tag"); // divide by tags
            for (int i = 0; i < tags.getLength(); i++) { //iterate through tags
                org.w3c.dom.Element tag = (org.w3c.dom.Element) tags.item(i);
                allTagStrings.add(tag.getAttribute("name"));
            }
        } else {               // when a basic transcription file is read
            BasicTranscription basictranscription = new BasicTranscription();
            basictranscription.BasicTranscriptionFromString(cd.toSaveableString());
            for (int pos = 0; pos < basictranscription.getBody().getNumberOfTiers(); pos++) {
                Tier tier = basictranscription.getBody().getTierAt(pos);
                //single out only the annotation tiers
                if (tier.getType().equals("a") && !tier.getCategory().equals("de") && !tier.getCategory().equals("en")
                        && !tier.getCategory().equals("ita") && !tier.getCategory().equals("fe")) {
                    //go through every event of that tier
                    for (int pos2 = 0; pos2 < tier.getNumberOfEvents(); pos2++) {
                        //get the event
                        Event event = tier.getEventAt(pos2);
                        //convert the event content to a string
                        //System.out.println(content);
                        String content = event.getDescription();
                        if (!content.isEmpty()) {
                            if (content.endsWith(" ")) { // scrap extra space at the end of the tag
                                content = content.substring(0, content.length() - 1);
                            }
                            // check if the content is contained in the possible tags
                            if (!(allTagStrings.contains(content))) {
                                System.err.println("Exb file " + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1) + " is containing annotation with incompatible tag ("
                                        + content + ") in its tier " + tier.getID() + " for the event " + event.getStart() + " not specified by annotation spec file!");
                                stats.addWarning("exb-annotation-panel-check", "Exb file " + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1)
                                        + " is containing annotation with incompatible tag (" + content
                                        + ") in its tier " + tier.getID() + " for the event " + event.getStart() + " not specified by annotation spec file!");
                                exmaError.addError("exb-annotation-panel-check", cd.getURL().getFile(), tier.getID(), event.getStart(), false,
                                        "Exb file " + cd.getURL().getFile().substring(cd.getURL().getFile().lastIndexOf("/") + 1) + " is containing annotation with incompatible tag (" + content
                                        + ") in its tier " + tier.getID() + " for the event " + event.getStart() + " not specified by annotation spec file!");
                            }
                        }
                    }
                }
            }
        }
        return stats;
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
            Class cl = Class.forName("de.uni_hamburg.corpora.AnnotationSpecification");
            Class clSecond = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
            IsUsableFor.add(clSecond);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ExbAnnotationPanelCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
