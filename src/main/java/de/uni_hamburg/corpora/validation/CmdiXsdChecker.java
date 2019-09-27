/**
 * @file CmdiErrorChecker.java
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;



import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
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
import org.exmaralda.partitureditor.jexmaralda.BasicBody;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.exmaralda.partitureditor.jexmaralda.TierFormatTable;
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
public class CmdiXsdChecker {

    ValidatorSettings settings;


    /**
     * Validate a coma file with XML schema from internet.
     *
     * @return true, if file is passable (valid enough for HZSK),
     *         false otherwise.
     */
    public Report check(String data) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(data);
        } catch(SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error.");
        } catch(IOException ioe) {
            stats.addException(ioe, "Unknown reading error.");
        }
        return stats;
    }


    private Report exceptionalCheck(String data)
            throws SAXException, IOException {
        // peek the profile first
        Pattern xsdpattern = Pattern.compile("xsi2:schemaLocation\\s*=\\s*" +
                "[\"']\\s*http://www.clarin.eu/cmd/?\\s\\s*([^\"']*)",
                Pattern.MULTILINE);
        Matcher xsdmatch = xsdpattern.matcher(data);
        String cmdiProfileXsdURL;
        if (!xsdmatch.find()) {
            Report stats = new Report();
            stats.addCritical("cmdi-xsd", "No CMDI XML schema found " +
                    "(should contain " +
                    "xsi2:schemalocation=\"http://www.clarin.eu/cmd )");
            return stats;
        } else {
            cmdiProfileXsdURL = xsdmatch.group(1);
        }
        URL cmdiXsdURL = new URL(cmdiProfileXsdURL);
        Source xmlStream = TypeConverter.String2StreamSource(data);
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(cmdiXsdURL);
        Validator validator = schema.newValidator();
        CmdiXsdErrorReportGenerator eh = new CmdiXsdErrorReportGenerator();
        validator.setErrorHandler(eh);
        validator.validate(xmlStream);
        return eh.getErrors();
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("CmdiXsdChecker",
                "Checks CMDI file against XML Schema",
                "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking CMDI files against schema...");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            try {
                String s = TypeConverter.InputStream2String(new FileInputStream(f));
                stats = check(s);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        }
        return stats;
    }

    public static void main(String[] args) {
        CmdiXsdChecker checker = new CmdiXsdChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }

}
