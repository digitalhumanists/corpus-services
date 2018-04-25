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

/**
 *
 * @author fsnv625
 */
public class RemoveAutoSaveExb extends Checker implements CorpusFunction {

    Document doc = null;
    BasicTranscriptionData btd = null;
    
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        try {
            List al = findAllAutoSaveInstances(cd);
            //if there is no autosave, nothing needs to be done
            if (al.isEmpty()) {
                report.addCorrect("RemoveAutoSaveExb", "there is no autosave info left, nothing to do");
            } else {
                report.addCritical("RemoveAutoSaveExb", "autosave info needs to be removed in " + cd.getURL().getFile());
            }
        } catch (JDOMException ex) {
            Logger.getLogger(RemoveAutoSaveExb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return report;
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        List al = findAllAutoSaveInstances(cd);
        if (!al.isEmpty()) {
            for (Object o: al){
                Element e = (Element) o;
                System.out.println(e);
                //remove it
                e.getParent().removeContent(e);
            }
                //then save file
                //add a report message
            btd.setReadbtasjdom(doc);
            cd = (CorpusData) btd;
            CorpusIO cio = new CorpusIO();
            cio.write(cd, cd.getURL());
             report.addCorrect("RemoveAutoSaveExb", "removed AutoSave info in " + cd.getURL().getFile());
        } else {
            report.addCorrect("RemoveAutoSaveExb", "there is no autosave info left, nothing to do");
        }
        return report;
    }

    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PrettyPrintData.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

    public List findAllAutoSaveInstances(CorpusData cd) throws JDOMException {
        btd = (BasicTranscriptionData) cd;
        doc = btd.getReadbtasjdom();
        XPath xp1;
        xp1 = XPath.newInstance("/basic-transcription/head/meta-information/ud-meta-information/ud-information[@attribute-name='AutoSave']");
        List allAutoSaveInfo = xp1.selectNodes(doc);
        return allAutoSaveInfo;
    }

}
