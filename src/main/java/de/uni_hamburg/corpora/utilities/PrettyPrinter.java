package de.uni_hamburg.corpora.utilities;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Daniel Jettka
 */
public class PrettyPrinter {
    
    
    /**
    * pretty-prints (indents) XML
    * 
    * @param xml                 The input XML string 
    * @param suppressedElements  blank-separated list of QNames for elements to be disregarded for indentation
    * @return	                 indented XML string
    */
    public static String indent(String xml, String suppressedElements) throws TransformerException, ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException, XPathExpressionException {
        
        return indent(xml, suppressedElements, "");
      
    }
    
    
    /**
    * pretty-prints (indents) XML
    * 
    * @param xml                 The input XML string 
    * @param suppressedElements  blank-separated list of QNames for elements to be disregarded for indentation
    * @return	                 indented XML string
    */
    public static String indent(String xml, String suppressedElements, String xslString) throws TransformerException, ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException, XPathExpressionException {
      
        
            // Turn xml string into a document
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));

            // Remove whitespaces outside tags
            document.normalize();
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
                                                          document,
                                                          XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            // Setup pretty print options
            // get the XSLT stylesheet and the XML base
            //String xslString = TypeConverter.InputStream2String(de.uni_hamburg.corpora.utilities.PrettyPrinter.class.getClassLoader().getResourceAsStream("/xsl/pretty-print-sort-elements.xsl"));
            
            String xmlString = TypeConverter.W3cDocument2String(document);
            
            // create XSLTransformer and set the parameters 
            XSLTransformer xt = new XSLTransformer("net.sf.saxon.TransformerFactoryImpl");

            xt.setOutputProperty(OutputKeys.ENCODING, "UTF-8");            
            xt.setOutputProperty(OutputKeys.VERSION, "1.0");
            xt.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, xml.indexOf("<?xml") >= 0 ? "no" : "yes");
            xt.setOutputProperty(OutputKeys.INDENT, "yes");
            xt.setOutputProperty("suppress-indentation", suppressedElements);
            
            
            // perform XSLT transformation
            String prettyXmlString;
            if(xslString.equals("")){                
                prettyXmlString = xt.transform(xmlString);

            } else{
                prettyXmlString = xt.transform(xmlString, xslString);
            }            
            
            
            /* insert some specific EXMARaLDA dialect styles */
            
            // insert a blank space at the end of empty elements
            Pattern r1 = Pattern.compile("<([^>]+)([^>\\s])/>", Pattern.DOTALL);
            prettyXmlString = r1.matcher(prettyXmlString).replaceAll("<$1$2 />");
            
            // insert explicit CDATA section for specific elements
            Pattern r2 = Pattern.compile("<nts([^>]*)>([\\s]+)</nts>", Pattern.DOTALL);
            prettyXmlString = r2.matcher(prettyXmlString).replaceAll("<nts$1><![CDATA[$2]]></nts>");
                   
	    // insert explicit CDATA section for specific elements
            Pattern r2a = Pattern.compile("<event([^>]*)>([\\s]+)</event>", Pattern.DOTALL);
            prettyXmlString = r2a.matcher(prettyXmlString).replaceAll("<event$1><![CDATA[$2]]></event>");
                        
            // insert explicit CDATA section for specific elements
            Pattern r2b = Pattern.compile("<ts([^>]*)>([\\s]+)</ts>", Pattern.DOTALL);
            prettyXmlString = r2b.matcher(prettyXmlString).replaceAll("<ts$1><![CDATA[$2]]></ts>");
            
            // insert explicit CDATA section for specific elements
            Pattern r2c = Pattern.compile("<ta([^>]*)>([\\s]+)</ta>", Pattern.DOTALL);
            prettyXmlString = r2c.matcher(prettyXmlString).replaceAll("<ta$1><![CDATA[$2]]></ta>");
            
            // insert explicit CDATA section for specific elements
            Pattern r2d = Pattern.compile("<ats([^>]*)>([\\s]+)</ats>", Pattern.DOTALL);
            prettyXmlString = r2d.matcher(prettyXmlString).replaceAll("<ats$1><![CDATA[$2]]></ats>");

            // re-sort attributes for EXBs from alphabetic to EXB style
            Pattern r3 = Pattern.compile("<event\\s*(end=\"[^\">]*\")\\s+(start=\"[^\">]*\")\\s*>", Pattern.DOTALL);
            prettyXmlString = r3.matcher(prettyXmlString).replaceAll("<event $2 $1>");
            
            Pattern r4 = Pattern.compile("<tier\\s+(category=\"[^\">]*\")\\s+(display\\-name=\"[^\">]*\")\\s+(id=\"[^\">]*\")\\s+(speaker=\"[^\">]*\")\\s+(type=\"[^\">]*\")\\s*(/?)>", Pattern.DOTALL);
            prettyXmlString = r4.matcher(prettyXmlString).replaceAll("<tier $3 $4 $1 $5 $2 $6>");
                   
            // return certain empty elements from EXB with opening and closing tags
            Pattern r5 = Pattern.compile("<(tier|event|ud\\-meta\\-information|languages\\-used|ud\\-speaker\\-information)([^/>]*?)\\s*/>", Pattern.DOTALL);
            prettyXmlString = r5.matcher(prettyXmlString).replaceAll("<$1$2></$1>");
            
            return prettyXmlString;
    }
}
