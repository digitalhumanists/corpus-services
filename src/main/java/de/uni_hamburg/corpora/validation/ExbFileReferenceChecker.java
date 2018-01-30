/**
 * @file ExbErrorChecker.java
 *
 * A command-line tool / non-graphical interface
 * for checking errors in exmaralda's EXB files.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.StatisticsReport;
import de.uni_hamburg.corpora.CommandLineable;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.Option;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * A validator for EXB-file's references.
 */
public class ExbFileReferenceChecker implements CommandLineable {

    ValidatorSettings settings;

    final String EXB_REFS="exb-referenced-file";

    String exbName = "";

    /**
     * Check for referenced-files.
     */
    public StatisticsReport check(File f) {
        StatisticsReport stats = new StatisticsReport();
        try {
            exbName = f.getName();
            stats = exceptionalCheck(f);
        } catch(IOException ioe) {
            stats.addException(ioe, "Reading error");
        } catch(ParserConfigurationException pce) {
            stats.addException(pce, "Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        }
        return stats;
    }

    public StatisticsReport exceptionalCheck(File f)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        NodeList reffiles = doc.getElementsByTagName("referenced-file");
        int reffilesFound = 0;
        int reffilesMissing = 0;
        StatisticsReport stats = new StatisticsReport();
        for (int i = 0; i < reffiles.getLength(); i++) {
            Element reffile = (Element)reffiles.item(i);
            String url = reffile.getAttribute("url");
            if (url.startsWith("file:///C:") || url.startsWith("file:/C:")) {
                stats.addCritical(EXB_REFS, exbName + ": " +
                            "Referenced-file " + url +
                            " points to absolute local path",
                            "use relative path instead?");
            }
            File justFile = new File(url);
            boolean found = false;
            if (justFile.exists()) {
                found = true;
            }
            String relfilename = url;
            if (url.lastIndexOf("/") != -1) {
                relfilename = url.substring(url.lastIndexOf("/"));
            }
            String referencePath = f.getParentFile().getCanonicalPath();
            String absPath = referencePath + File.separator + relfilename;
            File absFile = new File(absPath);
            if (absFile.exists()) {
                found = true;
            }
            if (!found) {
                reffilesMissing++;
                stats.addCritical(EXB_REFS, exbName + ": " +
                            "File in referenced-file NOT found: " + url);
            } else {
                reffilesFound++;
                stats.addCorrect(EXB_REFS, exbName + ": " +
                            "File in referenced-file was found: " + url);
            }
        }
        return stats;
    }


    public StatisticsReport doMain(String[] args) {
        settings = new ValidatorSettings("ExbFileReferenceChecker",
                "Checks Exmaralda .exb file for file references that do not " +
                "exist", "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking EXB files for references...");
        }
        StatisticsReport stats = new StatisticsReport();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            stats = check(f);
        }
        return stats;
    }

    public static void main(String[] args) {
        ExbFileReferenceChecker checker = new ExbFileReferenceChecker();
        StatisticsReport stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }
}
