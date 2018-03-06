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
        // take the data, change datatosaveable string, method indent() in utilities\PrettyPrinter.java
        //this one works for BasicTranscriptions only!!
        //need to have another string not intended depending on which
        //file is the input
        String prettyCorpusData = indent(cd.toSaveableString(), "event");
        //compare the files
                // if no diff - all fine, nothing needs to be done       
        if (cd.toSaveableString().equals(prettyCorpusData)){
        report.addCorrect("PrettyPrintData", "Already pretty printed.");
        }
                // if difference then - needs to be pretty printed
        else{
        report.addCritical("PrettyPrintData", "Needs to be pretty printed.");
        }
        return report;
    }

    public Report fix(CorpusData cd) throws IOException {
        // take the data, change datatosaveable string, method indent() in utilities\PrettyPrinter.java
        String prettyCorpusData = indent(cd.toSaveableString(), "event");
        System.out.println(cd.toSaveableString());
        System.out.println(prettyCorpusData);
        //save it instead of the old file
        CorpusIO cio = new CorpusIO();
        cio.write(prettyCorpusData, cd.getURL());
        report.addCorrect("PrettyPrintData", "CorpusData "+ cd.getURL()+" was pretty printed and saved.");
        // output which files were pretty printed
        // catch errors when writing etc. doesn't work 
        return report;
    }


    @Override
    public Report fix(Collection<CorpusData> cdc) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report execute(Corpus c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);           
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PrettyPrintData.class.getName()).log(Level.SEVERE, null, ex);
        }
    return IsUsableFor;
    }

    
}
