/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author anne
 */
public class ExbFileCoverageChecker  extends Checker implements CorpusFunction {
    
    final String EXB_FILECOVERAGE = "exb-filecoverage";
    List<String> whitelist;
    List<String> fileendingwhitelist;

     public ExbFileCoverageChecker() {
        // these are acceptable
        setWhitelist();
        
    }
     
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setWhitelist(){
        whitelist = new ArrayList<String>();
        whitelist.add(".git");
        whitelist.add(".gitignore");
        whitelist.add("README");
        whitelist.add("Thumbs.db");
        fileendingwhitelist = new ArrayList<String>();
    }
    
     public void setWhitelist(List<String> whitelisted){
         //TODO
     }
}
