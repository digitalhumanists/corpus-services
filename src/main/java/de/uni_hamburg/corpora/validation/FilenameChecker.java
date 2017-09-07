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



    /**
     * Check for existence of files in a coma file.
     *
     * @return true, if all files were found, false otherwise
     */
    public Collection<ErrorMessage> check(File rootdir) {
        Collection<ErrorMessage> errors;
        try {
            errors = exceptionalCheck(rootdir);
        } catch(IOException ioe) {
            errors = new ArrayList<ErrorMessage>();
            errors.add(new ErrorMessage(ErrorMessage.Severity.CRITICAL,
                    rootdir.getName(),
                    "Reading error", "Unknown"));
        }
        return errors;
    }


    private Collection<ErrorMessage> exceptionalCheck(File rootdir)
            throws IOException {
        Collection<ErrorMessage> errors = new ArrayList<ErrorMessage>();
        return recursiveCheck(rootdir, errors);
    }

    private Collection<ErrorMessage> recursiveCheck(File f,
            Collection<ErrorMessage> errors) throws IOException {
        String filename = f.getName();
        Matcher matchAccepting = acceptable.matcher(filename);
        if (!matchAccepting.matches()) {
            errors.add(new ErrorMessage(ErrorMessage.Severity.WARNING,
                        f.getCanonicalPath(), "This filename does not follow "
                        + "filename standards for HZSK corpora",
                        "Please check that it is formatted according to " +
                        "guidelines"));
        }
        Matcher matchUnaccepting = unacceptable.matcher(filename);
        if (matchUnaccepting.find()) {
            errors.add(new ErrorMessage(ErrorMessage.Severity.WARNING,
                        f.getCanonicalPath(), "This filename contains " +
                        "characters that may break in HZSK repository",
                        "Please check that filename follows the guidelines"));
        }
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (File g : files) {
                errors = recursiveCheck(g, errors);
            }
        }
        return errors;
    }

    public void doMain(String[] args) {
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
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            Collection<ErrorMessage> errors = check(f);
            for (ErrorMessage em : errors) {
                System.out.println("   - "  + em);
            }
        }
    }

    public static void main(String[] args) {
        FilenameChecker checker = new FilenameChecker();
        checker.doMain(args);
    }

}
