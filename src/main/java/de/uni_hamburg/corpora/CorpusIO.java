/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.util.Collection;

/**
 * Still to do
 *
 * @author fsnv625
 */
public class CorpusIO {

    //The content in here probably has not much to do with what we decided in UML now,
    //need to be reworked
    //important: That shouldn't be the filepath, but the File itself as a String!!
    //Maybe it's fine if we work with strings here? Then we would get a CorpusData 
    //object, turn it into a string, maybe prettyprint it, and save that on the fileserver 
    //or as a datastream in the repo :)
    
    
    
    //maybe we can work with the Java Interfaces DataInput and DataOutput? Or DataInputStream and DataOutputstream?
    
    
    String fileAsString;

    public String getCorpusDataAsString(CorpusData cd) {
        return cd.toSaveableString();
    }

    //not yet sure if we need the following

    /*
    * The following methods need to be in the Iterators for Coma and CMDI that don't exist yet
    *
  
    public abstract Collection getAllTranscripts();

    public abstract Collection getAllAudioFiles();

    public abstract Collection getAllVideoFiles();

    public abstract String getAudioLinkForTranscript();

    public abstract String getVideoLinkForTranscript();

   */

    public void write(){
        
    }

    public void writePrettyPrinted(){
        
    }

    public void zipThings(){
        
    }
}
