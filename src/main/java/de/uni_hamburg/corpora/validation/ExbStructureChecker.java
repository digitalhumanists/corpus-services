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

import de.uni_hamburg.corpora.BasicTranscriptionData;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Collection;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;

/**
 * This class checks basic transcription files for structural anomalies.
 *
 */
public class ExbStructureChecker extends Checker implements CorpusFunction {

    BasicTranscription bt;
    File exbfile;
    ValidatorSettings settings;
    String filename;

    final String function = "exb-structure";

    public ExbStructureChecker() {
        //fixing is possible
        super(true);
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
            stats = function(cd, false);
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
    @Override
    public Report function(CorpusData cd, Boolean fix)
            throws SAXException, JDOMException, IOException, JexmaraldaException {
        Report stats = new Report();
        if (fix) {
            stats.addCritical(function,
                    "No fix is applicable for this feature yet.");
        } else {
            BasicTranscriptionData btd = (BasicTranscriptionData) cd;
            filename = cd.getFilename();
            bt = btd.getEXMARaLDAbt();

            String[] duplicateTranscriptionTiers
                    = bt.getDuplicateTranscriptionTiers();
            String[] orphanedTranscriptionTiers
                    = bt.getOrphanedTranscriptionTiers();
            String[] orphanedAnnotationTiers = bt.getOrphanedAnnotationTiers();
            String[] temporalAnomalies
                    = bt.getBody().getCommonTimeline().getInconsistencies();
            Hashtable<String, String[]> annotationMismatches
                    = bt.getAnnotationMismatches();
            if (duplicateTranscriptionTiers.length == 0 && orphanedTranscriptionTiers.length == 0 && orphanedAnnotationTiers.length == 0 && temporalAnomalies.length == 0) {
                stats.addCorrect(function, cd, "No structure errors found.");
            } else {
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
            }
        }
        return stats;
    }

    /**
     * No fix is applicable for this feature.
     */
    @Override
    public Report fix(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = function(cd, true);
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

    /**
     * Default function which returns a two/three line description of what this
     * class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class checks basic transcription files for structural anomalies. ";
        return description;
    }

    @Override
    public Report check(Corpus c) {
        Report stats = new Report();
        for (CorpusData cdata : c.getBasicTranscriptionData()) {
            try {
                stats.merge(function(cdata, false));
            } catch (JexmaraldaException je) {
                stats.addException(je, function, cdata, "Unknown parsing error");
            } catch (SAXException saxe) {
                stats.addException(saxe, function, cdata, "Unknown parsing error");
            } catch (JDOMException ex) {
                stats.addException(ex, function, cdata, "Unknown JDOM error");
            } catch (IOException ex) {
                stats.addException(ex, function, cdata, "Unknown IO error");
            }
        }
        return stats;
    }

}
