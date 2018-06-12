package de.uni_hamburg.corpora;

import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
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
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
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
    public void write(CorpusData cd, URL url) throws IOException {
        write(cd.toSaveableString(), cd.getURL());
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
    
    public void write(Document doc, URL url) throws IOException {
       XMLOutputter xmOut = new XMLOutputter();
       String unformattedCorpusData = xmOut.outputString(doc);
       String prettyCorpusData = indent(unformattedCorpusData, "event");
       write(prettyCorpusData, url);
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

    //TODO
    public CorpusData readFile(URL url) {
        CorpusData cd = null;
        return cd;
    }

    //TODO
    public CorpusData toCorpusData(File f) throws MalformedURLException, SAXException, JexmaraldaException {
        if (f.getName().endsWith("exb")) {
            BasicTranscriptionData bt = new BasicTranscriptionData(f.toURI().toURL());
            //bt.loadFile(f);
            return bt;
        } else if (f.getName().endsWith("coma")) {
                ComaData cm = new ComaData(f.toURI().toURL());
                //TODO
                return cm;
        } else if (f.getName().endsWith("exs")||f.getName().endsWith("xml")){
            UnspecifiedXMLData usd = new UnspecifiedXMLData(f.toURI().toURL());
            return usd;
            //we can't read files other than coma and exb yet...

        } else if(f.getName().endsWith("cmdi")){
            CmdiData cmdi = new CmdiData(f.toURI().toURL());
            return cmdi; 
        }else {
            System.out.println(f.getName()+ " is not xml CorpusData");
            CorpusData cd = null;
            return cd;
        }
    }

    //TODO
    public Collection<File> getFileURLSRecursively(URL directoryURL) {
        Set<String> recursionBlackList = new HashSet<String>();
        recursionBlackList.add(".git");
        Set<File> recursed = new HashSet<File>();
        Stack<File> dirs = new Stack();
        File d = new File(directoryURL.getFile());
        dirs.add(d);
        while (!dirs.empty()) {
            File[] files = dirs.pop().listFiles();
            for (File f : files) {
                if (recursionBlackList.contains(f.getName())) {
                    continue;
                } else if (f.isDirectory()) {
                    dirs.add(f);
                } else {
                    recursed.add(f);
                }
            }
        }
        return recursed;
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
                Collection<File> recursed = getFileURLSRecursively(url);
                for (File f : recursed) {
                    try {
                        CorpusData cd = toCorpusData(f);
                        if(cd!=null){
                        acdc.add(toCorpusData(f));
                        }
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(CorpusIO.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SAXException ex) {
                        Logger.getLogger(CorpusIO.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JexmaraldaException ex) {
                        Logger.getLogger(CorpusIO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                cdc = (Collection) acdc;
                return cdc;
            } //if the url points to a file
            else {
                //we need to read this file as some implementation of corpusdata
                File f = new File(url.getFile());
                try {
                    CorpusData cd = toCorpusData(f);
                        if(cd!=null){
                        acdc.add(toCorpusData(f));
                    }
                } catch (MalformedURLException ex) {
                    Logger.getLogger(CorpusIO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(CorpusIO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JexmaraldaException ex) {
                    Logger.getLogger(CorpusIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                cdc = (Collection) acdc;
                return cdc;
            
        }
    } else {
        //it's a datastream in the repo
        //TODO later          
        return null;
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

    public void writePrettyPrinted(CorpusData cd, URL url) throws IOException {
        write(cd.toSaveableString(), cd.getURL());
    }

    public void zipThings() {

    }
}