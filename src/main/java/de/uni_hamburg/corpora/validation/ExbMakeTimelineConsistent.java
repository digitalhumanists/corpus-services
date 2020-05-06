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
 * This class makes the timeline of exbs consistent by removing incorrect timepoints 
 * and interpolates timeline items without time info if the parameter is set.
 * 
 */
public class ExbMakeTimelineConsistent extends Checker implements CorpusFunction {

    Document doc = null;
    BasicTranscriptionData btd = null;
    Boolean interpolateTimeline = false;

    public ExbMakeTimelineConsistent() {
        super("MakeTimelineConsistent");
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
            bt.getBody().getCommonTimeline().makeConsistent();
            if (interpolateTimeline) {
                bt.getBody().getCommonTimeline().completeTimes();
            }

            btd.setReadbtasjdom(bt.toJDOMDocument());
            btd.setOriginalString(bt.toXML(bt.getTierFormatTable()));
            //btd.updateReadbtasjdom();
            cd = (CorpusData) btd;
            CorpusIO cio = new CorpusIO();
            cio.write(cd, cd.getURL());
            if (cd != null) {
                report.addFix(function, cd, "made timeline consistent");
            } else {
                report.addCritical(function, cd, "making timeline consistent not possible");
            }
        } catch (JDOMException ex) {
            report.addException(ex, function, cd, "unknown xml exception");
        } catch (IOException ex) {
            report.addException(ex, function, cd, "unknown IO exception");
        } catch (TransformerException ex) {
            report.addException(ex, function, cd, "unknown IO exception");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, function, cd, "unknown IO exception");
        } catch (SAXException ex) {
            report.addException(ex, function, cd, "unknown IO exception");
        } catch (XPathExpressionException ex) {
            report.addException(ex, function, cd, "unknown IO exception");
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

    public void setInterpolateTimeline(String s) {
        interpolateTimeline = false;
        if (s.equals("true") || s.equals("wahr") || s.equals("ja") || s.equals("yes")) {
            interpolateTimeline = true;
        }
    }

    /**
     * Default function which returns a two/three line description of what this
     * class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class makes the timeline of exbs consistent by removing "
                + "incorrect timepoints and interpolates timeline items without time "
                + "info if the parameter is set. ";
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
