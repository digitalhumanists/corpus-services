package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.BasicTranscriptionData;
import de.uni_hamburg.corpora.ComaData;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
        } catch (JexmaraldaException ex) {
            stats.addException(ex, cscc, cd, "Exmaralda Exception");
        }
        return stats;
    }

    /**
     * Main functionality of the feature; checks the coma file whether or not
     * there are more than one segmentation algorithms used in the corpus.
     * Issues warnings and returns report which is composed of errors.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException, JexmaraldaException {
        Report stats = new Report();
        ComaData ccd = (ComaData) cd;
        CorpusIO cio = new CorpusIO();
        ArrayList<URL> resulturls;
        ArrayList<Tier> tiers = new ArrayList<>();
        ArrayList<BasicTranscriptionData> btds = new ArrayList<>();
        String htmltemplate = TypeConverter.InputStream2String(getClass().getResourceAsStream("/xsl/tier_overview_datatable_template.html"));
        String overviewTable = "";
        String communicationsTable = "";
        resulturls = ccd.getAllBasicTranscriptionURLs();
        for (URL resulturl : resulturls) {
            CorpusData cdexb = cio.readFileURL(resulturl);
            BasicTranscriptionData btexb = (BasicTranscriptionData) cdexb;

            btds.add(btexb);
            Tier t;
            for (int i = 0; i < btexb.getEXMARaLDAbt().getBody().getNumberOfTiers(); i++) {
                t = btexb.getEXMARaLDAbt().getBody().getTierAt(i);
                tiers.add(t);
            }
        }
        List<String> stringtiers = new ArrayList<String>();
        for (Tier tier : tiers) {
            //stringtiers.add(tier.getCategory() + "-" + tier.getType() + "-" + tier.getDisplayName());
            stringtiers.add(tier.getCategory() + " (type: " + tier.getType() + ")");
        }
        Set<String> hash_Set = new HashSet<String>(stringtiers);
        //System.out.println(tiers);
        //now we have all the existing tiers from the exbs, we need to make a table out of it
        //use the html template and add the content into id
        if (!tiers.isEmpty()) {
            // get the HTML stylesheet

            String h1 = "<h1> Tier Overview over Whole Corpus (" + resulturls.size() + " exbs) </h1>";
            String header = "<table id=\"\" class=\"compact\">\n"
                    + "   <thead>\n"
                    + "      <tr>\n"
                    + "         <th class=\"compact\">Category-Type-DisplayName</th>\n"
                    + "         <th class=\"compact\">Number of Tiers</th>\n"
                    + "      </tr>\n"
                    + "   </thead>\n"
                    + "   <tbody>\n";
            /* for (Tier tier : tiers) {
                //stringtiers.add(tier.getCategory() + "-" + tier.getType() + "-" + tier.getDisplayName());
                stringtiers.add(tier.getCategory() + "-" + tier.getType());
            } */
            // add the tables to the html
            //first table: one column with categories, one with count    
            // add the overviewTable to the html
            //first table: one column with categories, one with count
            String content = "";

            for (String s : hash_Set) {
                content = content + "<tr><td class=\"compact\">" + s + "</td><td class=\"compact\">" + Collections.frequency(stringtiers, s) + "</td></tr>";
            }
            String footer = "   </tbody>\n"
                    + "</table>";

            overviewTable = h1 + header + content + footer;

        } else {
            stats.addWarning(cscc, cd, "No tiers found in the linked exbs. ");
        }
        //now each exb linked in the coma file
        //TODO
//        if (!btds.isEmpty()) {
//            String h1 = "<h1> Tiers in each exb </h1>";
//            communicationsTable = h1;
//            //first is the column for filename, then all the tier category/type combinations
//            String header = "<table id=\"\" class=\"compact\">\n"
//                    + "   <thead>\n"
//                    + "<th class=\"compact\"> Exb Filename </th>";
//            for (String s : hash_Set) {
//                header = header + "<th class=\"compact\">" + s + "</th><";
//            }
//            header = header + "</tr>"
//                    + "   </thead>\n"
//                    + "   <tbody>\n";
//            String content = "";
//            for (BasicTranscriptionData btd : btds) {
//                //first is the column for filename, then all the tier category/type combinations
//                content = content + "<tr><td class=\"compact\">" + btd.getFilename() + "</td>";
//                for (String s : hash_Set) {
//                    //TO DO
//                    String st = btd.getEXMARaLDAbt().getBody().getTiersOfType(s).toString();
//                    content = content + "<th class=\"compact\">" + st + "</th><";
//                }
//                content = content + "</tr>";
//            }
//
//            String footer = " </tr>\n"
//                    + "   </tbody>\n"
//                    + "</table>";
//            communicationsTable = h1 + header + content + footer;
//        } else {
//            stats.addWarning(cscc, cd, "No linked exbs found in the coma file. ");
//        }

        //String result = htmltemplate + overviewTable + communicationsTable;
        String result = htmltemplate + overviewTable;

        URL overviewurl = new URL(cd.getParentURL(), "curation/tier_overview.html");
        cio.write(result, overviewurl);

        stats.addCorrect(cscc, cd, "created tier overview at " + overviewurl);

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

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class checks whether there are more than one"
                + " segmentation algorithms used in the corpus. If that is the case"
                + ", it issues warnings.";
        return description;
    }
}
