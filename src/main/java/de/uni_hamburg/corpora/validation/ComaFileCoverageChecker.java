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
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.cli.Option;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.net.URISyntaxException;

/**
 * A class that can load coma data and check for potential problems with HZSK
 * repository depositing.
 */
public class ComaFileCoverageChecker extends Checker implements CorpusFunction {

    ValidatorSettings settings;
    String referencePath = "./";
    File referenceFile;
    String comaLoc = "";
    int comacounter = 0;

    final String COMA_FILECOVERAGE = "coma-filecoverage";
    final List<String> whitelist;
    final List<String> fileendingwhitelist;
    final List<String> directorywhitelist;

    public ComaFileCoverageChecker() {
        // these are acceptable
        whitelist = new ArrayList<String>();
        whitelist.add(".git");
        whitelist.add(".gitignore");
        whitelist.add("README");
        whitelist.add("Thumbs.db");
        fileendingwhitelist = new ArrayList<String>();
        directorywhitelist = new ArrayList<String>();
        directorywhitelist.add("curation");
        directorywhitelist.add("resources");
        directorywhitelist.add("metadata");
    }

    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    public Report oldCheck(String s) {
        Report stats = new Report();
        try {
            stats = oldExceptionalCheck(s);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, COMA_FILECOVERAGE, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, COMA_FILECOVERAGE, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, COMA_FILECOVERAGE, cd, "Unknown file reading error");
        }
        return stats;
    }

    private String stripPrefix(String path, String prefix) {
        return path.replaceFirst("^" + prefix.replace("\\", "\\\\")
                + File.separator.replace("\\", "\\\\"), "");

    }

    private Report oldExceptionalCheck(String data)
            throws SAXException, IOException, ParserConfigurationException {
        Set<String> allFilesPaths = new HashSet<String>();
        Report stats = new Report();
        if (settings.getDataDirectory() != null) {
            Stack<File> dirs = new Stack<File>();
            dirs.add(settings.getDataDirectory());
            String prefix = settings.getDataDirectory().getCanonicalPath();
            while (!dirs.empty()) {
                File files[] = dirs.pop().listFiles();
                for (File f : files) {
                    if (whitelist.contains(f.getName()) || fileendingwhitelist.contains(getFileExtension(f)) || directorywhitelist.contains(f.getParentFile().getName()) || directorywhitelist.contains(f.getParentFile().getParentFile().getName())) {
                        continue;
                    } else if (f.isDirectory()) {
                        dirs.add(f);
                    } else if (f.getName().endsWith(".coma")) {
                        comacounter++;
                        if (comacounter > 1) {
                            stats.addCritical(COMA_FILECOVERAGE, cd, "There is more than one coma file in your corpus " + f.getName());
                        }
                        System.out.println(comacounter);
                        continue;
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
            Stack<File> dirs = new Stack<File>();
            dirs.add(settings.getBaseDirectory());
            String prefix = settings.getBaseDirectory().getCanonicalPath();
            while (!dirs.empty()) {
                File files[] = dirs.pop().listFiles();
                for (File f : files) {
                    if (whitelist.contains(f.getName()) || fileendingwhitelist.contains(getFileExtension(f)) || directorywhitelist.contains(f.getParentFile().getName()) || directorywhitelist.contains(f.getParentFile().getParentFile().getName())) {
                        continue;
                    } else if (f.isDirectory()) {
                        dirs.add(f);
                    } else if (f.getName().endsWith(".coma")) {
                        comacounter++;
                        if (comacounter > 1) {
                            stats.addCritical(COMA_FILECOVERAGE, cd, "There is more than one coma file in your corpus " + f.getName());
                        }
                        System.out.println(comacounter);
                        continue;
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
            Stack<File> dirs = new Stack<File>();
            dirs.add(referenceFile);
            String prefix = referencePath;
            while (!dirs.empty()) {
                File files[] = dirs.pop().listFiles();
                for (File f : files) {
                    if (whitelist.contains(f.getName()) || fileendingwhitelist.contains(getFileExtension(f)) || directorywhitelist.contains(f.getParentFile().getName()) || directorywhitelist.contains(f.getParentFile().getParentFile().getName())) {
                        continue;
                    } else if (f.isDirectory()) {
                        dirs.add(f);
                    } else if (f.getName().endsWith(".coma")) {
                        comacounter++;
                        if (comacounter > 1) {
                            stats.addCritical(COMA_FILECOVERAGE, cd, "There is more than one coma file in your corpus " + f.getName());
                        }
                        System.out.println(comacounter);
                        continue;
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
        Document doc = db.parse(TypeConverter.String2InputStream(data));
        NodeList nslinks = doc.getElementsByTagName("NSLink");
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
                String nspath = nstext.getWholeText();
                // added this line so it compares Coma NSLinks in the correct format of the OS
                // it still doesn't work if there are absoulte paths in the NSlinks, but that shouldn#t be the case anyway
                nspath = nspath.replace('/', File.separatorChar);
                System.out.println(nspath);
                NSLinksPaths.add(nspath);
            }
        }
        Set<String> RelPaths = new HashSet<String>();
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
                String relpath = reltext.getWholeText();
                // added this line so it compares Coma NSLinks in the correct format of the OS
                // it still doesn't work if there are absoulte paths in the NSlinks, but that shouldn't be the case anyway
                relpath = relpath.replace('/', File.separatorChar);
                System.out.println(relpath);
                RelPaths.add(relpath);
            }
        }
        Set<String> comaPaths = new HashSet<String>(NSLinksPaths);
        comaPaths.addAll(RelPaths);
        for (String s : allFilesPaths) {
            if (comaPaths.contains(s)) {
                stats.addCorrect(COMA_FILECOVERAGE, cd, "File is both in coma and filesystem" + s);
            } else {
                stats.addCritical(COMA_FILECOVERAGE, cd, "File on filesystem is not explained in coma" + s);
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("FileCoverageChecker",
                "Checks Exmaralda .coma file against directory, to find "
                + "undocumented files",
                "If input is a directory, performs recursive check "
                + "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking coma file against directory...");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            try {
                comaLoc = f.getName();
                String s = TypeConverter.InputStream2String(new FileInputStream(f));
                referencePath = "./";
                if (f.getParentFile() != null) {
                    referenceFile = f.getParentFile();
                    referencePath = f.getParentFile().getCanonicalPath();
                }
                stats = oldCheck(s);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return stats;
    }

    public static void main(String[] args) {
        ComaFileCoverageChecker checker = new ComaFileCoverageChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
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
            stats.addException(pce, COMA_FILECOVERAGE, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, COMA_FILECOVERAGE, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, COMA_FILECOVERAGE, cd, "Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, COMA_FILECOVERAGE, cd, "Unknown file reading error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature: checks whether files are both in coma
     * file and file system.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        Report stats = new Report();
        // FIXME:
        String[] path = new String[1];
        path[0] = cd.getURL().toString().substring(5);
        settings = new ValidatorSettings("FileCoverageChecker",
                "Checks Exmaralda .coma file against directory, to find "
                + "undocumented files",
                "If input is a directory, performs recursive check "
                + "from that directory, otherwise checks input file");
        settings.handleCommandLine(path, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking coma file against directory...");
        }
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            try {
                comaLoc = f.getName();
                String s = TypeConverter.InputStream2String(new FileInputStream(f));
                referencePath = "./";
                if (f.getParentFile() != null) {
                    referenceFile = f.getParentFile();
                    referencePath = f.getParentFile().getCanonicalPath();
                }
                Set<String> allFilesPaths = new HashSet<String>();
                if (settings.getDataDirectory() != null) {
                    Stack<File> dirs = new Stack<File>();
                    dirs.add(settings.getDataDirectory());
                    String prefix = settings.getDataDirectory().getCanonicalPath();
                    while (!dirs.empty()) {
                        File files[] = dirs.pop().listFiles();
                        for (File a : files) {
                            if (whitelist.contains(a.getName()) || fileendingwhitelist.contains(getFileExtension(a)) || directorywhitelist.contains(a.getParentFile().getName()) || directorywhitelist.contains(a.getParentFile().getParentFile().getName())) {
                                continue;
                            } else if (a.isDirectory()) {
                                dirs.add(a);
                            } else if (a.getName().endsWith(".coma")) {
                                comacounter++;
                                if (comacounter > 1) {
                                    stats.addCritical(COMA_FILECOVERAGE, cd, "There is more than one coma file in your corpus " + a.getName());
                                }
                                System.out.println(comacounter);
                                continue;
                            } else {
                                String relPath = stripPrefix(a.getCanonicalPath(),
                                        prefix);
                                if (relPath.equals(a.getCanonicalPath())) {
                                    System.err.println("Cannot figure out relative path"
                                            + " for: " + a.getCanonicalPath());
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
                        for (File b : files) {
                            if (whitelist.contains(b.getName()) || fileendingwhitelist.contains(getFileExtension(b)) || directorywhitelist.contains(b.getParentFile().getName()) || directorywhitelist.contains(b.getParentFile().getParentFile().getName())) {
                                continue;
                            } else if (b.isDirectory()) {
                                dirs.add(b);
                            } else if (b.getName().endsWith(".coma")) {
                                comacounter++;
                                if (comacounter > 1) {
                                    stats.addCritical(COMA_FILECOVERAGE, cd, "There is more than one coma file in your corpus " + b.getName());
                                }
                                System.out.println(comacounter);
                                continue;
                            } else {
                                String relPath = stripPrefix(b.getCanonicalPath(),
                                        prefix);
                                if (relPath.equals(b.getCanonicalPath())) {
                                    System.err.println("Cannot figure out relative path"
                                            + " for: " + b.getCanonicalPath());
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
                        for (File c : files) {
                            if (whitelist.contains(c.getName()) || fileendingwhitelist.contains(getFileExtension(c)) || directorywhitelist.contains(c.getParentFile().getName()) || directorywhitelist.contains(c.getParentFile().getParentFile().getName())) {
                                continue;
                            } else if (c.isDirectory()) {
                                dirs.add(c);
                            } else if (c.getName().endsWith(".coma")) {
                                comacounter++;
                                if (comacounter > 1) {
                                    stats.addCritical(COMA_FILECOVERAGE, cd, "There is more than one coma file in your corpus " + c.getName());
                                }
                                System.out.println(comacounter);
                                continue;
                            } else {
                                String relPath = stripPrefix(c.getCanonicalPath(),
                                        prefix);
                                if (relPath.equals(c.getCanonicalPath())) {
                                    System.err.println("Cannot figure out relative path"
                                            + " for: " + c.getCanonicalPath());
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
                Document doc = db.parse(TypeConverter.String2InputStream(s));
                NodeList nslinks = doc.getElementsByTagName("NSLink");
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
                        String relpath = reltext.getWholeText();
                        // added this line so it compares Coma NSLinks in the correct format of the OS
                        // it still doesn't work if there are absoulte paths in the NSlinks, but that shouldn#t be the case anyway
                        relpath = relpath.replace('/', File.separatorChar);
                        System.out.println(relpath);
                        RelPaths.add(relpath);
                    }
                }
                Set<String> comaPaths = new HashSet<String>(NSLinksPaths);
                comaPaths.addAll(RelPaths);
                for (String st : allFilesPaths) {
                    if (comaPaths.contains(st)) {
                        stats.addCorrect(COMA_FILECOVERAGE, cd, "File both in coma and filesystem: " + st);
                    } else {
                        stats.addCritical(COMA_FILECOVERAGE, cd, "File on filesystem is not explained in coma: " + st);
                    }
                }
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return stats;
    }

    /**
     * Fix to this issue is not supported yet.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(COMA_FILECOVERAGE, cd,
                "File names which do not comply with conventions cannot be fixed automatically");
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
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    public Report check(String data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void addWhiteListString(String s) {
        whitelist.add(s);
    }

    public void addFileEndingWhiteListString(String s) {
        fileendingwhitelist.add(s);
    }

    private String getFileExtension(File f) {
        String extension = "";
        String fileName = f.getName();
        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

}
