package de.uni_hamburg.corpora;

//import de.uni_hamburg.corpora.validation.CmdiChecker;
//import de.uni_hamburg.corpora.validation.ComaAddTiersFromExbsCorrector;
import de.uni_hamburg.corpora.validation.ComaApostropheChecker;
import de.uni_hamburg.corpora.validation.ComaNSLinksChecker;
import de.uni_hamburg.corpora.validation.ComaOverviewGeneration;

//import de.uni_hamburg.corpora.validation.ComaNameChecker;
import de.uni_hamburg.corpora.validation.ComaPIDLengthChecker;
import de.uni_hamburg.corpora.validation.ComaSegmentCountChecker;
import de.uni_hamburg.corpora.validation.ExbFileReferenceChecker;
//import de.uni_hamburg.corpora.validation.ExbPatternChecker;
//import de.uni_hamburg.corpora.validation.ExbSegmentationChecker;
//import de.uni_hamburg.corpora.validation.ExbStructureChecker;
import de.uni_hamburg.corpora.validation.FileCoverageChecker;
//import de.uni_hamburg.corpora.validation.FilenameChecker;
//import de.uni_hamburg.corpora.validation.NgexmaraldaCorpusChecker;
import de.uni_hamburg.corpora.validation.PrettyPrintData;
import de.uni_hamburg.corpora.validation.RemoveAbsolutePaths;
import de.uni_hamburg.corpora.validation.RemoveAutoSaveExb;
//import de.uni_hamburg.corpora.validation.TierChecker;
//import de.uni_hamburg.corpora.validation.TierCheckerWithAnnotation;
import de.uni_hamburg.corpora.validation.XSLTChecker;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.xml.sax.SAXException;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * This class has a Corpus and a Corpus Function as a field and is able to run a
 * Corpus Function on a corpus in a main method.
 *
 * @author fsnv625
 */
public class CorpusMagician {

    //the whole corpus I want to run checks on
    Corpus corpus;
    //one file I want to run a check on 
    CorpusData corpusData;
    //all functions there are in the code
    Collection<String> allExistingCFs;
    //all functions that should be run
    static Collection<String> chosencorpusfunctions = new ArrayList();
    static Collection<CorpusFunction> corpusfunctions = new ArrayList();
    //the final Report
    static Report report = new Report();
    //a list of all the available corpus data (no java objects, just URLs)
    static ArrayList<URL> alldata = new ArrayList();
    static CorpusIO cio = new CorpusIO();
    static boolean fixing = false;
    static CommandLine cmd = null;
    //the final Exmaralda error list
    public static ExmaErrorList exmaError = new ExmaErrorList();

    public CorpusMagician() {
    }

