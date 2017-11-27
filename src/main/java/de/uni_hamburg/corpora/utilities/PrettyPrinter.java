package de.uni_hamburg.corpora.utilities;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
    public static String indent(String xml, String suppressedElements) {
        
        try {

            
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
            TransformerFactory transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("suppress-indentation", suppressedElements);

            // Return pretty print xml string
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

            return stringWriter.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
