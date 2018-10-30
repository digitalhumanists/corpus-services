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

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import org.languagetool.rules.RuleMatch;
import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;

/**
 * A grammar and spelling error checker for EXB tiers mainly.
 */
public class LanguageToolChecker implements CommandLineable  {

    BasicTranscription bt;
    ValidatorSettings settings;
    List<String> conventions = new ArrayList<String>();
    List<String> problems = new ArrayList<String>();
    String tierToCheck;
    String language;

    final String LANGUAGETOOL = "languagetool";

    private void tryLoadBasicTranscription(String filename)
        throws SAXException, JexmaraldaException {
        if (bt == null) {
            bt = new BasicTranscription(filename);
        }
    }

    public Report check(File f) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(f);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, "Unknown parser error");
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parser error");
        } catch (IOException ioe) {
            stats.addException(ioe, "Unknown read error");
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
            report.addCritical(LANGUAGETOOL, "Missing languagetool for language " +
                    language);
            return report;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        NodeList tiers = doc.getElementsByTagName("tier");
        Report stats = new Report();
        for (int k = 0; k < tiers.getLength(); k++) {
            Element tier = (Element)tiers.item(k);
            if (!tier.getAttribute("category").equals(tierToCheck)) {
                continue;
            }
            NodeList events = tier.getElementsByTagName("event");
            for (int i = 0; i < events.getLength(); i++) {
                Element event = (Element)events.item(i);
                NodeList eventTexts = event.getChildNodes();
                for (int j = 0; j < eventTexts.getLength(); j++) {
                    Node maybeText = eventTexts.item(j);
                    if (maybeText.getNodeType() != Node.TEXT_NODE) {
                        if (maybeText.getNodeType() == Node.ELEMENT_NODE &&
                                maybeText.getNodeName().equals("ud-information")) {
                            // XXX: ud-information is weird I'll just skip it...
                            continue;
                        }
                        System.err.println("This is not a text node: " +
                                maybeText);
                        continue;
                    }
                    Text eventText = (Text) maybeText;
                    String text = eventText.getWholeText();
                    List<RuleMatch> matches = langTool.check(text);
                    for (RuleMatch match : matches) {
                        stats.addWarning(LANGUAGETOOL,
                                "Potential error at characters " +
                                match.getFromPos() + "-" + match.getToPos() + ": " +
                                match.getMessage() + ": \"" +
                                text.substring(match.getFromPos(),
                                               match.getToPos()) + "\" ",
                                "Suggested correction(s): " +
                                match.getSuggestedReplacements());
                    }
                }
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("LanguageToolChecker",
                "Checks Exmaralda .exb file annotations for spelling and " +
                "grammar errors", "Blah");
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
        for (String arg :  args) {
            if (arg.equals("-v") || arg.equals("--verbose")) {
                System.out.println(stats.getFullReports());
            }
        }
    }
}
