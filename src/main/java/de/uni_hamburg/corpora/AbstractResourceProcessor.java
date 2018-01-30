/*
 * A command-line interface for checking corpus files.
 *
 * @author Anne Ferger
 * @author HZSK
 *
 */
package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.validation.ValidatorSettings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.Option;
import org.exmaralda.partitureditor.jexmaralda.BasicBody;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;

/**
 * This Class reads a File and rewrites it, outputting errors/messages if needed
 *
 * @author fsnv625
 */
public abstract class AbstractResourceProcessor {

    File fileToBeFixed;
    ValidatorSettings settings;

    public StatisticsReport fix(File fileToBeFixed) {
        StatisticsReport stats = new StatisticsReport();
        try {
            stats = exceptionalFix(fileToBeFixed);
        } catch (JexmaraldaException je) {
            stats.addException(je, "Unknown parsing error");
        } catch (JDOMException jdome) {
            stats.addException(jdome, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, "File reading error");
        }
        return stats;
    }

    public abstract StatisticsReport exceptionalFix(File fileToBeFixed) throws
            SAXException, JDOMException, IOException, JexmaraldaException;

     public void doMain(String[] args) {
        settings = new ValidatorSettings("name",
                "what", "fix");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("");
        }
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            StatisticsReport stats = fix(f);
            if (settings.isVerbose()) {
                System.out.println(stats.getFullReports());
            } else {
                System.out.println(stats.getSummaryLines());
            }
        }

    }
}
