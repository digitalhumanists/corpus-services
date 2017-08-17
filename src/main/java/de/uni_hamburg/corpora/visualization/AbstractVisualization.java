/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.visualization;

import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;

/**
 *
 * @author Daniel Jettka
 */
public abstract class AbstractVisualization {
    
    private String html = null;
    protected BasicTranscription basicTranscription = null;
    protected String basicTranscriptionString = null;
    
    // resources loaded from directory supplied in pom.xml
    protected String STYLESHEET_PATH = null;
    protected String JS_HIGHLIGHTING_PATH = "/js/timelight-0.1.min.js";
    
    protected String EMAIL_ADDRESS = "corpora@uni-hamburg.de";
    protected String SERVICE_NAME = null;
    protected String HZSK_WEBSITE = "https://corpora.uni-hamburg.de/";
    
    public AbstractVisualization(){
    
    }
            
    public void setHTML(String c){
        html = c;
    }
    
    public String getHTML(){
        return html;
    }
        
    
    public void addMedia(String recordingId, String recordingType){
        
        /*
        String newMediaElem = ""; 
        
        recordingType = recordingType.toLowerCase();
        
        if(recordingType.matches("^(wav|mp3|ogg)$")){
            newMediaElem = "<audio controls=\"controls\" data-tlid=\"media\">\n" +
                           "   <source src=\""+recordingId+"\" type=\"audio/"+recordingType+"\"/>\n" +
                           "</audio>";
        }
        
        if(recordingType.matches("^(mpeg|mpg|webm)$")){
            newMediaElem = "<video controls=\"controls\" data-tlid=\"media\">\n" +
                           "   <source src=\""+recordingId+"\" type=\"video/"+recordingType+"\"/>\n" +
                           "</video>";
        }
        */
        //html = html.replaceAll("<div id=\"mediaplayer\".*?</div>", "<div id=\"mediaplayer\" class=\"sidebarcontrol\">"+newMediaElem+"</div>");
        
    }
    
}
