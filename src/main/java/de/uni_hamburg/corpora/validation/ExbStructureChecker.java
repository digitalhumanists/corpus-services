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

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.commons.cli.Option;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.partiture.transcriptionActions.GetSegmentationErrorsAction;
import org.jdom.JDOMException;

/**
 * This class checks basic transcription files for structural anomalies. 
 * 
 */
public class ExbStructureChecker extends Checker implements CorpusFunction {

    String exbName;
    String filename;
    BasicTranscription bt;
    File exbfile;
    ValidatorSettings settings;

    final String function = "exb-structure";

    public ExbStructureChecker() {
        super("exb-structure");
    }

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
            stats.addException(je, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
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
            stats.addCritical(function, exbName + ": "
                    + "More than one transcription tier for one "
                    + "speaker. Tier: " + tierID, "Open in PartiturEditor, "
                    + "change tier type or merge tiers.");
        }
        for (String tliID : temporalAnomalies) {
            stats.addCritical(function, exbName + ": "
                    + "Temporal anomaly at timeline item: " + tliID);
        }
        for (String tierID : orphanedTranscriptionTiers) {
            stats.addCritical(function, exbName + ": "
                    + "Orphaned transcription tier:" + tierID);
        }
        for (String tierID : orphanedAnnotationTiers) {
            stats.addCritical(function, exbName + ": "
                    + "Orphaned annotation tier:" + tierID);
        }
        for (String tierID : annotationMismatches.keySet()) {
            String[] eventIDs = annotationMismatches.get(tierID);
            for (String eventID : eventIDs) {
                stats.addCritical(function, exbName + ": "
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
     * additionally checks for exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            exbName = cd.getFilename();
            stats = exceptionalCheck(cd);
        } catch (JexmaraldaException je) {
            stats.addException(je, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (JDOMException ex) {
            stats.addException(ex, function, cd, "Unknown JDOM error");
        } catch (IOException ex) {
            stats.addException(ex, function, cd, "Unknown IO error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; checks basic transcription files for
     * structural anomalies.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, JDOMException, IOException, JexmaraldaException {
        filename = cd.getURL().getFile();
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
            stats.addCritical(function, cd,
                    "More than one transcription tier for one "
                    + "speaker. Tier: " + tierID + "Open in PartiturEditor, "
                    + "change tier type or merge tiers.");
            exmaError.addError(function, filename, tierID, "", false,
                    "More than one transcription tier for one speaker. Tier: "
                    + tierID + ". Change tier type or merge tiers.");
        }
        for (String tliID : temporalAnomalies) {
            stats.addCritical(function, cd,
                    "Temporal anomaly at timeline item: " + tliID);
            exmaError.addError(function, filename, "", "", false,
                    "Temporal anomaly at timeline item: " + tliID);
        }
        for (String tierID : orphanedTranscriptionTiers) {
            stats.addCritical(function, cd,
                    "Orphaned transcription tier:" + tierID);
            exmaError.addError(function, filename, tierID, "", false,
                    "Orphaned transcription tier:" + tierID);
        }
        for (String tierID : orphanedAnnotationTiers) {
            stats.addCritical(function, cd, 
                    "Orphaned annotation tier:" + tierID);
            exmaError.addError(function, filename, tierID, "", false,
                    "Orphaned annotation tier:" + tierID);
        }
        for (String tierID : annotationMismatches.keySet()) {
            String[] eventIDs = annotationMismatches.get(tierID);
            for (String eventID : eventIDs) {
                stats.addCritical(function, cd,
                        "Annotation mismatch: tier " + tierID
                        + " event " + eventID);
                exmaError.addError(function, filename, tierID, eventID, false,
                        "Annotation mismatch: tier " + tierID
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
        report.addCritical(function,
                "No fix is applicable for this feature yet.");
        return report;
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, " usable class not found");
        }
        return IsUsableFor;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class checks basic transcription files for structural anomalies. ";
        return description;
    }

    @Override
    public Report check(Corpus c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report function(CorpusData cd, Boolean fix) throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
