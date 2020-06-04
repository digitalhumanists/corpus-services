package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.IOUtils;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author Ozzy
 */
public class ReportStatistics extends Checker implements CorpusFunction {

    private static final String HTML_REPORT = "report-output.html";
    String REPORT_STATISTICS;
    CorpusData cd;
    String corpusname = "";

    public ReportStatistics() {
        //no fixing available
        super(false);
    }

    @Override
    public Report function(CorpusData cd, Boolean fix)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException, JexmaraldaException {
        Report stats = new Report();
        String reportStatisticsPath = cd.getParentURL().getPath() + "curation/report-statistics.html";
        String htmlReportPath = cd.getParentURL().getPath() + "curation/" +HTML_REPORT;
        File htmlReportFile = new File(htmlReportPath);
        if (htmlReportFile.isFile()) {
            FileInputStream fis = new FileInputStream(htmlReportPath);
            String html = IOUtils.toString(fis);
            File reportStatFile = new File(reportStatisticsPath);
            if (reportStatFile.isFile()) {
                String reportStatistics = IOUtils.toString(new FileInputStream(reportStatisticsPath));
                Pattern ok = Pattern.compile("[0-9\\.]+ OK"); // get okay messages
                Pattern bad = Pattern.compile("[0-9\\.]+ bad"); // get critical errors
                Pattern warnings = Pattern.compile("[0-9\\.]+ warnings"); // get warnings
                Pattern unknown = Pattern.compile("[0-9\\.]+ unknown"); // get unknown messages
                Matcher mOk = ok.matcher(html);
                Matcher mBad = bad.matcher(html);
                Matcher mWarnings = warnings.matcher(html);
                Matcher mUnknown = unknown.matcher(html);
                int nOK = 0;
                while (mOk.find()) {
                    String sOk = mOk.group();
                    nOK += Integer.parseInt(sOk.substring(0, sOk.indexOf("OK") - 1).replaceAll("\\.", ""));
                }
                int nBad = 0;
                while (mBad.find()) {
                    String sBad = mBad.group();
                    nBad += Integer.parseInt(sBad.substring(0, sBad.indexOf("bad") - 1).replaceAll("\\.", ""));
                }
                int nWarnings = 0;
                while (mWarnings.find()) {
                    String sWarnings = mWarnings.group();
                    nWarnings += Integer.parseInt(sWarnings.substring(0, sWarnings.indexOf("warnings") - 1).replaceAll("\\.", ""));
                }
                int nUnknown = 0;
                while (mUnknown.find()) {
                    String sUnknown = mUnknown.group();
                    nUnknown += Integer.parseInt(sUnknown.substring(0, sUnknown.indexOf("unknown") - 1).replaceAll("\\.", ""));
                }

                if (reportStatistics.indexOf("var labelCSV") != -1) {
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    String strDate = formatter.format(date);
                    int sIndex = reportStatistics.indexOf("var labelCSV");
                    int eIndex = reportStatistics.indexOf(";", sIndex);
                    String exData = reportStatistics.substring(sIndex, eIndex + 1);
                    String newData = exData.replace("\";", "," + strDate + "\";");
                    reportStatistics = reportStatistics.replace(exData, newData);
                }
                if (reportStatistics.indexOf("var criticalsCSV") != -1) {
                    int sIndex = reportStatistics.indexOf("var criticalsCSV");
                    int eIndex = reportStatistics.indexOf(";", sIndex);
                    String exData = reportStatistics.substring(sIndex, eIndex + 1);
                    String newData = exData.replace("\";", "," + Integer.toString(nBad) + "\";");
                    reportStatistics = reportStatistics.replace(exData, newData);
                }
                if (reportStatistics.indexOf("var warningsCSV") != -1) {
                    int sIndex = reportStatistics.indexOf("var warningsCSV");
                    int eIndex = reportStatistics.indexOf(";", sIndex);
                    String exData = reportStatistics.substring(sIndex, eIndex + 1);
                    String newData = exData.replace("\";", "," + Integer.toString(nWarnings) + "\";");
                    reportStatistics = reportStatistics.replace(exData, newData);
                }

                if (reportStatistics.indexOf("var notesCSV") != -1) {
                    int sIndex = reportStatistics.indexOf("var notesCSV");
                    int eIndex = reportStatistics.indexOf(";", sIndex);
                    String exData = reportStatistics.substring(sIndex, eIndex + 1);
                    String newData = exData.replace("\";", "," + Integer.toString(nOK) + "\";");
                    reportStatistics = reportStatistics.replace(exData, newData);
                }
                PrintWriter htmlOut = new PrintWriter(new FileOutputStream(reportStatisticsPath));
                htmlOut.print(reportStatistics);
                htmlOut.close();
                
                stats.addFix(function, cd, "Report Statistics file updated (see " + htmlReportPath + ").");
            } else {
                stats.addMissing(function, cd, "Corpus Report file not found "
                        + "at '" + htmlReportPath + "'. Report Statistics (graphic overview) not updated.");
            }
        } else {
            stats.addMissing(function, cd, "Report Statistics file not found at "
                    + "'" + reportStatisticsPath + "'. Report Statistics (graphic overview) not updated.");
        }
        return stats;
    }

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
        String description = "This class creates or updates the html statistics report"
                + " from the report output file outputted by the corpus services.";
        return description;
    }

    @Override
    public Report function(Corpus c, Boolean fix) throws SAXException, JDOMException, IOException, JexmaraldaException, TransformerException, ParserConfigurationException, UnsupportedEncodingException, XPathExpressionException, URISyntaxException {
        Report stats = new Report();
        CorpusData cdata = c.getComaData();
        stats = function(cdata, fix);
        return stats;
    }
}
