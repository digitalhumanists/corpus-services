package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The class that generates an annotation specification panel from the basic
 * transcription files (exb).
 */
public class GenerateAnnotationPanel extends Checker implements CorpusFunction {

    String genLoc = "";
    ArrayList<String> annotationsInExbs = new ArrayList<>(); // list for holding annotations in exbs
    boolean generateDoc = true; // flag for whether the file created or not
    int iterateExbs = 0;
    final String GAP = "gap";
    
    /**
    * Creates the annotation panel with the annotationsinExbs.
    */
    public Report generateAnnotation(CorpusData cd) throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        Report stats = new Report();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement;
        rootElement = doc.createElement("annotation-specification");
        doc.appendChild(rootElement);
        for (int i = iterateExbs; i < annotationsInExbs.size(); i++) {
            Element annotationSet = doc.createElement("annotation-set");
            rootElement.appendChild(annotationSet);
            Attr attr = doc.createAttribute("exmaralda-tier-category");
            attr.setValue(annotationsInExbs.get(i));
            annotationSet.setAttributeNode(attr);
            Element category = doc.createElement("category");
            category.setAttribute("name", annotationsInExbs.get(i) + "-tags");
            annotationSet.appendChild(category);
            Element tag = doc.createElement("tag");
            tag.setAttribute("name", annotationsInExbs.get(i));
            category.appendChild(tag);
            stats.addCorrect(GAP,
                    "Annotation added to the file annotation panel: "
                    + annotationsInExbs);
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        File f = new File(new File(cd.getURL().getFile()).getParentFile().getParentFile()+"\\AnnotationSpecFromExbs.xml");
        URI u = f.toURI();
        StreamResult result = new StreamResult(new File(u));
        transformer.transform(source, result);
        return stats;
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, genLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, genLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, genLoc + ": Unknown file reading error");
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(GenerateAnnotationPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(GenerateAnnotationPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stats;
    }
           
    /**
    * Main feature of the class: Adds annotation tags from exb files to a list.
    */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        Report stats = new Report();
        NodeList tiers = doc.getElementsByTagName("tier"); // get all tiers of the transcript
        for (int i = 0; i < tiers.getLength(); i++) { // loop for dealing with each tier
            Element tier = (Element) tiers.item(i);
            String category = tier.getAttribute("category"); // get category 
            String type = tier.getAttribute("type"); // get type
            if (!category.equals("v") && !annotationsInExbs.contains(category) && type.equals("a")) {
                annotationsInExbs.add(category);     // add annotations to an array list
            }
        }
        stats = generateAnnotation(cd);        // call the necessary method to create the annotation panel
        return stats;
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
    * Default function which determines for what type of files (basic
    * transcription, segmented transcription, coma etc.) this feature can be
    * used.
    */
    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GenerateAnnotationPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

}
