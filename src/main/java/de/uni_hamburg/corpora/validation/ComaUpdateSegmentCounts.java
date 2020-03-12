package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.SegmentedTranscriptionData;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.xml.sax.SAXException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

/**
 *
 * @author Ozzy
 */
public class ComaUpdateSegmentCounts extends Checker implements CorpusFunction {

    static String filename;
    static ValidatorSettings settings;
    String path2ExternalFSM = "";

    public ComaUpdateSegmentCounts() {
        super("coma-update-segment-counts");
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) {
        report.addCritical(function, cd, "Checking option is not available");
        return report;
    }

    /**
     * Main feature of the class: takes a coma file, updates the info using the
     * linked exbs and saves the coma file afterwards without changing exbs;
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, IOException, JexmaraldaException {
        Report stats = new Report();
        CorpusIO cio = new CorpusIO();
        SegmentedTranscriptionData exs;
        List<Element> toRemove = new ArrayList<Element>();
        try {
            Document comaDoc = TypeConverter.String2JdomDocument(cd.toSaveableString());
            XPath context;
            context = XPath.newInstance("//Transcription[Description/Key[@Name='segmented']/text()='true']");
            URL url;
            List allContextInstances = context.selectNodes(comaDoc);
            if (!allContextInstances.isEmpty()) {
                for (int i = 0; i < allContextInstances.size(); i++) {
                    Object o = allContextInstances.get(i);
                    if (o instanceof Element) {
                        Element e = (Element) o;
                        List<Element> descKeys;
                        //in the coma file remove old stats first
                        descKeys = e.getChild("Description")
                                .getChildren();
                        for (Element ke : (List<Element>) descKeys) {
                            if (Pattern.matches("#(..).*", ke.getAttributeValue("Name"))) {
                                toRemove.add(ke);
                            }
                        }
                        for (Element re : toRemove) {
                            descKeys.remove(re);
                        }
                        //now get the new segment counts and add them insted
                        String s = e.getChildText("NSLink");
                        //System.out.println("NSLink:" + s);
                        url = new URL(cd.getParentURL() + s);
                        exs = (SegmentedTranscriptionData) cio.readFileURL(url);
                        List segmentCounts = exs.getSegmentCounts();
                        for (Object segmentCount : segmentCounts) {                           
                            if (segmentCount instanceof Element) {
                                Element segmentCountEl = (Element) segmentCount;
                                //Object key = segmentCountEl.getAttributeValue("attribute-name").substring(2);
                                Object key = segmentCountEl.getAttributeValue("attribute-name");
                                Object value = segmentCountEl.getValue();
                                //System.out.println("Value:" + value);
                                Element newKey = new Element("Key");
                                newKey.setAttribute("Name", (String) key);
                                newKey.setText(value.toString());
                                e.getChild("Description").addContent(
                                        newKey);
                                report.addFix(function, cd, "Updated segment count " + key.toString() + ":" + value.toString() + "for transcription " + e.getAttributeValue("Name"));
                            }
                        }

                    }
                }
            }
            if (comaDoc != null) {
                cd.updateUnformattedString(TypeConverter.JdomDocument2String(comaDoc));
                cio.write(cd, cd.getURL());
                report.addCorrect(function, cd, "Updated the segment counts!");
            } else {
                report.addCritical(function, cd, "Updating the segment counts was not possible!");
            }
        } catch (IOException ex) {
            report.addException(ex, function, cd, "unknown IO exception");
        } catch (TransformerException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        } catch (SAXException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        } catch (XPathExpressionException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        } catch (JDOMException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        }
        return stats;
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
            report.addException(ex, " usable class not found");
        }
        return IsUsableFor;
    }

    /**
     * Default function which returns a two/three line description of what this
     * class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class takes a coma file, updates the info using"
                + " the linked exbs and saves the coma file afterwards without changing"
                + " exbs.";
        return description;
    }

}
