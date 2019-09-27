/**
 * @file ExbSegmenter.java
 *
 * A command-line tool / non-graphical interface for checking errors in
 * exmaralda's EXB files.
 *
 * @author Anne Ferger <anne.ferger@uni-hamburg.de>
 * @author INEL
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.BasicTranscriptionData;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.Option;
import org.xml.sax.SAXException;

import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.segment.AbstractSegmentation;
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.SegmentedTranscription;
import org.jdom.Document;
import org.jdom.JDOMException;

/**
 * A command-line tool for checking EXB files.
 */
public class ExbSegmenter extends Checker implements CorpusFunction {

    static String filename;
    static BasicTranscription bt;
    static BasicTranscriptionData btd;
    static File exbfile;
    AbstractSegmentation segmentation;
    static ValidatorSettings settings;
    final String EXB_SEG = "exb-segmentation-checker";
    String segmentationName = "GENERIC";
    String path2ExternalFSM = "";

    public static Report check(File f) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(f);
        } catch (SAXException saxe) {
            saxe.printStackTrace();
        } catch (JexmaraldaException je) {
            je.printStackTrace();
        }
        return stats;
    }

    public static Report
            exceptionalCheck(File f) throws SAXException, JexmaraldaException {
        filename = f.getAbsolutePath();
        bt = new BasicTranscription(filename);

        //EditErrorsDialog eed = new EditErrorsDialog(table.parent, false);
        //eed.setOpenSaveButtonsVisible(false);
        //eed.setTitle("Structure errors");
        //eed.addErrorCheckerListener(table);
        //eed.setErrorList(errorsDocument);
        //eed.setLocationRelativeTo(table);
        //eed.setVisible(true);
        return new Report();
    }

    public static void main(String[] args) {
        settings = new ValidatorSettings("ExbSegmentationChecker",
                "Checks Exmaralda .exb file for segmentation problems",
                "If input is a directory, performs recursive check "
                + "from that directory, otherwise checks input file");
        settings.handleCommandLine(args, new ArrayList<Option>());
        if (settings.isVerbose()) {
            System.out.println("Checking EXB files for segmentation "
                    + "problems...");
        }
        for (File f : settings.getInputFiles()) {
            if (settings.isVerbose()) {
                System.out.println(" * " + f.getName());
            }
            Report stats = check(f);
            if (settings.isVerbose()) {
                System.out.println(stats.getFullReports());
            } else {
                System.out.println(stats.getSummaryLines());
            }
        }
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (SAXException saxe) {
            saxe.printStackTrace();
        } catch (JexmaraldaException je) {
            je.printStackTrace();
        } catch (IOException ex) {
            stats.addException(ex, "Unknown read error");
        } catch (ParserConfigurationException ex) {
            stats.addException(ex, "Unknown read error");
        }
        return stats;
    }

    /**
     * Main feature of the class: Checks Exmaralda .exb file for segmentation
     * problems.
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException {
        Report stats = new Report();
        btd = new BasicTranscriptionData(cd.getURL());
        if (segmentationName.equals("HIAT")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.HIATSegmentation();
        } else if (segmentationName.equals("GAT")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.GATSegmentation();
        } else if (segmentationName.equals("cGAT_MINIMAL")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.cGATMinimalSegmentation();
        } else if (segmentationName.equals("CHAT")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.CHATSegmentation();
        } else if (segmentationName.equals("CHAT_MINIMAL")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.CHATMinimalSegmentation();
        } else if (segmentationName.equals("DIDA")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.DIDASegmentation();
        } else if (segmentationName.equals("IPA")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.IPASegmentation();
        } else {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.GenericSegmentation();
        }
        if (!path2ExternalFSM.equals("")) {
            segmentation.pathToExternalFSM = path2ExternalFSM;
        }
        List v = segmentation.getSegmentationErrors(btd.getEXMARaLDAbt());
        for (Object o : v) {
            FSMException fsme = (FSMException) o;
            String text = fsme.getMessage();
            stats.addCritical(EXB_SEG, cd, text);
            exmaError.addError(EXB_SEG, filename, fsme.getTierID(), fsme.getTLI(), false, text);
        }
        return stats;
    }

    /**
     * Fix to create segmented exs from the exbs.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalFix(cd);
        } catch (SAXException saxe) {
            saxe.printStackTrace();
        } catch (JexmaraldaException je) {
            je.printStackTrace();
        } catch (IOException ex) {
            stats.addException(ex, "Unknown read error");
        } catch (JDOMException ex) {
            stats.addException(ex, "Unknown JDOM error");
        } catch (FSMException ex) {
            stats.addException(ex, "Unknown FSM error");
        } catch (TransformerException ex) {
             stats.addException(ex, "Unknown Transformer error");
        } catch (ParserConfigurationException ex) {
             stats.addException(ex, "Unknown Parser error");
        } catch (XPathExpressionException ex) {
             stats.addException(ex, "Unknown XPath error");
        }
        return stats;
    }
    
    
    public Report exceptionalFix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException, FSMException, TransformerException, ParserConfigurationException, UnsupportedEncodingException, XPathExpressionException {
        Report stats = new Report();
        btd = new BasicTranscriptionData(cd.getURL());
        if (segmentationName.equals("HIAT")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.HIATSegmentation();
        } else if (segmentationName.equals("GAT")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.GATSegmentation();
        } else if (segmentationName.equals("cGAT_MINIMAL")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.cGATMinimalSegmentation();
        } else if (segmentationName.equals("CHAT")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.CHATSegmentation();
        } else if (segmentationName.equals("CHAT_MINIMAL")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.CHATMinimalSegmentation();
        } else if (segmentationName.equals("DIDA")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.DIDASegmentation();
        } else if (segmentationName.equals("IPA")) {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.IPASegmentation();
        } else {
            segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.GenericSegmentation();
        }
        if (!path2ExternalFSM.equals("")) {
            segmentation.pathToExternalFSM = path2ExternalFSM;
        }
        List v = segmentation.getSegmentationErrors(btd.getEXMARaLDAbt());
        if (v.isEmpty()){
        SegmentedTranscription st = segmentation.BasicToSegmented(btd.getEXMARaLDAbt());
        URL url = new URL(cd.getParentURL() + cd.getFilenameWithoutFileEnding() + "_s.exs");
        CorpusIO cio = new CorpusIO();
        Document doc = TypeConverter.String2JdomDocument(st.toXML());
        cio.write(doc, url);
        stats.addCorrect(EXB_SEG, cd, "Exs successfully created at " + url);
        } else {
            for (Object o : v) {
            FSMException fsme = (FSMException) o;
            String text = fsme.getMessage();
            stats.addCritical(EXB_SEG, cd, text);
            exmaError.addError(EXB_SEG, filename, fsme.getTierID(), fsme.getTLI(), false, text);
        }
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
            Logger.getLogger(ExbSegmenter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

    public void setSegmentation(String s) {
        segmentationName = s;
    }

    public void setExternalFSM(String s) {
        path2ExternalFSM = s;
    }
}
