/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.System.out;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.xml.sax.SAXException;

/**
 * Still to do
 *
 * @author fsnv625
 */
public class CorpusIO {

    public CorpusIO() {
    }

    //The content in here probably has not much to do with what we decided in UML now,
    //need to be reworked
    //that's the local filepath or repository url
    URL url;
    Collection<CorpusData> cdc;

    public String CorpusData2String(CorpusData cd) {
        return cd.toSaveableString();
    }


    /*
    * The following methods need to be in the Iterators for Coma and CMDI that don't exist yet
    *
  
    public abstract Collection getAllTranscripts();

    public abstract Collection getAllAudioFiles();

    public abstract Collection getAllVideoFiles();

    public abstract String getAudioLinkForTranscript();

    public abstract String getVideoLinkForTranscript();

     */
    public void write(CorpusData cd, URL url) {

    }

    //TODO
    public void write(String s, URL url) throws FileNotFoundException, IOException {
        //If URL is on fileserver only...
        System.out.println("started writing document...");
        outappend("============================\n");
        FileOutputStream fos = new FileOutputStream(new File(url.getFile()));
        fos.write(s.getBytes(("UTF-8")));
        fos.close();
        System.out.println("Document written...");
    }

    public void outappend(String a) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String time = sdf.format(cal.getTime());
        out.append("[" + time + "] ");
        out.append(a);
    }

    public void write(Collection<CorpusData> cdc, URL url) {

    }

    public CorpusData readFile(URL url) {
        CorpusData cd = null;
        return cd;
    }

    public Collection<CorpusData> read(URL url) {
        Collection<CorpusData> cdc = new ArrayList();
        ArrayList<CorpusData> acdc;
        acdc = (ArrayList) cdc;
        if (isLocalFile(url)) {
            //if the url points to a directory
            if (new File(url.getFile()).isDirectory()) {
                //we need to iterate    
                //and add everything to the cdc list
                return cdc;
            } //if the url points to a file
            else if (new File(url.getFile()).isFile()) {
                //we need to read this file as some implementation of corpusdata
                //for now maybe only exbs and coma
                if (new File(url.getFile()).getName().endsWith("exb")) {
                    BasicTranscriptionData bt = new BasicTranscriptionData();
                    try {
                        bt.loadFile(new File(url.getFile()));
                    } catch (SAXException ex) {
                        Logger.getLogger(CorpusIO.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JexmaraldaException ex) {
                        Logger.getLogger(CorpusIO.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(CorpusIO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    acdc.add(bt);
                    return cdc;
                } else if (new File(url.getFile()).getName().endsWith("coma")) {
                    //TODO
                    return cdc;
                } else {
                    //we can't read files other than coma and exb yet...
                    return cdc;
                }
            } else {
                //there's probably an error
                return cdc;
            }

        } else {
            //it's a datastream in the repo
            //TODO later           
            return cdc;
        }

    }

    /**
     * Whether the URL is a file in the local file system.
     */
    public static boolean isLocalFile(java.net.URL url) {
        String scheme = url.getProtocol();
        return "file".equalsIgnoreCase(scheme) && !hasHost(url);
    }

    public static boolean hasHost(java.net.URL url) {
        String host = url.getHost();
        return host != null && !"".equals(host);
    }

    public void writePrettyPrinted(CorpusData cd, URL url) {

    }

    public void zipThings() {

    }
}
