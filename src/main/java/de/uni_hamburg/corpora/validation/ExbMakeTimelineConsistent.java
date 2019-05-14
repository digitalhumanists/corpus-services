/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.BasicTranscriptionData;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class ExbMakeTimelineConsistent extends Checker implements CorpusFunction {
    Document doc = null;
    BasicTranscriptionData btd = null;
    Boolean interpolateTimeline = false;
    String ne = "MakeTimelineConsistent";
    
    @Override
    public Report check(CorpusData cd) {
        report.addCritical(ne, cd.getURL().getFile(), "Checking option is not available");
        return report;
    }

    @Override
    public Report fix(CorpusData cd) {
        try {
            btd = (BasicTranscriptionData) cd;
            BasicTranscription bt = btd.getEXMARaLDAbt();
            bt.getBody().getCommonTimeline().makeConsistent();
            if(interpolateTimeline){
                bt.getBody().getCommonTimeline().completeTimes();
            }
            
            btd.setReadbtasjdom(bt.toJDOMDocument());
            btd.setOriginalString(bt.toXML(bt.getTierFormatTable()));
            //btd.updateReadbtasjdom();
            cd = (CorpusData) btd;
            CorpusIO cio = new CorpusIO();
            cio.write(cd, cd.getURL());
            if(cd != null){
            report.addCorrect(ne, cd, "made timeline consistent");   
            }
            else{
            report.addCritical(ne, cd, "making timeline consistent not possible");
            }
        } catch (JDOMException ex) {
            report.addException(ex, ne, cd, "unknown xml exception");
        } catch (IOException ex) {
            report.addException(ex, ne, cd, "unknown IO exception");
        } catch (TransformerException ex) {
             report.addException(ex, ne, cd, "unknown IO exception");
        } catch (ParserConfigurationException ex) {
             report.addException(ex, ne, cd, "unknown IO exception");
        } catch (SAXException ex) {
             report.addException(ex, ne, cd, "unknown IO exception");
        } catch (XPathExpressionException ex) {
             report.addException(ex, ne, cd, "unknown IO exception");
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
    
    public void setInterpolateTimeline(String s){
        interpolateTimeline = false;
        if (s.equals("true") || s.equals("wahr") || s.equals("ja") || s.equals("yes")) {
            interpolateTimeline = true;
        }
    }
}
