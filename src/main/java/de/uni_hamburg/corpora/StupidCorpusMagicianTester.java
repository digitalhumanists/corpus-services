/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.validation.PrettyPrintData;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class StupidCorpusMagicianTester {
    
    String[] args ={"test", "test2"};
    
    public static void main(String[] args) {
        //one args needs to be the URL
        //URL url = new URL("file:///E:\\Anne\\DolganCorpus\\conv\\AkNN_KuNS_200212_LifeHandicraft_conv\\AkNN_KuNS_200212_LifeHandicraft_conv.exb");
        CorpusMagician corpuma = new CorpusMagician();
        args = new String[3];
        args[0] = "file:///E:\\Anne\\DolganCorpus";
        args[1] = "file:///E:\\Anne\\DolganCorpus";
        args[2] = "PrettyPrintDataFix";
        corpuma.main(args);
        //corpuma.initCorpusWithURL(url);
        //CorpusData cd = new BasicTranscriptionData();
        //File f = new File(url.getFile());
        //BasicTranscriptionData cdb;
        //cdb = (BasicTranscriptionData) cd;
        //cdb.loadFile(f);
        //System.out.println(url);
        //System.out.println(corpuma.getCorpus().toString());
        //CorpusFunction cf = new PrettyPrintData();
        //corpuma.runChosencorpusfunctions();
        //cd = (CorpusData) cdb;
        //System.out.println(corpuma.runCorpusFunction(cd, cf, true).getFullReports());
        
        //one args needs to be a string for the wanted corpus function
        //how do we align/code the checks with strings?
        //CorpusFunction cf = new Checker(args[1]);
        //corpuma.runCorpusFunction(corpus, cf);
    }
}
