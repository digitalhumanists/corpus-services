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

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.io.File;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A validator for EXB-file's references.
 */
public class ExbFileReferenceChecker extends Checker implements CorpusFunction {

    public ExbFileReferenceChecker() {
        //no fixing option available
        super(false);
    }

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
            stats.addException(ioe, function, cd, "Reading error");
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown parsing error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown parsing error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, function, cd, "Unknown URI parsing error");
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
                    stats.addCritical(function, cd, "Referenced-file " + url
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
                    stats.addCritical(function, cd, "File in referenced-file NOT found: " + url);
                    exmaError.addError(function, cd.getURL().getFile(), "", "", false, "Error: File in referenced-file NOT found: " + url);
                } else {
                    reffilesFound++;
                    stats.addCorrect(function, cd, "File in referenced-file was found: " + url);
                }
            } else {
            stats.addCorrect(function, cd, "No file was referenced in this transcription");
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
        report.addCritical(function, cd,
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

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class is a validator for EXB-file's references;"
                + " it checks Exmaralda .exb file for file references if a referenced "
                + "file does not exist, issues a warning;";
        return description;
    }

    @Override
    public Report check(Corpus c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report function(CorpusData cd, Boolean fix) throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