    //TODO main method
    //TODO we need a webservice for this functionality too
    //in the furture (for repo and external users)
    //this should work via commandline like that:
    //java -cp hzsk-corpus-services-0.1.1.jar de.uni_hamburg.corpora.validation.CorpusMagician {File:///URLtocorpusfolder} 
    //%cd%/report.txt(where and how report should be stored) PrettyPrintDataFix ComaNSLinkChecker(Functions that should be run) 
    public static void main(String[] args) {

        //first args needs to be the URL
        //check if it's a filepath, we could just convert it to an url
        createCommandLineOptions(args);
        try {
            String urlstring = cmd.getOptionValue("input");
            URL url;
            fixing = cmd.hasOption("f");
            if (urlstring.startsWith("file://")) {
                url = new URL(urlstring);
            } else {
                url = Paths.get(urlstring).toUri().toURL();
            }
            CorpusMagician corpuma = new CorpusMagician();
            //now the place where Report should end up
            //also allow normal filepaths and convert them
            String reportstring = cmd.getOptionValue("output");
            URL reportlocation;
            if (reportstring.startsWith("file://")) {
                reportlocation = new URL(reportstring);
            } else {
                reportlocation = Paths.get(reportstring).toUri().toURL();
            }
            //now add the functionsstrings to array
            String[] corpusfunctionarray = cmd.getOptionValues("c");
            for (String cf : corpusfunctionarray) {
                //corpuma.chosencorpusfunctions.add("test");
                corpuma.chosencorpusfunctions.add(cf);
                System.out.println(corpuma.chosencorpusfunctions.toString());
            }
            corpusfunctions = corpusFunctionStrings2Classes();
            
            
            
            //here is the heap space problem: everything is read all at one
            //and kept in the heap space the whole time
            corpuma.initCorpusWithURL(url);
            //and here is another problem, all the corpusfiles are given as objects
            report = corpuma.runChosencorpusfunctions();
            //this is a possible solution, but not working yet
            /*
            if (cmd.hasOption("s")) {
                corpuma.initCorpusWithURL(url);
                //and here is another problem, all the corpusfiles are given as objects
                report = corpuma.runChosencorpusfunctions();
            } else {
                //if we don't have so much heap space, we want things to be slower
                //so we just save a string array lsit of all the available files/urls/datastreams
                alldata = corpuma.createListofData(url);
                for (URL allurl : alldata) {
                    try {
                        File f = new File(allurl.getFile());
                        CorpusData cd;
                        cd = cio.toCorpusData(f);
                        if (cd != null) {
                            if (fixing) {
                                report.merge(runCorpusFunctions(cd, corpusfunctions, true));
                            } else {
                                report.merge(runCorpusFunctions(cd, corpusfunctions));
                            }
                        }
                    } catch (SAXException ex) {
                        Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JexmaraldaException ex) {
                        Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
            */
            System.out.println(report.getFullReports());
            String reportOutput;
            if (reportlocation.getFile().endsWith("html")) {
                reportOutput = ReportItem.generateDataTableHTML(report.getRawStatistics());
                cio.write(reportOutput, reportlocation);
            } else {
                //reportOutput = report.getSummaryLines() + "\n" + report.getErrorReports();
                reportOutput = report.getSummaryLines() + "\n" + report.getFullReports();
                cio.write(reportOutput, reportlocation);
            }
            //create the error list file
            //needs to be OS independent
            String errorstring = new File(reportstring).getParent() + File.separator + "errorlist.xml";
            URL errorlistlocation = Paths.get(errorstring).toUri().toURL();
            exmaError.createFullErrorList(errorlistlocation);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

//Give it a path to a parameters file that tells you
//which functions with which parameters should be
//run on which files
    public void readConfig(URL url) {
        //this depends on how this file will be structured
    }

    //this one can write a configfile with the workflow in the
    //selected format
    public void writeConfig(URL url) {
        //needs to have more params
        //this depends on how this file will be structured
    }

    public void registerCorpusFunction(CorpusFunction cf) {
        allExistingCFs.add(cf.getClass().getName());
    }

    //creates a new empty corpus object
    public void initCorpus() {
        corpus = new Corpus();
    }

    //creates a corpus object from an URL (filepath or "real" url)
    public void initCorpusWithURL(URL url) {
        corpus = new Corpus(url);
    }

    //creates a list of all the available data from an url (being a file oder directory)
    public ArrayList<URL> createListofData(URL url) {
        //add just that url if its a file
        //adds the urls recursively if its a directory
        return cio.URLtoList(url);
    }

    //checks which functions exist in the code by checking for implementations of the corpus function interface
    //this shows that it doesn't work to just check for implementations of corpus functions
    //probably need to check for implementations of CorpusFunction?
    //TODO
    public Collection<String> getAllExistingCFs() {

        this.allExistingCFs = new ArrayList<String>();
        allExistingCFs.add("ComaApostropheChecker");
        allExistingCFs.add("ComaNSLinksChecker");
        allExistingCFs.add("ComaOverviewGeneration");
        allExistingCFs.add("ComaSegmentCountChecker");
        allExistingCFs.add("ExbFileReferenceChecker");
        allExistingCFs.add("FileCoverageChecker");
        allExistingCFs.add("PrettyPrintData");
        allExistingCFs.add("RemoveAbsolutePaths");
        allExistingCFs.add("RemoveAutoSaveExb");
        allExistingCFs.add("XSLTChecker");
        //allExistingCFs.add("ExbPatternChecker");
        //allExistingCFs.add("ExbSegmentationChecker");
        //allExistingCFs.add("ExbStructureChecker");       
        //allExistingCFs.add("ComaAddTiersFromExbsCorrector");
        //allExistingCFs.add("ComaErrorReportGenerator");
        //allExistingCFs.add("SchematronChecker");
        //allExistingCFs.add("TierChecker");
        //allExistingCFs.add("ComaNameChecker");
        //allExistingCFs.add("TierCheckerWithAnnotation");
        //allExistingCFs.add("FilenameChecker");
        allExistingCFs.add("ComaPIDLengthChecker");
        //allExistingCFs.add("CmdiChecker");
        //allExistingCFs.add("NgexmaraldaCorpusChecker");
//        Reflections reflections = new Reflections("de.uni_hamburg.corpora");
//        Set<Class<? extends CorpusFunction>> classes = reflections.getSubTypesOf(CorpusFunction.class);
//        for (Class c : classes) {
//            System.out.println(c.toString());
//            try {
//                Constructor cons = c.getConstructor();
//                try {
//                    CorpusFunction cf = (CorpusFunction) cons.newInstance();
//                    allExistingCFs.add(cf.getClass().getName());
//                } catch (InstantiationException ex) {
//                    Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IllegalAccessException ex) {
//                    Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IllegalArgumentException ex) {
//                    Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (InvocationTargetException ex) {
//                    Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            } catch (NoSuchMethodException ex) {
//                Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (SecurityException ex) {
//                Logger.getLogger(CorpusMagician.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        for (String cf : allExistingCFs) {
            System.out.println(cf);
        }

        return allExistingCFs;
    }

    //TODO checks which functions can be run on specified data
    public Collection<CorpusFunction> getUsableFunctions(CorpusData cd) {
        //cf.IsUsableFor();
        //some switch or if else statements for the possible java objects 
        //and a list(?) which function can be apllied to what/which functions exist?
        Collection<CorpusFunction> usablecorpusfunctions = null;
        return usablecorpusfunctions;
    }

    //TODO return default functions, this is a list that needs to be somewhere
    //or maybe its an option a corpusfunction can have?
    public Collection<CorpusFunction> getDefaultUsableFunctions() {
        Collection<CorpusFunction> defaultcorpusfunctions = null;
        return defaultcorpusfunctions;
    }

    //TODO a dialog to choose functions you want to apply
    public Collection<String> chooseFunctionDialog() {
        chosencorpusfunctions = null;
        //add the chosen Functions
        return chosencorpusfunctions;
    }

    public static Collection<CorpusFunction> corpusFunctionStrings2Classes() {
        for (String function : chosencorpusfunctions) {
            switch (function.toLowerCase()) {                
                case "comaapostrophechecker":
                    ComaApostropheChecker cac = new ComaApostropheChecker();
                    corpusfunctions.add(cac);
                    break;
                 case "comanslinkschecker":
                    ComaNSLinksChecker cnslc = new ComaNSLinksChecker();
                    corpusfunctions.add(cnslc);
                    break;
                case "comaoverviewgeneration":
                    ComaOverviewGeneration cog = new ComaOverviewGeneration();
                    corpusfunctions.add(cog);
                    break;
                case "comasegmentcountchecker":
                    ComaSegmentCountChecker cscc = new ComaSegmentCountChecker();
                    corpusfunctions.add(cscc);
                    break;
                case "exbfilereferencechecker":
                    ExbFileReferenceChecker efrc = new ExbFileReferenceChecker();
                    corpusfunctions.add(efrc);
                    break;
                case "filecoveragechecker":
                    FileCoverageChecker fcc = new FileCoverageChecker();
                    corpusfunctions.add(fcc);
                    break;
                case "prettyprintdata":
                    PrettyPrintData pd = new PrettyPrintData();
                    corpusfunctions.add(pd);
                    break;
                case "removeabsolutepaths":
                    RemoveAbsolutePaths rap = new RemoveAbsolutePaths();
                    corpusfunctions.add(rap);
                    break;
                case "removeautosaveexb":
                    RemoveAutoSaveExb rase = new RemoveAutoSaveExb();
                    corpusfunctions.add(rase);
                    break;
                case "xsltchecker":
                    XSLTChecker xc = new XSLTChecker();
                    corpusfunctions.add(xc);
                    break;
                case "comapidlengthchecker":
                    ComaPIDLengthChecker cplc = new ComaPIDLengthChecker();
                    corpusfunctions.add(cplc);
                    break;
                /* 
                case "comaaddtiersfromexbscorrector":
                    ComaAddTiersFromExbsCorrector catfec = new ComaAddTiersFromExbsCorrector();
                    corpusfunctions.add(catfec);
                    break;
                case "tierchecker":
                    TierChecker tc = new TierChecker();
                    corpusfunctions.add(tc);
                    break;
                case "comanamechecker":
                    ComaNameChecker cnc = new ComaNameChecker();
                    corpusfunctions.add(cnc);
                    break;               
                case "tiercheckerwithannotation":
                    TierCheckerWithAnnotation tcwa = new TierCheckerWithAnnotation();
                    corpusfunctions.add(tcwa);
                    break;
                case "filenamechecker":
                    FilenameChecker fnc = new FilenameChecker();
                    corpusfunctions.add(fnc);
                    break;
                case "exbpatternchecker":
                    ExbPatternChecker epc = new ExbPatternChecker();
                    report.merge(runCorpusFunction(corpus, epc));
                    break;
                case "exbsegmentationchecker":
                    ExbSegmentationChecker esg = new ExbSegmentationChecker();
                    corpusfunctions.add(esg);
                    break;
                case "exbstructurechecker":
                    ExbStructureChecker esc = new ExbStructureChecker();
                    corpusfunctions.add(esc);
                    break;
                case "cmdichecker":
                    CmdiChecker cmdi = new CmdiChecker();
                    corpusfunctions.add(cmdi);
                    break;
                case "ngexmaraldacorpuschecker":
                    NgexmaraldaCorpusChecker ngex = new NgexmaraldaCorpusChecker();
                    corpusfunctions.add(ngex);
                    break; */
                default:
                    report.addCritical("CommandlineFunctionality", "Function String \"" + function + "\" is not recognized");
            }
        }
        return corpusfunctions;
    }

    //TODO
    //run the chosen functions on the chosen corpus
    Report runChosencorpusfunctions() {
        for (CorpusFunction function : corpusfunctions) {
            if (fixing) {
                report.merge(runCorpusFunction(corpus, function, true));
            } else {
                report.merge(runCorpusFunction(corpus, function));
            }
        }
        return report;
    }

    //run multiple functions on a corpus, that means all the files in the corpus
    //the function can run on 
    public Report runCorpusFunction(Corpus c, Collection<CorpusFunction> cfc) {
        Report report = new Report();
        for (CorpusFunction cf : cfc) {
            Report newReport = runCorpusFunction(c, cf);
            report.merge(newReport);
        }
        return report;
    }

    //run multiple functions on the set corpus, that means all the files in the corpus
    //the function can run on 
    public Report runCorpusFunction(Collection<CorpusFunction> cfc) {
        Report report = new Report();
        for (CorpusFunction cf : cfc) {
            Report newReport = runCorpusFunction(corpus, cf);
            report.merge(newReport);
        }
        return report;
    }

    //run one function on a corpus, that means all the files in the corpus
    //the funciton can run on 
    public Report runCorpusFunction(Corpus c, CorpusFunction cf) {
        Report report = new Report();
        //find out on which objects this corpus function can run
        //choose those from the corpus 
        //and run the checks on those files recursively
        for (Class cl : cf.getIsUsableFor()) {
            for (CorpusData cd : c.getCorpusData()) //if the corpus files are an instance 
            //of the class cl, run the function
            {
                if (cl.isInstance(cd)) {
                    Report newReport = runCorpusFunction(cd, cf);
                    report.merge(newReport);
                }
            }
        }
        return report;
    }

    //run one function on a corpus, that means all the files in the corpus
    //the funciton can run on 
    public Report runCorpusFunction(Corpus c, CorpusFunction cf, boolean fix) {
        Report report = new Report();
        //find out on which objects this corpus function can run
        //choose those from the corpus 
        //and run the checks on those files recursively
        for (Class cl : cf.getIsUsableFor()) {
            for (CorpusData cd : c.getCorpusData()) //if the corpus files are an instance 
            //of the class cl, run the function
            {
                if (cd != null && cl.isInstance(cd)) {
                    Report newReport = runCorpusFunction(cd, cf, fix);
                    report.merge(newReport);
                }
            }
        }
        return report;
    }

    //run one function on a corpus, that means all the files in the corpus
    //the function can run on 
    public Report runCorpusFunction(CorpusFunction cf) {
        Report report = new Report();
        //find out on which objects this corpus function can run
        //choose those from the corpus 
        //and run the checks on those files recursively
        for (Class cl : cf.getIsUsableFor()) {
            Report newReport = runCorpusFunction(corpus, cf);
            report.merge(newReport);
        }
        return report;
    }

    public Report runCorpusFunction(CorpusData cd, CorpusFunction cf) {
        return cf.execute(cd);
    }

    public Report runCorpusFunction(CorpusData cd, CorpusFunction cf, boolean fix) {
        return cf.execute(cd, fix);
    }

    public static Report runCorpusFunctions(CorpusData cd, Collection<CorpusFunction> cfc) {
        Report report = new Report();
        for (CorpusFunction cf : cfc) {
            Report newReport = (cf.execute(cd));
            report.merge(newReport);
        }
        return report;
    }

    public static Report runCorpusFunctions(CorpusData cd, Collection<CorpusFunction> cfc, boolean fix) {
        Report report = new Report();
        for (CorpusFunction cf : cfc) {
            Report newReport = (cf.execute(cd, fix));
            report.merge(newReport);
        }
        return report;
    }

    //TODO 
    //to save individual corpusparameters in a file
    //and maybe also save the functions todos there
    public void readParameters() {

    }

    public void setCorpusData(CorpusData corpusData) {
        this.corpusData = corpusData;
    }

    public void setChosencorpusfunctions(Collection<String> chosencorpusfunctions) {
        this.chosencorpusfunctions = chosencorpusfunctions;
    }

    public Corpus getCorpus() {
        return corpus;
    }

    public CorpusData getCorpusData() {
        return corpusData;
    }

    public Collection<String> getChosencorpusfunctions() {
        return chosencorpusfunctions;
    }

    private static void createCommandLineOptions(String[] args) {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(false);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        Option corpusfunction = new Option("c", "corpusfunction", true, "corpus function");
        // Set option c to take 1 to oo arguments
        corpusfunction.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(corpusfunction);

        /*
        Option speed = new Option("s", "speed", false, "faster but more heap space");
        speed.setRequired(false);
        options.addOption(speed);
        */
        
        Option fix = new Option("f", "fix", false, "fixes problems automatically");
        fix.setRequired(false);
        options.addOption(fix);

        Option help = new Option("h", "help", false, "display help");
        fix.setRequired(false);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("hzsk-corpus-services", options);
            System.exit(1);
        }

        if (cmd.hasOption("h")) {
            // automatically generate the help statement
            formatter.printHelp("hzsk-corpus-services", options);
        }
        /*
        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        
        System.out.println(inputFilePath);
        System.out.println(outputFilePath);
         */

    }

}
