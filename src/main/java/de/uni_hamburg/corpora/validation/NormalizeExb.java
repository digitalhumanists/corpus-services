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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author fsnv625
 */
public class NormalizeExb extends Checker implements CorpusFunction {

    Document doc = null;
    BasicTranscriptionData btd = null;
    Boolean fixWhiteSpaces = false;
    
    @Override
    public Report check(CorpusData cd) {
        report.addCritical("NormalizeExb", cd.getURL().getFile(), "Checking option is not available");
        return report;
    }

    @Override
    public Report fix(CorpusData cd) {
        try {
            btd = (BasicTranscriptionData) cd;
            BasicTranscription bt = btd.getEXMARaLDAbt();
            bt.normalize();
            if(fixWhiteSpaces){
                bt.normalizeWhiteSpace();
            }
            btd.setReadbtasjdom(bt.toJDOMDocument());
            btd.setOriginalString(bt.toXML());
            //btd.updateReadbtasjdom();
            cd = (CorpusData) btd;
            CorpusIO cio = new CorpusIO();
            cio.write(cd, cd.getURL());
            if(cd != null){
            report.addCorrect("NormalizeExb", cd.getURL().getFile() , "normalized the file");   
            }
            else{
            report.addCritical("NormalizeExb", cd.getURL().getFile() , "normalizing was not possible");
            }
        } catch (JDOMException ex) {
            report.addException("NormalizeExb", ex, "unknown xml exception");
        } catch (IOException ex) {
            report.addException("NormalizeExb", ex, "unknown IO exception");
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
    
    public void setfixWhiteSpaces(Boolean boo){
        fixWhiteSpaces = boo;
    }

}
