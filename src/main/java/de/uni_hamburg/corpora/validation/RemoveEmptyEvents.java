/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.XMLData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author fsnv625
 */
public class RemoveEmptyEvents extends Checker implements CorpusFunction {

    Document doc = null;
    String rase = "RemoveEmptyEvents";

    @Override
    public Report check(CorpusData cd) {
        try {
            XMLData xml = (XMLData) cd;
            List al = findAllEmptyEvents(xml);
            //if there is no autosave, nothing needs to be done
            if (al.isEmpty()) {
                report.addCorrect(rase, cd, "there are no empty events left");
            } else {
                report.addCritical(rase, cd, "empty events need to be removed");
                exmaError.addError(rase, cd.getURL().getFile(), "", "", false, "empty events need to be removed");
            }
        } catch (JDOMException ex) {
            report.addException(ex, rase, cd, "Jdom Exception");
        }
        return report;
    }

    @Override
    public Report fix(CorpusData cd) {
        try {
            XMLData xml = (XMLData) cd;
            List al = findAllEmptyEvents(xml);
            if (!al.isEmpty()) {
                try {
                    for (Object o : al) {
                        Element e = (Element) o;
                        System.out.println(e);
                        //remove it
                        e.getParent().removeContent(e);
                    }
                    //then save file
                    //add a report message
                    xml.setJdom(doc);
                    cd = (CorpusData) xml;
                    cd.updateUnformattedString(TypeConverter.JdomDocument2String(doc));
                    CorpusIO cio = new CorpusIO();
                    cio.write(cd, cd.getURL());
                    report.addCorrect(rase, cd, "removed empty event");
                } catch (IOException ex) {
                    report.addException(ex, rase, cd, "Input/Output Exception");
                } catch (TransformerException ex) {
                    report.addException(ex, rase, cd, "Input/Output Exception");
                } catch (ParserConfigurationException ex) {
                    report.addException(ex, rase, cd, "Input/Output Exception");
                } catch (SAXException ex) {
                    report.addException(ex, rase, cd, "Input/Output Exception");
                } catch (XPathExpressionException ex) {
                    report.addException(ex, rase, cd, "Input/Output Exception");
                }
            } else {
                report.addCorrect(rase, cd, "there are no empty events left");
            }
        } catch (JDOMException ex) {
            report.addException(ex, rase, cd, "Jdom Exception");
        }
                    return report;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
            Class cl2 = Class.forName("de.uni_hamburg.corpora.SegmentedTranscriptionData");
            IsUsableFor.add(cl2);

        } catch (ClassNotFoundException ex) {
            report.addException(ex, "unknown class not found error");
        }
        return IsUsableFor;
    }

    public List findAllEmptyEvents(XMLData xml) throws JDOMException {
        doc = xml.getJdom();
        //maybe pretty print too
        XPath xp1;
        //needs to be working for exs too
        xp1 = XPath.newInstance("//event[not(text())]");
        List allEmptyEvents = xp1.selectNodes(doc);
        return allEmptyEvents;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class removes empty events present in exb and"
                + " exs files.";
        return description;
    }

}
