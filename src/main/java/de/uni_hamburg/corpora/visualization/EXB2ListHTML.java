/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.common.corpusbuild.FileIO;
import org.exmaralda.common.jdomutilities.IOUtilities;
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.ListTranscription;
import org.exmaralda.partitureditor.jexmaralda.SegmentedTranscription;
import org.exmaralda.partitureditor.jexmaralda.segment.CHATSegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.GATSegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.GenericSegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.HIATSegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.IPASegmentation;
import org.exmaralda.partitureditor.jexmaralda.segment.SegmentedToListInfo;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author Daniel Jettka
 */
public class EXB2ListHTML {
    
    // resources loaded from directory supplied in pom.xml
    private static final String STYLESHEET_PATH = "/xsl/List2HTML.xsl";
    private static final String JS_HIGHLIGHTING_PATH = "/js/timelight-0.1.min.js";
    
    private final String EMAIL_ADDRESS = "corpora@uni-hamburg.de";
    private final String SERVICE_NAME = "EXB2ListHTML";
    private final String HZSK_WEBSITE = "https://corpora.uni-hamburg.de/";
    
    
    public String convert(String btAsString) throws JexmaraldaException, 
                                                    TransformerException, 
                                                    TransformerConfigurationException, 
                                                    IOException, 
                                                    MalformedURLException, 
                                                    JDOMException, 
                                                    FSMException, 
                                                    Exception{
        return convert(btAsString, "Generic", "", "");            
    }
    
    
    public String convert(String btAsString, String segmAlgorithm) throws JexmaraldaException, 
                                                    TransformerException, 
                                                    TransformerConfigurationException, 
                                                    IOException, 
                                                    MalformedURLException, 
                                                    JDOMException, 
                                                    FSMException, 
                                                    Exception{
        return convert(btAsString, segmAlgorithm, "", "");            
    }
    
    
    // This method deals with transforming EXB to list HTML
    public String convert(String btAsString, String segmAlgorithm, String recordingId, String recordingType)
            throws SAXException, JexmaraldaException, TransformerConfigurationException, TransformerException, MalformedURLException, IOException, JDOMException, FSMException, Exception {

        BasicTranscription bt = TypeConverter.String2BasicTranscription(btAsString);

        // create an utterance list as XML basis for transformation
        String xml = createUtteranceList(bt, segmAlgorithm);

        // get the XSLT stylesheet
        String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream(STYLESHEET_PATH));
         
        
        // create XSLTransformer and set the parameters 
        XSLTransformer xt = new XSLTransformer();
        xt.setParameter("RECORDING_PATH", recordingId);
        xt.setParameter("RECORDING_TYPE", recordingType);
        xt.setParameter("EMAIL_ADDRESS", EMAIL_ADDRESS);
        xt.setParameter("WEBSERVICE_NAME", SERVICE_NAME);
        xt.setParameter("HZSK_WEBSITE", HZSK_WEBSITE);
        
        // perform XSLT transformation
        String result = xt.transform(xml, xsl);
        
        // insert JavaScript for highlighting
        String js = TypeConverter.InputStream2String(getClass().getResourceAsStream(JS_HIGHLIGHTING_PATH));
        result = result.replace("<!--jsholder-->", js);
        
        return result;

    }

    public String createUtteranceList(BasicTranscription bt, String segmAlgorithm) throws JDOMException, IOException, SAXException, FSMException, JexmaraldaException, Exception {

        String list = "";
        switch (segmAlgorithm) {
            case "HIAT":
                {
                    HIATSegmentation hS = new HIATSegmentation();
                    ListTranscription lt = hS.BasicToUtteranceList(bt);
                    final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                    list = IOUtilities.documentToString(listXML);
                    break;
                }
            case "CHAT":
                {
                    CHATSegmentation cS = new CHATSegmentation();
                    ListTranscription lt = cS.BasicToUtteranceList(bt);
                    final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                    list = IOUtilities.documentToString(listXML);
                    break;
                }
            case "GAT":
                {
                    GATSegmentation gS = new GATSegmentation();
                    ListTranscription lt = gS.BasicToIntonationUnitList(bt);
                    final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                    list = IOUtilities.documentToString(listXML);
                    break;
                }
            case "IPA":
                {
                    IPASegmentation ipaS = new IPASegmentation();
                    SegmentedTranscription st = ipaS.BasicToSegmented(bt);
                    ListTranscription lt = st.toListTranscription(new SegmentedToListInfo(st, SegmentedToListInfo.TURN_SEGMENTATION));
                    final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                    list = IOUtilities.documentToString(listXML);
                    break;
                }
            case "Generic":
                {
                    GenericSegmentation genS = new GenericSegmentation();
                    SegmentedTranscription st = genS.BasicToSegmented(bt);
                    ListTranscription lt = st.toListTranscription(new SegmentedToListInfo(st, SegmentedToListInfo.TURN_SEGMENTATION));
                    final Document listXML = FileIO.readDocumentFromString(lt.toXML());
                    list = IOUtilities.documentToString(listXML);
                    break;
                }
            default:
                throw new Exception("createUtteranceList - unsupported parameter segmAlgorithm='"+segmAlgorithm+"'");
        }
        
        return list;
    }
    
}
