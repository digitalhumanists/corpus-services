/**
 * @file ExbErrorChecker.java
 *
 * A command-line tool / non-graphical interface for checking errors in
 * exmaralda's EXB files.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CommandLineable;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.Option;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;

/**
 * A command-line tool for checking EXB files.
 */
public class ExbSegmentationChecker extends Checker implements CommandLineable, CorpusFunction {

    static String filename;
    static BasicTranscription bt;
    static File exbfile;
    static ValidatorSettings settings;
    final String EXB_SEG = "exb-segmentation-checker";

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
    * Default check function which calls the exceptionalCheck function so that the
    * primal functionality of the feature can be implemented, and additionally 
    * checks for parser configuration, SAXE and IO exceptions.
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
    * Main feature of the class: Checks Exmaralda .exb file for segmentation problems.
    */  
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, JexmaraldaException {
        Report stats = new Report();
        File f = new File(cd.getURL().toString());
        filename = f.getPath().substring(6);
        bt = new BasicTranscription(filename);

        /** TODO: actually check the file:
         * Code snippet from Exmaralda:
         * 
         * exmaralda\src\org\exmaralda\common\corpusbuild\comafunctions\SegmentationErrorsChecker.java
         * 
         * 
         * package org.exmaralda.common.corpusbuild.comafunctions;

import java.net.URISyntaxException;
import java.util.Vector;
import org.exmaralda.common.corpusbuild.AbstractCorpusChecker;
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.segment.AbstractSegmentation;
import org.xml.sax.SAXException;


public class SegmentationErrorsChecker extends AbstractCorpusChecker {

    AbstractSegmentation segmentation;

    public SegmentationErrorsChecker(String segmentationName){
        this(segmentationName, "");
    }
    
    public SegmentationErrorsChecker(String segmentationName, String customFSMPath){
        super();
        if (customFSMPath==null || customFSMPath.length()==0){
            if (segmentationName.equals("HIAT")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.HIATSegmentation();
            } else if (segmentationName.equals("GAT")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.GATSegmentation();
            } else if (segmentationName.equals("cGAT_MINIMAL")) {
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.cGATMinimalSegmentation();
            } else if (segmentationName.equals("CHAT")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.CHATSegmentation();
            } else if (segmentationName.equals("CHAT_MINIMAL")) {
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.CHATMinimalSegmentation();
            } else if (segmentationName.equals("DIDA")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.DIDASegmentation();
            } else if (segmentationName.equals("IPA")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.IPASegmentation();
            } else {
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.GenericSegmentation();
            }
        } else {
            if (segmentationName.equals("HIAT")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.HIATSegmentation(customFSMPath);
            } else if (segmentationName.equals("GAT")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.GATSegmentation(customFSMPath);
            } else if (segmentationName.equals("cGAT_MINIMAL")) {
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.cGATMinimalSegmentation(customFSMPath);
            } else if (segmentationName.equals("CHAT")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.CHATSegmentation(customFSMPath);
            } else if (segmentationName.equals("CHAT_MINIMAL")) {
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.CHATMinimalSegmentation(customFSMPath);
            } else if (segmentationName.equals("DIDA")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.DIDASegmentation(customFSMPath);
            } else if (segmentationName.equals("IPA")){
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.IPASegmentation(customFSMPath);
            } else {
                segmentation = new org.exmaralda.partitureditor.jexmaralda.segment.GenericSegmentation(customFSMPath);
            }            
        }

    }

    @Override
    public void processTranscription(BasicTranscription bt, String currentFilename) throws URISyntaxException, SAXException {
        Vector v = segmentation.getSegmentationErrors(bt);
        for (Object o : v){
            FSMException fsme = (FSMException)o;
            String text = fsme.getMessage();
            addError(currentFilename, fsme.getTierID(), fsme.getTLI(), text);
        }
    }
         * 
         * 
         * 
         * 
         * 
         * 
         * 
         * 
        }                                    
    }
         */
        return stats;
    }
    
    /**
    * No fix is applicable for this feature.
    */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        report.addCritical(EXB_SEG,
                "Automatic fix is not yet supported.");
        return report;
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
            Logger.getLogger(ExbSegmentationChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

}