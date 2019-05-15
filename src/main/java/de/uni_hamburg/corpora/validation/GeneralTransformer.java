/*
 * This class runs an xsl transformation on a file
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class GeneralTransformer extends Checker {

    String pathToXSL = "";
    String urlToOutput;
    String getra = "GeneralTransformer";
    boolean overwritefiles = false;

    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        report.addCritical(getra,
                "XSL Transformation cannot be checked, only fixed (use -f)");
        return report;
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        try {
            CorpusIO cio = new CorpusIO();
            String corpusdata = cd.toUnformattedString();
            String stylesheet = cio.readInternalResourceAsString(pathToXSL);
            XSLTransformer xslt = new XSLTransformer();
            String result
                    = xslt.transform(corpusdata, stylesheet);
            if (result != null) {
                report.addCorrect(getra, cd.getURL().toString(),
                        "XSL Transformation was successful");
            }
            if (overwritefiles){
            cio.write(result, cd.getURL());    
            } else {
            cio.write(result, urlToOutput);
            }
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(GeneralTransformer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(GeneralTransformer.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (ClassNotFoundException ex) {
            report.addException(ex, " usable class not found");
        }
        return IsUsableFor;
    }

    public void setPathToXSL(String s) {
        pathToXSL = s;
    }

    public void setUrlToOutput(String s) throws MalformedURLException {
        if (s.startsWith("file://")) {
            urlToOutput = new URL(s);
        } else {
            urlToOutput = Paths.get(s).toUri().toURL();
        }
    }

    public void setOverwriteFiles(String s) {
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("wahr") || s.equalsIgnoreCase("ja")) {
            overwritefiles = true;
        } else if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("falsch") || s.equalsIgnoreCase("nein")) {
            overwritefiles = false;
        } else {
            report.addCritical(getra, cd, "Parameter coma not recognized: " + escapeHtml4(s));
        }
    }

}
