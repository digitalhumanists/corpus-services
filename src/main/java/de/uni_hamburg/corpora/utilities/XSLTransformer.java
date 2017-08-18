/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.utilities;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Daniel Jettka
 *
 * Class for performing XSLT transformation with net.sf.saxon.TransformerFactoryImpl or other custom implementation.
 */
public class XSLTransformer {
    
    private TransformerFactory tranformerFactory;
    private Transformer transformer;
    private String transformerFactoryImpl = "net.sf.saxon.TransformerFactoryImpl";
    private Map<String, Object> parameters = new HashMap<>();
    
    
    public XSLTransformer() throws TransformerConfigurationException{
        tranformerFactory = TransformerFactory.newInstance(transformerFactoryImpl, null);
    }
	
	
    public XSLTransformer() throws TransformerConfigurationException{
        tranformerFactory = TransformerFactory.newInstance(transformerFactoryImpl, null);
    }
    
    /**
	 * Returns a String object that represents the result of an XSLT transformation.
	 *
	 * @param  xml  XML as String object that is used as the basis for the XSLT transformation
	 * @param  xsl  XSLT stylesheet as String object
	 * @return      the result of the XSLT transformation as String object
	 */
    public String transform(String xml, String xsl) throws TransformerException{
        StreamSource xslSource = TypeConverter.String2StreamSource(xsl);
        StreamSource xmlSource = TypeConverter.String2StreamSource(xml);
        return transform(xmlSource, xslSource);
    }
    
    /**
	 * Returns a String object that represents the result of an XSLT transformation.
	 *
	 * @param  xml  XML as StreamSource object that is used as the basis for the XSLT transformation
	 * @param  xsl  XSLT stylesheet as StreamSource object
	 * @return      the result of the XSLT transformation as String object
	 */
    public String transform(StreamSource xmlSource, StreamSource xslSource){
        
        String result = null;
        
        try{
            transformer = tranformerFactory.newTransformer(xslSource);

            // set the parameters for XSLT transformation
            for (Map.Entry<String, Object> param : parameters.entrySet()){
                transformer.setParameter(param.getKey(), param.getValue());
            }

            //transform and fetch result
            StringWriter resultWriter = new StringWriter();
            transformer.transform(xmlSource, new StreamResult(resultWriter));
            result = resultWriter.toString();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XSLTransformer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XSLTransformer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
    
	/**
	 * Set a single parameter for the XSLT transformation.
	 *
	 * @param  parameterName   Name of the parameter
	 * @param  parameterValue  Value of the parameter
	 * @return      
	 */
    public void setParameter(String parameterName, Object parameterValue){        
        parameters.put(parameterName, parameterValue);
    }
    
	/**
	 * Set a bunch of parameters for the XSLT transformation.
	 *
	 * @param  params  Map object representing parameter names and values
	 * @return      
	 */
    public void setParameters(Map<String, Object> params){        
        parameters = params;
    }
    
	/**
	 * Get a single parameter that was set for the XSLT transformation.
	 *
	 * @param    parameterName  Name of the parameter that shall be returned
	 * @return   Value of the supplied parameter
	 */
    public Object getParameter(String parameterName){
        return parameters.get(parameterName);
    }
    
	/**
	 * Get all parameters as Map object that were set for the XSLT transformation.
	 *
	 * @param    
	 * @return   Map representing all parameters 
	 */
    public Map getParameters(){
        return parameters;
    }
    
	/**
	 * Set TransformerFactoryImpl (represented as class name in String) for the XSLT transformation.
	 *
	 * @param  impl  TransformerFactoryImpl represented as class name in String 
	 * @return   
	 */
    public void setTransformerFactoryImpl(String impl){
        transformerFactoryImpl = impl;
        tranformerFactory = TransformerFactory.newInstance(transformerFactoryImpl, null);
    }
    
	/**
	 * Get TransformerFactoryImpl (represented as class name in String) for the XSLT transformation.
	 *
	 * @param  
	 * @return   TransformerFactoryImpl represented as class name in String
	 */
    public String getTransformerFactoryImpl(){
        return transformerFactoryImpl;
    }
        
}
