package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private final String SERVICE_NAME = "ReportStatistics";
    String REPORT_STATISTICS;
    Report stats;
    CorpusData cd;
    String corpusname = "";

    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (IOException ex) {
            stats.addException(SERVICE_NAME, ex, "Input Output Exception");
        } catch (ParserConfigurationException ex) {
            stats.addException(SERVICE_NAME, ex, "Parser Exception");
        } catch (SAXException ex) {
            stats.addException(SERVICE_NAME, ex, "XML Exception");
        } catch (XPathExpressionException ex) {
            stats.addException(SERVICE_NAME, ex, "XPath Exception");
        } catch (URISyntaxException ex) {
            Logger.getLogger(ReportStatistics.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(ReportStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stats;
    }

    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException, JexmaraldaException {
        Report stats = new Report();
        String reportStatisticsPath = cd.getParentURL().getPath() + "curation/report-statistics.html";
        String htmlReportPath = cd.getParentURL().getPath() + HTML_REPORT;
        FileInputStream fis = new FileInputStream(htmlReportPath);
        String html = IOUtils.toString(fis);
        String reportStatistics = IOUtils.toString(new FileInputStream(reportStatisticsPath));
        Pattern ok = Pattern.compile("[0-9]+ OK"); // get okay messages
        Pattern bad = Pattern.compile("[0-9]+ bad"); // get critical errors
        Pattern warnings = Pattern.compile("[0-9]+ warnings"); // get warnings
        Pattern unknown = Pattern.compile("[0-9]+ unknown"); // get unknown messages
        Matcher mOk = ok.matcher(html);
        Matcher mBad = bad.matcher(html);
        Matcher mWarnings = warnings.matcher(html);
        Matcher mUnknown = unknown.matcher(html);
        int nOK = 0;
        while (mOk.find()) {
            String sOk = mOk.group();
            nOK += Integer.parseInt(sOk.substring(0, sOk.indexOf("OK") - 1));
        }
        int nBad = 0;
        while (mBad.find()) {
            String sBad = mBad.group();
            nBad += Integer.parseInt(sBad.substring(0, sBad.indexOf("bad") - 1));
        }
        int nWarnings = 0;
        while (mWarnings.find()) {
            String sWarnings = mWarnings.group();
            nWarnings += Integer.parseInt(sWarnings.substring(0, sWarnings.indexOf("warnings") - 1));
        }
        int nUnknown = 0;
        while (mUnknown.find()) {
            String sUnknown = mUnknown.group();
            nUnknown += Integer.parseInt(sUnknown.substring(0, sUnknown.indexOf("unknown") - 1));
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
        /*// perform XSLT transformation
         String result = null;
         try {
         XSLTransformer xt = new XSLTransformer();
         //xt.setParameter("WEBSERVICE_NAME", SERVICE_NAME);
         xt.setParameter("notes", nOK);
         xt.setParameter("criticals", nBad);
         xt.setParameter("warnings", nWarnings);
         //xt.setParameter("Unknown", nUnknown);
         result = xt.transform(reportStatistics, xsl);
         } catch (TransformerException ex) {
         Logger.getLogger(SERVICE_NAME).log(Level.SEVERE, null, ex);
         }*/
        PrintWriter htmlOut = new PrintWriter(new FileOutputStream(reportStatisticsPath));
        htmlOut.print(reportStatistics);
        htmlOut.close();
        return stats;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            stats.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
