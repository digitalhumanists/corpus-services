/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author fsnv625
 */
public class UnspecifiedXMLData implements CorpusData, XMLData {

    Document jdom;
    URL url;
    String originalstring;
    URL parenturl;
    String filename;
    String filenamewithoutending;

    public UnspecifiedXMLData(URL url) {
        try {
            this.url = url;
            SAXBuilder builder = new SAXBuilder();
            jdom = builder.build(url);
            originalstring = new String(Files.readAllBytes(Paths.get(url.toURI())), "UTF-8");
            URI uri = url.toURI();
            URI parentURI = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
            parenturl = parentURI.toURL();
            filename = FilenameUtils.getName(url.getPath());
            filenamewithoutending = FilenameUtils.getBaseName(url.getPath());
        } catch (JDOMException ex) {
            Logger.getLogger(UnspecifiedXMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UnspecifiedXMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(UnspecifiedXMLData.class.getName()).log(Level.SEVERE, null, ex);
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

    @Override
    public URL getParentURL() {
        return parenturl;
    }

    @Override
    public void setURL(URL nurl) {
        url = nurl;
    }

    @Override
    public void setParentURL(URL url) {
        parenturl = url;
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
}
