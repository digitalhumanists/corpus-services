/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.net.URL;
import java.util.Collection;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusIO;
import java.util.ArrayList;

/**
 *
 * @author fsnv625
 */
public class Corpus {

    //only the metadata file, coma or cmdi in most cases, or a list of files
    Collection<Metadata> metadata;
    //the transcriptions
    Collection<ContentData> contentdata = new ArrayList();
    Collection<Recording> recording  = new ArrayList();
    Collection<AdditionalData> additionaldata  = new ArrayList();
    Collection<AnnotationSpecification> annotationspecification  = new ArrayList();
    Collection<ConfigParameters> configparameters  = new ArrayList();
    //all the data together
    Collection<CorpusData> cdc;

    public Corpus() {

    }

    public Corpus(URL url) {
        CorpusIO cio = new CorpusIO();
        cdc = cio.read(url);
        for (CorpusData cd : cdc) {
            if (cd instanceof ContentData) {
                contentdata.add((ContentData) cd);
            } else if (cd instanceof Recording) {
                recording.add((Recording) cd);
            } else if (cd instanceof AdditionalData) {
                additionaldata.add((AdditionalData) cd);
            } else if (cd instanceof Metadata) {
                metadata.add((Metadata) cd);
            } else if (cd instanceof AnnotationSpecification) {
                annotationspecification.add((AnnotationSpecification) cd);
            } else if (cd instanceof ConfigParameters) {
                configparameters.add((ConfigParameters) cd);
            }
        }
        //and also the other collections maybe
    }

    public Collection<CorpusData> getCorpusData() {
        return cdc;
    }

    public Collection<Metadata> getMetadata() {
        return metadata;
    }

    public Collection<ContentData> getContentdata() {
        return contentdata;
    }

    public Collection<Recording> getRecording() {
        return recording;
    }

    public Collection<AdditionalData> getAdditionaldata() {
        return additionaldata;
    }

    public Collection<AnnotationSpecification> getAnnotationspecification() {
        return annotationspecification;
    }

    public Collection<ConfigParameters> getConfigparameters() {
        return configparameters;
    }

    public void setMetadata(Collection<Metadata> metadata) {
        this.metadata = metadata;
    }

    public void setContentdata(Collection<ContentData> contentdata) {
        this.contentdata = contentdata;
    }

    public void setRecording(Collection<Recording> recording) {
        this.recording = recording;
    }

    public void setAdditionaldata(Collection<AdditionalData> additionaldata) {
        this.additionaldata = additionaldata;
    }

    public void setAnnotationspecification(Collection<AnnotationSpecification> annotationspecification) {
        this.annotationspecification = annotationspecification;
    }

    public void setConfigparameters(Collection<ConfigParameters> configparameters) {
        this.configparameters = configparameters;
    }

    public void setCdc(Collection<CorpusData> cdc) {
        this.cdc = cdc;
    }

}
