/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.net.URL;
//don't know if this is the correct Coma class in Exmaralda yet...
import org.exmaralda.coma.root.Coma;

/**
 *
 * @author fsnv625
 */
public class ComaData implements Metadata, CorpusData{
    URL url;
    Coma coma;
    
    public ComaData() {
    }
    
    public ComaData(URL url) {
    }

    @Override
    public URL getURL() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toSaveableString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
