/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Daniel Jettka
 */
public class CorpusHTML extends AbstractVisualization {
    
    // resources loaded from directory supplied in pom.xml
    static final String STYLESHEET_PATH = "/xsl/Coma2HTML.xsl";  
    private static final String SERVICE_NAME = "ComaHTML";
    
    
    public CorpusHTML(){
    
    }
    
    public String createFromComa(String coma){
        
        String result = null;
            
        try {
            
            String corpusPrefix = coma.split("<Key Name=\"hzsk:corpusPrefix\">")[1].split("</Key>")[0];
            String corpusVersion = coma.split("<Key Name=\"hzsk:corpusVersion\">")[1].split("</Key>")[0];
            
            // read the XSL stylesheet into a String
            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream(STYLESHEET_PATH));

            XSLTransformer xt = new XSLTransformer();
            xt.setParameter("identifier", "spoken-corpus:"+corpusPrefix+"-"+corpusVersion);
            result = xt.transform(coma, xsl);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(CorpusHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(CorpusHTML.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
    
}
