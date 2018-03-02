/*
 *   A command-line interface for checking corpus files.
 *
 *  @author Anne Ferger
 *  @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CommandLineable;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
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
public abstract class Checker implements CorpusFunction{

    //I will keep the settings for now, so they can stay as they are for the Moment 
    //and we know where to refactor when we change them 
    ValidatorSettings settings;
    CorpusData cd;
    
    public Checker(){    
    }
    
    public Report check(CorpusData cd){  
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (SAXException saxe) {
            stats.addException(saxe, "Unknown parsing error");
        } catch (JexmaraldaException je) {
            stats.addException(je, "Unknown parsing error");
        }
        return stats;
    }

   public Report exceptionalCheck(CorpusData cd) throws SAXException, JexmaraldaException{
                Report report = new Report();
                return report;
            }

   public Report exceptionalCheck(CorpusData cd, CorpusData cd2) throws SAXException, JexmaraldaException, IOException, JDOMException{
                Report report = new Report();
                return report;
            }

    public Report doMain(String[] args){
    
        settings = new ValidatorSettings("name",
                "what", "fix");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("");
        }
        Report stats = new Report();
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            stats = check(cd);
        }
        
        
        
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
            stats = fix(f);
            if (settings.isVerbose()) {
                System.out.println(stats.getFullReports());
            } else {
                System.out.println(stats.getSummaryLines());
            }
        }
        
        return stats;
    }

    @Override
    public Collection<CorpusData> IsUsableFor() {
        return getIsUsableFor();       
    }

    @Override
    public Report execute(CorpusData cd) {
       Report report = check(cd);
       return report;
    }
    

  //Wenn es keine automatische Möglichkeit zum
  //fixen gibt, dann muss Erklärung in die ErrorMeldung

    /**
     *
     * @param cd
     */
  public abstract void fix(CorpusData cd);

  public abstract Collection<CorpusData> getIsUsableFor();

    public Report fix(File fileToBeFixed) {
        Report stats = new Report();
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

    public abstract Report exceptionalFix(File fileToBeFixed) throws
            SAXException, JDOMException, IOException, JexmaraldaException;
    
}
