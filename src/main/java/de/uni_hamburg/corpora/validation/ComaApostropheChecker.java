package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 * A class that checks whether or not the coma file contains an apostrophe ’. If
 * it does then these all apostrophes ’ are changed to apostrophes '.
 */
public class ComaApostropheChecker extends Checker implements CorpusFunction {

    String comaLoc = "";
    String comaFile = "";
    boolean apostrophe = false;

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, comaLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, comaLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, comaLoc + ": Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, comaLoc + ": Unknown file reading error");
        }
        return stats;
    }

    /**
     * One of the main functionalities of the feature; issues warnings if the
     * coma file contains apostrophe ’and add that warning to the report which
     * it returns.
     */
    private Report exceptionalCheck(CorpusData cd) // check whether there's any illegal apostrophes '
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        Report stats = new Report();         // create a new report
        comaFile = cd.toSaveableString();     // read the coma file as a string
        if (comaFile.contains("'")) {          // if coma file contains an apostrophe ' then issue warning
            apostrophe = true;
            System.err.println("Coma file is containing apostrophe(s) ’");
            stats.addWarning("coma-apostrophe-checker", "Coma file " + cd.getURL().getFile() + " is containing apostrophe(s) ’");
        }
        else {
            stats.addCorrect("coma-apostrophe-checker", "Coma file " + cd.getURL().getFile() + " does not contain apostrophes");
        }
        return stats; // return the report with warnings
    }

    @Override
    /**
     * One of the main functionalities of the feature; fix apostrophes ' with
     * apostrophes ´ add them to the report which it returns in the end.
     */
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
         Report stats = new Report();         // create a new report
        comaFile = cd.toSaveableString();     // read the coma file as a string
        if (comaFile.contains("'")) {         // if coma file contains an apostrophe ’ then issue warning
            apostrophe = true;                // flag points out if there are illegal apostrophes
            comaFile = comaFile.replaceAll("'", "’");    //replace all 's with ´s
            CorpusIO cio = new CorpusIO();
            cio.write(comaFile, cd.getURL());    // write back to coma file with allowed apostrophes ´ 
            stats.addCorrect("ComaApostropheChecker", "corrected the apostrophes in " + cd.getURL().getFile()); // fix report 
        } else {
            stats.addCorrect("coma-apostrophe-checker", "Coma file " + cd.getURL().getFile() + " does not contain apostrophes");
        }
        return stats;
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ComaApostropheChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

    /**
     * Execute function for calling check and fix functions if necessary.
     */
    public Report execute(CorpusData cd, boolean fix) {
        Report report = new Report();
        try {

            if (fix) {
                report.merge(fix(cd));
            } else {
                report.merge(check(cd));
            }
        } catch (SAXException ex) {
            Logger.getLogger(ComaApostropheChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(ComaApostropheChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ComaApostropheChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JexmaraldaException ex) {
            Logger.getLogger(ComaApostropheChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return report;
    }
}
