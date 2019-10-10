/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 *
 * @author Daniel Jettka
 */
public class CorpusHTML extends Visualizer {

    // resources loaded from directory supplied in pom.xml
    static final String STYLESHEET_PATH = "/xsl/Coma2HTML.xsl";
    private static final String SERVICE_NAME = "ComaHTML";
    Report stats = new Report();
    URL targeturl;
    CorpusData cod;

    public CorpusHTML() {

    }

    public String createFromComa(String coma) {

        String result = null;

        try {

            String corpusPrefix = coma.split("<Key Name=\"hzsk:corpusPrefix\">")[1].split("</Key>")[0];
            String corpusVersion = coma.split("<Key Name=\"hzsk:corpusVersion\">")[1].split("</Key>")[0];

            // read the XSL stylesheet into a String
            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream(STYLESHEET_PATH));

            XSLTransformer xt = new XSLTransformer();
            xt.setParameter("identifier", "spoken-corpus:" + corpusPrefix + "-" + corpusVersion);
            result = xt.transform(coma, xsl);
        } catch (TransformerConfigurationException ex) {
            stats.addException(ex, SERVICE_NAME, cd, "Unknown TransformerConfigurationException");
        } catch (TransformerException ex) {
            stats.addException(ex, SERVICE_NAME, cd, "Unknown TransformerException");
        }

        return result;
    }

    @Override
    public Report visualize(CorpusData cd) {
        try {
            cod = cd;
            String result = createFromComa(cd.toSaveableString());
            CorpusIO cio = new CorpusIO();
            targeturl = new URL(cd.getParentURL() + "/resources/" + cd.getFilenameWithoutFileEnding() + ".html");
            cio.write(result, targeturl);
        } catch (MalformedURLException ex) {
            stats.addException(ex, SERVICE_NAME, cd, "Malformed URL used");
        } catch (IOException ex) {
            stats.addException(ex, SERVICE_NAME, cd, "Unknown Input Output error");
        } catch (TransformerException ex) {
            stats.addException(SERVICE_NAME, ex, "Transformer Exception");
        } catch (ParserConfigurationException ex) {
            stats.addException(SERVICE_NAME, ex, "Parser Exception");
        } catch (SAXException ex) {
            stats.addException(SERVICE_NAME, ex, "XML Exception");
        } catch (XPathExpressionException ex) {
            stats.addException(SERVICE_NAME, ex, "XPath Exception");
        }
        return stats;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    public Report doMain(String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setURL(URL url) {
        targeturl = url;
    }

    public URL getTargetURL() throws MalformedURLException {
        return targeturl;
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
