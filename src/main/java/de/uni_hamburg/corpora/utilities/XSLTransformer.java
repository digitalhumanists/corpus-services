/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.utilities;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Daniel Jettka
 */
public class XSLTransformer {
    
    private TransformerFactory tranformerFactory;
    private Transformer transformer;
    private String transformerFactoryImpl = "net.sf.saxon.TransformerFactoryImpl";
    private Map<String, Object> parameters = new HashMap<>();
    
    
    public XSLTransformer() throws TransformerConfigurationException{
        tranformerFactory = TransformerFactory.newInstance(transformerFactoryImpl, null);
    }
    
    
    public String transform(String xml, String xsl) throws TransformerException{
        StreamSource xslSource = TypeConverter.String2StreamSource(xsl);
        StreamSource xmlSource = TypeConverter.String2StreamSource(xml);
        return transform(xmlSource, xslSource);
    }
    
    
    public String transform(StreamSource xmlSource, StreamSource xslSource) throws TransformerConfigurationException, 
                                                                                   TransformerException{
        
        transformer = tranformerFactory.newTransformer(xslSource);
        
        // set the parameters for XSLT transformation
        for (Map.Entry<String, Object> param : parameters.entrySet()){
            transformer.setParameter(param.getKey(), param.getValue());
        }
                
        //transform and fetch result
        String result = "";
        StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        result = resultWriter.toString();
        
        return result;
    }
    
    public void setParameter(String parameterName, Object parameterValue){        
        parameters.put(parameterName, parameterValue);
    }
    
    public void setParameters(Map<String, Object> params){        
        parameters = params;
    }
    
    public Object getParameter(String parameterName){
        return parameters.get(parameterName);
    }
    
    public Map getParameters(){
        return parameters;
    }
        
    public void setTransformerFactoryImpl(String impl){
        transformerFactoryImpl = impl;
        tranformerFactory = TransformerFactory.newInstance(transformerFactoryImpl, null);
    }
    
    public String getTransformerFactoryImpl(){
        return transformerFactoryImpl;
    }
        
}
