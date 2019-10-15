package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.ComaData;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.coma.root.Coma;
import org.xml.sax.SAXException;


/**
 *
 * @author Ozzy
 */
public class ComaUpdateSegmentCounts extends Checker implements CorpusFunction {

    static String filename;
    static Coma coma;
    static ComaData comaData;
    static File comaFile;
    static ValidatorSettings settings;
    final String COMA_UP_SEG = "coma-update-segment-counts";
    String segmentationName = "GENERIC";
    String path2ExternalFSM = "";

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) {
        report.addCritical(COMA_UP_SEG, cd.getURL().getFile(), "Checking option is not available");
        return report;
    }

    /**
     * Main feature of the class: takes a coma file, updates the info using the
     * linked exbs and saves the coma file afterwards without changing exbs;
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, IOException {
        Report stats = new Report();
        try {
            comaData = (ComaData) cd;
            coma = comaData.getEXMARaLDAComa();
            coma.refreshTranscriptionStats();
            comaFile = new File(cd.getURL().getPath());
            comaData.setJdom(coma.getComaDocument(comaFile));
            //comaData.setOriginalString(coma.);
            cd = (CorpusData) comaData;
            CorpusIO cio = new CorpusIO();
            cio.write(cd, cd.getURL());
            if (cd != null) {
                report.addCorrect(COMA_UP_SEG, cd, "Updated the segment counts!");
            } else {
                report.addCritical(COMA_UP_SEG, cd, "Updating the segment counts was not possible!");
            }
        } catch (IOException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown IO exception");
        } catch (TransformerException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown xml exception");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown xml exception");
        } catch (SAXException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown xml exception");
        } catch (XPathExpressionException ex) {
            report.addException(ex, COMA_UP_SEG, cd, "unknown xml exception");
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
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ExbSegmenter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

    /**
     * Default function which returns a two/three line description of what this
     * class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class takes a coma file, updates the info using"
                + " the linked exbs and saves the coma file afterwards without changing"
                + " exbs.";
        return description;
    }

}
