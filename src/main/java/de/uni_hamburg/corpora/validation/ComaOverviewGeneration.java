/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import de.uni_hamburg.corpora.visualization.ListHTML;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Scanner;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class ComaOverviewGeneration extends Checker implements CorpusFunction {

    final String COMA_OVERVIEW = "coma-overview";

    @Override
    public Report check(CorpusData cd){
        Report r = new Report();
        
        try{

            // get the XSLT stylesheet
            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream("/xsl/Output_metadata_summary.xsl"));

            // create XSLTransformer and set the parameters 
            XSLTransformer xt = new XSLTransformer();
        
            // perform XSLT transformation
            String result = xt.transform(cd.toSaveableString(), xsl);
            Path path = Paths.get(cd.getURL().toURI()); 
            Path pathwithoutfilename = path.getParent();
            URI overviewuri = pathwithoutfilename.toUri();
            URL overviewurl1 = overviewuri.toURL();
            System.out.println(overviewurl1);
            //TODO systemindependent!!
            URL overviewurl = new URL(overviewurl1, "coma_overview.html");
            CorpusIO cio = new CorpusIO();
            cio.write(result, overviewurl);
            
            r.addCorrect(COMA_OVERVIEW, cd, "created html overview at " + overviewurl);
            
            
//            //read lines and add to Report
//            Scanner scanner = new Scanner(result);
//            
//            int i = 1;
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                
//                //split line by ;
//                String[] lineParts = line.split(";");
//                
//                switch (lineParts[0].toUpperCase()) {
//                    case "WARNING":
//                        r.addWarning("XSLTChecker", cd.getURL().getFile() + ": " + lineParts[1]);
//                        break;
//                    case "CRITICAL":
//                        r.addCritical("XSLTChecker", cd.getURL().getFile() + ": " + lineParts[1]);
//                        break;
//                    case "NOTE":                    
//                        r.addNote("XSLTChecker", cd.getURL().getFile() + ": " + lineParts[1]);
//                        break;
//                    case "MISSING": 
//                        r.addMissing("XSLTChecker", cd.getURL().getFile() + ": " + lineParts[1]);
//                        break;
//                    default:
//                        r.addCritical("XSLTChecker", "(Unrecognized report type) "+ cd.getURL().getFile() + ": " + lineParts[1]);
//                }
//                
//                i++;
//            }
//
//            scanner.close();


        } catch (TransformerConfigurationException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Transformer configuration error");
        } catch (TransformerException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Transformer error");
        } catch (MalformedURLException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Malformed URL error");
        } catch (IOException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Unknown input/output error");
        } catch (URISyntaxException ex) {
            r.addException(ex, COMA_OVERVIEW, cd, "Unknown URI syntax error");
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
            cl1 = Class.forName("de.uni_hamburg.corpora.ComaData");
             IsUsableFor.add(cl1);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
            return IsUsableFor;
    }

}
