/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.BasicTranscriptionData;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class PrettyPrintData extends Checker implements CorpusFunction {

    public PrettyPrintData() {
    }
    
    public Report check(CorpusData cd) {      
        // if no diff - all fine, nothing needs to be done     
        if (CorpusDataIsAlreadyPretty(cd)){
        report.addCorrect("PrettyPrintData", "Already pretty printed.");
        }
                // if difference then - needs to be pretty printed
        else{
        report.addCritical("PrettyPrintData", "Needs to be pretty printed.");
        }
        return report;
    }

    public Report fix(CorpusData cd){
        // take the data, change datatosaveable string, method indent() in utilities\PrettyPrinter.java
        if(!CorpusDataIsAlreadyPretty(cd)){
            try {
                String prettyCorpusData = indent(cd.toUnformattedString(), "event");
                //System.out.println(cd.toSaveableString());
                //System.out.println(prettyCorpusData);
                //save it instead of the old file
                CorpusIO cio = new CorpusIO();
                cio.write(prettyCorpusData, cd.getURL());
                cd.updateUnformattedString(prettyCorpusData);
                report.addCorrect("PrettyPrintData", "CorpusData "+ cd.getURL()+" was pretty printed and saved.");
            } catch (IOException ex) {
                report.addException(ex, cd.getURL() + " causes an Input/Output error.");
            }
        }
        else{
        report.addCorrect("PrettyPrintData", "CorpusData "+ cd.getURL()+" was already pretty printed, nothing done.");
        }
        return report;
    }



    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");   
            IsUsableFor.add(cl);
            Class cl2 = Class.forName("de.uni_hamburg.corpora.UnspecifiedXMLData");
            IsUsableFor.add(cl2);
            Class cl3 = Class.forName("de.uni_hamburg.corpora.ComaData");   
            IsUsableFor.add(cl3);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, " usable class not found");
        }
    return IsUsableFor;
    }


    public boolean CorpusDataIsAlreadyPretty(CorpusData cd){
        //take the data, change datatosaveable string, method indent() in utilities\PrettyPrinter.java
        //this one works for BasicTranscriptions only (keeping events togehter), but doesn't harm others
        //need to have another string not intended depending on which
        //file is the input
        String prettyCorpusData = indent(cd.toUnformattedString(), "event");
        //compare the files
        // if no diff - all fine, nothing needs to be done
        //TODO error - to saveableString already pretty printed - need to change that        
        return cd.toUnformattedString().equals(prettyCorpusData);
            
        
    }
    
}
