/**
 * @file BasicTranscriptionData.java
 *
 * Connects BasicTranscription from Exmaralda to HZSK corpus services.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora;

import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;
import org.jdom.JDOMException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;

/**
 * Provides access to basic transcriptions as a data type that can be read
 * and written HZSK corpus services. Naming might change, depending on what it
 * ends up being implemented as. It seems to me like a bridge now, or just
 * aggregate.
 */
public class BasicTranscriptionData implements CorpusData, ContentData, XMLData {

    private BasicTranscription bt;

    /** 
     * loads basic transcription from file. Some versions of exmaralda this
     * emits a harmless message to stdout.
     */
    public void loadFile(File f) throws SAXException, JexmaraldaException {
        bt = new BasicTranscription(f.getAbsolutePath());
    }

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

    public String toSaveableString() {
        try {
            return toPrettyPrintedXML();
        } catch(SAXException saxe) {
            // XXX:
            saxe.printStackTrace();
            return saxe.toString();
        } catch(JDOMException jdome) {
            // XXX:
            jdome.printStackTrace();
            return jdome.toString();
        } catch(IOException ioe) {
            // XXX:
            ioe.printStackTrace();
            return ioe.toString();
        }
    }
}

