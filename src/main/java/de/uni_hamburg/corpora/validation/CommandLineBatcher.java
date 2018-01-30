/**
 * @file CommandLinve
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;


import de.uni_hamburg.corpora.StatisticsReport;
import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.TierFormatTable;
import org.exmaralda.partitureditor.jexmaralda.BasicBody;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.ini4j.Ini;


/**
 * A simple command-line interface for batch-running multiple tests.
 */
public class CommandLineBatcher {


    private static Collection<File> getFilesRecursively(File d) {
        Set<String> recursionBlackList = new HashSet<String>();
        recursionBlackList.add(".git");
        Set<File> recursed = new HashSet<File>();
        Stack<File> dirs = new Stack();
        dirs.add(d);
        while (!dirs.empty()) {
            File[] files = dirs.pop().listFiles();
            for (File f : files) {
                if (recursionBlackList.contains(f.getName())) {
                    continue;
                } else if (f.isDirectory()) {
                    dirs.add(f);
                } else {
                    recursed.add(f);
                }
            }
        }
        return recursed;
    }

    public static void main(String[] args) {
        CommandLine cmd;
        Options parameters = new Options();
        HelpFormatter formatter = new HelpFormatter();
        String name = "CommandLineBatcher";
        String header = "performs number of corpus checks with same options";
        String footer = "input must be a directory, that should contain a " +
            "single coma file";
        // parse
        try {
            parameters.addOption("h", "help", false,
                "print this help screen");
            parameters.addOption("v", "verbose", false,
                "print verbosely while processing");
            parameters.addOption("o", "output", true,
                    "fix problems where possible, write output to given file");
            parameters.addOption("i", "input", true,
                    "input directory to validate");
            parameters.addOption("b", "base-dir", true,
                    "base directory for solving relative file references etc.");
            parameters.addOption("d", "data-dir", true,
                    "data directory for solving relative file references etc.");
            parameters.addOption("X", "in-place", false,
                    "fix problems in place, replacing original file");
            Option testOption = new Option("c", "c desc");
            testOption.setArgs(Option.UNLIMITED_VALUES);
            parameters.addOption(testOption);
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(parameters, args);
        } catch (ParseException pe) {
            formatter.printHelp(name, header, parameters, footer, true);
            System.exit(2);
            return;
        }
        // check obligatories and conflicts
        if (!cmd.hasOption("input") &&  (cmd.getArgs().length < 1)) {
            System.err.println("No inputs given");
            formatter.printHelp(name, header, parameters, footer, true);
            System.exit(1);
        } else if (cmd.hasOption("input") && (cmd.getArgs().length > 0)) {
            System.err.println("Unrecognised parameters with --input: " +
                cmd.getArgs());
            formatter.printHelp(name, header, parameters, footer, true);
            System.exit(1);
        }
        // apply important / global parameters first
        if (cmd.hasOption("help")) {
            formatter.printHelp(name, header, parameters, footer, true);
            System.exit(0);
        }
        // create options stuff based on input stuff
        boolean verbose = false;
        if (cmd.hasOption("verbose")) {
            System.out.println("Printing long report");
            verbose = true;
        }
        // parse rest command lines any ways
        boolean replace = false;
        if (cmd.hasOption("in-place")) {
            replace = true;
        }
        File outputFile;
        if (replace) {
            if (verbose) {
                System.out.println("Making corrections in place");
            }
        } else if (cmd.hasOption("output")) {
            if (verbose) {
                System.out.println("Making corrections to " +
                        cmd.getOptionValue("output"));
            }
            outputFile = new File(cmd.getOptionValue("output"));
        }
        File inputDir;
        if (cmd.hasOption("input")) {
            if (verbose) {
                System.out.println("Reading: " +
                        cmd.getOptionValue("input"));
            }
            inputDir = new File(cmd.getOptionValue("input"));
        } else if (cmd.getArgs().length == 1) {
            if (verbose) {
                System.out.println("Reading: " + cmd.getArgs()[0]);
            }
            inputDir = new File(cmd.getArgs()[0]);
        } else {
            System.err.println("No inputs given.");
            System.exit(1);
            return;
        }

        if (!inputDir.isDirectory()) {
            System.err.println(inputDir.getName() + " is not a directory");
            System.exit(1);
        }
        // set base dir
        File baseDirectory;
        if (cmd.hasOption("base-dir")) {
            baseDirectory = new File(cmd.getOptionValue("base-dir"));
        } else {
            baseDirectory = inputDir;
        }
        File dataDirectory;
        if (cmd.hasOption("data-dir")) {
            dataDirectory = new File(cmd.getOptionValue("data-dir"));
        } else {
            dataDirectory = inputDir;
        }
        // get tests
        String[] tests;
        if (!cmd.hasOption("tests")) {
            tests = new String[]{"ComaNSLinksChecker", "ComaXsdChecker",
                  "ExbFileReferenceChecker", "ExbSegmentationChecker",
                  "ExbStructureChecker", "FileCoverageChecker",
                  "FilenameChecker"};
        } else {
            tests = cmd.getOptionValues("tests");
        }
        // recurse if necessary
        List<File> recursedFiles = new ArrayList<File>(
                getFilesRecursively(inputDir));
        List<File> comaFiles = new ArrayList<File>();
        List<File> exbFiles = new ArrayList<File>();
        List<File> otherFiles = new ArrayList<File>();
        List<File> linkedFromComa = new ArrayList<File>();
        List<File> linkedFromExb = new ArrayList<File>();
        for (File f : recursedFiles) {
            if (f.getName().matches("^.*\\.coma$")) {
                try {
                    comaFiles.add(f);
                    linkedFromComa.addAll(ComaRecurser.getNSLinksAsFiles(f));
                } catch (SAXException saxe) {
                    saxe.printStackTrace();
                    System.exit(1);
                } catch (ParserConfigurationException pce) {
                    pce.printStackTrace();
                    System.exit(1);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    System.exit(1);
                }
            } else if (f.getName().matches("^.*\\.exb$")) {
                try {
                    exbFiles.add(f);
                    linkedFromExb.addAll(
                        ExbRecurser.getReferencedFilesAsFiles(f));
                } catch (SAXException saxe) {
                    saxe.printStackTrace();
                    System.exit(1);
                } catch (ParserConfigurationException pce) {
                    pce.printStackTrace();
                    System.exit(1);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    System.exit(1);
                }
            } else {
                otherFiles.add(f);
            }
        }
        if (verbose) {
            System.out.println("Recursed files:");
            for (File f : recursedFiles) {
                System.out.println("* " + f.getName());
            }
            System.out.println("  probably coma:");
            for (File f : comaFiles) {
                System.out.println("* " + f.getName());
            }
            System.out.println("  probably exb:");
            for (File f : exbFiles) {
                System.out.println("* " + f.getName());
            }
            System.out.println("  other:");
            for (File f : otherFiles) {
                System.out.println("* " + f.getName());
            }
            System.out.println("  found in coma NSLinks:");
            for (File f : linkedFromComa) {
                System.out.println("* " + f.getName());
            }
            System.out.println("  found in exb referenced-file:");
            for (File f : linkedFromExb) {
                System.out.println("* " + f.getName());
            }

        }
        List<String> baseargs = new ArrayList<String>();
        if (verbose) {
            baseargs.add("-v");
        }
        baseargs.add("-b");
        baseargs.add(baseDirectory.getAbsolutePath());
        StatisticsReport stats = new StatisticsReport();
        for (String testclass : tests) {
            // tests for input dir
            // ...
            // tests for coma fiels
            for (File f : comaFiles) {
                List<String> fargs = new ArrayList<String>(baseargs);
                fargs.add("-i");
                fargs.add(f.getAbsolutePath());
                if (verbose) {
                    System.out.print(testclass);
                    System.out.println(fargs);
                }
                if (testclass.equalsIgnoreCase("FileCoverageChecker")) {
                    stats.merge(new FileCoverageChecker().doMain(fargs.toArray(
                                    new String[0])));
                }
                else if (testclass.equalsIgnoreCase("ComaNSLinksChecker")) {
                    stats.merge(new ComaNSLinksChecker().doMain(fargs.toArray(
                                    new String[0])));
                }
                else if (testclass.equalsIgnoreCase("ComaXsdChecker")) {
                    stats.merge(new ComaXsdChecker().doMain(fargs.toArray(
                                    new String[0])));
                }
            }
            // tests for coma fiels
            for (File f : exbFiles) {
                List<String> fargs = new ArrayList<String>(baseargs);
                fargs.add("-i");
                fargs.add(f.getAbsolutePath());
                if (verbose) {
                    System.out.print(testclass);
                    System.out.println(fargs);
                }
                if (testclass.equalsIgnoreCase("ExbFileReferenceChecker")) {
                    stats.merge(new ExbFileReferenceChecker().doMain(fargs.toArray(
                                    new String[0])));
                }
                else if (testclass.equalsIgnoreCase("ExbStructureChecker")) {
                    stats.merge(new ExbStructureChecker().doMain(fargs.toArray(
                                    new String[0])));
                }
            }
        }
        if (verbose) {
            System.out.println(stats.getFullReports());
        } else {
            System.out.println(stats.getErrorReports());
            System.out.println(stats.getSummaryLines());
        }
    }


}

