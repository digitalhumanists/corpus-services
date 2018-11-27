/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
/**
 *
 * @author fsnv625
 */
public class CorpusDataRegexReplacer extends Checker implements CorpusFunction{
    String cdLoc = "";
    String cdFile = "";
    boolean containsRegEx = false;
    String cdrr = "CorpusDataRegexReplacer";
    String replace = "";
    String replacement = "";
    boolean coma = false;

    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, cdrr, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, cdrr, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, cdrr, cd, "Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, cdrr, cd, "Unknown file reading error");
        }
        return stats;
    }

    /**
     * One of the main functionalities of the feature; issues warnings if the
     * coma file contains containsRegEx ’and add that warning to the report which
     * it returns.
     */
    private Report exceptionalCheck(CorpusData cd) // check whether there's any illegal apostrophes '
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        Report stats = new Report();         // create a new report
        cdFile = cd.toSaveableString();     // read the file as a string
        Pattern replacePattern = Pattern.compile(replace);
                //;
        if (replacePattern.matcher(cdFile).find()) {          // if file contains the RegEx then issue warning
            containsRegEx = true;
            System.err.println("CorpusData file is containing " + replace);
            stats.addWarning(cdrr, cd, "CorpusData file is containing " + replace);
        }
        else {
            stats.addCorrect(cdrr, cd, "CorpusData file does not contain " + replace);
        }
        return stats; // return the report with warnings
    }

    
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        Report stats = new Report();         // create a new report
        cdFile = cd.toSaveableString();     // read the file as a string
        Pattern replacePattern = Pattern.compile(replace);
        if (replacePattern.matcher(cdFile).find()) {         // if file contains regex then issue warning
            containsRegEx = true;                // flag points out if there are regex
            cdFile = cdFile.replaceAll(replace, replacement);    //replace all replace with replacement
            CorpusIO cio = new CorpusIO();
            cio.write(cdFile, cd.getURL());    // write back to file with replacement
            stats.addCorrect(cdrr, cd, "Replaced " + replace + " with " + replacement); // fix report 
        } else {
            stats.addCorrect(cdrr, cd, "CorpusData does not contain" + replace);
        }
        return stats;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            if (coma){
            Class cl3 = Class.forName("de.uni_hamburg.corpora.ComaData");   
            IsUsableFor.add(cl3);
            } else {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");   
            IsUsableFor.add(cl);
            }
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }
    
    public void setReplace(String s){
        replace = s;
    }
    
    public void setReplacement(String s){
        replacement = s;
    }
}
