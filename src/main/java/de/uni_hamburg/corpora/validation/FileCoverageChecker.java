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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.List;
import java.util.Stack;
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


/**
 * A class that can load coma data and check for potential problems with HZSK
 * repository depositing.
 */
public class FileCoverageChecker implements CommandLineable, StringChecker {

    ValidatorSettings settings;
    String referencePath = "./";
    File referenceFile;
    String comaLoc = "";

    final String COMA_FILECOVERAGE = "coma-filecoverage";

    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    public StatisticsReport check(String s) {
        StatisticsReport stats = new StatisticsReport();
        try {
            stats = exceptionalCheck(s);
        } catch(ParserConfigurationException pce) {
            stats.addException(pce, comaLoc + ": " + "Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, comaLoc + ": " + "Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, comaLoc + ": " + "Unknown file reading error");
        }
        return stats;
    }

    private String stripPrefix(String path, String prefix) {
        return path.replaceFirst("^" + prefix.replace("\\", "\\\\") +
                File.separator.replace("\\", "\\\\"), "");

    }

    private StatisticsReport exceptionalCheck(String data)
            throws SAXException, IOException, ParserConfigurationException {
        Set<String> allFilesPaths = new HashSet<String>();
        StatisticsReport stats = new StatisticsReport();
        if (settings.getDataDirectory() != null) {
            Stack<File> dirs = new Stack<File>();
            dirs.add(settings.getDataDirectory());
            String prefix = settings.getDataDirectory().getCanonicalPath();
            while (!dirs.empty()) {
                File files[] = dirs.pop().listFiles();
                for (File f : files) {
                    if (f.getName().equals(".git")) {
                        continue;
                    } else if (f.isDirectory()) {
                        dirs.add(f);
                    } else {
                        String relPath = stripPrefix(f.getCanonicalPath(),
                                prefix);
                        if (relPath.equals(f.getCanonicalPath())) {
                            System.err.println("Cannot figure out relative path"
                                + " for: " + f.getCanonicalPath());
                        } else {
                            allFilesPaths.add(relPath);
                        }
                    }
                }
            }
        }
        if (settings.getBaseDirectory() != null) {
            Stack<File> dirs = new Stack();
            dirs.add(settings.getBaseDirectory());
            String prefix = settings.getBaseDirectory().getCanonicalPath();
            while (!dirs.empty()) {
                File files[] = dirs.pop().listFiles();
                for (File f : files) {
                    if (f.getName().equals(".git")) {
                        continue;
                    } else if (f.isDirectory()) {
                        dirs.add(f);
                    } else {
                        String relPath = stripPrefix(f.getCanonicalPath(),
                                 prefix);
                        if (relPath.equals(f.getCanonicalPath())) {
                            System.err.println("Cannot figure out relative path"
                                + " for: " + f.getCanonicalPath());
                        } else {
                            allFilesPaths.add(relPath);
                        }
                    }
                }
            }
        }
        if (allFilesPaths.size() == 0) {
            Stack<File> dirs = new Stack();
            dirs.add(referenceFile);
            String prefix = referencePath;
            while (!dirs.empty()) {
                File files[] = dirs.pop().listFiles();
                for (File f : files) {
                    if (f.getName().equals(".git")) {
                        continue;
                    } else if (f.isDirectory()) {
                        dirs.add(f);
                    } else {
                        String relPath = stripPrefix(f.getCanonicalPath(),
                                prefix);
                        if (relPath.equals(f.getCanonicalPath())) {
                            System.err.println("Cannot figure out relative path"
                                + " for: " + f.getCanonicalPath());
                        } else {
                            allFilesPaths.add(relPath);
                        }
                    }
                }
            }
        }
        Set<String> NSLinksPaths = new HashSet<String>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(data);
        NodeList nslinks = doc.getElementsByTagName("NSLink");
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
                // added this line so it compares Coma NSLinks in the correct format of the OS
                // it still doesn't work if there are absoulte paths in the NSlinks, but that shouldn#t be the case anyway
                nspath = nspath.replace('/', File.separatorChar);
                //System.out.println(nspath);
                NSLinksPaths.add(nspath);
            }
        }
        Set<String> RelPaths = new HashSet<String>();
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
                RelPaths.add(relpath);
            }
        }
        Set<String> comaPaths = new HashSet<String>(NSLinksPaths);
        comaPaths.addAll(RelPaths);
        for (String s : allFilesPaths) {
            if (comaPaths.contains(s)) {
                stats.addCorrect(COMA_FILECOVERAGE, comaLoc + ": " +
                        "File both in coma and filesystem: " + s);
            } else {
                stats.addCritical(COMA_FILECOVERAGE, comaLoc + ": " +
                        "File on filesystem is not explained in coma: " + s);
            }
        }
        return stats;
    }

    public StatisticsReport doMain(String[] args) {
        settings = new ValidatorSettings("FileCoverageChecker",
                "Checks Exmaralda .coma file against directory, to find " +
                "undocumented files",
                "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking coma file against directory...");
        }
        StatisticsReport stats = new StatisticsReport();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            try {
                comaLoc = f.getName();
                String s = TypeConverter.InputStream2String(new
                        FileInputStream(f));
                referencePath = "./";
                if (f.getParentFile() != null) {
                    referenceFile = f.getParentFile();
                    referencePath = f.getParentFile().getCanonicalPath();
                }
                stats = check(s);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return stats;
    }

    public static void main(String[] args) {
        FileCoverageChecker checker = new FileCoverageChecker();
        StatisticsReport stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }


}
