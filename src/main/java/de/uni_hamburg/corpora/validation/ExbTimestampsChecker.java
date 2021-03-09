/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.jdom.Element;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;

/**
 *
 * @author bay7303
 *
 * This class looks for missing timestamps and issues warnings if finds them.
 * 
 */

public class ExbTimestampsChecker extends Checker implements CorpusFunction {

    boolean missingTimestamp = false;
    Document doc;

    public ExbTimestampsChecker() {
        //fixing option not available
        super(false);
    }
        
    /**
     * One of the main functionalities of the feature; issues warnings if the
     * exb file has missing timestamps in the common timeline.  
     */
    @Override
    public Report function(CorpusData cd, Boolean fix) // check whether there's any illegal apostrophes '
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, JDOMException, TransformerException, XPathExpressionException {
        Report stats = new Report();         // create a new report
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString()); 
        String xpathTimeline = "//tli";
        Pattern tliSegmentPattern = Pattern.compile("^T\\d+$"); 
        XPath timeline = XPath.newInstance(xpathTimeline);
        List allTlis = timeline.selectNodes(doc);
        CorpusIO cio = new CorpusIO();     
        for (int i = 0; i < allTlis.size(); i++) {
            Object o = allTlis.get(i);
            if (o instanceof Element) {
                Element e = (Element) o;
                String tli = e.getAttributeValue("id");
                String tliTimestamp = e.getAttributeValue("time");
                if (tliTimestamp == null) {
                    if (tliSegmentPattern.matcher(tli).find()) {
                        missingTimestamp = true;
                        String message = "Missing timestamp at " + tli;
                        exmaError.addError(function, cd.getURL().getFile(), "", tli, false, message);
                        stats.addCorrect(function, cd, message);
                    }
                }
            }
        }
        if (!missingTimestamp) {
            stats.addCorrect(function, cd, "Timestamps OK");
        }
        return stats; // return the report with warnings
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
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
        String description = "This class issues warnings if it finds missing "
                + "timestamps in the timeline";
        return description;
    }

    @Override
    public Report function(Corpus c, Boolean fix) throws SAXException, IOException, ParserConfigurationException, URISyntaxException, JDOMException, TransformerException, XPathExpressionException {
        Report stats = new Report();
        for (CorpusData cdata : c.getBasicTranscriptionData()) {
            stats.merge(function(cdata, fix));
        }
        return stats;
    }

}
 
