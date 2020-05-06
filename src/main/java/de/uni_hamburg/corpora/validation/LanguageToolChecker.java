/**
 * @file ExbErrorChecker.java
 *
 * A command-line tool / non-graphical interface
 * for checking errors in exmaralda's EXB files.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.BasicTranscriptionData;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;

import org.languagetool.rules.RuleMatch;
import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;

/**
 * A grammar and spelling error checker for EXB tiers mainly.
 */
public class LanguageToolChecker extends Checker implements CorpusFunction {

    static String filename;
    BasicTranscription bt;
    static BasicTranscriptionData btd;
    ValidatorSettings settings;
    List<String> conventions = new ArrayList<String>();
    List<String> problems = new ArrayList<String>();
    String tierToCheck = "fg";
    String language = "de";
    JLanguageTool langTool;

    public LanguageToolChecker() {
    }

    public Report check(File f) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(f);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, function, cd, "Unknown parser error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parser error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown read error");
        }
        return stats;
    }

    public Report exceptionalCheck(File f)
            throws SAXException, IOException, ParserConfigurationException {
        // XXX: get languages and good tiers somehow
        JLanguageTool langTool;
        if (language.equals("de")) {
            langTool = new JLanguageTool(new GermanyGerman());
        } else {
            Report report = new Report();
            report.addCritical(function, "Missing languagetool for language "
                    + language);
            return report;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        NodeList tiers = doc.getElementsByTagName("tier");
        Report stats = new Report();
        for (int k = 0; k < tiers.getLength(); k++) {
            Element tier = (Element) tiers.item(k);
            if (!tier.getAttribute("category").equals(tierToCheck)) {
                continue;
            }
            NodeList events = tier.getElementsByTagName("event");
            for (int i = 0; i < events.getLength(); i++) {
                Element event = (Element) events.item(i);
                NodeList eventTexts = event.getChildNodes();
                for (int j = 0; j < eventTexts.getLength(); j++) {
                    Node maybeText = eventTexts.item(j);
                    if (maybeText.getNodeType() != Node.TEXT_NODE) {
                        if (maybeText.getNodeType() == Node.ELEMENT_NODE
                                && maybeText.getNodeName().equals("ud-information")) {
                            // XXX: ud-information is weird I'll just skip it...
                            continue;
                        }
                        System.err.println("This is not a text node: "
                                + maybeText);
                        continue;
                    }
                    Text eventText = (Text) maybeText;
                    String text = eventText.getWholeText();
                    List<RuleMatch> matches = langTool.check(text);
                    for (RuleMatch match : matches) {
                        stats.addWarning(function,
                                "Potential error at characters "
                                + match.getFromPos() + "-" + match.getToPos() + ": "
                                + match.getMessage() + ": \""
                                + text.substring(match.getFromPos(),
                                        match.getToPos()) + "\" ",
                                "Suggested correction(s): "
                                + match.getSuggestedReplacements());
                    }
                }
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("LanguageToolChecker",
                "Checks Exmaralda .exb file annotations for spelling and "
                + "grammar errors", "Blah");
        // XXX: the option version is quite useless unless for quick checks
        List<Option> ltOptions = new ArrayList<Option>();
        ltOptions.add(new Option("l", "language", true, "use language"));
        ltOptions.add(new Option("t", "tier", true, "check tier"));
        CommandLine cmd = settings.handleCommandLine(args, ltOptions);
        if (cmd == null) {
            System.exit(0);
        }
        if (cmd.hasOption("language")) {
            language = cmd.getOptionValue("language");
            if (!language.equals("de")) {
                System.err.println("Language " + language + " is not supported"
                );
                System.exit(1);
            }
        } else {
            System.err.println("Language defaulted to German (de)");
            language = "de";
        }
        if (cmd.hasOption("tier")) {
            tierToCheck = cmd.getOptionValue("tier");
        } else {
            System.err.println("Use --tier= to select a tier");
            System.exit(1);
        }
        if (settings.isVerbose()) {
            System.out.println("Checking exb files with languagetool");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            stats = check(f);
        }
        return stats;
    }

    public static void main(String[] args) {
        LanguageToolChecker checker = new LanguageToolChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
        for (String arg : args) {
            if (arg.equals("-v") || arg.equals("--verbose")) {
                System.out.println(stats.getFullReports());
            }
        }
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
            stats = exceptionalCheck(cd);
        } catch (SAXException saxe) {
            saxe.printStackTrace();
        } catch (JexmaraldaException je) {
            je.printStackTrace();
        } catch (IOException ex) {
            stats.addException(ex, function, cd, "Unknown read error");
        } catch (ParserConfigurationException ex) {
            stats.addException(ex, function, cd, "Unknown read error");
        }
        return stats;
    }

    /**
     * Main feature of the class: Checks Exmaralda .exb file for segmentation
     * problems.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException {
        Report stats = new Report();
        btd = new BasicTranscriptionData(cd.getURL());
        if (language.equals("de")) {
            langTool = new JLanguageTool(new GermanyGerman());
            System.out.println("Language set to German");
        } else if (language.equals("en")) {
            //needs to be English!
            langTool = new JLanguageTool(new GermanyGerman());
        } else if (language.equals("ru")) {
            //needs to be Russian!
            langTool = new JLanguageTool(new GermanyGerman());
        } else {
            Report report = new Report();
            report.addCritical(function, cd, "Missing languagetool for language "
                    + language);
            return stats;
        }
        Document doc = TypeConverter.JdomDocument2W3cDocument(btd.getJdom());
        NodeList tiers = doc.getElementsByTagName("tier");
        List<RuleMatch> matches = new ArrayList<RuleMatch>();
        int count = 0;
        for (int k = 0; k < tiers.getLength(); k++) {
            Element tier = (Element) tiers.item(k);
            if (!tier.getAttribute("category").equals(tierToCheck)) {
                continue;
            }
            NodeList events = tier.getElementsByTagName("event");
            for (int i = 0; i < events.getLength(); i++) {
                Element event = (Element) events.item(i);
                NodeList eventTexts = event.getChildNodes();
                for (int j = 0; j < eventTexts.getLength(); j++) {
                    Node maybeText = eventTexts.item(j);
                    if (maybeText.getNodeType() != Node.TEXT_NODE) {
                        if (maybeText.getNodeType() == Node.ELEMENT_NODE
                                && maybeText.getNodeName().equals("ud-information")) {
                            // XXX: ud-information is weird I'll just skip it...
                            continue;
                        }
                        System.err.println("This is not a text node: "
                                + maybeText);
                        continue;
                    }
                    Text eventText = (Text) maybeText;
                    String text = eventText.getWholeText();
                    matches = langTool.check(text);
                    for (RuleMatch match : matches) {
                        String message = "Potential error at characters "
                                + match.getFromPos() + "-" + match.getToPos() + ": "
                                + match.getMessage() + ": \""
                                + text.substring(match.getFromPos(),
                                        match.getToPos()) + "\" "
                                + "Suggested correction(s): "
                                + match.getSuggestedReplacements();
                        stats.addWarning(function, cd, message
                        );
//                        System.out.println("Potential error at characters " + 
//                                match.getFromPos() + "-" + match.getToPos() + ": " +
//                                match.getMessage() + ": \"" +
//                                text.substring(match.getFromPos(),
//                                               match.getToPos()) + "\" " +
//                                "Suggested correction(s): " +
//                                match.getSuggestedReplacements());
                        //add ExmaError tierID eventID
                        exmaError.addError(function, cd.getURL().getFile(), tier.getAttribute("id"), event.getAttribute("start"), false, message);
                    }
                    if (!matches.isEmpty()) {
                        count++;
                    }
                }

            }
        }
        if (count==0) {
            stats.addCorrect(function, cd, "No spelling errors found.");
        }
        return stats;
    }

    /**
     * No fix is applicable for this feature.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(function, cd,
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
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "unknown class not found error");
        }
        return IsUsableFor;
    }

    @Override
    public String getDescription() {
        String description = "This class takes a CorpusDataObject that is an Exb, "
                + "checks if there are spell or grammar errors in German, English or Russian using LnaguageTool and"
                + " returns the errors in the Report and in the ExmaErrors.";
        return description;
    }

    public void setLanguage(String lang) {
        language = lang;
    }

    public void setTierToCheck(String ttc) {
        tierToCheck = ttc;
    }

    @Override
    public Report check(Corpus c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report function(CorpusData cd, Boolean fix) throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
