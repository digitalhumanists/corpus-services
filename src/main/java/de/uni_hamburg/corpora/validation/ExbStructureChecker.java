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

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
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

    String exbName;
    String filename;
    BasicTranscription bt;
    File exbfile;
    ValidatorSettings settings;

    final String EXB_STRUCTURE = "exb-structure";

    /**
     * Check for structural errors.
     *
     * @see GetSegmentationErrorsAction
     */
    public Report check(File f) {
        Report stats = new Report();
        try {
            exbName = f.getName();
            stats = exceptionalCheck(f);
        } catch(JexmaraldaException je) {
            stats.addException("exb-parse", je, "Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException("exb-parse", saxe, "Unknown parsing error");
        }
        return stats;
    }

    public Report
            exceptionalCheck(File f) throws SAXException, JexmaraldaException {
        filename = f.getAbsolutePath();
        bt = new BasicTranscription(filename);
        Report stats = new Report();
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
            stats.addCritical(EXB_STRUCTURE, exbName + ": " +
                    "More than one transcription tier for one " +
                        "speaker. Tier: " + tierID, "Open in PartiturEditor, " +
                        "change tier type or merge tiers.");
        }
        for (String tliID : temporalAnomalies) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": " +
                    "Temporal anomaly at timeline item: " + tliID);
        }
        for (String tierID : orphanedTranscriptionTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": " +
                    "Orphaned transcription tier:" + tierID);
        }
        for (String tierID : orphanedAnnotationTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": " +
                    "Orphaned annotation tier:" + tierID);
        }
        for (String tierID : annotationMismatches.keySet()) {
            String[] eventIDs = annotationMismatches.get(tierID);
            for (String eventID : eventIDs) {
                stats.addCritical(EXB_STRUCTURE, exbName + ": " +
                            "Annotation mismatch: tier " + tierID +
                            " event " + eventID);
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("ExbStructureChecker",
                "Checks Exmaralda .exb file for segmentation problems",
                "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking EXB files for segmentation " +
                    "problems...");
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
        ExbStructureChecker checker = new ExbStructureChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }


}
