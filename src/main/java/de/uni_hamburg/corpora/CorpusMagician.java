package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.validation.CmdiChecker;
import de.uni_hamburg.corpora.publication.ZipCorpus;
import de.uni_hamburg.corpora.conversion.EXB2HIATISOTEI;
import de.uni_hamburg.corpora.conversion.EXB2INELISOTEI;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.validation.ComaApostropheChecker;
import de.uni_hamburg.corpora.validation.ComaNSLinksChecker;
import de.uni_hamburg.corpora.validation.ComaOverviewGeneration;
import de.uni_hamburg.corpora.validation.ComaNameChecker;
import de.uni_hamburg.corpora.validation.GenerateAnnotationPanel;
import de.uni_hamburg.corpora.validation.ComaPIDLengthChecker;
import de.uni_hamburg.corpora.validation.ComaSegmentCountChecker;
import de.uni_hamburg.corpora.validation.ExbFileReferenceChecker;
import de.uni_hamburg.corpora.validation.ExbAnnotationPanelCheck;
//import de.uni_hamburg.corpora.validation.ExbPatternChecker;
//import de.uni_hamburg.corpora.validation.ExbSegmentationChecker;
//import de.uni_hamburg.corpora.validation.ExbStructureChecker;
import de.uni_hamburg.corpora.validation.FileCoverageChecker;
import de.uni_hamburg.corpora.validation.FilenameChecker;
import de.uni_hamburg.corpora.validation.IAAFunctionality;
import de.uni_hamburg.corpora.validation.ExbNormalize;
//import de.uni_hamburg.corpora.validation.NgexmaraldaCorpusChecker;
import de.uni_hamburg.corpora.validation.PrettyPrintData;
import de.uni_hamburg.corpora.validation.RemoveAbsolutePaths;
import de.uni_hamburg.corpora.validation.RemoveAutoSaveExb;
import de.uni_hamburg.corpora.validation.TierChecker;
import de.uni_hamburg.corpora.validation.TierCheckerWithAnnotation;
import de.uni_hamburg.corpora.validation.XSLTChecker;
import de.uni_hamburg.corpora.validation.CorpusDataRegexReplacer;
import de.uni_hamburg.corpora.validation.ExbEventLinebreaksChecker;
import de.uni_hamburg.corpora.validation.ExbMakeTimelineConsistent;
import de.uni_hamburg.corpora.visualization.CorpusHTML;
import de.uni_hamburg.corpora.visualization.ListHTML;
import de.uni_hamburg.corpora.visualization.ScoreHTML;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.Document;
import org.xml.sax.SAXException;

/**
 * This class has a Corpus and a Corpus Function as a field and is able to run a
 * Corpus Function on a corpus in a main method.
 *
 * @author fsnv625
 */
public class CorpusMagician {

