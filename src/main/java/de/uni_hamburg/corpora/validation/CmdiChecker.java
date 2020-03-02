/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for cmdi errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.cli.Option;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;

/**
 * A class that can load cmdi data and check for potential problems with HZSK
 * repository depositing.
 */
public class CmdiChecker extends Checker implements CorpusFunction {

    ValidatorSettings settings;
    final String CMDI_MISC = "cmdi-misc";
    String cmdiLoc = "";

    /**
     * Check for existence of files in a cmdi file.
     *
     * @return true, if all files were found, false otherwise
     */
    public Report check(String data) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(data);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, cmdiLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, cmdiLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, cmdiLoc + ": Unknown file reading error");
        }
        return stats;
    }

    private boolean isUrlHandleOrHzsk(String url) {
        if ((url.startsWith("http://hdl.handle.net/11022/")) ||
                (url.startsWith("https://corpora.uni-hamburg.de/repository/")) ||
                (url.startsWith("http://annis.corpora.uni-hamburg.de"))) {
            return true;
        } else {
            return false;
        }
    }

    private Report exceptionalCheck(String data)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(data));
        NodeList rps = doc.getElementsByTagName("ResourceProxy");
        Report stats = new Report();
        boolean hasLandingPage = false;
        for (int i = 0; i < rps.getLength(); i++) {
            Element rpe = (Element) rps.item(i);
            NodeList restypes = rpe.getElementsByTagName("ResourceType");
            Element restype = (Element) restypes.item(0);
            if (restype.getTextContent().equals("LandingPage")) {
                hasLandingPage = true;
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                        + "Good resource type LandingPage");
            } else if (restype.getTextContent().equals("Resource")) {
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                    "Good resource type Resource");
            } else if (restype.getTextContent().equals("SearchPage")) {
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                    "Good resource type SearchPage");
            } else if (restype.getTextContent().equals("SearchService")) {
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                    "Good resource type SearchService");
            } else if (restype.getTextContent().equals("Metadata")) {
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": " +
                    "Good resource type Metadata");
            } else {
                stats.addWarning(CMDI_MISC, cmdiLoc + ": "
                        + "Unrecognised resource type "
                        + restype.getTextContent());
            }
            NodeList resrefs = rpe.getElementsByTagName("ResourceRef");
            Element resref = (Element) resrefs.item(0);
            String url = resref.getTextContent();
            if (!isUrlHandleOrHzsk(url)) {
                stats.addCritical(CMDI_MISC, cmdiLoc + ": "
                        + "Invalid URL for reesource proxy:"
                        + url,
                        "URLs should start with http://hdl.handle.net... or "
                        + "https://corpora.uni-hamburg.de/repository/...");
            } else {
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                        + "Good resource proxy URL " + url);
            }
        }
        if (!hasLandingPage) {
            stats.addCritical(CMDI_MISC, cmdiLoc + ": "
                    + "Missing landing page");
        } else {
            stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                    + "Good landing page found");
        }
        NodeList gis = doc.getElementsByTagName("GeneralInfo");
        for (int i = 0; i < gis.getLength(); i++) {
            Node ginode = gis.item(i);
            if (ginode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element gi = (Element) ginode;
            NodeList childs = gi.getChildNodes();
            boolean englishTitle = false;
            boolean englishDesc = false;
            boolean legalOwner = false;
            boolean pidFound = false;
            for (int j = 0; j < childs.getLength(); j++) {
                Node n = childs.item(j);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element e = (Element) n;
                if (e.getTagName().equals("PID")) {
                    if (!isUrlHandleOrHzsk(e.getTextContent())) {
                        stats.addCritical(CMDI_MISC, cmdiLoc + ": "
                                + "Invalid URL for PID:"
                                + e.getTextContent(),
                                "URLs should start with "
                                + "http://hdl.handle.net... or "
                                + "https://corpora.uni-hamburg.de/repository/...");
                    } else {
                        stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                                + "Good PID URL: "
                                + e.getTextContent());
                    }
                    pidFound = true;
                } else if (e.getTagName().equals("Description")) {
                    if (e.getAttribute("xml:lang").equals("en")) {
                        englishDesc = true;
                        stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                                + "English Description present");
                    }
                } else if (e.getTagName().equals("Title")) {
                    if (e.getAttribute("xml:lang").equals("en")) {
                        englishTitle = true;
                        stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                                + "English title present");
                    }
                } else if (e.getTagName().equals("LegalOwner")) {
                    legalOwner = true;
                    stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                            + "LegalOwner present");
                } else {
                    System.out.println("DEBUG: GeneralInfo/" + e.getTagName());
                    // pass
                }
            }
            if (!englishTitle) {
                stats.addWarning(CMDI_MISC, cmdiLoc + ": "
                        + "English title missing from General Info "
                        + "(needed by FCS for example)");
            }
            if (!englishDesc) {
                stats.addWarning(CMDI_MISC, cmdiLoc + ": "
                        + "English Description missing from General Info "
                        + "(needed by FCS for example)");
            }
            if (!pidFound) {
                stats.addCritical(CMDI_MISC, cmdiLoc + ": "
                        + "PID missing");
            }
        }
        NodeList cis = doc.getElementsByTagName("CorpusInfo");
        for (int i = 0; i < cis.getLength(); i++) {
            Node cinode = cis.item(i);
            if (cinode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element ci = (Element) cis.item(i);
            checkCorpusInfo(ci, stats);
        }
        return stats;
    }

    private void checkCorpusInfo(Element ci, Report stats) {
        NodeList childs = ci.getChildNodes();
        boolean corpusType = false;
        boolean genre = false;
        boolean modality = false;
        boolean annotationTypes = false;
        boolean timeCoverage = false;
        for (int i = 0; i < childs.getLength(); i++) {
            Node n = childs.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element) n;
            if (e.getTagName().equals("CorpusContext")) {
                NodeList cts = e.getElementsByTagName("CorpusType");
                if (cts.getLength() != 0) {
                    corpusType = true;
                }
            } else if (e.getTagName().equals("SubjectLanguages")) {
                checkSubjectLanguages(e, stats);
            } else if (e.getTagName().equals("Coverage")) {
                NodeList tcs = e.getElementsByTagName("TimeCoverage");
                if (tcs.getLength() != 0) {
                    timeCoverage = true;
                }
                checkCoverage(e, stats);
            } else if (e.getTagName().equals("Content")) {
                NodeList genres = e.getElementsByTagName("Genre");
                if (genres.getLength() != 0) {
                    genre = true;
                }
                NodeList modalities = e.getElementsByTagName("Modalities");
                if (modalities.getLength() != 0) {
                    modality = true;
                }
            } else {
                //
                System.out.println("DEBUG: CorpusInfo/" + e.getTagName());
            }
        }
        if (!corpusType) {
            stats.addCritical(CMDI_MISC, cmdiLoc + ": "
                    + "Corpus type is needed for repo web pages");
        } else {
            stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                    + "Corpus type included");
        }
        if (!genre) {
            stats.addCritical(CMDI_MISC, cmdiLoc + ": "
                    + "Genre is needed for repo web pages");
        } else {
            stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                    + "Genre included");
        }
        if (!modality) {
            stats.addCritical(CMDI_MISC, cmdiLoc + ": "
                    + "Modality is needed for repo web pages");
        } else {
            stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                    + "modality included");
        }
        if (!timeCoverage) {
            stats.addWarning(CMDI_MISC, cmdiLoc + ": "
                    + "time coverage is missing (recommended for VLO)");
        }
    }

    private void checkCoverage(Element coverage, Report stats) {
        NodeList timeCoverages = coverage.getElementsByTagName("TimeCoverage");
        for (int i = 0; i < timeCoverages.getLength(); i++) {
            Node n = timeCoverages.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element) n;
            String tc = e.getTextContent();
            if (tc.matches("[0-9]+/[0-9]+")) {
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                        + "Good time coverage");
            } else {
                stats.addCritical(CMDI_MISC, cmdiLoc + ": "
                        + "TimeCoverage should be YYYY/YYYY for VLO");
            }
        }

    }

    private void checkSubjectLanguages(Element sls, Report stats) {
        NodeList langs = sls.getElementsByTagName("Language");
        for (int i = 0; i < langs.getLength(); i++) {
            Node n = langs.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element) n;
            NodeList childs = e.getElementsByTagName("LanguageName");
            boolean engFound = false;
            for (int j = 0; j < childs.getLength(); j++) {
                Element lang = (Element) childs.item(j);
                if (lang.getAttribute("xml:lang").equals("eng")) {
                    engFound = true;
                }
            }
            if (!engFound) {
                stats.addCritical(CMDI_MISC, cmdiLoc + ": "
                        + "Each subject language must have @xml:lang eng "
                        + "filled in");
            } else {
                stats.addCorrect(CMDI_MISC, cmdiLoc + ": "
                        + "Goog language data");
            }
        }
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("CmdiChecker",
                "Checks CLARIN .cmdi file for various common practices ",
                "If input is a directory, performs recursive "
                + "check from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking CMDI files for metadata...");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            try {
                cmdiLoc = f.getName();
                String s = TypeConverter.InputStream2String(new FileInputStream(f));
                stats = check(s);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return stats;
    }

    public static void main(String[] args) {
        CmdiChecker checker = new CmdiChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getWarningReports());
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            File f;
            if (cd.getURL().toString().contains("file:/")) {
                f = new File(cd.getURL().toString().substring(cd.getURL().toString().indexOf("file:/") + 6));
            } else {
                f = new File(cd.getURL().toString());
            }
            cmdiLoc = f.getName();
            String s = TypeConverter.InputStream2String(new FileInputStream(f));
            stats = exceptionalCheck(s);  
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, cmdiLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, cmdiLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, cmdiLoc + ": Unknown file reading error");
        }
        return stats;
    }

    /**
     * No fix is applicable for this feature.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(CMDI_MISC,
                "Automatic fix is not yet supported.");
        return report;
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.CmdiData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, " usable class not found");
        }
        return IsUsableFor;
    }

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class loads cmdi data and check for potential "
                + "problems with HZSK repository depositing.";
        return description;
    }

}
