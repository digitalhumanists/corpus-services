/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.net.URL;

/**
 *
 * @author fsnv625
 */
public interface CorpusData {

    public URL getURL();
    
    public void setURL(URL url);
    
    public URL getParentURL();
    
    public void setParentURL(URL url);
    
    public String getFilename();
    
    public void setFilename(String s);
    
    public String getFilenameWithoutFileEnding();
    
    public void setFilenameWithoutFileEnding(String s);

    public String toSaveableString();

    public String toUnformattedString();
    
    //needed if there were changes to the file so they are represented in the object too
    public void updateUnformattedString(String newUnformattedString);

}
