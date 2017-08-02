/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.conversion;

import static de.uni_hamburg.corpora.conversion.EXB2EAF.EX2ELAN_STYLESHEET;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.ListTranscription;
import org.exmaralda.partitureditor.jexmaralda.SegmentedTranscription;
import org.exmaralda.partitureditor.jexmaralda.convert.StylesheetFactory;
import org.exmaralda.partitureditor.jexmaralda.segment.SegmentedToListInfo;
import org.xml.sax.SAXException;

/**
 *
 * @author sesv009
 */
public class EXB2TEI {
    
    private String segmentationAlgorithm = "GENERIC";
    private String language = "en";
    private static String STYLESHEET_PATH = "/org/exmaralda/partitureditor/jexmaralda/xsl/EXMARaLDA2TEI.xsl";
    
    /** Creates a new instance of EXB2TEI */
    public EXB2TEI() {
        
    }
               
    /** reads the EXB as String specified by basicTranscrition and returns a TEI Transcription
     * @param basicTranscription Representation of EXMARaLDA basic transcrition in String
     * @return Representation of ISO-TEI Spoken transcription in a String
     * @throws org.exmaralda.partitureditor.jexmaralda.JexmaraldaException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.transform.TransformerException */
    
    public String convert(String basicTranscription) throws IOException, 
                                                            ParserConfigurationException, 
                                                            SAXException, 
                                                            TransformerException,
                                                            TransformerConfigurationException,
                                                            JexmaraldaException{
        return convert(basicTranscription, segmentationAlgorithm);
    }
    
    public String convert(String basicTranscription, String algorithmName) throws IOException, 
                                                                                  SAXException, 
                                                                                  ParserConfigurationException, 
                                                                                  TransformerException,
                                                                                  TransformerConfigurationException,
                                                                                  JexmaraldaException{
        
        String result;
        
        switch (algorithmName) {
            case "GENERIC":
                result = BasicTranscriptionToGenericISOTEI(basicTranscription);
                break;
            case "AZM":
                result = BasicTranscriptionToGenericISOTEI(basicTranscription);
                break;
            case "MODENA":
                result = BasicTranscriptionToGenericISOTEI(basicTranscription);
                break;
            case "HIAT":
                result = BasicTranscriptionToGenericISOTEI(basicTranscription);
                break;
            default:
                throw new IllegalArgumentException("Invalid segmentation algoritm: " + algorithmName);
        }
                
        return result;
    }
    
    public void setAlgorithm(String algorithmName){
        segmentationAlgorithm = algorithmName;
    }
    
    public String getAlgorithm(){
        return segmentationAlgorithm;
    }
    
    public void setLanguage(String lang){
        language = lang;
    }
    
    public String getLanguage(){
        return language;
    }
    
    private String BasicTranscriptionToGenericISOTEI(String btString) throws IOException, 
                                                                             SAXException, 
                                                                             ParserConfigurationException, 
                                                                             TransformerConfigurationException, 
                                                                             TransformerException,
                                                                             JexmaraldaException {
        
        BasicTranscription bt = new BasicTranscription();
        bt.BasicTranscriptionFromString(btString);
        
        SegmentedTranscription st = bt.toSegmentedTranscription();
        ListTranscription lt = st.toListTranscription(new SegmentedToListInfo(st, SegmentedToListInfo.TURN_SEGMENTATION));
        
        lt.getBody().sort();
        // read the XSL stylesheet into a String
        InputStream xslIS = org.exmaralda.partitureditor.jexmaralda.convert.TEIConverter.class.getResourceAsStream(STYLESHEET_PATH);
        java.util.Scanner s = new java.util.Scanner(xslIS).useDelimiter("\\A");
        String xsl = s.hasNext() ? s.next() : "";
              
        // perform XSLT transformation
        XSLTransformer xt = new XSLTransformer();
        String result = xt.transform(lt.toXML(), xsl);
        
        return result;
    }
    
}
