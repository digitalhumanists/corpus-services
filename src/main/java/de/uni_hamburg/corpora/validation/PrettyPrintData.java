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
import de.uni_hamburg.corpora.utilities.PrettyPrinter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 * 
 * This class takes XML corpusdata and formats it in the same way to avoid merge conflicts. 
 */
public class PrettyPrintData extends Checker implements CorpusFunction {

    String ppd = "PrettyPrintData";

    public PrettyPrintData() {
    }

    public Report check(CorpusData cd) {
        try {
            // if no diff - all fine, nothing needs to be done
            if (CorpusDataIsAlreadyPretty(cd)) {
                report.addCorrect(ppd, cd, "Already pretty printed.");
            } // if difference then - needs to be pretty printed
            else {
                report.addCritical(ppd, cd, "Needs to be pretty printed.");
            }

        } catch (IOException ex) {
            report.addException(ex, ppd, cd, "Causes an Input/Output error.");
        } catch (TransformerException ex) {
            report.addException(ex, ppd, cd, "Causes an Transformer error.");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, ppd, cd, "Causes an Parser error.");
        } catch (SAXException ex) {
            report.addException(ex, ppd, cd, "Causes an XML error.");
        } catch (XPathExpressionException ex) {
            report.addException(ex, ppd, cd, "Causes an Xpath error.");
        }
        return report;
    }

    public Report fix(CorpusData cd) {
        // take the data, change datatosaveable string, method indent() in utilities\PrettyPrinter.java
        try {
            if (cd.toUnformattedString() == null) {
                report.addCritical(ppd, cd, "Could not create the unformatted String!");
            } else {
                if (!CorpusDataIsAlreadyPretty(cd)) {

                    PrettyPrinter pp = new PrettyPrinter();
                    String prettyCorpusData = pp.indent(cd.toUnformattedString(), "event");
                    //System.out.println(cd.toSaveableString());
                    //System.out.println(prettyCorpusData);
                    //save it instead of the old file
                    CorpusIO cio = new CorpusIO();
                    cio.write(prettyCorpusData, cd.getURL());
                    cd.updateUnformattedString(prettyCorpusData);
                    report.addCorrect(ppd, cd, "CorpusData was pretty printed and saved.");

                } else {
                    report.addCorrect(ppd, cd, "Was already pretty printed.");
                }
            }
        } catch (IOException ex) {
            report.addException(ex, ppd, cd, "Causes an Input/Output error.");
        } catch (TransformerException ex) {
            report.addException(ex, ppd, cd, "Causes an Transformer error.");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, ppd, cd, "Causes an Parser error.");
        } catch (SAXException ex) {
            report.addException(ex, ppd, cd, "Causes an XML error.");
        } catch (XPathExpressionException ex) {
            report.addException(ex, ppd, cd, "Causes an Xpath error.");
        }
        return report;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
            Class cl2 = Class.forName("de.uni_hamburg.corpora.UnspecifiedXMLData");
            IsUsableFor.add(cl2);
            Class cl3 = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl3);
            Class cl4 = Class.forName("de.uni_hamburg.corpora.SegmentedTranscriptionData");
            IsUsableFor.add(cl4);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    public boolean CorpusDataIsAlreadyPretty(CorpusData cd) throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, UnsupportedEncodingException {
        //take the data, change datatosaveable string, method indent() in utilities\PrettyPrinter.java
        //this one works for BasicTranscriptions only (keeping events togehter), but doesn't harm others
        //need to have another string not intended depending on which
        //file is the input

        if (cd.toUnformattedString() != null) {
            PrettyPrinter pp = new PrettyPrinter();
            String prettyCorpusData = pp.indent(cd.toUnformattedString(), "event");
            return cd.toUnformattedString().equals(prettyCorpusData);
        } else {
            return false;
        }
        //compare the files
        // if no diff - all fine, nothing needs to be done
        //TODO error - to saveableString already pretty printed - need to change that        

    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class takes XML corpusdata and formats it in the same way to avoid merge conflicts. ";
        return description;
    }
}
