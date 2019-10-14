/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author anne
 * 
 * This class adds the path to an MP3 file next to the WAV file linked as a recording in an exb file.
 * 
 */
public class ExbMP3Next2WavAdder extends Checker implements CorpusFunction {
     final String function = "add-mp3-next-2-wav";
     
    @Override
    public Report check(CorpusData cd){
        Report r = new Report();
        
        try{

            // get the XSLT stylesheet
            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream("/xsl/AddMP3next2WavExb.xsl"));
            // create XSLTransformer and set the parameters 
            XSLTransformer xt = new XSLTransformer();
        
            // perform XSLT transformation
            String result = xt.transform(cd.toSaveableString(), xsl);
            CorpusIO cio = new CorpusIO();
            //update the xml of the cd object
            cd.updateUnformattedString(result);
            //save it - overwrite exb
            cio.write(cd, cd.getURL());
            //everything worked
            r.addCorrect(function, cd, "Added mp3 next to wav.");
            

        } catch (TransformerConfigurationException ex) {
            r.addException(ex, function, cd, "Transformer configuration error");
        } catch (TransformerException ex) {
            r.addException(ex, function, cd, "Transformer error");
        } catch (MalformedURLException ex) {
            r.addException(ex, function, cd, "Malformed URL error");
        } catch (IOException ex) {
            r.addException(ex, function, cd, "Unknown input/output error");
        } catch (ParserConfigurationException ex) {
            r.addException(ex, function, cd, "Unknown Parser error");
        } catch (SAXException ex) {
            r.addException(ex, function, cd, "Unknown XML error");
        } catch (XPathExpressionException ex) {
            r.addException(ex, function, cd, "Unknown XPath error");
        }
        return r;
        
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
       return check(cd);
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        Class cl1;   
        try {
            cl1 = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
             IsUsableFor.add(cl1);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
            return IsUsableFor;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class adds the path to an MP3 file next to the WAV file "
                + "linked as a recording in an exb file.";
        return description;
    }

}
