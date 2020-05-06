/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;

/**
 *
 * @author Daniel Jettka
 *
 * This now should be a normal, not abstract class that has an implementation of
 * "Visualize" as its field (that would be e.g. ListHTML) which seems to make
 * things a lot easier
 *
 */
public abstract class Visualizer implements CorpusFunction {

    private String html = null;
    protected BasicTranscription basicTranscription = null;
    protected String basicTranscriptionString = null;

    // resources loaded from directory supplied in pom.xml
    protected String STYLESHEET_PATH = null;
    protected String JS_HIGHLIGHTING_PATH = "/js/timelight-0.1.min.js";

    protected String EMAIL_ADDRESS = "corpora@uni-hamburg.de";
    protected String SERVICE_NAME = null;
    protected String HZSK_WEBSITE = "https://corpora.uni-hamburg.de/";
    protected String RECORDING_PATH = null;
    protected String RECORDING_TYPE = null;

    CorpusData cd;
    Report report;
    Collection<Class<? extends CorpusData>> IsUsableFor = new ArrayList<Class<? extends CorpusData>>();
    final String function;

    public Visualizer(String func) {
        function = func;
    }


    /**
     * Manually set the HTML content of the visualization
     *
     * @param c content to be set as HTML of the visualization
     * @return
     */
    public void setHTML(String c) {
        html = c;
    }

    /**
     * Get the HTML content of the visualization
     *
     * @param
     * @return the HTML content of the visualization
     */
    public String getHTML() {
        return html;
    }

    /**
     * set a media element (video or audio depending on recordingType) in the
     * HTML content of the visualization
     *
     * @param recordingId path/URL to the recording file
     * @param recordingType type of the recording (e.g. wav, mp3, mpg, webm)
     * @return
     */
    public void setMedia(String recordingId, String recordingType) {

        String newMediaElem = "";

        recordingType = recordingType.toLowerCase();

        if (recordingType.matches("^(wav|mp3|ogg)$")) {

            newMediaElem = "<audio controls=\"controls\" data-tlid=\"media\">\n"
                    + "   <source src=\"" + recordingId + "\" type=\"audio/" + recordingType + "\"/>\n"
                    + "</audio>";
        }

        if (recordingType.matches("^(mpeg|mpg|webm)$")) {
            newMediaElem = "<video controls=\"controls\" data-tlid=\"media\">\n"
                    + "   <source src=\"" + recordingId + "\" type=\"video/" + recordingType + "\"/>\n"
                    + "</video>";
        }

        setHTML(Pattern.compile("<div[^>]*id=\"mediaplayer\".*?</div>", Pattern.DOTALL).matcher(html).replaceAll("<div id=\"mediaplayer\" class=\"sidebarcontrol\">" + newMediaElem + "</div>"));

    }

    /**
     * remove content from media element in the HTML content of the
     * visualization
     *
     * @return
     */
    public void removeMedia() {

        setHTML(Pattern.compile("<div[^>]*id=\"mediaplayer\".*?</div>", Pattern.DOTALL).matcher(html).replaceAll("<div id=\"mediaplayer\" class=\"sidebarcontrol\"></div>"));

    }
    
     //always take a coma file and the relative paths in there to generate a list of the files
    //
    //Methode arbeitet anhand von Liste von Dateien, im Moment wird diese aus Coma ausgelesen
    public Report execute(Corpus c) {
        return execute(c.getCorpusData());
    }

    //this will always be a coma file
    public Report execute(CorpusData cd) {
        report = new Report();
        report = visualize(cd);
        return report;
    }

    public Report execute(Collection<CorpusData> cdc) {
        report = new Report();
        visualize(cdc);
        return report;

    }
    
    //no fix boolean needed
    public Report execute(CorpusData cd, boolean fix){
        report = new Report();
        report = visualize(cd);
        return report;
    }

    //no fix boolean needed
    public Report execute(Collection<CorpusData> cdc, boolean fix){
        report = new Report();
        visualize(cdc);
        return report;
    }

    //TODO
    public abstract Report visualize(CorpusData cd);

    //TODO
    public Report visualize(Collection<CorpusData> cdc) {
        for (CorpusData cd : cdc) {
            report.merge(visualize(cd));
        }
        return report;
    }
    
     public abstract Collection<Class<? extends CorpusData>> getIsUsableFor();

    public void setIsUsableFor(Collection<Class<? extends CorpusData>> cdc){
        for (Class<? extends CorpusData> cl : cdc){
        IsUsableFor.add(cl);
        }
    }
            public String getFunction(){
        return function;
    }
}
