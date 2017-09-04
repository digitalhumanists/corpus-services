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

import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;

/**
 * A command-line tool for checking EXB files.
 */
public class ExbStructureChecker implements CommandLineable {

    String filename;
    BasicTranscription bt;
    File exbfile;
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
        } catch(JexmaraldaException je) {
            errors = new ArrayList<ErrorMessage>();
            errors.add(new ErrorMessage(ErrorMessage.Severity.CRITICAL,
                    exbfile.getName(),
                    "Parsing error", "Unknown"));
        } catch(SAXException saxe) {
            errors = new ArrayList<ErrorMessage>();
            errors.add(new ErrorMessage(ErrorMessage.Severity.CRITICAL,
                    exbfile.getName(),
                    "Parsing error", "Unknown"));
        }
        return errors;
    }

    public Collection<ErrorMessage>
            exceptionalCheck(File f) throws SAXException, JexmaraldaException {
        filename = f.getAbsolutePath();
        bt = new BasicTranscription(filename);
        List<ErrorMessage> errors = new
            ArrayList<ErrorMessage>();
        String[] duplicateTranscriptionTiers =
            bt.getDuplicateTranscriptionTiers();
        String[] orphanedTranscriptionTiers =
            bt.getOrphanedTranscriptionTiers();
        String[] orphanedAnnotationTiers = bt.getOrphanedAnnotationTiers();
        String[] temporalAnomalies =
            bt.getBody().getCommonTimeline().getInconsistencies();
        Hashtable<String, String[]> annotationMismatches =
            bt.getAnnotationMismatches();

        for (String tierID : duplicateTranscriptionTiers) {
            errors.add(new
                    ErrorMessage(ErrorMessage.Severity.CRITICAL,
                        filename, "More than one transcription tier for one " +
                        "speaker. Tier: " + tierID, "Open in PartiturEditor, " +
                        "change tier type or merge tiers."));
        }
        for (String tliID : temporalAnomalies) {
            errors.add(new
                    ErrorMessage(ErrorMessage.Severity.CRITICAL,
                        filename, "Temporal anomaly at timeline item: " + tliID,
                        "Open the file in PartiturEditor, " +
                        "go to menu ??? to get more detailed report"));
        }
        for (String tierID : orphanedTranscriptionTiers) {
            errors.add(new
                    ErrorMessage(ErrorMessage.Severity.CRITICAL,
                        filename, "Orphaned transcription tier:" + tierID,
                        "Open the file in PartiturEditor, " +
                        "go to menu ??? to get more detailed report"));
        }
        for (String tierID : orphanedAnnotationTiers) {
            errors.add(new
                    ErrorMessage(ErrorMessage.Severity.CRITICAL,
                        filename, "Orphaned annotation tier:" + tierID,
                        "Open the file in PartiturEditor, " +
                        "go to menu ??? to get more detailed report"));
        }
        for (String tierID : annotationMismatches.keySet()) {
            String[] eventIDs = annotationMismatches.get(tierID);
            for (String eventID : eventIDs) {
                errors.add(new
                        ErrorMessage(ErrorMessage.Severity.CRITICAL,
                            filename, "Annotation mismatch: tier " + tierID +
                            " event " + eventID, "Open the file " +
                            "in PartiturEditor, go to menu ??? " +
                            "to get more detailed report"));
            }
        }
        return errors;
    }

    public void doMain(String[] args) {
        settings = new ValidatorSettings("ExbStructureChecker",
                "Checks Exmaralda .exb file for segmentation problems",
                "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking EXB files for segmentation " +
                    "problems...");
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
        ExbStructureChecker checker = new ExbStructureChecker();
        checker.doMain(args);
    }


}
