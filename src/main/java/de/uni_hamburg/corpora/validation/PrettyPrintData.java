/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
import java.util.Collection;

/**
 *
 * @author fsnv625
 */
public class PrettyPrintData{


    public void check(CorpusData cd) {      
        // take the data, change datatosaveable string, method indent() in utilities\PrettyPrinter.java
        String prettyCorpusData = indent(cd.toSaveableString(), "event");
        //save it in temp folder
        //compare the files
        // if difference then - needs to be pretty printed
        // if no diff - all fine, nothing needs to be done
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    
    }

    public void fix(CorpusData cd) {
        // take the data, change datatosaveable string, method indent() in utilities\PrettyPrinter.java
        String prettyCorpusData = indent(cd.toSaveableString(), "event");
        //save it instead of the old file
        // output which files were pretty printed
        // catch errors when writing etc. doesn't work 
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Collection<CorpusData> getIsUsableFor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
