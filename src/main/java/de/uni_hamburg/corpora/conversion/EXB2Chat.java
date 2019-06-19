/**
 * @File EXB2Chat.java
 */

package de.uni_hamburg.corpora.conversion;

import de.uni_hamburg.corpora.utilities.TypeConverter;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.jdom.JDOMException;
import org.jdom.transform.XSLTransformException;
import org.xml.sax.SAXException;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.segment.CHATSegmentation;
import org.exmaralda.partitureditor.jexmaralda.ListTranscription;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.CorpusData;
import java.util.Collection;
import java.net.URL;
import de.uni_hamburg.corpora.CorpusIO;
import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;
import net.sf.saxon.trans.XPathException;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author Tommi A Pirinen
 */
public class EXB2Chat extends Converter {

    public EXB2Chat(){

    }

    public static final String EXB2CHAT = "exb2chat";

    public Report convert(CorpusData cod) {
        Report stats = new Report();
        try {
            String basicTranscription = cod.toSaveableString();
            BasicTranscription bt = TypeConverter.String2BasicTranscription(basicTranscription);
            String data = exportCHATTranscript(bt, "UTF-8");
            URL targeturl = new URL(cd.getParentURL() + cd.getFilenameWithoutFileEnding() + ".chat");
            CorpusIO cio = new CorpusIO();
            cio.write(data, targeturl);
            stats.addCorrect(EXB2CHAT, cod, "Conversion of file was successfully saved at " + targeturl);
        } catch(JexmaraldaException je) {
            stats.addException(je, EXB2CHAT, cod, "Input Output Exception");
        } catch(FSMException fe) {
            stats.addException(fe, EXB2CHAT, cod, "Input Output Exception");
        } catch(SAXException se) {
            stats.addException(se, EXB2CHAT, cod, "Input Output Exception");
        } catch(FileNotFoundException fnfe) {
            stats.addException(fnfe, EXB2CHAT, cod, "Input Output Exception");
        } catch(IOException ioe) {
            stats.addException(ioe, EXB2CHAT, cod, "Input Output Exception");
        } catch(XPathException xe) {
            stats.addException(xe, EXB2CHAT, cod, "Input Output Exception");
        } catch(XPathExpressionException xee) {
            stats.addException(xee, EXB2CHAT, cod, "Input Output Exception");
        } catch(ParserConfigurationException pe) {
            stats.addException(pe, EXB2CHAT, cod, "Input Output Exception");
        } catch(TransformerException te) {
            stats.addException(te, EXB2CHAT, cod, "Input Output Exception");
        }
        return stats;
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    /**
     * No check is applicable for this feature.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        this.cd = cd;
        // FIXME:
        return convert(cd);
        // report.addCritical(EXB2CHAT,
        //        "Automatic check is not yet supported.");
        // return report;
    }

    /**
     * No fix is applicable for this feature.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        this.cd = cd;
        report.addCritical(EXB2CHAT,
                "Automatic fix is not yet supported.");
        return report;
    }

    /**
     * export chat transcirpt from org/exmaralda/exmaralda/src/
     */
    String exportCHATTranscript(BasicTranscription bt, String encoding)
            throws JexmaraldaException, FSMException, SAXException,
                              FileNotFoundException, IOException{
         // segment the basic transcription and transform it into a list transcription
         CHATSegmentation segmenter = new
             org.exmaralda.partitureditor.jexmaralda.segment.CHATSegmentation();
         ListTranscription lt = segmenter.BasicToUtteranceList(bt);
         String text = CHATSegmentation.toText(lt);
         return text;
    }

}
