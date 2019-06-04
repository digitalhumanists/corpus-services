/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 *
 * @author Daniel Jettka
 *
 * Class containing methods for converting between data types.
 */
public class TypeConverter {

    /**
     * Converts an InputStream object into a String object.
     *
     * @param is InputStream object that shall be converted to String object
     * @return String object that was created from InputStream object
     */
    public static String InputStream2String(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }

    /**
     * Converts a String object into an InputStream object.
     *
     * @param s String object that shall be converted to InputStream object
     * @return InputStream object that was created from String object
     */
    public static InputStream String2InputStream(String s) {
        InputStream stream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        return stream;
    }

    /**
     * Converts a BasicTranscription object into a String object.
     *
     * @param bt BasicTranscription object that shall be converted to String
     * object
     * @return String object that was created from BasicTranscription
     */
    public static String BasicTranscription2String(BasicTranscription bt) {
        return bt.toXML();
    }

    /**
     * Converts a String object into a BasicTranscription object.
     *
     * @param btAsString String object that shall be converted to
     * BasicTranscription object
     * @return String object that was created from BasicTranscription object
     */
    public static BasicTranscription String2BasicTranscription(String btAsString) {
        BasicTranscription btResult = null;
        try {
            BasicTranscription bt = new BasicTranscription();
            bt.BasicTranscriptionFromString(btAsString);
            btResult = bt;
        } catch (SAXException ex) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JexmaraldaException ex) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return btResult;
    }

    /**
     * Converts a String object into a StreamSource object.
     *
     * @param s String object that shall be converted to StreamSource object
     * @return StreamSource object that was created from String object
     */
    public static StreamSource String2StreamSource(String s) {
        StreamSource ss = new StreamSource(new StringReader(s));
        return ss;
    }

    /**
     * Converts a org.jdom.Document object into a String object.
     *
     * @param s org.jdom.Document object that shall be converted to String
     * object
     * @return String object that was created from org.jdom.Document object
     */
    public static String JdomDocument2String(org.jdom.Document jdomDocument) {
        return new XMLOutputter().outputString(jdomDocument);

    }

    /**
     * Converts a org.jdom.Document object into a String object.
     *
     * @param s org.jdom.Document object that shall be converted to String
     * object
     * @return String object that was created from org.jdom.Document object
     */
    public static org.jdom.Document String2JdomDocument(String stringRespresentingDocument) {
        org.jdom.Document newDocument = null;
        try {

            InputStream stream = null;
            SAXBuilder builder = new SAXBuilder();
            stream = new ByteArrayInputStream(stringRespresentingDocument.getBytes("UTF-8"));
            newDocument = builder.build(stream);
            return newDocument;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newDocument;
    }

    /**
     * Converts a org.w3c.dom.Document object into a org.jdom.Document object.
     *
     * @param s org.w3c.dom.Document object that shall be converted to
     * org.jdom.Document object
     * @return org.jdom.Document object that was created from
     * org.w3c.dom.Document object
     */
    public static org.jdom.Document W3cDocument2JdomDocument(org.w3c.dom.Document input) {
        org.jdom.Document jdomDoc = null;
        try {
            DOMBuilder builder = new DOMBuilder();
            jdomDoc = builder.build(input);
        } catch (Exception e) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, e);
        }
        return jdomDoc;
    }

    /**
     * Converts a org.jdom.Document object into a org.w3c.dom.Document object.
     *
     * @param s org.jdom.Document object that shall be converted to
     * org.w3c.dom.Document object
     * @return org.w3c.dom.Document object that was created from
     * org.jdom.Document object
     */
    public static org.w3c.dom.Document JdomDocument2W3cDocument(org.jdom.Document jdomDoc) {
        org.w3c.dom.Document w3cDoc = null;
        try {
            DOMOutputter outputter = new DOMOutputter();
            w3cDoc = outputter.output(jdomDoc);
        } catch (JDOMException je) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, je);
        }
        return w3cDoc;
    }
    
    /**
     * Converts a org.w3c.dom.Document object into a String object.
     *
     * @param doc org.w3c.dom.Document object that shall be converted to String object
     * @return String object that was created from org.w3c.dom.Document object
     */
    public static String W3cDocument2String(org.w3c.dom.Document doc) {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            transformer.transform(domSource, result);
        } catch (TransformerException ex) {
            Logger.getLogger(TypeConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString();
    }
    
    
    
    

}
