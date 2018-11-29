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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
public class ComaNSLinksChecker extends Checker implements CommandLineable, CorpusFunction {

    String referencePath = "./";
    String comaLoc = "";
    String communicationname;

    final String COMA_NSLINKS = "coma-nslinks";
    final String COMA_RELPATHS = "coma-relpaths";

    public ComaNSLinksChecker() {
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
            stats.addException(pce, COMA_NSLINKS, cd, "Unknown parsing error.");
        } catch (SAXException saxe) {
            stats.addException(saxe, COMA_NSLINKS, cd, "Unknown parsing error.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            stats.addException(ioe, COMA_NSLINKS, cd, "Unknown file reading error.");
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            stats.addException(ex, COMA_NSLINKS, cd, "Unknown file reading error.");
        }
        return stats;
    }

    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        Document doc = (Document) TypeConverter.String2JdomDocument(cd.toSaveableString());
        NodeList nslinks = doc.getElementsByTagName("NSLink");
        Report stats = new Report();
        for (int i = 0; i < nslinks.getLength(); i++) {
            Element nslink = (Element) nslinks.item(i);
            Node communication = nslink.getParentNode();
            if (communication.getNodeName() != null && communication.getNodeName().equals("Transcription")) {
                communicationname = communication.getParentNode().getAttributes().getNamedItem("Name").getTextContent();
            } else if (communication.getNodeName() != null && communication.getNodeName().equals("Recording")) {
                communicationname = communication.getParentNode().getParentNode().getAttributes().getNamedItem("Name").getTextContent();
            } else {
                //could not find matching communication name
                communicationname = "Could not figure out Communication name";
            }
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
                System.out.println(absPath + "##############");
                File absFile = new File(absPath);
                if (absFile.exists()) {
                    found = true;
                }
                if (cd.getURL() != null) {
                    URL urlPath = cd.getURL();
                    //I think here is the Linux Problem 
                    URL urlAbsPath = new URL(urlPath, nspath.replace(File.separator, "/"));
                    System.out.println(urlPath + "##############");
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
                    stats.addCritical(COMA_NSLINKS, cd,
                            "In Communication: " + communicationname + " File in NSLink not found: " + nspath);
                } else {
                    stats.addCorrect(COMA_NSLINKS, cd,
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
                Node communicationrel = maybeText.getParentNode().getParentNode();
                if (communicationrel.getNodeName() != null && communicationrel.getNodeName().equals("File")) {
                    communicationname = communicationrel.getParentNode().getAttributes().getNamedItem("Name").getTextContent();
                } else {
                    //could not find matching communication name
                    communicationname = "Could not figure out Communication name";
                }
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
                if (cd.getURL() != null) {
                    URL urlPath = cd.getURL();
                    URL urlRelPath = new URL(urlPath, relpath.replace("\\", "/"));
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
                    stats.addCritical(COMA_NSLINKS, cd,
                            "In Communication: " + communicationname + " File in relPath not found: " + relpath);
                } else {
                    stats.addCorrect(COMA_NSLINKS, cd,
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
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
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
