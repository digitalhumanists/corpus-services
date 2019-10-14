/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.jdom.Element;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

/**
 *
 * @author fsnv625
 */
public class ExbEventLinebreaksChecker extends Checker implements CorpusFunction {

    boolean linebreak = false;
    String elb = "ExbEventLinebreaksChecker";
    String xpathContext = "//event";
    XPath context;
    Document doc;

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, elb, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, elb, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, elb, cd, "Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, elb, cd, "Unknown file reading error");
        } catch (JDOMException ex) {
            stats.addException(ex, elb, cd, "Unknown parsing error");
        } catch (TransformerException ex) {
             stats.addException(ex, elb, cd, "Unknown parsing error");
        } catch (XPathExpressionException ex) {
             stats.addException(ex, elb, cd, "Unknown parsing error");
        }
        return stats;
    }

    /**
     * One of the main functionalities of the feature; issues warnings if the
     * exb file contains linebreaks in events and add that warning to the report
     * which it returns.
     */
    private Report exceptionalCheck(CorpusData cd) // check whether there's any illegal apostrophes '
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, JDOMException, TransformerException, XPathExpressionException {
        Report stats = new Report();         // create a new report
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString()); // read the file as a doc
        Pattern replacePattern = Pattern.compile("[\r\n]");
        context = XPath.newInstance(xpathContext);
        List allContextInstances = context.selectNodes(doc);
        String s = "";
        if (!allContextInstances.isEmpty()) {
            for (int i = 0; i < allContextInstances.size(); i++) {
                Object o = allContextInstances.get(i);
                if (o instanceof Element) {
                    Element e = (Element) o;
                    s = e.getText();
                    if (replacePattern.matcher(s).find()) {          // if file contains the RegEx then issue warning
                    linebreak = true;
                    System.err.println("Exb is containing line ending in an event: " + escapeHtml4(s));
                    stats.addCritical(elb, cd, "Exb is containing line ending in an event: " + escapeHtml4(s));
                }
                }
            }
            if (!linebreak) {
                stats.addCorrect(elb, cd, "CorpusData file does not contain line ending in an event");
            }
        } else {
            stats.addCorrect(elb, cd, "CorpusData file does not contain any event");
        }
        return stats; // return the report with warnings
    }

    @Override
    /**
     * One of the main functionalities of the feature; fix linebreaks in events
     * add them to the report which it returns in the end.
     */
    public Report fix(CorpusData cd) {
        Report stats = new Report();         // create a new report
        try {
            doc = TypeConverter.String2JdomDocument(cd.toSaveableString()); // read the file as a doc
            Pattern replacePattern = Pattern.compile("[\r\n]");
            context = XPath.newInstance(xpathContext);
            List allContextInstances = context.selectNodes(doc);
            String s = "";
            if (!allContextInstances.isEmpty()) {
                for (int i = 0; i < allContextInstances.size(); i++) {
                    Object o = allContextInstances.get(i);
                    if (o instanceof Element) {
                        Element e = (Element) o;
                        s = e.getText();
                        if (replacePattern.matcher(s).find()) {          // if file contains the RegEx then issue warning
                            linebreak = true;
                            String snew = s.replaceAll("[\r\n]", "");    //replace all replace with replacement
                            //TODO Attributes?
                            e.setText(snew);
                            stats.addCorrect(elb, cd, "Removed line ending in an event: " + escapeHtml4(s) + " with " + escapeHtml4(snew));
                        }
                    }
                    
                }
                if (!linebreak) {
                    stats.addCorrect(elb, cd, "CorpusData file does not contain line ending in an event");
                }
            } else {
                stats.addCorrect(elb, cd, "CorpusData file does not contain any event");
            }
            
        } catch (TransformerException ex) {
            Logger.getLogger(ExbEventLinebreaksChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ExbEventLinebreaksChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ExbEventLinebreaksChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExbEventLinebreaksChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ExbEventLinebreaksChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(ExbEventLinebreaksChecker.class.getName()).log(Level.SEVERE, null, ex);
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

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class issues warnings if the exb file contains "
                + "linebreaks or fix linebreaks in the events and add those "
                + "warnings to the report which it returns.";
        return description;
    }
}
