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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        ArrayList<URL> resulturls;
        ArrayList<Tier> tiers = new ArrayList<>();
        ArrayList<BasicTranscriptionData> bts = new ArrayList<>();
        resulturls = ccd.getAllBasicTranscriptionURLs();
        for (URL resulturl : resulturls) {
            CorpusData cdexb = cio.readFileURL(resulturl);
            BasicTranscriptionData btexb = (BasicTranscriptionData) cdexb;
            bts.add(btexb);
            Tier t;
            for (int i = 0; i < btexb.getEXMARaLDAbt().getBody().getNumberOfTiers(); i++) {
                t = btexb.getEXMARaLDAbt().getBody().getTierAt(i);
                tiers.add(t);
            }
        }
        //System.out.println(tiers);
        //now we have all the existing tiers from the exbs, we need to make a table out of it
        //use the html template and add the content into id
        if (!tiers.isEmpty()) {
            // get the HTML stylesheet
            String htmltemplate = TypeConverter.InputStream2String(getClass().getResourceAsStream("/xsl/tier_overview_datatable_template.html"));
            String h1 = "<h1> Tier Overview over Whole Corpus (" + resulturls.size() +" exbs) </h1>";
            String tables = h1 + "<table id=\"\" class=\"compact\">\n"
                    + "   <thead>\n"
                    + "      <tr>\n"
                    + "         <th class=\"compact\">Category-Type-DisplayName</th>\n"
                    + "         <th class=\"compact\">Number of Tiers</th>\n"
                    + "      </tr>\n"
                    + "   </thead>\n"
                    + "   <tbody>\n";
            List<String> stringtiers = new ArrayList<String>();
            for (Tier tier : tiers) {
                //stringtiers.add(tier.getCategory() + "-" + tier.getType() + "-" + tier.getDisplayName());
                stringtiers.add(tier.getCategory() + "-" + tier.getType());
            }
            Set<String> hash_Set = new HashSet<String>(stringtiers);
            // add the tables to the html
            //first table: one column with categories, one with count    
            for (String s : hash_Set) {
                tables = tables + "<tr><td class=\"compact\">" + s + "</td><td class=\"compact\">" + Collections.frequency(stringtiers, s) + "</td></tr>";
            }
            tables = tables + " </tr>\n"
                    + "   </tbody>\n"
                    + "</table>";
            String result = htmltemplate + tables;

            URL overviewurl = new URL(cd.getParentURL(), "tier_overview.html");
            cio.write(result, overviewurl);

            stats.addCorrect(cscc, cd, "created tier overview at " + overviewurl);
        } else {
            stats.addWarning(cscc, cd, "No tiers found in the linked exbs. ");
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
