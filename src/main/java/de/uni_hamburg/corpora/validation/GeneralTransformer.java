/*
 * This class runs an xsl transformation on a file
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class GeneralTransformer extends Checker{
    
    String pathToXSL;
    URL urlToOutput;

    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        try {
            CorpusIO cio = new CorpusIO();
            String corpusdata = cd.toUnformattedString();
            String stylesheet = cio.readInternalResourceAsString(pathToXSL);
            XSLTransformer xslt = new XSLTransformer();
            String result
                    = xslt.transform(corpusdata, stylesheet);
            cio.write(result, urlToOutput);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(GeneralTransformer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(GeneralTransformer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return report;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
