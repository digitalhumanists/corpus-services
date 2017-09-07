/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;


import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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


/**
 * A class that can load coma data and check for potential problems with HZSK
 * repository depositing.
 */
public class ComaNSLinksChecker implements CommandLineable {

    ValidatorSettings settings;

    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    public Collection<ErrorMessage> check(File f) {
        Collection<ErrorMessage> errors;
        try {
            errors = exceptionalCheck(f);
        } catch(ParserConfigurationException pce) {
            errors = new ArrayList<ErrorMessage>();
            errors.add(new ErrorMessage(ErrorMessage.Severity.CRITICAL,
                    f.getName(),
                    "Parsing error", "Unknown"));
        } catch(SAXException saxe) {
            errors = new ArrayList<ErrorMessage>();
            errors.add(new ErrorMessage(ErrorMessage.Severity.CRITICAL,
                    f.getName(),
                    "Parsing error", "Unknown"));
        } catch(IOException ioe) {
            errors = new ArrayList<ErrorMessage>();
            errors.add(new ErrorMessage(ErrorMessage.Severity.CRITICAL,
                    f.getName(),
                    "Reading error", "Unknown"));
        }
        return errors;
    }


    private Collection<ErrorMessage> exceptionalCheck(File f)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        NodeList nslinks = doc.getElementsByTagName("NSLink");
        List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
        for (int i = 0; i < nslinks.getLength(); i++) {
            Element nslink = (Element)nslinks.item(i);
            NodeList nstexts = nslink.getChildNodes();
            for (int j = 0; j < nstexts.getLength(); j++) {
                Node maybeText = nstexts.item(j);
                if (maybeText.getNodeType() != Node.TEXT_NODE) {
                    System.err.print("This is not a text node: " +
                            maybeText);
                    continue;
                }
                Text nstext = (Text)nstexts.item(j);
                String nspath = nstext.getWholeText();
                File justFile = new File(nspath);
                boolean found = false;
                if (justFile.exists()) {
                    found = true;
                }
                String referencePath = "./";
                if (f.getParentFile() != null) {
                    referencePath = f.getParentFile()
                        .getCanonicalPath();
                }
                String absPath = referencePath + File.separator + nspath;
                File absFile = new File(absPath);
                if (absFile.exists()) {
                    found = true;
                }
                if (settings.getDataDirectory() != null) {
                    String dataPath =
                        settings.getDataDirectory().getCanonicalPath() +
                        File.separator + nspath;
                    File dataFile = new File(dataPath);
                    if (dataFile.exists()) {
                        found = true;
                    }
                }
                if (settings.getBaseDirectory() != null) {
                    String basePath =
                        settings.getBaseDirectory().getCanonicalPath() +
                        File.separator + nspath;
                    File baseFile = new File(basePath);
                    if (baseFile.exists()) {
                        found = true;
                    }
                }
                if (!found) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.CRITICAL,
                                f.getAbsolutePath(),
                                "File in NSLink not found: " + nspath,
                                "check that the file exists or locate it"));
                } else {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.NOTE,
                                f.getAbsolutePath(),
                                "File in NSLink was found: " + nspath,
                                "Everything's good!"));
                }
            }
        }
        NodeList relpathnodes = doc.getElementsByTagName("relPath");
        for (int i = 0; i < relpathnodes.getLength(); i++) {
            Element relpathnode = (Element)relpathnodes.item(i);
            NodeList reltexts = relpathnode.getChildNodes();
            for (int j = 0; j < reltexts.getLength(); j++) {
                Node maybeText = reltexts.item(j);
                if (maybeText.getNodeType() != Node.TEXT_NODE) {
                    System.err.print("This is not a text node: " +
                            maybeText);
                    continue;
                }
                Text reltext = (Text)reltexts.item(j);
                String relpath = reltext.getWholeText();
                File justFile = new File(relpath);
                boolean found = false;
                if (justFile.exists()) {
                    found = true;
                }
                String referencePath = "./";
                if (f.getParentFile() != null) {
                    referencePath = f.getParentFile()
                        .getCanonicalPath();
                }
                String absPath = referencePath + File.separator + relpath;
                File absFile = new File(absPath);
                if (absFile.exists()) {
                    found = true;
                }
                if (settings.getDataDirectory() != null) {
                    String dataPath =
                        settings.getDataDirectory().getCanonicalPath() +
                        File.separator + relpath;
                    File dataFile = new File(dataPath);
                    if (dataFile.exists()) {
                        found = true;
                    }
                }
                if (settings.getBaseDirectory() != null) {
                    String basePath =
                        settings.getBaseDirectory().getCanonicalPath() +
                        File.separator + relpath;
                    File baseFile = new File(basePath);
                    if (baseFile.exists()) {
                        found = true;
                    }
                }
                if (!found) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.CRITICAL,
                                f.getAbsolutePath(),
                                "File in NSLink not found: " + relpath,
                                "check that the file exists or locate it"));
                } else {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.NOTE,
                                f.getAbsolutePath(),
                                "File in NSLink was found: " + relpath,
                                "Everything's good!"));
                }
            }
        }
        return errors;
    }

    public void doMain(String[] args) {
        settings = new ValidatorSettings("ComaNSLinksChecker",
                "Checks Exmaralda .coma file for NSLink references that do not " +
                "exist", "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking COMA files for references...");
        }
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            Collection<ErrorMessage> errors = check(f);
            for (ErrorMessage em : errors) {
                System.out.println("   - "  + em);
            }
        }
    }

    public static void main(String[] args) {
        ComaNSLinksChecker checker = new ComaNSLinksChecker();
        checker.doMain(args);
    }

}
