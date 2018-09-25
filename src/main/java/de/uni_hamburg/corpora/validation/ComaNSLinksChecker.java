/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.TierFormatTable;
import org.exmaralda.partitureditor.jexmaralda.BasicBody;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.net.URISyntaxException;

/**
 * A class that can load coma data and check for potential problems with HZSK
 * repository depositing.
 */
public class ComaNSLinksChecker extends Checker implements CommandLineable, CorpusFunction {

    ValidatorSettings settings;
    String referencePath = "./";
    String comaLoc = "";

    final String COMA_NSLINKS = "coma-nslinks";
    final String COMA_RELPATHS = "coma-relpaths";

    public ComaNSLinksChecker() {
    settings = new ValidatorSettings("ComaNSLinksChecker",
                "Checks Exmaralda .coma file for NSLink references and relPaths that do not "
                + "exist", "If input is a directory, performs recursive check "
                + "from that directory, otherwise checks input file");
    }

    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, comaLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, comaLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            stats.addException(ioe, comaLoc + ": Unknown file reading error");
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            stats.addException(ex, comaLoc + ": Unknown file reading error");
        }
        return stats;
    }

    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString()));
        NodeList nslinks = doc.getElementsByTagName("NSLink");
        Report stats = new Report();
        for (int i = 0; i < nslinks.getLength(); i++) {
            Element nslink = (Element) nslinks.item(i);
            NodeList nstexts = nslink.getChildNodes();
            for (int j = 0; j < nstexts.getLength(); j++) {
                Node maybeText = nstexts.item(j);
                if (maybeText.getNodeType() != Node.TEXT_NODE) {
                    System.err.print("This is not a text node: "
                            + maybeText);
                    continue;
                }
                Text nstext = (Text) nstexts.item(j);
                String nspath = nstext.getWholeText().replace("/", File.separator);
                File justFile = new File(nspath);
                boolean found = false;
                if (justFile.exists()) {
                    found = true;
                }
                String absPath = referencePath + File.separator + nspath;
                File absFile = new File(absPath);
                if (absFile.exists()) {
                    found = true;
                }
                if(cd.getURL() != null){
                    URL urlPath = cd.getURL();
                    URL urlAbsPath = new URL(urlPath , nspath.replace(File.separator, "/"));
                    //System.out.println(urlPath + "##############");
                    File dataFile = new File(urlAbsPath.toURI());
                    if (dataFile.exists()) {
                        found = true;
                    }
                }
                if (settings.getDataDirectory() != null) {
                    String dataPath
                            = settings.getDataDirectory().getCanonicalPath()
                            + File.separator + nspath;
                    File dataFile = new File(dataPath);
                    if (dataFile.exists()) {
                        found = true;
                    }
                }
                if (settings.getBaseDirectory() != null) {
                    String basePath
                            = settings.getBaseDirectory().getCanonicalPath()
                            + File.separator + nspath;
                    File baseFile = new File(basePath);
                    if (baseFile.exists()) {
                        found = true;
                    }
                }
                if (!found) {
                    stats.addCritical(COMA_NSLINKS,
                            "File in NSLink not found: " + nspath);
                } else {
                    stats.addCorrect(COMA_NSLINKS,
                            "File in NSLink was found: " + nspath);
                }
            }
        }
        NodeList relpathnodes = doc.getElementsByTagName("relPath");
        for (int i = 0; i < relpathnodes.getLength(); i++) {
            Element relpathnode = (Element) relpathnodes.item(i);
            NodeList reltexts = relpathnode.getChildNodes();
            for (int j = 0; j < reltexts.getLength(); j++) {
                Node maybeText = reltexts.item(j);
                if (maybeText.getNodeType() != Node.TEXT_NODE) {
                    System.err.print("This is not a text node: "
                            + maybeText);
                    continue;
                }
                Text reltext = (Text) reltexts.item(j);
                String relpath = reltext.getWholeText().replace("/", File.separator);
                File justFile = new File(relpath);
                boolean found = false;
                if (justFile.exists()) {
                    found = true;
                }
                String absPath = referencePath + File.separator + relpath;
                File absFile = new File(absPath);
                if (absFile.exists()) {
                    found = true;
                }
                if(cd.getURL() != null){
                    URL urlPath = cd.getURL();
                    URL urlRelPath = new URL(urlPath , relpath.replace("\\", "/"));
                    File dataFile = new File(urlRelPath.toURI());
                    if (dataFile.exists()) {
                        found = true;
                    }
                }
                if (settings.getDataDirectory() != null) {
                    String dataPath
                            = settings.getDataDirectory().getCanonicalPath()
                            + File.separator + relpath;
                    File dataFile = new File(dataPath);
                    if (dataFile.exists()) {
                        found = true;
                    }
                }
                if (settings.getBaseDirectory() != null) {
                    String basePath
                            = settings.getBaseDirectory().getCanonicalPath()
                            + File.separator + relpath;
                    File baseFile = new File(basePath);
                    if (baseFile.exists()) {
                        found = true;
                    }
                }
                if (!found) {
                    stats.addCritical(COMA_NSLINKS,
                            "File in relPath not found: " + relpath);
                } else {
                    stats.addCorrect(COMA_NSLINKS,
                            "File in relPath was found: " + relpath);
                }
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("ComaNSLinksChecker",
                "Checks Exmaralda .coma file for NSLink references that do not "
                + "exist", "If input is a directory, performs recursive check "
                + "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking COMA files for references...");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            try {
                if (settings.isVerbose()) {
                    System.out.println(" * " + f.getName());
                }
                referencePath = "./";
                if (f.getParentFile() != null) {
                    referencePath = f.getParentFile()
                            .getCanonicalPath();
                }
                comaLoc = f.getName();
                CorpusIO cio = new CorpusIO();
                CorpusData cd = cio.readFile(f.toURI().toURL());
                stats = check(cd);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return stats;
    }

    public static void main(String[] args) {
        ComaNSLinksChecker checker = new ComaNSLinksChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }

    @Override
    public Report check(Collection<CorpusData> cdc) throws SAXException, JexmaraldaException, IOException, JDOMException {
        for (CorpusData cd : cdc) {
            report.merge(check(cd));
        }
        return report;
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(COMA_NSLINKS,
                "Wrong NS links cannot be fixed automatically");
        return report;
    }

    @Override
    public Report fix(Collection<CorpusData> cdc) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(COMA_NSLINKS,
                "Wrong NS links cannot be fixed automatically");
        return report;
    }

    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ComaNSLinksChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

    @Override
    public Report execute(Corpus c) {
        for (CorpusData cd : c.getCorpusData()) {
            report.merge(check(cd));
        }
        return report;
    }

}
