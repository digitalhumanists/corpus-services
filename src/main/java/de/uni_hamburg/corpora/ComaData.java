/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
//don't know if this is the correct Coma class in Exmaralda yet...
import org.exmaralda.coma.root.Coma;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.xpath.XPath;

/**
 *
 * @author fsnv625
 */
public class ComaData implements Metadata, CorpusData {

    //TODO
    //private Coma coma;
    URL url;
    Document readcomaasjdom = new Document();
    String originalstring;

    public String CORPUS_BASEDIRECTORY = "";
    
    public static String SEGMENTED_FILE_XPATH = "//Transcription[Description/Key[@Name='segmented']/text()='true']/NSLink";
    public static String BASIC_FILE_XPATH = "//Transcription[Description/Key[@Name='segmented']/text()='false']/NSLink";
    public static String ALL_FILE_XPATH = "//Transcription/NSLink";
    
    public ArrayList<URL> referencedCorpusDataURLs;

    public ComaData() {
    }

    public ComaData(URL url) {
        try {
            this.url = url;
            SAXBuilder builder = new SAXBuilder();
            readcomaasjdom = builder.build(url);
            File f = new File(url.toURI());
           originalstring = new
                String(Files.readAllBytes(Paths.get(url.toURI())), "UTF-8");
            //loadFile(f);
        } catch (JDOMException ex) {
            Logger.getLogger(UnspecifiedXMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UnspecifiedXMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(BasicTranscriptionData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //TODO
//     public void loadFile(File f) throws SAXException, JexmaraldaException, MalformedURLException {
//        coma = new BasicTranscription(f.getAbsolutePath());
//        url = f.toURI().toURL();
//    }
    //TODO
    /*  
     public void updateReadcomaasjdom() throws SAXException, JexmaraldaException, MalformedURLException, JDOMException, IOException {
        String xmlString = bt.toXML();
        SAXBuilder builder = new SAXBuilder();
        readbtasjdom = builder.build(xmlString);
    }
     */
    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public String toSaveableString() {
        return toPrettyPrintedXML();
    }

    private String toPrettyPrintedXML() {
        String prettyCorpusData = indent(toUnformattedString(), "event");
        //String prettyCorpusData = indent(bt.toXML(bt.getTierFormatTable()), "event");
        return prettyCorpusData;
    }

    @Override
    public String toUnformattedString() {
        return originalstring;
    }

    @Override
    public void updateUnformattedString(String newUnformattedString) {
        originalstring = newUnformattedString;
    }
}
