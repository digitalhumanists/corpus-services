/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.net.URL;
import java.util.Collection;
import de.uni_hamburg.corpora.CorpusData;
/**
 *
 * @author fsnv625
 */
public class Corpus {
    
    Metadata metadata;
    Collection <ContentData> contentdata;
    Collection <Recording> recording;
    Collection <AdditionalData> additionaldata;
    AnnotationSpecification annotationspecification;
    ConfigParameters configparameters;
    
    public Corpus(){
        
    }
    
    public Corpus(URL url){
    }
    
    public Collection<CorpusData> getCorpusData(){
    Collection<CorpusData> col = null; 
    return col;
    }
}
