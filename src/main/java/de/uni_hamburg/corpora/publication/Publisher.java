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
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author fsnv625
 */
public abstract class Publisher implements CorpusFunction {

    CorpusData cd;
    Report report;
    Collection<Class> IsUsableFor = new ArrayList();

    public Publisher() {
    }

    //always take a coma file and the relative paths in there to generate a list of the files
    //
    //Methode arbeitet anhand von Liste von Dateien, im Moment wird diese aus Coma ausgelesen
    public Report execute(Corpus c) {
        return execute(c.getCorpusData());
    }

    //this will always be a coma file
    public Report execute(CorpusData cd) {
        report = new Report();
        report = publish(cd);
        return report;
    }

    public Report execute(Collection<CorpusData> cdc) {
        report = new Report();
        publish(cdc);
        return report;

    }
    
    //no fix boolean needed
    public Report execute(CorpusData cd, boolean fix){
        report = new Report();
        report = publish(cd);
        return report;
    }

    //no fix boolean needed
    public Report execute(Collection<CorpusData> cdc, boolean fix){
        report = new Report();
        publish(cdc);
        return report;
    }

    //TODO
    public abstract Report publish(CorpusData cd);

    //TODO
    public Report publish(Collection<CorpusData> cdc) {
        for (CorpusData cd : cdc) {
            report.merge(publish(cd));
        }
        return report;
    }

    public abstract Collection<Class> getIsUsableFor();

    public void setIsUsableFor(Collection<Class> cdc) {
        for (Class cl : cdc) {
            IsUsableFor.add(cl);
        }
    }

}
