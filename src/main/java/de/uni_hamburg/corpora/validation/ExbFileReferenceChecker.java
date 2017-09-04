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
 * A command-line tool for checking EXB files.
 */
public class ExbFileReferenceChecker implements CommandLineable {

    ValidatorSettings settings;

    /**
     * Check for structural errors.
     *
     * @see GetSegmentationErrorsAction
     */
    public Collection<ErrorMessage> check(File f) {
        Collection<ErrorMessage> errors;
        try {
            errors = exceptionalCheck(f);
        } catch(IOException ioe) {
            errors = new ArrayList<ErrorMessage>();
            errors.add(new ErrorMessage(ErrorMessage.Severity.CRITICAL,
                    f.getName(),
                    "Parsing error", "Unknown"));
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
        }
        return errors;
    }

    public Collection<ErrorMessage> exceptionalCheck(File f)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        NodeList reffiles = doc.getElementsByTagName("referenced-file");
        int reffilesFound = 0;
        int reffilesMissing = 0;
        List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
        for (int i = 0; i < reffiles.getLength(); i++) {
            Element reffile = (Element)reffiles.item(i);
            String url = reffile.getAttribute("url");
            if (url.startsWith("file:///C:") || url.startsWith("file:/C:")) {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL,
                            f.getAbsolutePath(),
                            "Referenced-file " + url +
                            " points to absolute local path",
                            "use relative path instead?"));
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
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL,
                            f.getAbsolutePath(),
                            "File in referenced-file not found: " + url,
                            "check that the file exists or locate it"));
            } else {
                reffilesFound++;
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.NOTE,
                            f.getAbsolutePath(),
                            "File in referenced-file was found: " + url,
                            "Everything's good!"));
            }
        }
        return errors;
    }


    public void doMain(String[] args) {
        settings = new ValidatorSettings("ExbFileReferenceChecker",
                "Checks Exmaralda .exb file for file references that do not " +
                "exist", "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking EXB files for references...");
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
        ExbFileReferenceChecker checker = new ExbFileReferenceChecker();
        checker.doMain(args);
    }
}
