/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import org.exmaralda.coma.root.Coma;
import de.uni_hamburg.corpora.utilities.PrettyPrinter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.apache.commons.io.FilenameUtils;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;

/**
 *
 * @author fsnv625
 */
public class ComaData implements Metadata, CorpusData, XMLData {

    //TODO
    private Coma coma;
    //TODO change exceptions to adding ReportItems
    URL url;
    Document readcomaasjdom = new Document();
    String originalstring;
    String filename;
    String filenamewithoutending;

    public URL CORPUS_BASEDIRECTORY;

    public static String SEGMENTED_FILE_XPATH = "//Transcription[Description/Key[@Name='segmented']/text()='true']/NSLink";
    public static String BASIC_FILE_XPATH = "//Transcription[Description/Key[@Name='segmented']/text()='false']/NSLink";
    public static String ALL_FILE_XPATH = "//Transcription/NSLink";

    public ArrayList<URL> referencedCorpusDataURLs;

    public ComaData() {
    }

    public ComaData(URL url) throws SAXException, JexmaraldaException {
        try {
            this.url = url;
            SAXBuilder builder = new SAXBuilder();
            readcomaasjdom = builder.build(url);
            originalstring = new String(Files.readAllBytes(Paths.get(url.toURI())), "UTF-8");
            loadComaString(originalstring);
            URI uri = url.toURI();
            URI parentURI = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
            CORPUS_BASEDIRECTORY = parentURI.toURL();
            filename = FilenameUtils.getName(url.getPath());
            filenamewithoutending = FilenameUtils.getBaseName(url.getPath());
        } catch (JDOMException ex) {
            Logger.getLogger(UnspecifiedXMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UnspecifiedXMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(BasicTranscriptionData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadComaString(String s) throws SAXException, JexmaraldaException, FileNotFoundException, MalformedURLException {
        coma = new Coma(s, "0.0", "0", false);
    }

    /*public void updateReadcomaasjdom() throws SAXException, JexmaraldaException, MalformedURLException, JDOMException, IOException {
        String xmlString = 
        SAXBuilder builder = new SAXBuilder();
        readcomaasjdom = builder.build(xmlString);
    }*/

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public String toSaveableString() throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        return toPrettyPrintedXML();
    }

    private String toPrettyPrintedXML() throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        PrettyPrinter pp = new PrettyPrinter();
        String prettyCorpusData = pp.indent(toUnformattedString(), "event");
        //String prettyCorpusData = pp.indent(bt.toXML(bt.getTierFormatTable()), "event");
        return prettyCorpusData;
    }

    @Override
    public String toUnformattedString() {
        return originalstring;
    }

    //TODO!
    public Collection<URL> getReferencedCorpusDataURLs() {
        //now read the NSLinks and add the URLs from the files
        //we need to have different ArrayLists for exb, exs, audio, pdf
        //TODO! 
        return referencedCorpusDataURLs;
    }

    public ArrayList<URL> getAllBasicTranscriptionURLs() throws MalformedURLException, URISyntaxException {
        try {
            URL resulturl;
            ArrayList<URL> resulturls = new ArrayList<>();
            XPath xpath = XPath.newInstance(BASIC_FILE_XPATH);
            List transcriptionList = xpath.selectNodes(readcomaasjdom);
            for (int pos = 0; pos < transcriptionList.size(); pos++) {
                Element nslink = (Element) (transcriptionList.get(pos));
                //String fullTranscriptionName = CORPUS_BASEDIRECTORY.toURI().getPath() + nslink.getText();
                resulturl = new URL(CORPUS_BASEDIRECTORY + nslink.getText());
                //Paths.get(fullTranscriptionName).toUri().toURL();
                resulturls.add(resulturl);
            }
            return resulturls;
        } catch (JDOMException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getAllBasicTranscriptionFilenames() {
        try {
            ArrayList<String> result = new ArrayList<>();
            XPath xpath = XPath.newInstance(BASIC_FILE_XPATH);
            List transcriptionList = xpath.selectNodes(readcomaasjdom);
            for (int pos = 0; pos < transcriptionList.size(); pos++) {
                Element nslink = (Element) (transcriptionList.get(pos));
                // currentElement = nslink;
                // String fullTranscriptionName = CORPUS_BASEDIRECTORY + "\\" +
                // nslink.getText();
                result.add(nslink.getText());
                //resulturl = Paths.get(nslink.getText()).toUri().toURL();
                //resulturls.add(resulturl);
            }
            return result;
        } catch (JDOMException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void updateUnformattedString(String newUnformattedString) {
        originalstring = newUnformattedString;
    }

    public void setBaseDirectory(URL url) {
        CORPUS_BASEDIRECTORY = url;
    }

    public URL getBasedirectory() throws URISyntaxException, MalformedURLException {
        URI uri = url.toURI();
        URI parentURI = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
        CORPUS_BASEDIRECTORY = parentURI.toURL();
        return CORPUS_BASEDIRECTORY;
    }

    @Override
    public URL getParentURL() {
        return CORPUS_BASEDIRECTORY;
    }

    @Override
    public void setURL(URL nurl) {
        url = nurl;
    }

    @Override
    public void setParentURL(URL url) {
        CORPUS_BASEDIRECTORY = url;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public void setFilename(String s) {
        filename = s;
    }

    @Override
    public String getFilenameWithoutFileEnding() {
        return filenamewithoutending;
    }

    @Override
    public void setFilenameWithoutFileEnding(String s) {
        filenamewithoutending = s;
    }

    @Override
    public Document getJdom() {
        return readcomaasjdom;
    }

    @Override
    public void setJdom(Document jdom) {
        readcomaasjdom = jdom;
    }

    public Coma getEXMARaLDAComa() {
        return coma;
    }

    public void setOriginalString(String s) {
        originalstring = s;
    }
}
