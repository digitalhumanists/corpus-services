/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.BasicTranscriptionData;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 * 
 * This class normalises the basic transcription data using the EXMARaLDA function and fixes white spaces if set by a parameter.
 * 
 */
public class ExbNormalize extends Checker implements CorpusFunction {

    Document doc = null;
    BasicTranscriptionData btd = null;
    Boolean fixWhiteSpaces = false;

    public ExbNormalize() {
        super(true);
    }

    @Override
    public Report check(CorpusData cd) {
        report.addCritical(function, cd, "Checking option is not available");
        return report;
    }

    @Override
    public Report fix(CorpusData cd) {
        try {
            btd = (BasicTranscriptionData) cd;
            BasicTranscription bt = btd.getEXMARaLDAbt();
            bt.normalize();
            if (fixWhiteSpaces) {
                bt.normalizeWhiteSpace();
            }
            btd.setReadbtasjdom(bt.toJDOMDocument());
            btd.setOriginalString(bt.toXML());
            //btd.updateReadbtasjdom();
            cd = (CorpusData) btd;
            CorpusIO cio = new CorpusIO();
            cio.write(cd, cd.getURL());
            if (cd != null) {
                report.addFix(function, cd, "normalized the file");
            } else {
                report.addCritical(function, cd, "normalizing was not possible");
            }
        } catch (JDOMException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        } catch (IOException ex) {
            report.addException(ex, function, cd, "unknown IO exception");
        } catch (TransformerException ex) {
           report.addException(ex, function, cd, "unknown xml exception");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        } catch (SAXException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        } catch (XPathExpressionException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        }
        return report;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);

        } catch (ClassNotFoundException ex) {
            report.addException(ex, "unknown class not found error");
        }
        return IsUsableFor;
    }

    public void setfixWhiteSpaces(String s) {
        fixWhiteSpaces = false;
        if (s.equals("true") || s.equals("wahr") || s.equals("ja") || s.equals("yes")) {
            fixWhiteSpaces = true;
        }
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class normalises the basic transcription data using "
                + "the EXMARaLDA function and fixes white spaces if set by a parameter. ";
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
