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
public class ComaPIDLengthChecker implements CommandLineable {

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
        NodeList keys = doc.getElementsByTagName("Key");
        String corpusPrefix = "";
        String corpusVersion = "";
        for (int i = 0; i < keys.getLength(); i++) {
            Element keyElement = (Element)keys.item(i);
            if (keyElement.getAttribute("Name").equals("HZSK:corpusprefix")) {
                corpusPrefix = keyElement.getTextContent();
            } else if
                (keyElement.getAttribute("Name").equals("HZSK:corpusversion")) {
                corpusVersion = keyElement.getTextContent();
            }
        }
        List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
        if (corpusPrefix.equals("")) {
            errors.add(new ErrorMessage(ErrorMessage.Severity.WARNING,
                        f.getName(),
                        "Missing <Key name='HZSK:corpusprefix'>, " +
                        "PID length cannot be estimated accurately.",
                        "Add that key in coma."));
            corpusPrefix = "muster";
        }
        if (corpusVersion.equals("")) {
            errors.add(new ErrorMessage(ErrorMessage.Severity.WARNING,
                        f.getName(),
                        "Missing <Key name='HZSK:corpusversion'>, " +
                        "PID length cannot be estimated accurately.",
                        "Add that key in coma."));
            corpusVersion = "0.0";
        }
        NodeList communications = doc.getElementsByTagName("Communication");
        for (int i = 0; i < communications.getLength(); i++) {
            Element communication = (Element)communications.item(i);
            String communicationName = communication.getAttribute("Name");
            String fedoraPID = new String("communication: " + corpusPrefix +
                    "-" + corpusVersion +
                    "_" + communicationName);
            if (fedoraPID.length() >= 64) {
                errors.add(new ErrorMessage(ErrorMessage.Severity.CRITICAL,
                            f.getName(),
                            "Communication is too long for Fedora PID" +
                            "generation: " + fedoraPID,
                            "It must be shortened, e.g. use: " +
                            fedoraPID.substring(0, 60) + ", or change " +
                            "the corpus prefix"));
            } else {
                errors.add(new ErrorMessage(ErrorMessage.Severity.NOTE,
                            f.getName(),
                            "Following PID will be generated for this " +
                            "communication in Fedora: " + fedoraPID,
                            "All ok."));
            }
        }
        return errors;
    }

    public void doMain(String[] args) {
        settings = new ValidatorSettings("ComaPIDLengthChecker",
                "Checks Exmaralda .coma file for ID's that violate Fedora's " +
                "PID limits", "If input is a directory, performs recursive " +
                "check from that directory, otherwise checks input file");
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
        ComaPIDLengthChecker checker = new ComaPIDLengthChecker();
        checker.doMain(args);
    }

}
