/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.utilities;

import java.util.Collection;

/**
 * Still to do
 *
 * @author fsnv625
 */
public abstract class AbstractCollectionManager {

    //important: That shouldn't be the filepath, but the File itself as a String!!
    String fileAsString;

    public abstract String getFileAsString(String s);

    public abstract String getParametersFileAsString();

    //or should this be an array or something?
    public abstract String getCorpusStructureSpecificationAsString();

    public abstract String getAnnotationSpecificationAsString();

    //not yet sure if we need the following
    public abstract String getComaSpecificationAsString();

    public abstract String getComaFileAsString();

    public abstract String getCMDIAsString();

    public abstract Collection getAllTranscripts();

    public abstract Collection getAllAudioFiles();

    public abstract Collection getAllVideoFiles();

    public abstract String getAudioLinkForTranscript();

    public abstract String getVideoLinkForTranscript();

    public abstract void write();

    public abstract void zipThings();
}
