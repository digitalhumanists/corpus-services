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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringBufferInputStream;
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

import de.uni_hamburg.corpora.utilities.TypeConverter;

/**
 * A class that can load coma data and check for potential problems with HZSK
 * repository depositing.
 */
public class ComaPIDLengthChecker implements CommandLineable, StringChecker {

    ValidatorSettings settings;
    final String COMA_PID_LENGTH = "coma-pid-length";
    String comaLoc = "";

    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    public StatisticsReport check(String data) {
        StatisticsReport stats = new StatisticsReport();
        try {
            stats = exceptionalCheck(data);
        } catch(ParserConfigurationException pce) {
            stats.addException(pce, comaLoc + ": Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, comaLoc + ": Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, comaLoc + ": File reading error");
        }
        return stats;
    }


    private StatisticsReport exceptionalCheck(String data)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new StringBufferInputStream(data));
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
        StatisticsReport stats = new StatisticsReport();
        if (corpusPrefix.equals("")) {
            stats.addWarning(COMA_PID_LENGTH + "-config", comaLoc + ": " +
                        "Missing <Key name='HZSK:corpusprefix'>.",
                        "PID length cannot be estimated accurately. " +
                        "Add that key in coma.");
            corpusPrefix = "muster";
        } else {
            stats.addCorrect(COMA_PID_LENGTH + "-config", comaLoc + ": " +
                    "HZSK corpus prefix OK: " + corpusPrefix);
        }
        if (corpusVersion.equals("")) {
            stats.addWarning(COMA_PID_LENGTH + "-config", comaLoc + ": " +
                        "Missing <Key name='HZSK:corpusversion'>.",
                        "PID length cannot be estimated accurately. " +
                        "Add that key in coma.");
            corpusVersion = "0.0";
        } else {
            stats.addCorrect(COMA_PID_LENGTH + "-config", comaLoc + ": " +
                    "HZSK corpus version OK: " + corpusVersion);
        }
        NodeList communications = doc.getElementsByTagName("Communication");
        for (int i = 0; i < communications.getLength(); i++) {
            Element communication = (Element)communications.item(i);
            String communicationName = communication.getAttribute("Name");
            String fedoraPID = new String("communication: " + corpusPrefix +
                    "-" + corpusVersion +
                    "_" + communicationName);
            if (fedoraPID.length() >= 64) {
                stats.addCritical(COMA_PID_LENGTH, comaLoc + ": " +
                            "Communication is too long for Fedora PID" +
                            "generation: " + fedoraPID,
                            "It must be shortened, e.g. use: " +
                            communicationName.substring(0, 40) + ", or change " +
                            "the corpus prefix");
            } else {
                stats.addCorrect(COMA_PID_LENGTH, comaLoc + ": " +
                            "Following PID will be generated for this " +
                            "communication in Fedora: " + fedoraPID);
            }
        }
        return stats;
    }

    public StatisticsReport doMain(String[] args) {
        settings = new ValidatorSettings("ComaPIDLengthChecker",
                "Checks Exmaralda .coma file for ID's that violate Fedora's " +
                "PID limits", "If input is a directory, performs recursive " +
                "check from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking COMA files for references...");
        }
        StatisticsReport stats = new StatisticsReport();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            try {
                comaLoc = f.getName();
                String s = TypeConverter.InputStream2String(new FileInputStream(f));
                stats = check(s);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return stats;
    }

    public static void main(String[] args) {
        ComaPIDLengthChecker checker = new ComaPIDLengthChecker();
        StatisticsReport stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }

}
