/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.validation.Checker;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.*;
/**
 * This class has a Corpus and a Corpus Function as a field and is able to run a
 * Corpus Function on a corpus in a main method.
 *
 * @author fsnv625
 */
public class CorpusMagician {

    //the whole corpus I want to run checks on
    Corpus corpus;
    //one file I want to run a check on 
    CorpusData corpusData;
    //all functions there are in the code 
    Collection<CorpusFunction> allExistingCFs;

    public CorpusMagician() {

    }

    //TODO main method
     public static void main(String[] args) {
        try {
            //one args needs to be the URL
            URL url = new URL(args[0]);
            Corpus corpus = new Corpus(url);
            CorpusMagician corpuma = new CorpusMagician();
            //one args needs to be a string for the wanted corpus function
            //how do we align/code the checks with strings?
            //CorpusFunction cf = new Checker(args[1]);
            //corpuma.runCorpusFunction(corpus, cf);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
        }
     
     
  
     }
    
    //Give it a path to a parameters file that tells you
    //which functions with which parameters should be
    //run on which files
    public void readConfig(URL url){
        //this depends on how this file will be structured
    }
    
    //this one can write a configfile with the workflow in the
    //selected format
     public void writeConfig(URL url){
        //needs to have more params
        //this depends on how this file will be structured
    }
    
    public void registerCorpusFunction(CorpusFunction cf){
        allExistingCFs.add(cf);
    }
    
    //creates a new empty corpus object
    public void initCorpus() {
        corpus = new Corpus();
    }

    //creates a corpus object from an URL (filepath or "real" url)
    public void initCorpusWithURL(URL url) {
        corpus = new Corpus(url);
    }

    //checks which functions exist in the code by checking for implementations of the corpus function interface
    //this shows that it doesn't work to just check for implementations of corpus functions
    //probably need to check for implementations of "Check", "Visualize", "Convert", "Quantify"?
    public Collection<CorpusFunction> getAllExistingCFs() {
        System.out.println("13reratrh");
        allExistingCFs = Collections.EMPTY_LIST;
        Reflections reflections = new Reflections("de.uni_hamburg.corpora");    
        Set<Class<? extends CorpusFunction>> classes = reflections.getSubTypesOf(CorpusFunction.class);
        for (Class c : classes){
            System.out.println(c.toString() + "1");
            try {
                Constructor cons = c.getConstructor();
                try {
                    CorpusFunction cf = (CorpusFunction) cons.newInstance();
                    allExistingCFs.add(cf);
                } catch (InstantiationException ex) {
                    Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
                }               
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (CorpusFunction cf : allExistingCFs){
        System.out.println(cf.toString() + "test2");
        }

        return allExistingCFs;
    }
    
    //TODO checks which functions can be run on specified data
    public Collection<CorpusFunction> getUsableFunctions(CorpusData cd) {
        //cf.IsUsableFor();
        //some switch or if else statements for the possible java objects 
        //and a list(?) which function can be apllied to what/which functions exist?
        Collection<CorpusFunction> usablecorpusfunctions = null;
        return usablecorpusfunctions;
    }

    //TODO return default functions, this is a list that needs to be somewhere
    //or maybe its an option a corpusfunction can have?
    public Collection<CorpusFunction> getDefaultUsableFunctions() {
        Collection<CorpusFunction> defaultcorpusfunctions = null;
        return defaultcorpusfunctions;
    }

    //TODO a dialog to choose functions you want to apply
    public Collection<CorpusFunction> chooseFunctionDialog() {
        Collection<CorpusFunction> chosencorpusfunctions = null;
        return chosencorpusfunctions;
    }

    //run multiple functions on a corpus, that means all the files in the corpus
    //the funciton can run on 
    public Report runCorpusFunction(Corpus c, Collection<CorpusFunction> cfc) {
        Report report = new Report();
        for (CorpusFunction cf : cfc) {
            Report newReport = runCorpusFunction(c, cf);
            report.merge(newReport);
        }
        return report;
    }
    
    //run one function on a corpus, that means all the files in the corpus
    //the funciton can run on 
    public Report runCorpusFunction(Corpus c, CorpusFunction cf) {
        Report report = new Report();
        //find out on which objects this corpus function can run
        //choose those from the corpus 
        //and run the checks on those files recursively
        for (CorpusData cd : cf.IsUsableFor()) {
            Report newReport = runCorpusFunction(c, cf);
            report.merge(newReport);
        }
        return report;
    }
    


    public Report runCorpusFunction(CorpusData cd, CorpusFunction cf) {
        return cf.execute(cd);
    }

    public Report runCorpusFunctions(CorpusData cd, Collection<CorpusFunction> cfc) {
        Report report = new Report();
        for (CorpusFunction cf : cfc) {
            Report newReport = (cf.execute(cd));
            report.merge(newReport);
        }
        return report;
    }

    //TODO what was this for again....?
    public void readParameters() {

    }
}
