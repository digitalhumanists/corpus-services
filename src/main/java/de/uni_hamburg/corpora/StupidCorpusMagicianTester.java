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
        try {
            //one args needs to be the URL
            URL url = new URL("file:///E:\\Anne\\DolganCorpus\\conv\\AkNN_KuNS_200212_LifeHandicraft_conv\\AkNN_KuNS_200212_LifeHandicraft_conv.exb");
            CorpusMagician corpuma = new CorpusMagician();

            corpuma.initCorpusWithURL(url);
            CorpusData cd = new BasicTranscriptionData();
            File f = new File(url.getFile());
            BasicTranscriptionData cdb;
            cdb = (BasicTranscriptionData) cd;
            cdb.loadFile(f);
            System.out.println(url);
            System.out.println(corpuma.getCorpus().toString());
            CorpusFunction cf = new PrettyPrintData();
            cd = (CorpusData) cdb;
             System.out.println(corpuma.runCorpusFunction(cd, cf, true).getFullReports());
            
            //one args needs to be a string for the wanted corpus function
            //how do we align/code the checks with strings?
            //CorpusFunction cf = new Checker(args[1]);
            //corpuma.runCorpusFunction(corpus, cf);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(StupidCorpusMagicianTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JexmaraldaException ex) {
            Logger.getLogger(StupidCorpusMagicianTester.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
