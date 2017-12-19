/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;

/**
 * A class that checks all file names in a directory to be deposited in HZSK
 * repository.
 */
public class FilenameChecker implements CommandLineable {

    Pattern acceptable;
    Pattern unacceptable;
    ValidatorSettings settings;

    final String FILENAME_CONVENTIONS = "filename-conventions";

    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    public StatisticsReport check(File rootdir) {
        StatisticsReport stats = new StatisticsReport();
        try {
            stats = exceptionalCheck(rootdir);
        } catch(IOException ioe) {
            stats.addException(ioe, "Unknown reading error");
        }
        return stats;
    }


    private StatisticsReport exceptionalCheck(File rootdir)
            throws IOException {
        StatisticsReport stats = new StatisticsReport();
        return recursiveCheck(rootdir, stats);
    }

    private StatisticsReport recursiveCheck(File f,
            StatisticsReport stats) throws IOException {
        String filename = f.getName();
        Matcher matchAccepting = acceptable.matcher(filename);
        boolean allesGut = true;
        if (!matchAccepting.matches()) {
            stats.addWarning(FILENAME_CONVENTIONS,
                        filename + " does not follow "
                        + "filename conventions for HZSK corpora");
            allesGut = false;
        }
        Matcher matchUnaccepting = unacceptable.matcher(filename);
        if (matchUnaccepting.find()) {
            stats.addWarning(FILENAME_CONVENTIONS,
                        filename + " contains " +
                        "characters that may break in HZSK repository");
            allesGut = false;
        }

        if (allesGut) {
            stats.addCorrect(FILENAME_CONVENTIONS,
                    filename + " is OK by HZSK standards.");
        }
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (File g : files) {
                stats = recursiveCheck(g, stats);
            }
        }
        return stats;
    }

    public StatisticsReport doMain(String[] args) {
        settings = new ValidatorSettings("FileCoverageChecker",
                "Checks Exmaralda .coma file against directory, to find " +
                "undocumented files",
                "If input is a directory, performs recursive check " +
                "from that directory, otherwise checks input file");
        List<Option> patternOptions = new ArrayList<Option>();
        patternOptions.add(new Option("a", "accept", true, "add an acceptable "
                    + "pattern"));
        patternOptions.add(new Option("d", "disallow", true, "add an illegal "
                    + "pattern"));
        CommandLine cmd = settings.handleCommandLine(args, patternOptions);
        if (cmd == null) {
            System.exit(0);
        }
        if (cmd.hasOption("accept")) {
            acceptable = Pattern.compile(cmd.getOptionValue("accept"));
        } else {
            acceptable = Pattern.compile("^[A-Za-z0-9_.-]*$");
        }
        if (cmd.hasOption("disallow")) {
            unacceptable = Pattern.compile(cmd.getOptionValue("disallow"));
        } else {
            unacceptable = Pattern.compile("[ üäöÜÄÖ]");
        }
        if (settings.isVerbose()) {
            System.out.println("Checking coma file against directory...");
        }
        StatisticsReport stats = new StatisticsReport();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            stats = check(f);
        }
        return stats;
    }

    public static void main(String[] args) {
        FilenameChecker checker = new FilenameChecker();
        StatisticsReport stats = checker.doMain(args);
        System.out.println(stats.getSummaryLines());
        System.out.println(stats.getErrorReports());
    }

}
