package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.utilities.PrettyPrinter;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static java.lang.System.out;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.Document;
import org.jdom.JDOMException;
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
    Collection<CorpusData> cdc = new ArrayList();
    Collection<URL> recursed = new ArrayList();
    Collection<URL> alldata = new ArrayList();
    URL basedirectory;

    public String CorpusData2String(CorpusData cd) throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
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
    public void write(CorpusData cd, URL url) throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
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

    public void write(Document doc, URL url) throws IOException, TransformerException, ParserConfigurationException, ParserConfigurationException, UnsupportedEncodingException, UnsupportedEncodingException, SAXException, XPathExpressionException {
        XMLOutputter xmOut = new XMLOutputter();
        String unformattedCorpusData = xmOut.outputString(doc);
        PrettyPrinter pp = new PrettyPrinter();
        String prettyCorpusData = pp.indent(unformattedCorpusData, "event");
        write(prettyCorpusData, url);
    }
    
    public void write(org.w3c.dom.Document doc, URL url) throws IOException, TransformerException, ParserConfigurationException, ParserConfigurationException, UnsupportedEncodingException, UnsupportedEncodingException, SAXException, XPathExpressionException {
        String unformattedCorpusData = TypeConverter.W3cDocument2String(doc);
        PrettyPrinter pp = new PrettyPrinter();
        String prettyCorpusData = pp.indent(unformattedCorpusData, "event");
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
        //TODO
    }

    //read a single file as a corpus data object from an url
    public CorpusData readFileURL(URL url) throws SAXException, JexmaraldaException {
        if (url.getPath().endsWith("exb")) {
            BasicTranscriptionData bt = new BasicTranscriptionData(url);
            //bt.loadFile(f);
            return bt;
        } else if (url.getPath().toLowerCase().endsWith("coma")) {
            ComaData cm = new ComaData(url);
            return cm;
        } else if (url.getPath().toLowerCase().endsWith("xml") && ((url.getPath().toLowerCase().contains("Annotation")))) {
            AnnotationSpecification as = new AnnotationSpecification(url);
            return as;
        } else if ((url.getPath().toLowerCase().endsWith("xml") && url.getPath().toLowerCase().contains("cmdi")) || url.getPath().toLowerCase().endsWith("cmdi")) {
            CmdiData cmdi = new CmdiData(url);
            return cmdi;
        } else if (url.getPath().toLowerCase().endsWith("xml")) {
            UnspecifiedXMLData usd = new UnspecifiedXMLData(url);
            return usd;
        } else if (url.getPath().toLowerCase().endsWith("exs")) {
            SegmentedTranscriptionData usd = new SegmentedTranscriptionData(url);
            return usd;
        } else {
            System.out.println(url + " is not xml CorpusData");
            CorpusData cd = null;
            return cd;
        }
    }

    //read all the files as corpus data objects from a directory url
    public Collection<CorpusData> read(URL url) throws URISyntaxException, IOException, SAXException, JexmaraldaException {
        alldata = URLtoList(url);
        for (URL readurl : alldata) {
            CorpusData cdread = readFileURL(readurl);
            cdc.add(cdread);
        }
        return cdc;
    }

    public String readInternalResourceAsString(String path2resource) throws JDOMException, IOException {
        String xslstring = TypeConverter.InputStream2String(getClass().getResourceAsStream(path2resource));
        System.out.println(path2resource);
        if (xslstring == null) {
            throw new IOException("Stylesheet not found!");
        }
        return xslstring;
    }
    
    public String readExternalResourceAsString(String path2resource) throws JDOMException, IOException, URISyntaxException {
        String xslstring = new String(Files.readAllBytes(Paths.get(new URL(path2resource).toURI())));
        System.out.println(path2resource);
        if (xslstring == null) {
            throw new IOException("File not found!");
        }
        return xslstring;
    }

    public Collection<URL> URLtoList(URL url) throws URISyntaxException, IOException {
        if (isLocalFile(url)) {
            //if the url points to a directory
            if (isDirectory(url)) {
                //if it's a directory, the directory is the basedirectory
                basedirectory = url;
                //we need to iterate    
                //and add everything to the list
                Path path = Paths.get(url.toURI());
                listFiles(path);
                for (URL urlread : recursed) {
                    if (!isDirectory(urlread)) {
                        alldata.add(urlread);
                    }
                }
                return alldata;
            } //if the url points to a file
            else {
                //if it's a file, the directory of the file is the basedirectory
                URI uri = url.toURI();
                URI parentURI = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
                basedirectory = parentURI.toURL();
                //we need to add just this file
                alldata.add(url);
                return alldata;
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

    /**
     * Whether the URL is a directory in the local file system.
     */
    public static boolean isDirectory(java.net.URL url) throws URISyntaxException {
        //return new File(url.toURI()).isDirectory();
        return Files.isDirectory(Paths.get(url.toURI()));
    }

    public static boolean hasHost(java.net.URL url) {
        String host = url.getHost();
        return host != null && !"".equals(host);
    }

    public void writePrettyPrinted(CorpusData cd, URL url) throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        write(cd.toSaveableString(), cd.getURL());
    }

    public void zipThings() {

    }
    
    public URL getBaseDirectory() {
    return basedirectory;
    }

    void listFiles(Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    listFiles(entry);
                }
                String sentry = entry.getFileName().toString().toLowerCase();
                if (sentry.endsWith(".exb") || sentry.endsWith(".exs") || sentry.endsWith(".coma") || sentry.endsWith(".xml") || sentry.endsWith(".cmdi") || sentry.endsWith(".eaf") || sentry.endsWith(".flextext") || sentry.endsWith(".esa") || sentry.endsWith(".tei") || sentry.endsWith(".xsl")) {
                    recursed.add(entry.toUri().toURL());
                }
            }
        }
    }
}
