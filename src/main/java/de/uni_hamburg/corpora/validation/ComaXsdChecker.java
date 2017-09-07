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
public class ComaXsdChecker implements CommandLineable {

    ValidatorSettings settings;


    /**
     * Validate a coma file with XML schema from internet.
     *
     * @return true, if file is passable (valid enough for HZSK),
     *         false otherwise.
     */
    public Collection<ErrorMessage> check(File f) {
        Collection<ErrorMessage> errors;
        try {
            errors = exceptionalCheck(f);
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
            throws SAXException, IOException {
        URL COMA_XSD = new URL("http://www.exmaralda.org/xml/comacorpus.xsd");
        Source xmlStream = new StreamSource(f);
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(COMA_XSD);
        Validator validator = schema.newValidator();
        ComaErrorReportGenerator eh = new ComaErrorReportGenerator();
        validator.setErrorHandler(eh);
        validator.validate(xmlStream);
        return eh.getErrors();
    }

    public void doMain(String[] args) {
        settings = new ValidatorSettings("ComaXSDChecker",
                "Checks Exmaralda .coma file against XML Schema",
                "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking COMA files against schema...");
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
        ComaXsdChecker checker = new ComaXsdChecker();
        checker.doMain(args);
    }

}
