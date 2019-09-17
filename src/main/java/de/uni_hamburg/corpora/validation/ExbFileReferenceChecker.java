/**
 * @file ExbErrorChecker.java
 *
 * A command-line tool / non-graphical interface for checking errors in
 * exmaralda's EXB files.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.io.File;
import java.util.Collection;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.cli.Option;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A validator for EXB-file's references.
 */
public class ExbFileReferenceChecker extends Checker implements CommandLineable, CorpusFunction {

    final String EXB_REFS = "exb-referenced-file";


    CorpusData cd;

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        this.cd = cd;
        try {
            stats = exceptionalCheck(cd);
        } catch (IOException ioe) {
            stats.addException(ioe, "Reading error");
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        } catch (TransformerException ex) {
            stats.addException(ex, "Unknown parsing error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, "Unknown parsing error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, "Unknown URI parsing error");
        }
        return stats;
    }

    /**
     * Main feature of the class: Checks Exmaralda .exb file for file
     * references, if a referenced file does not exist, issues a warning.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException, URISyntaxException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        NodeList reffiles = doc.getElementsByTagName("referenced-file");
        int reffilesFound = 0;
        int reffilesMissing = 0;
        Report stats = new Report();
        for (int i = 0; i < reffiles.getLength(); i++) {
            Element reffile = (Element) reffiles.item(i);
            String url = reffile.getAttribute("url");
            if (!url.isEmpty()) {
                if (url.startsWith("file:///C:") || url.startsWith("file:/C:")) {
                    stats.addCritical(EXB_REFS, cd, "Referenced-file " + url
                            + " points to absolute local path, fix to relative path first");
                }
                boolean found = false;
                File justFile = new File(url);
                if (justFile.exists()) {
                    found = true;
                }
                URL referencePath = cd.getParentURL();
                URL absPath = new URL(referencePath + "/" + url);
                File absFile = new File(absPath.toURI());
                if (absFile.exists()) {
                    found = true;
                }
                if (!found) {
                    reffilesMissing++;
                    stats.addMissing(EXB_REFS, cd, "File in referenced-file NOT found: " + url);
                    exmaError.addError(EXB_REFS, cd.getURL().getFile(), "", "", false, "Error: File in referenced-file NOT found: " + url);
                } else {
                    reffilesFound++;
                    stats.addCorrect(EXB_REFS, cd, "File in referenced-file was found: " + url);
                }
            } else {
            stats.addCorrect(EXB_REFS, cd, "No file was referenced in this transcription");
            }
        }
        return stats;
    }

    /**
     * No fix is applicable for this feature.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        this.cd = cd;
        report.addCritical(EXB_REFS,
                "Automatic fix is not yet supported.");
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
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

}
