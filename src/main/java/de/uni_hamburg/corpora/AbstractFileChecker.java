/*
 *   A command-line interface for checking corpus files.
 * 
 *  @author Anne Ferger
 *  @author HZSK
 */
package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.validation.CommandLineable;
import de.uni_hamburg.corpora.validation.ErrorMessage;
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
public abstract class AbstractFileChecker implements CommandLineable {

    ValidatorSettings settings;
    String fileasstring;

    public Collection<ErrorMessage> check(File f) {
        Collection<ErrorMessage> errors = new ArrayList<ErrorMessage>();
        try {
            errors = exceptionalCheck(f);
        } catch (SAXException saxe) {
            saxe.printStackTrace();
        } catch (JexmaraldaException je) {
            je.printStackTrace();
        }
        return errors;
    }

   public abstract Collection<ErrorMessage>
            exceptionalCheck(File f) throws SAXException, JexmaraldaException;
            
   public abstract Collection<ErrorMessage>
            exceptionalCheck(File f, File g) throws SAXException, JexmaraldaException, IOException, JDOMException;

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
            Collection<ErrorMessage> errors = check(f);
            for (ErrorMessage em : errors) {
                System.out.println("   - " + em);
            }
        }

    }
}
