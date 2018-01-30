/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

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

    public ErrorList runCorpusFunction(CorpusData cd, CorpusFunction cf) {
    cf.execute(cd);
    }
    
     public ErrorList runCorpusFunctions(CorpusData cd, Collection<CorpusFunction> cfs) {
    for (CorpusFunction cf :cfs){
         cf.execute(cd);
    }
    }

    public void readParameters() {

    }
}
