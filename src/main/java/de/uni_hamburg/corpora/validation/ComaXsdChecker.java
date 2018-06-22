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
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
import de.uni_hamburg.corpora.utilities.TypeConverter;
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
public class ComaXsdChecker extends Checker implements CommandLineable, CorpusFunction {

    ValidatorSettings settings;
    final String COMA_XSD_CHECKER = "coma-xsd";

    /**
     * Validate a coma file with XML schema from internet.
     *
     * @return true, if file is passable (valid enough for HZSK),
     *         false otherwise.
     */
    public Report check(File f) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(f);
        } catch(SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error.");
        } catch(IOException ioe) {
            stats.addException(ioe, "Unknown reading error.");
        }
        return stats;
    }


    private Report exceptionalCheck(File f)
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

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("ComaXSDChecker",
                "Checks Exmaralda .coma file against XML Schema",
                "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking COMA files against schema...");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            stats = check(f);
        }
        return stats;
    }

    public static void main(String[] args) {
        ComaXsdChecker checker = new ComaXsdChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }
    
    /**
    * Default check function which calls the exceptionalCheck function so that the
    * primal functionality of the feature can be implemented, and additionally 
    * checks for exceptions.
    */   
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch(JexmaraldaException je) {
            stats.addException(je, "Unknown parsing error");
        } catch(JDOMException jdome) {
            stats.addException(jdome, "Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, "Reading/writing error");
        }
        return stats;
    }
    
    /**
    * Main functionality of the feature; validates a coma file with XML schema from internet.
    */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, JDOMException, IOException, JexmaraldaException{
        System.out.println("Checking COMA file against schema...");
        URL COMA_XSD = new URL("http://www.exmaralda.org/xml/comacorpus.xsd");
        Source xmlStream = new StreamSource(TypeConverter.String2InputStream(cd.toSaveableString()));
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(COMA_XSD);
        Validator validator = schema.newValidator();
        ComaErrorReportGenerator eh = new ComaErrorReportGenerator();
        validator.setErrorHandler(eh);
        validator.validate(xmlStream);
        return eh.getErrors();
    }
    
    /**
    * No fix is applicable for this feature.
    */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(COMA_XSD_CHECKER,
                "No fix is applicable for this feature.");
        return report;
    }
    
    /**
    * Default function which determines for what type of files (basic transcription, 
    * segmented transcription, coma etc.) this feature can be used.
    */
    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ComaXsdChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

}