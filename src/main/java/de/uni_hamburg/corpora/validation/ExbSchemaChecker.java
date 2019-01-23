/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for exb errors for HZSK repository purposes.
 *
 * @author Ozan Ozdemir <ozan.oezdemir@studium.uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;
import org.apache.commons.cli.Option;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;

/**
 * A class that can load basic transcription data and check for potential problems with HZSK
 * repository depositing.
 */

public class ExbSchemaChecker extends Checker implements CommandLineable, CorpusFunction {

    final String EXB_DTD_CHECKER = "exb-dtd";

    /**
     * Validate an exb file with a DTD file.
     */
    
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
    * Main functionality of the feature; validates an exb file with a DTD file.
    */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, JDOMException, IOException, JexmaraldaException{
        System.out.println("Checking the exb file against DTD...");
        //URL exb_dtd = new URL("C:\\Users\\Ozzy\\Desktop\\exb_schema.xsd");
        String exbSchemaPath = new File("src\\test\\java\\de\\uni_hamburg\\corpora\\resources\\schemas\\exb_schema.xsd").getAbsolutePath();
        File exbSchema = new File(exbSchemaPath);
        Source xmlStream = new StreamSource(TypeConverter.String2InputStream(cd.toSaveableString()));
        SchemaFactory schemaFactory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(exbSchema);
        Validator validator = schema.newValidator();
        ErrorHandler eh = null;
        //ComaErrorReportGenerator eh = new ComaErrorReportGenerator();
        //validator.setErrorHandler(eh);
        validator.setErrorHandler(eh);
        validator.validate(xmlStream);

        Report stats = new Report();
        //stats.getErrorReport(validator.toString());
        return stats;
    }
    
    /**
    * No fix is applicable for this feature.
    */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(EXB_DTD_CHECKER,
                "No fix is applicable for this feature.");
        return report;
    }
    
    /**
    * Default function which determines for what type of files (basic transcription, 
    * segmented transcription, coma etc.) this feature can be used.
    */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ComaXsdChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

}

