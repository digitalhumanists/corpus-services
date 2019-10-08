package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author Daniel Jettka, daniel.jettka@uni-hamburg.de
 */
public class XSLTChecker extends Checker implements CorpusFunction {

    String xslresource = "/xsl/nslc-checks.xsl";
    String xc = "XSLTChecker";
    String filename = "";
    String UTTERANCEENDSYMBOLS = "[.!?…:]";

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report fix(Collection<CorpusData> cdc) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report execute(Corpus c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {

        Report r = new Report();
        filename = cd.getURL().getFile().subSequence(cd.getURL().getFile().lastIndexOf('/') + 1, cd.getURL().getFile().lastIndexOf('.')).toString();
        try {

            // get the XSLT stylesheet
            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream(xslresource));

            // create XSLTransformer and set the parameters 
            XSLTransformer xt = new XSLTransformer();

            xt.setParameter("filename", filename);
            xt.setParameter("UTTERANCEENDSYMBOL", UTTERANCEENDSYMBOLS);
            // perform XSLT transformation
            String result = xt.transform(cd.toSaveableString(), xsl);

            //read lines and add to Report
            Scanner scanner = new Scanner(result);

            int i = 1;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                //split line by ;
                String[] lineParts = line.split(";", -1);
                if (lineParts.length != 5) {
                    String message = "";
                    for (String s : lineParts) {
                        message = message + s;
                    }
                    r.addCritical(lineParts[0], cd, "There was an exception while creating the error probably because of a semicolon or newline in an event: " + message);
                } else {
                    switch (lineParts[1].toUpperCase()) {
                        case "WARNING":
                            r.addWarning(lineParts[0], cd, lineParts[2]);
                            /* if (cd.getFilename().endsWith(".exb")) {
                                exmaError.addError("XSLTChecker", cd.getURL().getFile(), lineParts[2], lineParts[3], false, lineParts[1]);
                            } */
                            break;
                        case "CRITICAL":
                            r.addCritical(lineParts[0], cd, lineParts[2]);
                            if (cd.getFilename().endsWith(".exb")) {
                                exmaError.addError(lineParts[0], cd.getURL().getFile(), lineParts[3], lineParts[4], false, lineParts[2]);

                            }
                            break;
                        case "NOTE":
                            r.addNote(lineParts[0], cd, lineParts[2]);
                            break;
                        case "MISSING":
                            r.addMissing(lineParts[0], cd, lineParts[2]);
                            if (cd.getFilename().endsWith(".exb")) {
                                exmaError.addError(lineParts[0], cd.getURL().getFile(), lineParts[3], lineParts[4], false, lineParts[2]);
                            }
                            break;
                        default:
                           r.addCritical(lineParts[0], cd, "(Unrecognized report type): " + lineParts[2]);
                            if (cd.getFilename().endsWith(".exb")) {
                                exmaError.addError(lineParts[0], cd.getURL().getFile(), lineParts[3], lineParts[4], false, lineParts[2]);
                            }
                    }
                }

                i++;
            }

            scanner.close();

        } catch (TransformerConfigurationException ex) {
            report.addException(ex, xc, cd, "unknown tranformation configuration error");
        } catch (TransformerException ex) {
            report.addException(ex, xc, cd, "unknown tranformation error");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, xc, cd, "unknown parsing error");
        } catch (SAXException ex) {
            report.addException(ex, xc, cd, "unknown XML error");
        } catch (XPathExpressionException ex) {
            report.addException(ex, xc, cd, "unknown XPath error");
        } catch (IOException ex) {
            report.addException(ex, xc, cd, "unknown IO error");
        }

        return r;

    }

    public void setXSLresource(String s) {
        xslresource = s;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
            Class cl1 = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl1);
            //Class cl2 = Class.forName("de.uni_hamburg.corpora.UnspecifiedXMLData");
            //IsUsableFor.add(cl2);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "unknown class not found error");
        }
        return IsUsableFor;
    }
    
     public void setUtteranceEndSymbols(String s) {
        UTTERANCEENDSYMBOLS = s;
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
