/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.utilities;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

/**
 *
 * @author Daniel Jettka
 */
public class XSLTransformer {
    
    private String xslAsString;
    private String xmlAsString;
    private String transformerFactoryImpl = "net.sf.saxon.TransformerFactoryImpl";
    
    
    public XSLTransformer(){}
    
    public String transform(String xml, String xsl) throws TransformerException{
        StreamSource xslSource = new StreamSource(new StringReader(xsl));
        StreamSource xmlSource = new StreamSource(new StringReader(xml));
        return transform(xmlSource, xslSource);
    }
    
    
    public String transform(StreamSource xmlSource, StreamSource xslSource) throws TransformerConfigurationException, 
                                                                                   TransformerException{
        
        TransformerFactory tf = TransformerFactory.newInstance(transformerFactoryImpl, null);
        Transformer transformer = tf.newTransformer(xslSource);  
                
        //transform and fetch result
        String result = "";
        StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        result = resultWriter.toString();
        
        return result;
    }
    
    public void setTransformerFactoryImpl(String impl){
        transformerFactoryImpl = impl;
    }
    
    public String getTransformerFactoryImpl(String impl){
        return transformerFactoryImpl;
    }
        
}
