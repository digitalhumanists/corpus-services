/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.net.URL;
import java.util.Collection;

/**
 * This class has a Corpus and a Corpus Function as a field and is able to run a
 * Corpus Function on a corpus in a main method.
 *
 * @author fsnv625
 */
public class CorpusMagician {
    Corpus corpus;
    CorpusFunction corpusfunction;

    public CorpusMagician() {

    }

    public void initCorpus(){
    corpus = new Corpus();
    }
    
    public void initCorpusWithURL(URL url){
    corpus = new Corpus(url);
    }
    
    public Collection<CorpusFunction> getUsableFunctions(CorpusData cd) {
    Collection<CorpusFunction> usablecorpusfunctions = null;
    return usablecorpusfunctions;
    }

    public Collection<CorpusFunction> getDefaultUsableFunctions() {
    Collection<CorpusFunction> defaultcorpusfunctions = null;
    return defaultcorpusfunctions;
    }

    public Collection<CorpusFunction> chooseFunctionDialog() {
    Collection<CorpusFunction> chosencorpusfunctions = null;
    return chosencorpusfunctions;
    }

    public Report runCorpusFunction(CorpusData cd, CorpusFunction cf) {
    return cf.execute(cd);
    }
    
    public Report runCorpusFunctions(CorpusData cd, Collection<CorpusFunction> cfs) {
    Report report = new Report();
    for (CorpusFunction cf :cfs){
         Report newReport = (cf.execute(cd));
         report.merge(newReport);
    }
    return report;
    }

    public void readParameters() {

    }
}
