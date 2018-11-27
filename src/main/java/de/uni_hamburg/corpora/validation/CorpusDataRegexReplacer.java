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
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class CorpusDataRegexReplacer extends Checker implements CorpusFunction {

    boolean containsRegEx = false;
    String cdrr = "CorpusDataRegexReplacer";
    String replace = "";
    String replacement = "";
    boolean coma = false;
    String xpathContext = "//";
    Document doc = null;
    XPath context;

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
        } catch (JDOMException ex) {
            stats.addException(ex, cdrr, cd, "Unknown parsing error");
        }
        return stats;
    }

    /**
     * One of the main functionalities of the feature; issues warnings if the
     * coma file contains containsRegEx ’and add that warning to the report
     * which it returns.
     */
    private Report exceptionalCheck(CorpusData cd) // check whether there's any regEx instances on specified XPath
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, JDOMException {
        Report stats = new Report();         // create a new report
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString()); // read the file as a doc
        Pattern replacePattern = Pattern.compile(replace);
        context = XPath.newInstance(xpathContext);
        List allContextInstances = context.selectNodes(doc);
        if (!allContextInstances.isEmpty()) {
            for (int i = 0; i < allContextInstances.size(); i++) {
                Object o = allContextInstances.get(i);
                Element e = (Element) o;
                String s = e.getText();
                if (replacePattern.matcher(s).find()) {          // if file contains the RegEx then issue warning
                    containsRegEx = true;
                    System.err.println("CorpusData file is containing " + replace  + " at " + xpathContext + ": " + s);
                    stats.addWarning(cdrr, cd, "CorpusData file is containing " + replace  + " at " + xpathContext + ": " + s);
                } //;
                else {
                    stats.addCorrect(cdrr, cd, "CorpusData file does not contain " + replace  + " at " + xpathContext);
                }

            }
        }
        return stats; // return the report with warnings
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        Report stats = new Report();         // create a new report
        Pattern replacePattern = Pattern.compile(replace);
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString()); // read the file as a doc
        context = XPath.newInstance(xpathContext);
        List allContextInstances = context.selectNodes(doc);
        if (!allContextInstances.isEmpty()) {
            for (int i = 0; i < allContextInstances.size(); i++) {
                Object o = allContextInstances.get(i);
                Element e = (Element) o;
                String s = e.getText();
                if (replacePattern.matcher(s).find()) {          // if file contains the RegEx then issue warning
                    containsRegEx = true;
                    String snew = s.replaceAll(replace, replacement);    //replace all replace with replacement
                    e.setText(snew);
                    stats.addCorrect(cdrr, cd, "Replaced " + replace + " with "+ replacement + " at " + xpathContext + " here: " + s + " with " + snew );
                } 
            }
        } else {
            stats.addCorrect(cdrr, cd, "CorpusData file does not contain " + replace + " at " + xpathContext);
        }
        return stats;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            if (coma) {
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

    public void setReplace(String s) {
        replace = s;
    }

    public void setReplacement(String s) {
        replacement = s;
    }

    public void setXpathContext(String s) {
        xpathContext = s;
    }
}
