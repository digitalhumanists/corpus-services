/**
 * @file ExbErrorChecker.java
 *
 * A command-line tool / non-graphical interface for checking errors in
 * exmaralda's EXB files.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CommandLineable;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jdom.JDOMException;

/**
 * A command-line tool for checking EXB files.
 */
public class ExbPatternChecker extends Checker implements CommandLineable, CorpusFunction {

    BasicTranscription bt;
    ValidatorSettings settings;
    List<String> conventions = new ArrayList<String>();
    List<String> problems = new ArrayList<String>();
    int noOfCalls = 0;
    final String EXB_PATTERNS = "exb-patterns";

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
        // XXX: get conventions from settings somehow
        List<Pattern> correctPatterns = new ArrayList<Pattern>();
        for (String convention : conventions) {
            correctPatterns.add(Pattern.compile(convention));
        }
        List<Pattern> errorPatterns = new ArrayList<Pattern>();
        for (String problem : problems) {
            errorPatterns.add(Pattern.compile(problem));
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        NodeList events = doc.getElementsByTagName("event");
        Report stats = new Report();
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
                int k = 0;
                for (Pattern pattern : correctPatterns) {
                    Matcher matcher = pattern.matcher(text);
                    if (!matcher.matches()) {
                        stats.addCritical(EXB_PATTERNS,
                                "Text: " + text + " does not fit to the "
                                + "conventions given.", "Expression was: "
                                + conventions.get(k));
                    }
                }
                k = 0;
                for (Pattern pattern : errorPatterns) {
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.matches()) {
                        stats.addCritical(EXB_PATTERNS,
                                "Text: " + text + " does not fit to the "
                                + "conventions given.", "Expression was: "
                                + errorPatterns.get(k));
                    }
                }
            }
        }
        return stats;
    }

    public Report doMain(String[] args) {
        settings = new ValidatorSettings("ExbPatternChecker",
                "Checks Exmaralda .exb file annotations for conventions using "
                + "patterns", "If input is a directory, performs recursive check "
                + "from that directory, otherwise checks input file\n"
                + "Patterns are given as regular expressions to match against "
                + "(regular expression is compiled with java.util.regex)");
        // XXX: the option version is quite useless unless for quick checks
        List<Option> patternOptions = new ArrayList<Option>();
        patternOptions.add(new Option("a", "accept", true, "add an acceptable "
                + "pattern"));
        patternOptions.add(new Option("d", "disallow", true, "add an illegal "
                + "pattern"));
        CommandLine cmd = settings.handleCommandLine(args, patternOptions);
        if (cmd == null) {
            System.exit(0);
        }
        if (!cmd.hasOption("accept") && !cmd.hasOption("disallow")) {
            System.err.println("Nothing to accept or disallow, "
                    + "skipping checks.");
            System.exit(0);
        }
        if (cmd.hasOption("accept")) {
            conventions.add(cmd.getOptionValue("accept"));
        }
        if (cmd.hasOption("disallow")) {
            problems.add(cmd.getOptionValue("disallow"));
        }
        if (settings.isVerbose()) {
            System.out.println("Checking exb files for unconventional "
                    + "annotations...");
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
        ExbPatternChecker checker = new ExbPatternChecker();
        Report stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
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
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, "Unknown parser error");
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parser error");
        } catch (IOException ioe) {
            stats.addException(ioe, "Unknown read error");
        }
        return stats;
    }

    /**
     * Main feature of the class: Checks Exmaralda .exb file annotations for
     * conventions using patterns.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException {
        if (noOfCalls == 0) {     // only in the first call
            settings = new ValidatorSettings("ExbPatternChecker",
                    "Checks Exmaralda .exb file annotations for conventions using "
                    + "patterns", "If input is a directory, performs recursive check "
                    + "from that directory, otherwise checks input file\n"
                    + "Patterns are given as regular expressions to match against "
                    + "(regular expression is compiled with java.util.regex)");
            // XXX: the option version is quite useless unless for quick checks
            List<Option> patternOptions = new ArrayList<Option>();
            patternOptions.add(new Option("a", "accept", true, "add an acceptable "
                    + "pattern"));
            patternOptions.add(new Option("d", "disallow", true, "add an illegal "
                    + "pattern"));
            CommandLine cmd = null;
            // needs arguments to be handled 
            //cmd = settings.handleCommandLine(args, patternOptions);
            if (cmd == null) {
                System.exit(0);
            }
            if (!cmd.hasOption("accept") && !cmd.hasOption("disallow")) {
                System.err.println("Nothing to accept or disallow, "
                        + "skipping checks.");
                System.exit(0);
            }
            if (cmd.hasOption("accept")) {
                conventions.add(cmd.getOptionValue("accept"));
            }
            if (cmd.hasOption("disallow")) {
                problems.add(cmd.getOptionValue("disallow"));
            }
            if (settings.isVerbose()) {
                System.out.println("Checking exb files for unconventional "
                        + "annotations...");
            }
            conventions.add("add an acceptable pattern");
            problems.add("add an illegal pattern");
            if (settings.isVerbose()) {
                System.out.println("Checking exb files for unconventional "
                        + "annotations...");
            }
        }
        noOfCalls++;
        // XXX: get conventions from settings somehow
        List<Pattern> correctPatterns = new ArrayList<Pattern>();
        for (String convention : conventions) {
            correctPatterns.add(Pattern.compile(convention));
        }
        List<Pattern> errorPatterns = new ArrayList<Pattern>();
        for (String problem : problems) {
            errorPatterns.add(Pattern.compile(problem));
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        NodeList events = doc.getElementsByTagName("event");
        Report stats = new Report();
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
                int k = 0;
                for (Pattern pattern : correctPatterns) {
                    Matcher matcher = pattern.matcher(text);
                    if (!matcher.matches()) {
                        stats.addCritical(EXB_PATTERNS,
                                "Text: " + text + " does not fit to the "
                                + "conventions given.", "Expression was: "
                                + conventions.get(k));
                    }
                }
                k = 0;
                for (Pattern pattern : errorPatterns) {
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.matches()) {
                        stats.addCritical(EXB_PATTERNS,
                                "Text: " + text + " does not fit to the "
                                + "conventions given.", "Expression was: "
                                + errorPatterns.get(k));
                    }
                }
            }
        }
        return stats;
    }

    /**
     * No fix is applicable for this feature.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(EXB_PATTERNS,
                "Automatic fix is not yet supported.");
        return report;
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ExbPatternChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
}