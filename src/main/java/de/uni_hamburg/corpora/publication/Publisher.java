/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.publication;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.validation.ValidatorSettings;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author fsnv625
 */
public abstract class Publisher implements CorpusFunction {

    CorpusData cd;
    Report report;
    Collection<Class<? extends CorpusData>> IsUsableFor = new ArrayList<Class<? extends CorpusData>>();
    ValidatorSettings settings;
    final String function;
    Boolean canfix = false;

    public Publisher(){
        function = this.getClass().getSimpleName();
    }

    public Report execute(Corpus c) {
        report = new Report();
        report = function(c);
        return report;
    }

    public Report execute(CorpusData cd) {
        report = new Report();
        report = function(cd);
        return report;
    }

    //no fix boolean needed
    public Report execute(CorpusData cd, boolean fix) {
        return execute(cd);
    }

    //no fix boolean needed
    public Report execute(Corpus c, boolean fix) {
        return execute(c);
    }

    //to be implemented in class
    public abstract Report function(CorpusData cd);

    //to be implemented in class
    public abstract Report function(Corpus c);

    public abstract Collection<Class<? extends CorpusData>> getIsUsableFor();

    public void setIsUsableFor(Collection<Class<? extends CorpusData>> cdc) {
        for (Class<? extends CorpusData> cl : cdc) {
            IsUsableFor.add(cl);
        }
    }

    public String getFunction() {
        return function;
    }

    public Boolean getCanFix() {
        return canfix;
    }

}
