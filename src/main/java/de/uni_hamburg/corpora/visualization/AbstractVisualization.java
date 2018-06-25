/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;

/**
 *
 * @author Daniel Jettka
 * 
 * This now should be a normal, not abstract class that has 
 * an implementation of "Visualize" as its field (that would be e.g. ListHTML)
 * which seems to make things a lot easier
 * 
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
    protected String RECORDING_PATH = null;
    protected String RECORDING_TYPE = null;
    
    public AbstractVisualization(){
    
    }
           
	/**
	 * Manually set the HTML content of the visualization
	 *
	 * @param  c  content to be set as HTML of the visualization
	 * @return      
	 */
    public void setHTML(String c){
        html = c;
    }
	
    /**
	 * Get the HTML content of the visualization
	 *
	 * @param  
	 * @return  the HTML content of the visualization    
	 */
    public String getHTML(){
        return html;
    }
        
    /**
	 * set a media element (video or audio depending on recordingType) in the HTML content of the visualization
	 *
	 * @param  recordingId  path/URL to the recording file
	 * @param  recordingType  type of the recording (e.g. wav, mp3, mpg, webm)
	 * @return  
	 */
    public void setMedia(String recordingId, String recordingType){
        
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
        
        setHTML( Pattern.compile("<div id=\"mediaplayer\".*?</div>", Pattern.DOTALL).matcher(html).replaceAll("<div id=\"mediaplayer\" class=\"sidebarcontrol\">"+newMediaElem+"</div>") );
                
    }
    
}
