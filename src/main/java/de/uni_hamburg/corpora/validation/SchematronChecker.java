/**
 * @file SchematronChecker.java
 *
 * Checker for applying a Schematron validation to XML file.
 *
 * @author Daniel Jettka <daniel.jettka@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;


import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.cli.Option;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.xml.sax.SAXException;


/**
 * A class that can load an XML file and check for potential problems with rules defined in Schematron schema.
 */
public class SchematronChecker implements CommandLineable {

    ValidatorSettings settings;
    static final private String schematronPath = "/schematron/sample.sch";

    
    /**
     * Validate an XML file against a Sshematron schema.
     *
     * @return true, if file is passable,
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
        
        Report r = new Report();
        
        ClassLoader classLoader = getClass().getClassLoader();
        File schematronFile = new File(classLoader.getResource(schematronPath).getFile());

        final ISchematronResource aResSCH = SchematronResourceSCH.fromFile (schematronFile);
        if (!aResSCH.isValidSchematron ())            
            r.addCritical("", new IllegalArgumentException ("Invalid Schematron!"), "Schematron validation not executed!");
        
        try {
            SchematronOutputType sot = aResSCH.applySchematronValidationToSVRL (new StreamSource(f));
            
            List<String> schNoteList = sot.getText();
            for (int i = 0; i < schNoteList.size(); i++) {
                r.addNote("schematron", schNoteList.get(i)); 
            }
                   
        } catch (Exception ex) {
            r.addCritical("", ex, "Schematron validation not executed!");            
        }
        
        return r;
        
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("SchematronChecker",
                "Checks XML file against Schematron schema",
                "");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking file against schematron schema...");
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
        SchematronChecker checker = new SchematronChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }

}
