/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author fsnv625
 */
public class SegmentedTranscriptionData implements CorpusData, ContentData, XMLData {

    Document jdom;
    URL url;
    String originalstring;
    
    public SegmentedTranscriptionData(URL url) {
        try {
            this.url = url;
            SAXBuilder builder = new SAXBuilder();
            jdom = builder.build(url);
           originalstring = new
                String(Files.readAllBytes(Paths.get(url.toURI())), "UTF-8");
        } catch (JDOMException ex) {
            Logger.getLogger(SegmentedTranscriptionData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SegmentedTranscriptionData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(SegmentedTranscriptionData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public String toSaveableString() {
        return toPrettyPrintedXML();
    }

    @Override
    public String toUnformattedString() {
        return originalstring;
    }

    private String toPrettyPrintedXML() {
        String prettyCorpusData = indent(toUnformattedString(), "event");
        //String prettyCorpusData = indent(bt.toXML(bt.getTierFormatTable()), "event");
        return prettyCorpusData;
    }
    
    @Override
    public void updateUnformattedString(String newUnformattedString) {
        originalstring = newUnformattedString;
    }
    @Override
    public Document getJdom() {
        return jdom;
    }

    @Override
    public void setJdom(Document doc) {
        jdom = doc;
    }
    
}
