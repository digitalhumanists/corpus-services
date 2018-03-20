/**
 * @file BasicTranscriptionData.java
 *
 * Connects BasicTranscription from Exmaralda to HZSK corpus services.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora;

import static de.uni_hamburg.corpora.utilities.PrettyPrinter.indent;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;
import org.jdom.JDOMException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;

/**
 * Provides access to basic transcriptions as a data type that can be read and
 * written HZSK corpus services. Naming might change, depending on what it ends
 * up being implemented as. It seems to me like a bridge now, or just aggregate.
 */
public class BasicTranscriptionData implements CorpusData, ContentData, XMLData {

    private BasicTranscription bt;
    URL url;
    Document readbtasjdom = new Document();

    public BasicTranscriptionData() {
    }

    public BasicTranscriptionData(URL url) {
        try {
            this.url = url;
            SAXBuilder builder = new SAXBuilder();
            readbtasjdom = builder.build(url);
            File f = new File(url.toURI());
            loadFile(f);
        } catch (JDOMException ex) {
            Logger.getLogger(UnspecifiedXMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UnspecifiedXMLData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(BasicTranscriptionData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(BasicTranscriptionData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JexmaraldaException ex) {
            Logger.getLogger(BasicTranscriptionData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * loads basic transcription from file. Some versions of exmaralda this
     * emits a harmless message to stdout.
     */
    public void loadFile(File f) throws SAXException, JexmaraldaException, MalformedURLException {
        bt = new BasicTranscription(f.getAbsolutePath());
        url = f.toURI().toURL();
    }
    
    public void updateReadbtasjdom() throws SAXException, JexmaraldaException, MalformedURLException, JDOMException, IOException {
        String xmlString = bt.toXML();
        SAXBuilder builder = new SAXBuilder();
        readbtasjdom = builder.build(xmlString);
    }

    /* 
    private String toPrettyPrintedXML() throws SAXException, JDOMException,
            IOException, UnsupportedEncodingException {
        String xmlString = bt.toXML();
        // this is a bit ugly workaround:
        SAXBuilder builder = new SAXBuilder();
        Document xmlDoc = builder.build(new StringReader(xmlString));
        // FIXME: make HZSK format somewhere
        Format hzskFormat = Format.getPrettyFormat();
        hzskFormat.setIndent("\t");
        XMLOutputter xmlout = new XMLOutputter(hzskFormat);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlout.output(xmlDoc, baos);
        return new String(baos.toByteArray(), "UTF-8");
    }
     */
    //I just use the hzsk-corpus-services\src\main\java\de\ uni_hamburg\corpora\
    //utilities\PrettyPrinter.java here to pretty print the files, so they
    //will always get pretty printed in the same way
    //TODO
    private String toPrettyPrintedXML() {
        String prettyCorpusData = indent(toUnformattedString(), "event");
        //String prettyCorpusData = indent(bt.toXML(bt.getTierFormatTable()), "event");
        return prettyCorpusData;
    }

    public String toSaveableString() {
        return toPrettyPrintedXML();
    }

    public static void main(String[] args) {
        if ((args.length != 2) && (args.length != 1)) {
            System.out.println("Usage: "
                    + BasicTranscriptionData.class.getName()
                    + " INPUT [OUTPUT]");
            System.exit(1);
        }
        try {
            BasicTranscriptionData btd = new BasicTranscriptionData();
            btd.loadFile(new File(args[0]));
            String prettyXML = btd.toSaveableString();
            boolean emplace = false;
            PrintWriter output;
            if (args.length == 2) {
                output = new PrintWriter(args[1]);
            } else {
                // FIXME: reaö temp
                output = new PrintWriter("tempfile.exb");
                emplace = true;
            }
            output.print(prettyXML);
            output.close();
            if (emplace) {
                Files.move(Paths.get("tempfile.exb"), Paths.get(args[0]),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (SAXException saxe) {
            saxe.printStackTrace();
            System.exit(1);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            System.exit(1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        } catch (JexmaraldaException je) {
            je.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public URL getURL() {
        return url;
    }
    
    public Document getReadbtasjdom() {
        return readbtasjdom;
    }

    @Override
    public String toUnformattedString() {
       XMLOutputter xmOut = new XMLOutputter();
       String unformattedCorpusData = xmOut.outputString(readbtasjdom);
       return unformattedCorpusData;
    }
}
