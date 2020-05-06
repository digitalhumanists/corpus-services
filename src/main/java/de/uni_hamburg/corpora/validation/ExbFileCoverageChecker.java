/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
 *
 * @author anne
 */
public class ExbFileCoverageChecker extends Checker implements CorpusFunction {

    static List<String> whitelist;
    static List<String> fileendingwhitelist;

    public ExbFileCoverageChecker() {
        // these are acceptable
        setWhitelist();
        

    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, function, cd, "Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown tranformer error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown XPath error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature: checks whether files are both in the
     * exb file and file system.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException {
        Report stats = new Report();
        // FIXME:
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        NodeList reffiles = doc.getElementsByTagName("referenced-file");
        ArrayList<String> refsInExb = new ArrayList<String>();
        for (int i = 0; i < reffiles.getLength(); i++) {
            Element reffile = (Element) reffiles.item(i);
            String url = reffile.getAttribute("url");
            if (!url.isEmpty()) {
                if (url.startsWith("file:///C:") || url.startsWith("file:/C:")) {
                    stats.addCritical(function, cd, "Referenced-file " + url
                            + " points to absolute local path, fix to relative path first");
                }
                refsInExb.add(url);
            }
        }
        URL referencePath = cd.getParentURL();
        
        File exbFolder = new File(referencePath.toURI());
        ArrayList<String> files = new ArrayList<String>();
        search(exbFolder, files);
        for (String absolutePath : files) {
            String relativePath = absolutePath.substring(absolutePath.indexOf(exbFolder.getAbsolutePath())+exbFolder.getAbsolutePath().length()+File.separator.length());
            if (refsInExb.contains(absolutePath)) {
                stats.addCritical(function, cd, "Referenced-file " + absolutePath
                        + " points to absolute local path, fix to relative path first");
            } else if (refsInExb.contains(relativePath)) {
                stats.addCorrect(function, cd, "File " + relativePath + " found in the exb as a reference.");
            } else {
                stats.addCritical(function, cd, "File " + relativePath + " CANNOT be found in the exb as a reference!");
                exmaError.addError(function, cd.getURL().getFile(), "", "", false, "File " + relativePath + " CANNOT be found in the exb as a reference!");
            }
        }
        return stats;
    }

    /**
     * Fix to this issue is not supported yet.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(function, cd,
                "No fix is supported yet");
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

    public static void setWhitelist() {
        whitelist = new ArrayList<String>();
        whitelist.add(".git");
        whitelist.add(".gitignore");
        whitelist.add("README");
        whitelist.add("Thumbs.db");
        fileendingwhitelist = new ArrayList<String>();
        fileendingwhitelist.add("exb");
        fileendingwhitelist.add("exs");
        fileendingwhitelist.add("doc"); 
        fileendingwhitelist.add("docx");
        fileendingwhitelist.add("odt");
        fileendingwhitelist.add("pdf");
        fileendingwhitelist.add("rtf");
        fileendingwhitelist.add("tex");
        fileendingwhitelist.add("txt");
        fileendingwhitelist.add("xml");
        fileendingwhitelist.add("html");
        fileendingwhitelist.add("flextext");
    }
    
     /**
     * Search function for getting all the files under the same folder with the
     * basic transcription file and sub-folders.
     */
    public static void search(File folder, List<String> result) {
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                search(f, result);
            }
            if (f.isFile() && !fileendingwhitelist.contains(getFileExtension(f)) 
                    && !whitelist.contains(f.getAbsolutePath())) {
                result.add(f.getAbsolutePath());
            }
        }
    }

    private static String getFileExtension(File f) {
        String extension = "";
        String fileName = f.getName();
        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class checks whether files are both in the "
                + "exb file and file system.";
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
