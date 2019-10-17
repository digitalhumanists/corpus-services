package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.SegmentedTranscriptionData;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    final String COMA_UP_SEG = "coma-update-segment-counts";
    String path2ExternalFSM = "";

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) {
        report.addCritical(COMA_UP_SEG, cd.getURL().getFile(), "Checking option is not available");
        return report;
    }

    /**
     * Main feature of the class: takes a coma file, updates the info using the
     * linked exbs and saves the coma file afterwards without changing exbs;
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, IOException {
        Report stats = new Report();
        CorpusIO cio = new CorpusIO();
        SegmentedTranscriptionData exs;
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
                        String s = e.getChildText("NSLink");
                        url = new URL(s);
                        exs = (SegmentedTranscriptionData) cio.readFileURL(url);
                        List segmentCounts = exs.getSegmentCounts();
                        for (Object segmentCount : segmentCounts) {
                            e.addContent("Key");
                        }
                        /*
                        descKeys = element.getChild("Description")
                                .getChildren();
                        if (removeStats) {
                            List<Element> toRemove = new ArrayList<Element>();

                            for (Element ke : (List<Element>) descKeys) {
                                if (Pattern.matches("#(..).*:.*", ke.getAttributeValue("Name"))) {
                                    //System.out.println("RefreshTranscriptionTask.doInBackground" + ke.getAttributeValue("Name"));
                                    // System.out.println(ke.getAttributeValue("Name"));
                                    toRemove.add(ke);
                                }
                            }
                            for (Element re : toRemove) {
                                descKeys.remove(re);
                            }
                        }

                        Iterator sI = segments.entrySet().iterator();
                        boolean set = false;
                        while (sI.hasNext()) {
                            Map.Entry entry = (Map.Entry) sI.next();
                            Object key = entry.getKey();
                            Object value = entry.getValue();
                            descKeys = element.getChild("Description")
                                    .getChildren();
                            Iterator keyI = descKeys.iterator();
                            while (keyI.hasNext()) {
                                myKey = (Element) keyI.next();
                                if (myKey.getAttributeValue("Name").equals(
                                        "# " + key)) {
                                    myKey.setText(value.toString());
                                    set = true;
                                }
                            }
                            if (!set) {
                                Element newKey = new Element("Key");
                                newKey.setAttribute("Name", "# " + key);
                                newKey.setText(value.toString());
                                element.getChild("Description").addContent(
                                        newKey);
                            }
                            set = false;
                        }
                        */
                    } else {
                        stats.addCorrect(COMA_UP_SEG, cd, "Coma fiel does not contain segmented transcriptions");
                    }
                }
            }
            cio.write(cd, cd.getURL());
            if (cd != null) {
                report.addCorrect(COMA_UP_SEG, cd, "Updated the segment counts!");
            } else {
                report.addCritical(COMA_UP_SEG, cd, "Updating the segment counts was not possible!");
            }
        } catch (IOException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown IO exception");
        } catch (TransformerException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown xml exception");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown xml exception");
        } catch (SAXException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown xml exception");
        } catch (XPathExpressionException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown xml exception");
        } catch (JDOMException ex) {
            Logger.getLogger(ComaUpdateSegmentCounts.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JexmaraldaException ex) {
            Logger.getLogger(ComaUpdateSegmentCounts.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ExbSegmenter.class.getName()).log(Level.SEVERE, null, ex);
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
