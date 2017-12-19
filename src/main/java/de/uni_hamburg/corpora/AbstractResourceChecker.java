/*
 *   A command-line interface for checking corpus files.
 *
 *  @author Anne Ferger
 *  @author HZSK
 */
package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.validation.CommandLineable;
import de.uni_hamburg.corpora.validation.StatisticsReport;
import de.uni_hamburg.corpora.validation.StringChecker;
import de.uni_hamburg.corpora.validation.ValidatorSettings;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.cli.Option;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * an abstract class to be extended by additional validators or checkers This
 * Class reads a File and outputs errors but doesn't change it The commandline
 * input is the file to be checked as a string
 *
 *
 * How to also put another file as input for an check?
 *
 */
public abstract class AbstractResourceChecker implements CommandLineable {

    ValidatorSettings settings;
    String fileasstring;

    public StatisticsReport check(File f) {
        StatisticsReport stats = new StatisticsReport();
        try {
            stats = exceptionalCheck(f);
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        } catch (JexmaraldaException je) {
            stats.addException(je, "Unknown parsing error");
        }
        return stats;
    }

   public abstract StatisticsReport
            exceptionalCheck(File f) throws SAXException, JexmaraldaException;

   public abstract StatisticsReport
            exceptionalCheck(File f, File g) throws SAXException, JexmaraldaException, IOException, JDOMException;

    public StatisticsReport doMain(String[] args) {
        settings = new ValidatorSettings("name",
                "what", "fix");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("");
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
}
