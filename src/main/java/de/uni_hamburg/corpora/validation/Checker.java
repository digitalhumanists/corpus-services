/*
 *   A command-line interface for checking corpus files.
 *
 *  @author Anne Ferger
 *  @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.validation.ValidatorSettings;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * an abstract class to be extended by additional validators or checkers This
 * Class reads a File and outputs errors but doesn't change it The commandline
 * input is the file to be checked as a string
 *
 *
 * How to also put another file as input for an check?
 *
 */
public abstract class Checker implements CorpusFunction {

    //I will keep the settings for now, so they can stay as they are for the Moment
    //and we know where to refactor when we change them
    //They are only allowed to be used in the main method, not the other CorpusFunction methods
    CorpusData cd;
    Report report = new Report();
    Collection<Class<? extends CorpusData>> IsUsableFor = new ArrayList<Class<? extends CorpusData>>();
    final String function;
    boolean canfix;

    Checker(boolean hasfixingoption) {
        function = this.getClass().getSimpleName();
        canfix = hasfixingoption;

    }

    public Report execute(Corpus c) {
        return execute(c, false);
    }

    public Report execute(CorpusData cd) {
        return execute(cd, false);
    }

    public Report execute(CorpusData cd, boolean fix) {
        report = new Report();
        try {
            if (fix) {

                if (canfix) {
                    report = fix(cd);
                } else {
                    report.addCritical(function,
                            "Automatic fix is not yet supported.");
                }

                return report;
            } else {
                report = check(cd);
            }
        } catch (JexmaraldaException je) {
            report.addException(je, function, cd, "Unknown parsing error");
        } catch (JDOMException jdome) {
            report.addException(jdome, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            report.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            report.addException(ioe, function, cd, "File reading error");
        } catch (FSMException ex) {
            report.addException(ex, function, cd, "File reading error");
        } catch (URISyntaxException ex) {
            report.addException(ex, function, cd, "File reading erro");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, function, cd, "File reading error");
        } catch (TransformerException ex) {
            report.addException(ex, function, cd, "File reading error");
        } catch (XPathExpressionException ex) {
            report.addException(ex, function, cd, "File reading error");
        }
        return report;
    }

    public Report execute(Corpus c, boolean fix) {
        report = new Report();
        try {
            if (fix) {

                if (canfix) {
                    report = fix(c);
                } else {
                    report.addCritical(function,
                            "Automatic fix is not yet supported.");
                }

                return report;
            } else {
                report = check(c);
            }
        } catch (JexmaraldaException je) {
            report.addException(je, function, cd, "Unknown parsing error");
        } catch (JDOMException jdome) {
            report.addException(jdome, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            report.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            report.addException(ioe, function, cd, "File reading error");
        } catch (FSMException ex) {
            report.addException(ex, function, cd, "File reading error");
        } catch (URISyntaxException ex) {
            report.addException(ex, function, cd, "File reading erro");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, function, cd, "File reading error");
        } catch (TransformerException ex) {
            report.addException(ex, function, cd, "File reading error");
        } catch (XPathExpressionException ex) {
            report.addException(ex, function, cd, "File reading error");
        }
        return report;
    }

    //To implement in the class
    public abstract Report check(CorpusData cd) throws FSMException, URISyntaxException, SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException, JDOMException;

    //To implement in the class
    public abstract Report check(Corpus c) throws FSMException, URISyntaxException, SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException, JDOMException;

    //To implement in the class
    public abstract Report function(CorpusData cd, Boolean fix) throws FSMException, URISyntaxException, SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException, JDOMException;

    //To implement in the class
    //If there is no possibility to fix it throw a warning that says that
    public Report fix(CorpusData cd) throws FSMException, URISyntaxException, SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException, JDOMException {
        report.addCritical(function,
                "Automatic fix is not yet supported.");
        return report;
    }

    //To implement in the class
    //If there is no possibility to fix it throw a warning that says that
    public Report fix(Corpus c) throws FSMException, URISyntaxException, SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException, JDOMException {
        report.addCritical(function,
                "Automatic fix is not yet supported.");
        return report;
    }

    public abstract Collection<Class<? extends CorpusData>> getIsUsableFor();

    public void setIsUsableFor(Collection<Class<? extends CorpusData>> cdc) {
        for (Class<? extends CorpusData> cl : cdc) {
            IsUsableFor.add(cl);
        }
    }

    public String getFunction() {
        return function;
    }

}
