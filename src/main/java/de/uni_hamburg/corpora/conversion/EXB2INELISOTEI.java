/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.conversion;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import org.exmaralda.common.corpusbuild.FileIO;
import org.exmaralda.common.corpusbuild.TEIMerger;
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.SegmentedTranscription;
import org.exmaralda.partitureditor.jexmaralda.segment.HIATSegmentation;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.transform.XSLTransformException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class EXB2INELISOTEI {

    //copied partly from exmaralda\src\org\exmaralda\partitureditor\jexmaralda\convert\TEIConverter.java
    String language = "en";

    public void writeMORPHEMEHIATISOTEIToFile(BasicTranscription bt, String filename) throws SAXException,
            FSMException,
            XSLTransformException,
            JDOMException,
            IOException,
            Exception {
        writeMORPHEMEHIATISOTEIToFile(bt, filename, false, "/basic-transcription/basic-body/tier[@id = \"mb\"]");
    }

    public void writeMORPHEMEHIATISOTEIToFile(BasicTranscription bt,
            String filename,
            boolean includeFullText, String XPath2Morphemes) throws SAXException,
            FSMException,
            XSLTransformException,
            JDOMException,
            IOException,
            Exception {
        // added 13-12-2013
        BasicTranscription copyBT = bt.makeCopy();
        copyBT.normalize();
        System.out.println("started writing document...");
        HIATSegmentation segmentation = new HIATSegmentation();
        SegmentedTranscription st = segmentation.BasicToSegmented(copyBT);
        System.out.println("Segmented transcription created");
        String nameOfDeepSegmentation = "SpeakerContribution_Utterance_Word";
        TEIMerger teiMerger = new TEIMerger(true);
        Document stdoc = FileIO.readDocumentFromString(st.toXML());
        Document teiDoc = teiMerger.SegmentedTranscriptionToTEITranscription(stdoc,
                nameOfDeepSegmentation,
                "SpeakerContribution_Event",
                true,
                includeFullText);
        System.out.println("Merged");
        generateWordIDs(teiDoc);
        setDocLanguage(teiDoc, language);
        FileIO.writeDocumentToLocalFile(filename, teiDoc);
        System.out.println("document written.");
    }

    // new 30-03-2016
    private void generateWordIDs(Document document) throws JDOMException {
        // added 30-03-2016
        HashSet<String> allExistingIDs = new HashSet<String>();
        XPath idXPath = XPath.newInstance("//tei:*[@xml:id]");
        idXPath.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        idXPath.addNamespace(Namespace.XML_NAMESPACE);
        List idElements = idXPath.selectNodes(document);
        for (Object o : idElements) {
            Element e = (Element) o;
            allExistingIDs.add(e.getAttributeValue("id", Namespace.XML_NAMESPACE));
        }

        // changed 30-03-2016
        XPath wordXPath = XPath.newInstance("//tei:w[not(@xml:id)]");
        wordXPath.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        wordXPath.addNamespace(Namespace.XML_NAMESPACE);

        List words = wordXPath.selectNodes(document);
        int count = 1;
        for (Object o : words) {
            Element word = (Element) o;
            while (allExistingIDs.contains("w" + Integer.toString(count))) {
                count++;
            }

            String wordID = "w" + Integer.toString(count);
            allExistingIDs.add(wordID);
            //System.out.println("*** " + wordID);
            word.setAttribute("id", wordID, Namespace.XML_NAMESPACE);
        }

        // new 02-12-2014
        XPath pcXPath = XPath.newInstance("//tei:pc[not(@xml:id)]");
        pcXPath.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        pcXPath.addNamespace(Namespace.XML_NAMESPACE);

        List pcs = pcXPath.selectNodes(document);
        count = 1;
        for (Object o : pcs) {
            Element pc = (Element) o;
            while (allExistingIDs.contains("pc" + Integer.toString(count))) {
                count++;
            }

            String pcID = "pc" + Integer.toString(count);
            allExistingIDs.add(pcID);
            //System.out.println("*** " + wordID);
            pc.setAttribute("id", pcID, Namespace.XML_NAMESPACE);
        }
    }

    private void setDocLanguage(Document teiDoc, String language) throws JDOMException {
        // /TEI/text[1]/@*[namespace-uri()='http://www.w3.org/XML/1998/namespace' and local-name()='lang']
        XPath xpathToLangAttribute = XPath.newInstance("//tei:text/@xml:lang");
        xpathToLangAttribute.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        xpathToLangAttribute.addNamespace(Namespace.XML_NAMESPACE);
        Attribute langAtt = (Attribute) xpathToLangAttribute.selectSingleNode(teiDoc);
        if (langAtt != null) {
            langAtt.setValue(language);
        } else {
            XPath xpathToTextElement = XPath.newInstance("//tei:text");
            xpathToTextElement.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
            xpathToTextElement.addNamespace(Namespace.XML_NAMESPACE);
            Element textEl = (Element) xpathToTextElement.selectSingleNode(teiDoc);
            textEl.setAttribute("lang", language, Namespace.XML_NAMESPACE);
        }
        System.out.println("Language of document set to " + language);

    }

}
