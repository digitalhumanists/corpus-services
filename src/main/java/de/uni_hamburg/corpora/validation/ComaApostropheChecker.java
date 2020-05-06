package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * A class that checks whether or not the coma file contains an apostrophe '. If
 * it does then these all apostrophes ' are changed to apostrophes ’.
 */
public class ComaApostropheChecker extends Checker implements CorpusFunction {

    String comaLoc = "";
    String comaFile = "";
    boolean apostrophe = false;

    public ComaApostropheChecker() {
        super("ComaApostropheChecker");
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = function(cd, false);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, function, cd, "Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown transformer error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown Xpath error");
        }
        return stats;
    }

    /**
     * One of the main functionalities of the feature; issues warnings if the
     * coma file contains apostrophe ’and add that warning to the report which
     * it returns.
     */
    @Override
    public Report function(CorpusData cd, Boolean fix) // check whether there's any illegal apostrophes '
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException {
        Report stats = new Report();         // create a new report
        comaFile = cd.toSaveableString();     // read the coma file as a string
        if (comaFile.contains("'")) {          // if coma file contains an apostrophe ' then issue warning
            apostrophe = true;
            if (fix) {
                comaFile = comaFile.replaceAll("'", "’");    //replace all 's with ´s
                CorpusIO cio = new CorpusIO();
                cd.updateUnformattedString(comaFile);
                cio.write(cd, cd.getURL());    // write back to coma file with allowed apostrophes ´
                stats.addFix(function, cd, "Corrected the apostrophes"); // fix report
            } else {
                System.err.println("Coma file is containing apostrophe(s) ’");
                stats.addWarning(function, cd, "Coma file is containing apostrophe(s) ’");
            }
        } else {
            stats.addCorrect(function, cd, "Coma file does not contain apostrophes");
        }
        return stats; // return the report with warnings
    }

    @Override
    /**
     * One of the main functionalities of the feature; fix apostrophes ' with
     * apostrophes ´ add them to the report which it returns in the end.
     */
    public Report fix(CorpusData cd) {
        Report stats = new Report();         // create a new report
        try {
            stats = function(cd, true);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown transformer error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown Xpath error");
        } catch (URISyntaxException ex) {
             stats.addException(ex, function, cd, "Unknown URI error");
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
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    /**
     * Default function which returns a two/three line description of what this
     * class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class checks whether or not the coma file "
                + "contains an apostrophe '. If it does then these all apostrophes"
                + " ' are changed to apostrophes ’.";
        return description;
    }

    @Override
    public Report check(Corpus c) {
        Report stats = new Report();
        cd = c.getComaData();
        try {
            stats = function(cd, false);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown transformer error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown Xpath error");
        } catch (URISyntaxException ex) {
             stats.addException(ex, function, cd, "Unknown URI error");
        }
        return stats;
    }

        @Override
    public Report fix(Corpus c) {
        Report stats = new Report();
        cd = c.getComaData();
        try {
            stats = function(cd, true);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown transformer error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown Xpath error");
        } catch (URISyntaxException ex) {
             stats.addException(ex, function, cd, "Unknown URI error");
        }
        return stats;
    }
}
