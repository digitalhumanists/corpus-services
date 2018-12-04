/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.conversion;


import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.validation.ValidatorSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public abstract class Converter implements CorpusFunction{
    /**
     * 
     * 
     */
    
     //I will keep the settings for now, so they can stay as they are for the Moment 
    //and we know where to refactor when we change them 
    ValidatorSettings settings;
    CorpusData cd;
    Report report;
    Collection<Class<? extends CorpusData>> IsUsableFor = new ArrayList<Class<?
            extends CorpusData>>();

    public Converter() {
    }

    public Report execute(Corpus c) {
        return execute(c.getCorpusData());
    }
      
    public Report execute(CorpusData cd) {
        return execute(cd, false);
    }

    public Report execute(Collection<CorpusData> cdc) {
        return execute(cdc, false);
    }

    public Report execute(CorpusData cd, boolean fix) {
        report = new Report();
        if (fix) {
            try {
                report = fix(cd);
            } catch (JexmaraldaException je) {
                report.addException(je, "Unknown parsing error");
            } catch (JDOMException jdome) {
                report.addException(jdome, "Unknown parsing error");
            } catch (SAXException saxe) {
                report.addException(saxe, "Unknown parsing error");
            } catch (IOException ioe) {
                report.addException(ioe, "File reading error");
            }
            return report;
        } else {
            try {
                report = check(cd);
            } catch (SAXException saxe) {
                report.addException(saxe, "Unknown parsing error");
            } catch (JexmaraldaException je) {
                report.addException(je, "Unknown parsing error");
            }
            return report;
        }
    }

    public Report execute(Collection<CorpusData> cdc, boolean fix) {
        report = new Report();
        if (fix) {
            try {
                return fix(cdc);
            } catch (SAXException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JDOMException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JexmaraldaException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            }
            return report;
        } else {
            try {
                return check(cdc);
            } catch (SAXException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JexmaraldaException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JDOMException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            }
            return report;
        }
    }

    //TODO
    public abstract Report check(CorpusData cd) throws SAXException, JexmaraldaException;
    

    //TODO
    //needed for annotation panel check maybe
    //no iteration because files need to be treated differently
    public Report check(Collection<CorpusData> cdc) throws SAXException, JexmaraldaException, IOException, JDOMException {
        for (CorpusData cd: cdc){
            report.merge(check(cd));
        }
        return report;
    }

    //Wenn es keine automatische Möglichkeit zum
    //fixen gibt, dann muss Erklärung in die ErrorMeldung
    public abstract Report fix(CorpusData cd) throws
            SAXException, JDOMException, IOException, JexmaraldaException;

    //Wenn es keine automatische Möglichkeit zum
    //fixen gibt, dann muss Erklärung in die ErrorMeldung
    //also for stuff like Annotation Panel Check
    public Report fix(Collection<CorpusData> cdc) throws
            SAXException, JDOMException, IOException, JexmaraldaException{
        for (CorpusData cd: cdc){
            report.merge(fix(cd));
        }
        return report;
    }

    //TODO
    public Report doMain(String[] args) {
        settings = new ValidatorSettings("name",
                "what", "fix");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("");
        }
        report = new Report();
        //TODO
//        for (File f : settings.getInputFiles()) {
//            if (settings.isVerbose()) {
//                System.out.println(" * " + f.getName());
//            }
//            stats = check(cd);
//        }
//
//        settings = new ValidatorSettings("name",
//                "what", "fix");
//        settings.handleCommandLine(args, new ArrayList<Option>());
//        if (settings.isVerbose()) {
//            System.out.println("");
//        }
//        for (File f : settings.getInputFiles()) {
//            if (settings.isVerbose()) {
//                System.out.println(" * " + f.getName());
//            }
//            stats = fix(f);
//            if (settings.isVerbose()) {
//                System.out.println(stats.getFullReports());
//            } else {
//                System.out.println(stats.getSummaryLines());
//            }
//        }
        return report;
    }

    @Override
    public abstract Collection<Class<? extends CorpusData>> getIsUsableFor();
    
    public void setIsUsableFor(Collection<Class<? extends CorpusData>> cdc){
        for (Class cl : cdc){
        IsUsableFor.add(cl);
        }
    }

}