    //the whole corpus I want to run checks on
    static Corpus corpus;
    //Basedirectory if it exists
    static URL basedirectory;
    //one file I want to run a check on
    CorpusData corpusData;
    //all functions there are in the code
    static Collection<String> allExistingCFs = new ArrayList<String>();
    //all functions that should be run
    static Collection<String> chosencorpusfunctions = new ArrayList<String>();
    static Collection<CorpusFunction> corpusfunctions = new ArrayList<CorpusFunction>();
    //the final Report
    static Report report = new Report();
    //a list of all the available corpus data (no java objects, just URLs)
    static ArrayList<URL> alldata = new ArrayList<URL>();
    static CorpusIO cio = new CorpusIO();
    static boolean fixing = false;
    static boolean iserrorsonly = false;
    static CommandLine cmd = null;
    //the final Exmaralda error list
    public static ExmaErrorList exmaError = new ExmaErrorList();
    static Properties cfProperties;

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
            iserrorsonly = cmd.hasOption("e");
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
                CorpusMagician.chosencorpusfunctions.add(cf);
                System.out.println(CorpusMagician.chosencorpusfunctions.toString());
            }
            corpusfunctions = corpusFunctionStrings2Classes();

            //here is the heap space problem: everything is read all at one
            //and kept in the heap space the whole time
            corpuma.initCorpusWithURL(url);
            //get the basedirectory if there is a coma file
            if (!(corpus.getMetadata().isEmpty())) {
                Metadata md = corpus.getMetadata().iterator().next();
                basedirectory = md.getParentURL();
            }
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
                if (iserrorsonly) {
                    //ToDo
                    //reportOutput = ReportItem.generateDataTableHTML(report.getErrorStatistics(basedirectory), report.getSummaryLines());
                    reportOutput = ReportItem.generateDataTableHTML(report.getErrorStatistics(), report.getSummaryLines());                    
                } else {
                    reportOutput = ReportItem.generateDataTableHTML(report.getRawStatistics(), report.getSummaryLines());
                }
            } else {
                //reportOutput = report.getSummaryLines() + "\n" + report.getErrorReports();
                reportOutput = report.getSummaryLines() + "\n" + report.getFullReports();
            }
            String absoluteReport = reportOutput;
            if(absoluteReport!=null && basedirectory!=null && absoluteReport.contains(basedirectory.toString())){
            absoluteReport = reportOutput.replaceAll(basedirectory.toString(), "");
            }
            if(absoluteReport!=null){
            cio.write(absoluteReport, reportlocation);
            }
            //create the error list file
            //needs to be OS independent
            //There is an error for me when running on windows: \null gets created
            URL errorlistlocation = new URL(basedirectory + "/" + "CorpusServices_Errors.xml");            
            Document exmaErrorList = TypeConverter.W3cDocument2JdomDocument(ExmaErrorList.createFullErrorList());
            if (exmaErrorList != null){
            cio.write(exmaErrorList, errorlistlocation);
            System.out.println("Wrote ErrorList at " + errorlistlocation);
            }           
        } catch (MalformedURLException ex) {
            report.addException(ex, "The given URL was incorrect");
        } catch (IOException ex) {
            report.addException(ex, "A file could not be read");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, "A file could not be parsed");
        } catch (TransformerException ex) {
            report.addException(ex, "A transformation error occured");       
        } catch (SAXException ex) {
            report.addException(ex, "An XSLT error occured");
        } catch (JexmaraldaException ex) {
            report.addException(ex, "A Exmaralda file reading error occured");
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
    public void initCorpusWithURL(URL url) throws MalformedURLException, SAXException, JexmaraldaException {
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
    public static Collection<String> getAllExistingCFs() {
        allExistingCFs.add("ComaApostropheChecker");
        allExistingCFs.add("ComaNSLinksChecker");
        allExistingCFs.add("ComaOverviewGeneration");
        allExistingCFs.add("ZipCorpus");
        allExistingCFs.add("ComaSegmentCountChecker");
        allExistingCFs.add("ExbFileReferenceChecker");
        allExistingCFs.add("ExbAnnotationPanelCheck");
        allExistingCFs.add("EXB2INELISOTEI");
        allExistingCFs.add("EXB2HIATISOTEI");
        allExistingCFs.add("FileCoverageChecker");
        allExistingCFs.add("FileCoverageCheckerInel");
        allExistingCFs.add("NormalizeEXB");
        allExistingCFs.add("PrettyPrintData");
        allExistingCFs.add("RemoveAbsolutePaths");
        allExistingCFs.add("RemoveAutoSaveExb");
        allExistingCFs.add("XSLTChecker");
        allExistingCFs.add("FilenameChecker");
        allExistingCFs.add("CmdiChecker");
        allExistingCFs.add("ComaNameChecker");
        allExistingCFs.add("TierCheckerWithAnnotation");
        allExistingCFs.add("TierChecker");
        allExistingCFs.add("XsltCheckerInel");
        allExistingCFs.add("GenerateAnnotationPanel");
        allExistingCFs.add("CorpusDataRegexReplacer");
        allExistingCFs.add("ScoreHTML");
        allExistingCFs.add("CorpusHTML");
        allExistingCFs.add("IAAFunctionality");
        allExistingCFs.add("ListHTML");
        allExistingCFs.add("ExbEventLinebreaksChecker");
        allExistingCFs.add("MakeTimelineConsistent");
        return allExistingCFs;
    }

    public static String getAllExistingCFsAsString() {
        String all = "";
        for (Iterator<String> it = getAllExistingCFs().iterator(); it.hasNext();) {
            String s = it.next();
            all = all + "\n" + s;
        }
        return all;
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
                case "exbannotationpanelcheck":
                    ExbAnnotationPanelCheck eapc = new ExbAnnotationPanelCheck();
                    corpusfunctions.add(eapc);
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
		case "filenamechecker":
                    FilenameChecker fnc = new FilenameChecker();
                    corpusfunctions.add(fnc);
                    break;
		case "cmdichecker":
                    CmdiChecker cmdi = new CmdiChecker();
                    corpusfunctions.add(cmdi);
                    break;
                case "comapidlengthchecker":
                    ComaPIDLengthChecker cplc = new ComaPIDLengthChecker();
                    corpusfunctions.add(cplc);
                    break;
                case "comanamechecker":
                    ComaNameChecker cnc = new ComaNameChecker();
                    corpusfunctions.add(cnc);
                    break;
                case "tiercheckerwithannotation":
                    TierCheckerWithAnnotation tcwa = new TierCheckerWithAnnotation();
                    corpusfunctions.add(tcwa);
                case "tierchecker":
                    TierChecker tc = new TierChecker();
                    corpusfunctions.add(tc);
                case "xsltcheckerinel":
                    XSLTChecker xci = new XSLTChecker();
                    xci.setXSLresource("/xsl/inel-checks.xsl");
                    corpusfunctions.add(xci);
                    break;
                case "exb2inelisotei":
                    EXB2INELISOTEI eiit = new EXB2INELISOTEI();
                    corpusfunctions.add(eiit);
                    break;
                case "exb2inelisoteisel":
                    EXB2INELISOTEI eiitsel = new EXB2INELISOTEI();
                    eiitsel.setLanguage("sel");
                    corpusfunctions.add(eiitsel);
                    break;
                case "exb2inelisoteidlg":
                    EXB2INELISOTEI eiitdlg = new EXB2INELISOTEI();
                    eiitdlg.setLanguage("dlg");
                    corpusfunctions.add(eiitdlg);
                    break;
                case "exb2inelisoteixas":
                    EXB2INELISOTEI eiitxas = new EXB2INELISOTEI();
                    eiitxas.setLanguage("xas");
                    corpusfunctions.add(eiitxas);
                    break;
                case "exb2hiatisotei":
                    EXB2HIATISOTEI ehit = new EXB2HIATISOTEI();
                    corpusfunctions.add(ehit);
                    break;
                case "normalizeexb":
                    ExbNormalize ne = new ExbNormalize();
                     if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("whitespace")) {
                            ne.setfixWhiteSpaces(cfProperties.getProperty("whitespace"));
                            System.out.println("FixWhitespace set to " + cfProperties.getProperty("whitespace"));
                        }
                     }
                    corpusfunctions.add(ne);
                    break;
                case "generateannotationpanel":
                    GenerateAnnotationPanel gap = new GenerateAnnotationPanel();
                    corpusfunctions.add(gap);
                    break;
                case "iaafunctionality":
                    IAAFunctionality iaa = new IAAFunctionality();
                    corpusfunctions.add(iaa);
                    break;
                case "filecoveragecheckerinel":
                    FileCoverageChecker fcci = new FileCoverageChecker();
                    fcci.addFileEndingWhiteListString("flextext");
                    fcci.addWhiteListString("report-output.html");
                    fcci.addWhiteListString("Segmentation_Errors.xml");
                    fcci.addWhiteListString("Structure_Errors.xml");
                    corpusfunctions.add(fcci);
                    break;
                case "corpusdataregexreplacer":
                    //ToDo                   
                    CorpusDataRegexReplacer cdrr = new CorpusDataRegexReplacer();
                    //try custom properties for the different corpusfunctions
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("replace")) {
                            cdrr.setReplace(cfProperties.getProperty("replace"));
                            System.out.println("Replace set to " + cfProperties.getProperty("replace"));
                        }
                        if (cfProperties.containsKey("replacement")) {
                            System.out.println(cfProperties.getProperty("replacement"));
                            cdrr.setReplacement("Replacement set to " + cfProperties.getProperty("replacement"));
                        }
                        if (cfProperties.containsKey("xpathcontext")) {
                            cdrr.setXpathContext(cfProperties.getProperty("xpathcontext"));
                            System.out.println("Xpath set to " + cfProperties.getProperty("xpathcontext"));
                        }
                        if (cfProperties.containsKey("coma")) {
                            cdrr.setComa(cfProperties.getProperty("coma"));
                            System.out.println("Replace in Coma set to " + cfProperties.getProperty("xpathcontext"));
                        }
                    }
                    corpusfunctions.add(cdrr);
                    break;
                case "zipcorpus":
                    ZipCorpus zc = new ZipCorpus();
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("SOURCE_FOLDER")) {
                            zc.setSourceFolder(cfProperties.getProperty("SOURCE_FOLDER"));
                            System.out.println("Location of source folder set to " + cfProperties.getProperty("SOURCE_FOLDER"));
                        }
                        if (cfProperties.containsKey("OUTPUT_ZIP_FILE")) {                           
                            zc.setOutputFile(cfProperties.getProperty("OUTPUT_ZIP_FILE"));
                            System.out.println("Location of output file set to " + cfProperties.getProperty("OUTPUT_ZIP_FILE"));
                        }
                        if (cfProperties.containsKey("AUDIO")) {
                            zc.setWithAudio(cfProperties.getProperty("AUDIO"));
                            System.out.println("Should contain audio set to " + cfProperties.getProperty("AUDIO"));
                        }
                    }
                    corpusfunctions.add(zc);
                    break;
                case "scorehtml":
                    ScoreHTML shtml = new ScoreHTML();
                    corpusfunctions.add(shtml);
                    break;
                case "corpushtml":
                    CorpusHTML chtml = new CorpusHTML();
                    corpusfunctions.add(chtml);
                    break;
                case "listhtml":
                    ListHTML lhtml = new ListHTML();
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("SEGMENTATION")) {
                            lhtml.setSegmentation(cfProperties.getProperty("SEGMENTATION"));
                            System.out.println("Segmentation set to " + cfProperties.getProperty("SEGMENTATION"));
                        }
                    }
                    corpusfunctions.add(lhtml);
                    break;
                case "exbeventlinebreakschecker":
                    ExbEventLinebreaksChecker elb = new ExbEventLinebreaksChecker();
                    corpusfunctions.add(elb);
                    break;
                case "maketimelineconsistent":
                    ExbMakeTimelineConsistent emtc = new ExbMakeTimelineConsistent();
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("interpolate")) {
                             emtc.setInterpolateTimeline(cfProperties.getProperty("interpolate"));
                            System.out.println("FixWhitespace set to " + cfProperties.getProperty("interpolate"));
                        }
                     }
                    corpusfunctions.add(emtc);
                    break;
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
        for (Class<? extends CorpusData> cl : cf.getIsUsableFor()) {
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
        for (Class<? extends CorpusData> cl : cf.getIsUsableFor()) {
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
        for (Class<? extends CorpusData> cl : cf.getIsUsableFor()) {
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
        CorpusMagician.chosencorpusfunctions = chosencorpusfunctions;
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
        input.setRequired(true);
        input.setArgName("FILE PATH");
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(true);
        output.setArgName("FILE PATH");
        options.addOption(output);

        Option corpusfunction = new Option("c", "corpusfunction", true, "corpus function");
        // Set option c to take 1 to oo arguments
        corpusfunction.setArgs(Option.UNLIMITED_VALUES);
        corpusfunction.setArgName("CORPUS FUNCTION");
        corpusfunction.setRequired(true);
        corpusfunction.setValueSeparator(',');
        options.addOption(corpusfunction);

        /*
        Option speed = new Option("s", "speed", false, "faster but more heap space");
        speed.setRequired(false);
        options.addOption(speed);
         */
        Option propertyOption = Option.builder("p")
                .longOpt("property")
                .argName("property=value")
                .hasArgs()
                .valueSeparator()
                .numberOfArgs(2)
                .desc("use value for given properties")
                .build();

        options.addOption(propertyOption);

        Option fix = new Option("f", "fix", false, "fixes problems automatically");
        fix.setRequired(false);
        options.addOption(fix);

        Option help = new Option("h", "help", false, "display help");
        fix.setRequired(false);
        options.addOption(help);

        Option errorsonly = new Option("e", "errorsonly", false, "output only errors");
        fix.setRequired(false);
        options.addOption(errorsonly);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);

        String header = "Specify a corpus folder or file and a function to be applied\n\n";
        String footer = "\nthe available functions are:\n" + getAllExistingCFsAsString() + "\n\nPlease report issues at https://lab.multilingua.uni-hamburg.de/redmine/projects/corpus-services/issues";

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("hzsk-corpus-services", header, options, footer, true);
            System.exit(1);
        }

        if (cmd.hasOption("h")) {
            // automatically generate the help statement
            formatter.printHelp("hzsk-corpus-services", header, options, footer, true);
            System.exit(1);
        }
        if (cmd.hasOption("p")) {
            cfProperties = cmd.getOptionProperties("p");
        }
        /*
        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");

        System.out.println(inputFilePath);
        System.out.println(outputFilePath);
         */

    }

}
