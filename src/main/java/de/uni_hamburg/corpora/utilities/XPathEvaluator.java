/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.utilities;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException; 
import net.sf.saxon.lib.NamespaceConstant; 
import org.w3c.dom.Document;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author Daniel Jettka
 */
public class XPathEvaluator {
    
    private static Document document;
    
    
    public XPathEvaluator(Document doc){
        document = doc;
    }
    
    
    public Object evaluate(String xpe){
        
        Object result = null;
        
        try{
            
            System.setProperty("javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");
            XPathFactory factory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
            XPath xpath = factory.newXPath();
            XPathExpression expr = xpath.compile(xpe);
            result = expr.evaluate(document);
            
        } catch (XPathFactoryConfigurationException ex) {
            Logger.getLogger(XPathEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XPathEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    
    }
    
    public Object evaluate(String xpe, Document doc){        
        document = doc;
        return evaluate(xpe);    
    }
    
}
