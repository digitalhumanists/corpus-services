package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.validation.ComaAddTiersFromExbsCorrector;
import de.uni_hamburg.corpora.validation.CmdiChecker;
import de.uni_hamburg.corpora.publication.ZipCorpus;
import de.uni_hamburg.corpora.conversion.EXB2HIATISOTEI;
import de.uni_hamburg.corpora.conversion.EXB2INELISOTEI;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.validation.ComaApostropheChecker;
import de.uni_hamburg.corpora.validation.ComaNSLinksChecker;
import de.uni_hamburg.corpora.validation.ComaOverviewGeneration;
import de.uni_hamburg.corpora.validation.ComaXsdChecker;
import de.uni_hamburg.corpora.validation.GenerateAnnotationPanel;
import de.uni_hamburg.corpora.validation.ComaFedoraIdentifierLengthChecker;
import de.uni_hamburg.corpora.validation.ComaSegmentCountChecker;
import de.uni_hamburg.corpora.validation.ExbFileReferenceChecker;
import de.uni_hamburg.corpora.validation.ExbFileCoverageChecker;
import de.uni_hamburg.corpora.validation.ExbAnnotationPanelCheck;
import de.uni_hamburg.corpora.validation.ExbRefTierChecker;
import de.uni_hamburg.corpora.validation.CalculateAnnotatedTime;
import de.uni_hamburg.corpora.validation.ExbSegmentationChecker;
import de.uni_hamburg.corpora.validation.ExbStructureChecker;
import de.uni_hamburg.corpora.validation.ComaFileCoverageChecker;
import de.uni_hamburg.corpora.validation.FilenameChecker;
import de.uni_hamburg.corpora.validation.IAAFunctionality;
import de.uni_hamburg.corpora.validation.ExbNormalize;
import de.uni_hamburg.corpora.validation.NgexmaraldaCorpusChecker;
import de.uni_hamburg.corpora.validation.NgTierCheckerWithAnnotation;
import de.uni_hamburg.corpora.validation.PrettyPrintData;
import de.uni_hamburg.corpora.validation.RemoveAbsolutePaths;
import de.uni_hamburg.corpora.validation.RemoveAutoSaveExb;
import de.uni_hamburg.corpora.validation.ExbTierDisplayNameChecker;
import de.uni_hamburg.corpora.validation.ComaTiersDescriptionAnnotationPanelChecker;
import de.uni_hamburg.corpora.validation.XSLTChecker;
import de.uni_hamburg.corpora.validation.CorpusDataRegexReplacer;
import de.uni_hamburg.corpora.validation.ExbEventLinebreaksChecker;
import de.uni_hamburg.corpora.validation.ExbMakeTimelineConsistent;
import de.uni_hamburg.corpora.validation.ExbScriptMixChecker;
import de.uni_hamburg.corpora.visualization.CorpusHTML;
import de.uni_hamburg.corpora.visualization.ListHTML;
import de.uni_hamburg.corpora.visualization.ScoreHTML;
import de.uni_hamburg.corpora.validation.ComaKmlForLocations;
import de.uni_hamburg.corpora.conversion.AddCSVMetadataToComa;
import de.uni_hamburg.corpora.utilities.PrettyPrinter;
import de.uni_hamburg.corpora.validation.ComaTierOverviewCreator;
import de.uni_hamburg.corpora.validation.GeneralTransformer;
import de.uni_hamburg.corpora.validation.RemoveEmptyEvents;
import de.uni_hamburg.corpora.validation.ComaTranscriptionsNameChecker;
import de.uni_hamburg.corpora.validation.ExbMP3Next2WavAdder;
import de.uni_hamburg.corpora.validation.ExbSegmenter;
import de.uni_hamburg.corpora.visualization.HScoreHTML;
import de.uni_hamburg.corpora.validation.ReportStatistics;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.nio.file.Paths;
import java.util.Collections;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
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
    static PrettyPrinter pp = new PrettyPrinter();

    public CorpusMagician() {
    }

    //TODO we need a webservice for this functionality too
    //in the future (for repo and external users)
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
                CorpusMagician.chosencorpusfunctions.add(cf);
                System.out.println(CorpusMagician.chosencorpusfunctions.toString());
            }
            corpusfunctions = corpusFunctionStrings2Classes();

            //here is the heap space problem: everything is read all at one
            //and kept in the heap space the whole time
            corpuma.initCorpusWithURL(url);
            //get the basedirectory
            basedirectory = url;
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
            if (absoluteReport != null && basedirectory != null && absoluteReport.contains(basedirectory.toString())) {
                absoluteReport = reportOutput.replaceAll(basedirectory.toString(), "");
            }
            if (absoluteReport != null) {
                cio.write(absoluteReport, reportlocation);
            }
            //create the error list file
            System.out.println("Basedirectory is " + basedirectory);
            System.out.println("BasedirectoryPath is " + basedirectory.getPath());
            URL errorlistlocation = new URL(basedirectory + "curation/CorpusServices_Errors.xml");
            File curationFolder = new File((new URL(basedirectory + "curation").getFile()));
            if (!curationFolder.exists()) {
                //the curation folder it not there and needs to be created
                curationFolder.mkdirs();
            }
            // new File(url.getFile()
            Document exmaErrorList = TypeConverter.W3cDocument2JdomDocument(ExmaErrorList.createFullErrorList());
            String exmaErrorListString = TypeConverter.JdomDocument2String(exmaErrorList);
            if (exmaErrorListString != null && basedirectory != null && exmaErrorListString.contains(basedirectory.getPath())) {
                exmaErrorListString = exmaErrorListString.replaceAll(basedirectory.getPath(), "../");
            }
            if (exmaErrorListString != null) {
                exmaErrorListString = pp.indent(exmaErrorListString, "event");
                cio.write(exmaErrorListString, errorlistlocation);
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
            report.addException(ex, "An Exmaralda file reading error occured");
        } catch (URISyntaxException ex) {
            report.addException(ex, "A URI was incorrect");
        } catch (XPathExpressionException ex) {
            report.addException(ex, "An Xpath expression was incorrect");
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
    public void initCorpusWithURL(URL url) throws MalformedURLException, SAXException, JexmaraldaException, URISyntaxException, IOException {
        corpus = new Corpus(url);
    }

    //creates a list of all the available data from an url (being a file oder directory)
    public Collection<URL> createListofData(URL url) throws URISyntaxException, IOException {
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
        allExistingCFs.add("ExbFileCoverageChecker");
        allExistingCFs.add("ExbAnnotationPanelCheck");
        allExistingCFs.add("EXB2INELISOTEI");
        allExistingCFs.add("EXB2HIATISOTEI");
        allExistingCFs.add("ExbStructureChecker");
        allExistingCFs.add("FileCoverageChecker");
        allExistingCFs.add("FileCoverageCheckerInel");
        allExistingCFs.add("NormalizeEXB");
        allExistingCFs.add("PrettyPrintData");
        allExistingCFs.add("RemoveAbsolutePaths");
        allExistingCFs.add("RemoveAutoSaveExb");
        allExistingCFs.add("XSLTChecker");
        allExistingCFs.add("ComaAddTiersFromExbsCorrector");
        allExistingCFs.add("ComaXsdChecker");
        allExistingCFs.add("NgexmaraldaCorpusChecker");
        allExistingCFs.add("FilenameChecker");
        allExistingCFs.add("CmdiChecker");
        allExistingCFs.add("ComaTiersDescriptionAnnotationPanelChecker");
        allExistingCFs.add("ExbTierDisplayNameChecker");
        allExistingCFs.add("NgTierCheckerWithAnnotation");
        allExistingCFs.add("XsltCheckerInel");
        allExistingCFs.add("GenerateAnnotationPanel");
        allExistingCFs.add("CorpusDataRegexReplacer");
        allExistingCFs.add("ScoreHTML");
        allExistingCFs.add("HScoreHTML");
        allExistingCFs.add("CorpusHTML");
        allExistingCFs.add("IAAFunctionality");
        allExistingCFs.add("ListHTML");
        allExistingCFs.add("ExbEventLinebreaksChecker");
        allExistingCFs.add("MakeTimelineConsistent");
        allExistingCFs.add("ExbSegmentationChecker");
        allExistingCFs.add("CalculateAnnotatedTime");
        allExistingCFs.add("AddCSVMetadataToComa");
        allExistingCFs.add("ComaKmlForLocations");
        allExistingCFs.add("RemoveEmptyEvents");
        allExistingCFs.add("ComaTranscriptionsNameChecker");
        allExistingCFs.add("ComaTierOverviewCreator");
        allExistingCFs.add("GeneralTransformer");
        allExistingCFs.add("ComaFedoraIdentifierLengthChecker");
        allExistingCFs.add("ExbMP3Next2WavAdder");
        allExistingCFs.add("ExbRefTierChecker");
        allExistingCFs.add("ReportStatistics");
        allExistingCFs.add("ExbSegmenter");
        allExistingCFs.add("ExbScriptMixChecker");
        Collections.sort((List<String>) allExistingCFs);
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
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("inel")) {
                            cog.setInel(cfProperties.getProperty("inel"));
                            System.out.println("INEL set to " + cfProperties.getProperty("inel"));
                        }
                    }
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
                case "exbfilecoveragechecker":
                    ExbFileCoverageChecker efcc = new ExbFileCoverageChecker();
                    corpusfunctions.add(efcc);
                    break;
                case "exbannotationpanelcheck":
                    ExbAnnotationPanelCheck eapc = new ExbAnnotationPanelCheck();
                    corpusfunctions.add(eapc);
                    break;
                case "filecoveragechecker":
                    ComaFileCoverageChecker fcc = new ComaFileCoverageChecker();
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
                    if (cfProperties != null) {
                        if (cfProperties.containsKey("MODE") && cfProperties.getProperty("MODE").equals("INEL")) {
                            xc.setXSLresource("/xsl/inel-checks.xsl");
                            System.out.println("MODE set to " + cfProperties.getProperty("MODE"));
                        }
                        if (cfProperties.containsKey("UTTERANCEENDSYMBOLS")) {
                            xc.setUtteranceEndSymbols(cfProperties.getProperty("UTTERANCEENDSYMBOLS"));
                            System.out.println("UTTERANCEENDSYMBOLS set to " + cfProperties.getProperty("UTTERANCEENDSYMBOLS"));
                        }
                    }
                    corpusfunctions.add(xc);
                    break;
                case "comaaddtiersfromexbscorrector":
                    ComaAddTiersFromExbsCorrector catfec = new ComaAddTiersFromExbsCorrector();
                    corpusfunctions.add(catfec);
                    break;
                case "comaxsdchecker":
                    ComaXsdChecker cxsd = new ComaXsdChecker();
                    corpusfunctions.add(cxsd);
                    break;
                case "ngexmaraldacorpuschecker":
                    NgexmaraldaCorpusChecker ngex = new NgexmaraldaCorpusChecker();
                    corpusfunctions.add(ngex);
                    break;
                case "filenamechecker":
                    FilenameChecker fnc = new FilenameChecker();
                    corpusfunctions.add(fnc);
                    break;
                case "cmdichecker":
                    CmdiChecker cmdi = new CmdiChecker();
                    corpusfunctions.add(cmdi);
                    break;
                case "comafedoraidentifierlengthchecker":
                    ComaFedoraIdentifierLengthChecker cplc = new ComaFedoraIdentifierLengthChecker();
                    corpusfunctions.add(cplc);
                    break;
                case "comatranscriptionsnamechecker":
                    ComaTranscriptionsNameChecker cnc = new ComaTranscriptionsNameChecker();
                    corpusfunctions.add(cnc);
                    break;
                case "comatiersdescriptionannotationpanelchecker":
                    ComaTiersDescriptionAnnotationPanelChecker tcwa = new ComaTiersDescriptionAnnotationPanelChecker();
                    corpusfunctions.add(tcwa);
                    break;
                case "exbtierdisplaynamechecker":
                    ExbTierDisplayNameChecker tc = new ExbTierDisplayNameChecker();
                    corpusfunctions.add(tc);
                    break;
                case "ngtiercheckerwithannotation":
                    NgTierCheckerWithAnnotation ngtcwa = new NgTierCheckerWithAnnotation();
                    corpusfunctions.add(ngtcwa);
                    break;
                case "xsltcheckerinel":
                    XSLTChecker xci = new XSLTChecker();
                    xci.setXSLresource("/xsl/inel-checks.xsl");
                    if (cfProperties != null) {
                        if (cfProperties.containsKey("UTTERANCEENDSYMBOLS")) {
                            xci.setUtteranceEndSymbols(cfProperties.getProperty("UTTERANCEENDSYMBOLS"));
                            System.out.println("UTTERANCEENDSYMBOLS set to " + cfProperties.getProperty("UTTERANCEENDSYMBOLS"));
                        }
                    }
                    corpusfunctions.add(xci);
                    break;
                case "exb2inelisotei":
                    EXB2INELISOTEI eiit = new EXB2INELISOTEI();
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("LANG")) {
                            eiit.setLanguage(cfProperties.getProperty("LANG"));
                            System.out.println("Language set to " + cfProperties.getProperty("LANG"));
                        }
                    }
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
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("LANG")) {
                            ehit.setLanguage(cfProperties.getProperty("LANG"));
                            System.out.println("Language set to " + cfProperties.getProperty("LANG"));
                        }
                    }
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
                    ComaFileCoverageChecker fcci = new ComaFileCoverageChecker();
                    fcci.addFileEndingWhiteListString("flextext");
                    fcci.addWhiteListString("report-output.html");
                    fcci.addWhiteListString("Segmentation_Errors.xml");
                    fcci.addWhiteListString("Structure_Errors.xml");
                    corpusfunctions.add(fcci);
                    break;
                case "comakmlforlocations":
                    ComaKmlForLocations ckml = new ComaKmlForLocations();
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("KML")) {
                            ckml.setKMLFilePath(cfProperties.getProperty("KML"));
                            System.out.println("KML file path set to " + cfProperties.getProperty("KML"));
                        }
                    }
                    corpusfunctions.add(ckml);
                    break;
                case "reportstatistics":
                    ReportStatistics rs = new ReportStatistics();
                    corpusfunctions.add(rs);
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
                            cdrr.setReplacement(cfProperties.getProperty("replacement"));
                            System.out.println("Replacement set to " + cfProperties.getProperty("replacement"));
                        }
                        if (cfProperties.containsKey("xpathcontext")) {
                            cdrr.setXpathContext(cfProperties.getProperty("xpathcontext"));
                            System.out.println("Xpath set to " + cfProperties.getProperty("xpathcontext"));
                        }
                        if (cfProperties.containsKey("coma")) {
                            cdrr.setComa(cfProperties.getProperty("coma"));
                            System.out.println("Replace in Coma set to " + cfProperties.getProperty("coma"));
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
                    if (cfProperties != null) {
                        if (cfProperties.containsKey("CORPUSNAME")) {
                            shtml.setCorpusName(cfProperties.getProperty("CORPUSNAME"));
                            System.out.println("Corpus name set to " + cfProperties.getProperty("CORPUSNAME"));
                        }
                    }
                    corpusfunctions.add(shtml);
                    break;
                case "hscorehtml":
                    HScoreHTML hshtml = new HScoreHTML();
                    corpusfunctions.add(hshtml);
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
                        if (cfProperties.containsKey("CORPUSNAME")) {
                            lhtml.setCorpusName(cfProperties.getProperty("CORPUSNAME"));
                            System.out.println("Corpus name set to " + cfProperties.getProperty("CORPUSNAME"));
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
                case "exbstructurechecker":
                    ExbStructureChecker esc = new ExbStructureChecker();
                    corpusfunctions.add(esc);
                    break;
                case "exbsegmentationchecker":
                    ExbSegmentationChecker eseg = new ExbSegmentationChecker();
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("SEGMENTATION")) {
                            eseg.setSegmentation(cfProperties.getProperty("SEGMENTATION"));
                            System.out.println("Segmentation set to " + cfProperties.getProperty("SEGMENTATION"));
                        }
                        if (cfProperties.containsKey("FSM")) {
                            eseg.setExternalFSM(cfProperties.getProperty("FSM"));
                            System.out.println("External FSM path set to " + cfProperties.getProperty("FSM"));
                        }
                    }
                    corpusfunctions.add(eseg);
                    break;
                case "exbsegmenter":
                    ExbSegmenter esegr = new ExbSegmenter();
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("SEGMENTATION")) {
                            esegr.setSegmentation(cfProperties.getProperty("SEGMENTATION"));
                            System.out.println("Segmentation set to " + cfProperties.getProperty("SEGMENTATION"));
                        }
                        if (cfProperties.containsKey("FSM")) {
                            esegr.setExternalFSM(cfProperties.getProperty("FSM"));
                            System.out.println("External FSM path set to " + cfProperties.getProperty("FSM"));
                        }
                    }
                    corpusfunctions.add(esegr);
                    break;
                case "calculateannotatedtime":
                    CalculateAnnotatedTime cat = new CalculateAnnotatedTime();
                    corpusfunctions.add(cat);
                    break;
                case "addcsvmetadatatocoma":
                    AddCSVMetadataToComa acmtc = new AddCSVMetadataToComa();
                    if (cfProperties != null) {
                        // Pass on the configuration parameter
                        if (cfProperties.containsKey("CSV")) {
                            acmtc.setCSVFilePath(cfProperties.getProperty("CSV"));
                            System.out.println("CSV file path set to " + cfProperties.getProperty("CSV"));
                        }
                        if (cfProperties.containsKey("SPEAKER")) {
                            acmtc.setSpeakerOrCommunication(cfProperties.getProperty("SPEAKER"));
                            System.out.println("CSV file set for " + cfProperties.getProperty("SPEAKER"));
                        }
                    }
                    corpusfunctions.add(acmtc);
                    break;
                case "removeemptyevents":
                    RemoveEmptyEvents ree = new RemoveEmptyEvents();
                    corpusfunctions.add(ree);
                    break;
                case "comatieroverviewcreator":
                    ComaTierOverviewCreator ctoc = new ComaTierOverviewCreator();
                    corpusfunctions.add(ctoc);
                    break;
                case "generaltransformer":
                    GeneralTransformer gt = new GeneralTransformer();
                    if (cfProperties != null) {
                        if (cfProperties.containsKey("coma")) {
                            gt.setComa(cfProperties.getProperty("coma"));
                            System.out.println("Run on Coma set to " + cfProperties.getProperty("coma"));
                        }
                        if (cfProperties.containsKey("exb")) {
                            gt.setExb(cfProperties.getProperty("exb"));
                            System.out.println("Run on exb set to " + cfProperties.getProperty("exb"));
                        }
                        if (cfProperties.containsKey("exs")) {
                            gt.setExs(cfProperties.getProperty("exs"));
                            System.out.println("Run on exs set to " + cfProperties.getProperty("exs"));
                        }
                        if (cfProperties.containsKey("xsl")) {
                            gt.setPathToXSL(cfProperties.getProperty("xsl"));
                            System.out.println("Path to XSL set to " + cfProperties.getProperty("xsl"));
                        }
                        if (cfProperties.containsKey("overwritefiles")) {
                            gt.setOverwriteFiles(cfProperties.getProperty("overwritefiles"));
                            System.out.println("overwritefiles set to " + cfProperties.getProperty("overwritefiles"));
                        }
                    }
                    corpusfunctions.add(gt);
                    break;
                case "exbmp3next2wavadder":
                    ExbMP3Next2WavAdder emn2wa = new ExbMP3Next2WavAdder();
                    corpusfunctions.add(emn2wa);
                    break;
                case "exbreftierchecker":
                    ExbRefTierChecker ertc = new ExbRefTierChecker();
                    corpusfunctions.add(ertc);
                    break;
                case "exbscriptmixchecker":
                    ExbScriptMixChecker esmc = new ExbScriptMixChecker();
                    corpusfunctions.add(esmc);
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
        
        Option verbose = new Option("v", "verbose", false, "output verbose help");
        fix.setRequired(false);
        options.addOption(verbose);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);

        String header = "Specify a corpus folder or file and a function to be applied\n\n";
        String footer = "\nthe available functions are:\n" + getAllExistingCFsAsString() + "\n\nPlease report issues at https://lab.multilingua.uni-hamburg.de/redmine/projects/corpus-services/issues";
        String footerverbose = "\nthe available functions are:\n";
        for(CorpusFunction cf: corpusFunctionStrings2Classes()){
            cf.getDescription();
        }
        footerverbose += "\n\nPlease report issues at https://lab.multilingua.uni-hamburg.de/redmine/projects/corpus-services/issues";
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
        if (cmd.hasOption("v")) {
            // automatically generate the help statement
            formatter.printHelp("hzsk-corpus-services", header, options, footerverbose, true);
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
