
package de.uni_hamburg.corpora;

public class StupidCorpusMagicianTester {
    
    String[] args ={"test", "test2"};
    
    public static void main(String[] args) {
        //one args needs to be the URL
        //URL url = new URL("file:///E:\\Anne\\DolganCorpus\\conv\\AkNN_KuNS_200212_LifeHandicraft_conv\\AkNN_KuNS_200212_LifeHandicraft_conv.exb");
        CorpusMagician corpuma = new CorpusMagician();
        args = new String[3];
        args[0] = "C:\\Users\\Ozzy\\Desktop\\Demo-Corpus";
        args[1] = "C:\\Users\\Ozzy\\Desktop\\report\\report.txt";
        //args[2] = "filecoveragechecker";
        args[2] = "tierchecker";
        //args[0] = "E:\\Anne\\NganasanCorpus_HZSK_master\\NSLC_SubCorp.coma";
        //args[1] = "-verbose";
        corpuma.main(args);
        //ComaNSLinksChecker cnslc = new ComaNSLinksChecker();
        //cnslc.main(args);
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
        //System.out.println(corpuma.runCorpusFunctqion(cd, cf, true).getFullReports());
        
        //one args needs to be a string for the wanted corpus function
        //how do we align/code the checks with strings?
        //CorpusFunction cf = new Checker(args[1]);
        //corpuma.runCorpusFunction(corpus, cf);
    }
}