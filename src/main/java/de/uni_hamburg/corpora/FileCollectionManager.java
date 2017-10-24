/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.AbstractCollectionManager;
import java.io.File;
import java.util.Collection;

/**
 * this class can be used to get corpus files on a filesystem
 *
 * @author fsnv625
 */
public class FileCollectionManager extends AbstractCollectionManager {

    String fileAsString;

    /**
     * This method reads the filepath and turns the file into a String
     * representation
     *
     * @param   s   the filepath to the file on a filesystem
     * @return      that file as a String representation
     */
    @Override
    public String getFileAsString(String s) {
        File f = new File(s);
        return f.toString();
    }

    @Override
    public String getParametersFileAsString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCorpusStructureSpecificationAsString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //Maybe work with the fact, that this has to be specified as AsocFile in Coma?
    @Override
    public String getAnnotationSpecificationAsString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    //But also if there is a String (Filepath) given 
    @Override
     public String getAnnotationSpecificationAsString(String s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getComaSpecificationAsString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getComaFileAsString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCMDIAsString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection getAllTranscripts() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection getAllAudioFiles() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection getAllVideoFiles() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAudioLinkForTranscript() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getVideoLinkForTranscript() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void zipThings() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     *
     * + write():
     *
     *
     * + getXpathToTranscriptions(): String + getXPathToRecordings(): String +
     * getXPathToAsocFiles(): String
     *
     * + getCurrentFilename(): String + getCurrentDirectoryname(): String +
     * getNakedFilename(): String + getNakedFilenameWithoutSuffix(): String
     *
     *
     *
     */
}
