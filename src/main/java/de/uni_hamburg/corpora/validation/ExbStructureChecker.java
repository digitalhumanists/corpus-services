/**
 * @file ExbErrorChecker.java
 *
 * A command-line tool / non-graphical interface for checking errors in
 * exmaralda's EXB files.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.ExmaErrorList;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import org.apache.commons.cli.Option;
import org.xml.sax.SAXException;

import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;

/**
 * A command-line tool for checking EXB files.
 */
public class ExbStructureChecker extends Checker implements CommandLineable, CorpusFunction {

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
        } catch (JexmaraldaException je) {
            stats.addException("exb-parse", je, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException("exb-parse", saxe, "Unknown parsing error");
        }
        return stats;
    }

    public Report
            exceptionalCheck(File f) throws SAXException, JexmaraldaException {
        filename = f.getAbsolutePath();
        bt = new BasicTranscription(filename);
        Report stats = new Report();
        String[] duplicateTranscriptionTiers
                = bt.getDuplicateTranscriptionTiers();
        String[] orphanedTranscriptionTiers
                = bt.getOrphanedTranscriptionTiers();
        String[] orphanedAnnotationTiers = bt.getOrphanedAnnotationTiers();
        String[] temporalAnomalies
                = bt.getBody().getCommonTimeline().getInconsistencies();
        Hashtable<String, String[]> annotationMismatches
                = bt.getAnnotationMismatches();

        for (String tierID : duplicateTranscriptionTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "More than one transcription tier for one "
                    + "speaker. Tier: " + tierID, "Open in PartiturEditor, "
                    + "change tier type or merge tiers.");
        }
        for (String tliID : temporalAnomalies) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "Temporal anomaly at timeline item: " + tliID);
        }
        for (String tierID : orphanedTranscriptionTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "Orphaned transcription tier:" + tierID);
        }
        for (String tierID : orphanedAnnotationTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "Orphaned annotation tier:" + tierID);
        }
        for (String tierID : annotationMismatches.keySet()) {
            String[] eventIDs = annotationMismatches.get(tierID);
            for (String eventID : eventIDs) {
                stats.addCritical(EXB_STRUCTURE, exbName + ": "
                        + "Annotation mismatch: tier " + tierID
                        + " event " + eventID);
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("ExbStructureChecker",
                "Checks Exmaralda .exb file for segmentation problems",
                "If input is a directory, performs recursive check "
                + "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking EXB files for segmentation "
                    + "problems...");
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

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            exbName = cd.getURL().toString().substring(cd.getURL().toString().lastIndexOf("/") + 1);
            stats = exceptionalCheck(cd);
        } catch (IOException ioe) {
            stats.addException(ioe, "Reading error");
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        }
        return stats;
    }

    /**
     * Main feature of the class: Checks Exmaralda .exb file for segmentation
     * problems.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException {
        File f = new File(cd.getURL().toString());
        filename = f.getPath();
        if(filename.contains("file:\\")){
            filename = filename.substring(filename.indexOf("file:\\")+6);
        }
        bt = new BasicTranscription(filename);
        Report stats = new Report();
        String[] duplicateTranscriptionTiers
                = bt.getDuplicateTranscriptionTiers();
        String[] orphanedTranscriptionTiers
                = bt.getOrphanedTranscriptionTiers();
        String[] orphanedAnnotationTiers = bt.getOrphanedAnnotationTiers();
        String[] temporalAnomalies
                = bt.getBody().getCommonTimeline().getInconsistencies();
        Hashtable<String, String[]> annotationMismatches
                = bt.getAnnotationMismatches();

        for (String tierID : duplicateTranscriptionTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "More than one transcription tier for one "
                    + "speaker. Tier: " + tierID, "Open in PartiturEditor, "
                    + "change tier type or merge tiers.");
            exmaError.addError(EXB_STRUCTURE, cd.getURL().getFile(), tierID, "", false, "Error: More than one transcription tier for one speaker.");
        }
        for (String tliID : temporalAnomalies) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "Temporal anomaly at timeline item: " + tliID);
            exmaError.addError(EXB_STRUCTURE, cd.getURL().getFile(), "", tliID, false, "Error: Temporal anomaly at timeline item.");
        }
        for (String tierID : orphanedTranscriptionTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "Orphaned transcription tier:" + tierID);
            exmaError.addError(EXB_STRUCTURE, cd.getURL().getFile(), tierID, "", false, "Error: Orphaned transcription tier.");
        }
        for (String tierID : orphanedAnnotationTiers) {
            stats.addCritical(EXB_STRUCTURE, exbName + ": "
                    + "Orphaned annotation tier:" + tierID);
            exmaError.addError(EXB_STRUCTURE, cd.getURL().getFile(), tierID, "", false, "Error: Orphaned annotation tier.");
        }
        for (String tierID : annotationMismatches.keySet()) {
            String[] eventIDs = annotationMismatches.get(tierID);
            for (String eventID : eventIDs) {
                stats.addCritical(EXB_STRUCTURE, exbName + ": "
                        + "Annotation mismatch: tier " + tierID
                        + " event " + eventID);
                exmaError.addError(EXB_STRUCTURE, cd.getURL().getFile(), tierID, eventID, false, "Error: Annotation mismatch: tier " + tierID
                        + " event " + eventID);
            }
        }
        return stats;
    }

    /**
    * No fix is applicable for this feature.
    */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(EXB_STRUCTURE,
                "Automatic fix is not yet supported.");
        return report;
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ExbStructureChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

}
