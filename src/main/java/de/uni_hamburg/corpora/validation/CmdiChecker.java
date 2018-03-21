/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for cmdi errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;


import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
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
 * A class that can load cmdi data and check for potential problems with HZSK
 * repository depositing.
 */
public class CmdiChecker implements CommandLineable, StringChecker {

    ValidatorSettings settings;
    final String CMDI_MISC = "cmdi-misc";
    String cmdiLoc = "";

    /**
     * Check for existence of files in a cmdi file.
     *
     * @return true, if all files were found, false otherwise
     */
    public Report check(String data) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(data);
        } catch(ParserConfigurationException pce) {
            stats.addException(pce, cmdiLoc + ": Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, cmdiLoc + ": Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, cmdiLoc + ": Unknown file reading error");
        }
        return stats;
    }

    private boolean isUrlHandleOrHzsk(String url) {
        if ((url.startsWith("http://hdl.handle.net/11022/")) ||
                (url.startsWith("https://corpora.uni-hamburg.de/repository/"))) {
            return true;
        } else {
            return false;
        }
    }

    private Report exceptionalCheck(String data)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(data));
        NodeList rps = doc.getElementsByTagName("ResourceProxy");
        Report stats = new Report();
        boolean hasLandingPage = false;
        for (int i = 0; i < rps.getLength(); i++) {
            Element rpe = (Element)rps.item(i);
            NodeList restypes = rpe.getElementsByTagName("ResourceType");
            Element restype = (Element)restypes.item(0);
            if (restype.getTextContent().equals("LandingPage")) {
                hasLandingPage = true;
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                    "Good resource type LandingPage");
            } else if (restype.getTextContent().equals("Resource")) {
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                    "Good resource type Resource");
            } else {
                stats.addWarning(CMDI_MISC, cmdiLoc + ": " +
                        "Unrecognised resource type " +
                        restype.getTextContent());
            }
            NodeList resrefs = rpe.getElementsByTagName("ResourceRef");
            Element resref = (Element)resrefs.item(0);
            String url = resref.getTextContent();
            if (!isUrlHandleOrHzsk(url)) {
                stats.addCritical(CMDI_MISC, cmdiLoc + ": " +
                        "Invalid URL for reesource proxy:" +
                        url,
                        "URLs should start with http://hdl.handle.net... or " +
                        "https://corpora.uni-hamburg.de/repository/...");
            } else {
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                    "Good resource proxy URL " + url);
            }
        }
        if (!hasLandingPage) {
            stats.addCritical(CMDI_MISC, cmdiLoc + ": " +
                    "Missing landing page");
        } else {
            stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                    "Good landing page found");
        }
        NodeList gis = doc.getElementsByTagName("GeneralInfo");
        for (int i = 0; i < gis.getLength(); i++) {
            Node ginode = gis.item(i);
            if (ginode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element gi = (Element)ginode;
            NodeList childs = gi.getChildNodes();
            boolean englishTitle = false;
            boolean englishDesc = false;
            boolean legalOwner = false;
            boolean pidFound = false;
            for (int j = 0; j < childs.getLength(); j++) {
                Node n = childs.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element e = (Element)n;
                if (e.getTagName().equals("PID")) {
                    if (!isUrlHandleOrHzsk(e.getTextContent())) {
                        stats.addCritical(CMDI_MISC, cmdiLoc + ": " +
                            "Invalid URL for PID:" +
                            e.getTextContent(),
                            "URLs should start with "+
                            "http://hdl.handle.net... or " +
                            "https://corpora.uni-hamburg.de/repository/...");
                    } else {
                        stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                                "Good PID URL: " +
                                e.getTextContent());
                    }
                    pidFound = true;
                } else if (e.getTagName().equals("Description")) {
                    if (e.getAttribute("xml:lang").equals("en")) {
                        englishDesc = true;
                        stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                                "English Description present");
                    }
                } else if (e.getTagName().equals("Title")) {
                    if (e.getAttribute("xml:lang").equals("en")) {
                        englishTitle = true;
                        stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                                "English title present");
                    }
                } else if (e.getTagName().equals("LegalOwner")) {
                    legalOwner = true;
                    stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                            "LegalOwner present");
                }
            }
            if (!englishTitle) {
                stats.addWarning(CMDI_MISC, cmdiLoc + ": " +
                        "English title missing from General Info " +
                        "(needed by FCS for example)");
            }
            if (!englishDesc) {
                stats.addWarning(CMDI_MISC, cmdiLoc + ": " +
                        "English Description missing from General Info " +
                        "(needed by FCS for example)");
            }
            if (!pidFound) {
                stats.addCritical(CMDI_MISC, cmdiLoc + ": " +
                        "PID missing");
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("CmdiChecker",
                "Checks CLARIN .cmdi file for various common practices ",
                "If input is a directory, performs recursive " +
                "check from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking CMDI files for metadata...");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            try {
                cmdiLoc = f.getName();
                String s = TypeConverter.InputStream2String(new FileInputStream(f));
                stats = check(s);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return stats;
    }

    public static void main(String[] args) {
        CmdiChecker checker = new CmdiChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }

}
