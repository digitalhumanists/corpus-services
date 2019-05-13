package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.BasicTranscriptionData;
import de.uni_hamburg.corpora.ComaData;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 * A class that checks whether there are more than one segmentation algorithms
 * used in the corpus. If that is the case, it issues warnings.
 */
public class ComaTierOverviewCreator extends Checker implements CorpusFunction {

    String comaLoc = "";
    String cscc = "ComaTierOverviewCreator";

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, cscc, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, cscc, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, cscc, cd, "Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, cscc, cd, "Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, cscc, cd, "Transformer Exception");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, cscc, cd, "XPath Exception");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; checks the coma file whether or not
     * there are more than one segmentation algorithms used in the corpus.
     * Issues warnings and returns report which is composed of errors.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException {
        Report stats = new Report();
        ComaData ccd = (ComaData) cd;
        CorpusIO cio = new CorpusIO();
        ArrayList<URL> resulturls = new ArrayList<>();
        ArrayList<Tier> tiers = new ArrayList<>();
        resulturls = ccd.getAllBasicTranscriptionFilenames();
        for (URL resulturl : resulturls) {
            CorpusData cdexb = cio.readFileURL(resulturl);
            BasicTranscriptionData btexb = (BasicTranscriptionData) cdexb;
            Tier t;
            for (int i = 0; i < btexb.getEXMARaLDAbt().getBody().getNumberOfTiers(); i++) {
                t = btexb.getEXMARaLDAbt().getBody().getTierAt(i);
                tiers.add(t);
            }
        }
        System.out.println(tiers);
        //now we have all the existing tiers from the exbs, we need to make a table out of it
        if (!tiers.isEmpty()) {
             // get the XSLT stylesheet
            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream("/xsl/Tier_Overview.xsl"));

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
            URL overviewurl = new URL(overviewurl1, "tier_overview.html");
            cio.write(result, overviewurl);
            
            stats.addCorrect(cscc, cd, "created html tier overview at " + overviewurl);
        } else {
            stats.addWarning(cscc, cd, "No segment counts added yet. Use Coma > Maintenance > Update segment counts to add them. ");
        }
        return stats; // return the report with warnings
    }

    /**
     * This feature does not have fix functionality yet.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        return check(cd);
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }
}
