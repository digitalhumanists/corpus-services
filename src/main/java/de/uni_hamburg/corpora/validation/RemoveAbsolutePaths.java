/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;

/**
 *
 * @author fsnv625
 */
public class RemoveAbsolutePaths extends Checker implements CorpusFunction {

    Document doc = null;
    Path pathRelative = null;
    String nameOfCorpusFolder;
    String nameOfExbFolder;

    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            Class cl2 = Class.forName("de.uni_hamburg.corpora.ComaData");
            if (cl.isInstance(cd)) {
                List al = findAllAbsolutePathsExb(cd);
                //if there is no absolute path, nothing needs to be done
                //check if the paths that are there are absolute
                if (!al.isEmpty()) {
                    for (int i = 0; i < al.size(); i++) {
                        Object o = al.get(i);
                        Attribute a = (Attribute) o;
                        //System.out.println(a);
                        String refurl = a.getValue();
                        Path pabs;
                        if (refurl.startsWith("file")) {
                            URL refurlurl = new URL(refurl);
                            pabs = Paths.get(refurlurl.toURI());
                        } else {
                            pabs = Paths.get(refurl);
                        }
                        if (pabs.isAbsolute()) {
                            report.addCritical("RemoveAbsolutePaths", "absolute path info needs to be replaced in " + cd.getURL().getFile());
                            exmaError.addError("RemoveAbsolutePaths", cd.getURL().getFile(), "", "", false, "absolute path info needs to be replaced");
                        } else {
                            al.remove(o);
                            report.addCorrect("RemoveAbsolutePaths", "path is already relative, nothing to do");
                        }
                    }
                }
            } else if (cl2.isInstance(cd)) {
                List al = findAllAbsolutePathsComa(cd);
                //if there is no autosave, nothing needs to be done
                if (!al.isEmpty()) {
                    for (int i = 0; i < al.size(); i++) {
                        Object o = al.get(i);
                        Element e = (Element) o;
                        Path pabs = Paths.get(e.getText());
                        if (pabs.isAbsolute()) {
                            report.addCritical("RemoveAbsolutePaths", "absolute path info needs to be replaced in " + cd.getURL().getFile());
                            exmaError.addError("RemoveAbsolutePaths", cd.getURL().getFile(), "", "", false, "absolute path info needs to be replaced");
                        } else {
                            al.remove(o);
                            report.addCorrect("RemoveAbsolutePaths", "path is already relative, nothing to do");
                        }

                    }
                }
            } else {
                report.addCritical("RemoveAbsolutePaths", "File is neither coma nor exb file");
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RemoveAbsolutePaths.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(RemoveAbsolutePaths.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(RemoveAbsolutePaths.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(RemoveAbsolutePaths.class.getName()).log(Level.SEVERE, null, ex);
        }
        return report;
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            Class cl2 = Class.forName("de.uni_hamburg.corpora.ComaData");
            if (cl.isInstance(cd)) {
                List al = findAllAbsolutePathsExb(cd);
                System.out.println(cd.getURL());
                Path directory = Paths.get(cd.getURL().toURI());
                System.out.println(directory);
                nameOfExbFolder = directory.getParent().getFileName().toString();
                if (!al.isEmpty()) {
                    for (int i = 0; i < al.size(); i++) {
                        Object o = al.get(i);
                        Attribute a = (Attribute) o;
                        String refurl = a.getValue();
                        Path pabs;
                        if (refurl.startsWith("file")) {
                            URL refurlurl = new URL(refurl);
                            pabs = Paths.get(refurlurl.toURI());
                        } else {
                            pabs = Paths.get(refurl);
                        }
                        if (pabs.isAbsolute()) {
                            //make the path relative to the exb folder
                            //need to cut it somehow
                            System.out.println(pabs);
                            System.out.println(nameOfExbFolder);
                            pathRelative = trimFilePathBeforeDirectory(pabs, nameOfExbFolder);
                            System.out.println(pathRelative);
                            if (!(pathRelative == null)) {
                                a.setValue(pathRelative.toString());
                                //then save file
                                //add a report message
                                CorpusIO cio = new CorpusIO();
                                cio.write(doc, cd.getURL());
                                report.addCorrect("RemoveAbsolutePaths", "removed absolute path in " + cd.getURL().getFile());
                            } else {
                                report.addCritical("RemoveAbsolutePaths",
                                        "relative path cannot be figured out for file "
                                        + cd.getURL().getFile() + " with path " + pabs.toString());
                                exmaError.addError("RemoveAbsolutePaths", cd.getURL().getFile(), "", "", false, "absolute path needs to be replaced manually");
                            }
                        } else {
                            report.addCorrect("RemoveAbsolutePaths", "path is already relative in" + cd.getURL().getFile());
                        }
                    }
                } else {
                    report.addCorrect("RemoveAbsolutePaths", "there is no absolute path left, nothing to do in " + cd.getURL().getFile());
                }
            } else if (cl2.isInstance(cd)) {
                List al = findAllAbsolutePathsComa(cd);
                Path directory = Paths.get(cd.getURL().toURI());
                System.out.println(directory);
                //nameOfExbFolder = directory.getParent().getFileName().toString();
                nameOfCorpusFolder = directory.getParent().getFileName().toString();
                if (!al.isEmpty()) {
                    for (int i = 0; i < al.size(); i++) {
                        Object o = al.get(i);
                        Element e = (Element) o;
                        String refurl = e.getValue();
                        Path pabs;
                        if (refurl.startsWith("file")) {
                            URL refurlurl = new URL(refurl);
                            pabs = Paths.get(refurlurl.toURI());
                        } else {
                            pabs = Paths.get(refurl);
                        }
                        System.out.println(pabs);
                        System.out.println(nameOfCorpusFolder);
                        pathRelative = trimFilePathBeforeDirectory(pabs, nameOfCorpusFolder);
                        if (pabs.isAbsolute()) {
                            if (!(pathRelative == null)) {
                                e.setText(pathRelative.toString());
                                //then save file
                                //add a report message
                                CorpusIO cio = new CorpusIO();
                                cio.write(doc, cd.getURL());
                                report.addCorrect("RemoveAbsolutePaths", "removed absolute path in " + cd.getURL().getFile());
                            } else {
                                report.addCritical("RemoveAbsolutePaths",
                                        "relative path cannot be figured out for file "
                                        + cd.getURL().getFile() + " with path " + pabs.toString());
                            }

                        } else {
                            al.remove(o);
                            report.addCorrect("RemoveAbsolutePaths", "path is already relative, nothing to do");
                        }
                    }
                }

                /**
                 *
                 * if (!pabs.isAbsolute()) {
                 * report.addCorrect("RemoveAbsolutePaths", "paths are already
                 * relative, nothing to do"); allAbsolutePaths.clear();
                 *
                 *
                 *
                 *
                 * //oh this is so annoying, I will just put the filenames of
                 * the paths in here, //so it will only work for HZSK corpora
                 * and INEL of course //System.out.println(pabs.toString());
                 *
                 * //URL url = cd.getURL(); //Path pabsFile =
                 * Paths.get(url.toURI()); //there couldn't be only one relative
                 * Path for one exb, maybe there are more :( //pathRelative =
                 * pabs.getFileName(); // if
                 * (pabs.getRoot().equals(pabsFile.getRoot())) { // pathRelative
                 * = pabsFile.relativize(pabs.getParent()); // } else { //
                 * //pathRelative = null; // Path pabsParent = pabs.getParent();
                 * // System.out.println(pabsParent); // Path pabsFileParent =
                 * pabsFile.getParent(); // System.out.println(pabsFileParent);
                 * // System.out.println(pabsParent.getNameCount()); // //this
                 * works only if the file is in the same folder as the exb //
                 * pathRelative = pabs.getFileName(); //
                 * System.out.println(pathRelative); //// for (i =
                 * pabsParent.getNameCount()-1; i > 0; i--) { //// int u =
                 * pabsFileParent.getNameCount()-1; ////
                 * System.out.println(pabsParent.subpath(i,
                 * pabsParent.getNameCount())); ////
                 * System.out.println(pabsParent.subpath(u,
                 * pabsFileParent.getNameCount())); //// if
                 * (pabsParent.subpath(i,
                 * pabsParent.getNameCount()).equals(pabsFileParent.subpath(u,
                 * pabsFileParent.getNameCount()))) { //// pathRelative =
                 * pabsParent.subpath(i, pabsParent.getNameCount()); //// } else
                 * { //// System.out.println("break at" + i); //// break; //// }
                 * //// u--; //// } // if (pathRelative == null) { //
                 * report.addCritical("RemoveAbsolutePaths", "relative path
                 * cannot be figured out for file " + cd.getURL().getFile() + "
                 * with path " + pabs.toString()); // } // } //if it already is
                 * absolute, do nothing
                 *
                 *
                 *
                 *
                 *
                 *
                 *
                 * for (int i = 0; i < allAbsolutePaths.size(); i++) { Object o
                 * = allAbsolutePaths.get(i); Element e = (Element) o; Path pabs
                 * = Paths.get(e.getText()); URL url = cd.getURL(); Path
                 * pabsFile = Paths.get(url.toURI()); if (pabs.isAbsolute()) {
                 * if (pabs.getRoot().equals(pabsFile.getRoot())) { //there
                 * shouldn't be only one relative path... pathRelative =
                 * pabsFile.relativize(pabs.getParent()); } else { pathRelative
                 * = null; report.addCritical("RemoveAbsolutePaths", "relative
                 * path cannot be figured out for file " + cd.getURL().getFile()
                 * + "with path " + pabs.toString()); } } //if it already is
                 * absolute, do nothing else {
                 * report.addCorrect("RemoveAbsolutePaths", "in " +
                 * cd.getURL().getFile() + " paths are already relative, nothing
                 * to do"); allAbsolutePaths.clear(); } }
                 *
                 *
                 *
                 *
                 */
            } else {
                report.addCritical("RemoveAbsolutePaths", "File is neither coma nor exb file");
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RemoveAbsolutePaths.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(RemoveAbsolutePaths.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(RemoveAbsolutePaths.class.getName()).log(Level.SEVERE, null, ex);
        }
        return report;
    }

    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
            Class cl3 = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl3);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PrettyPrintData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }

    public List findAllAbsolutePathsExb(CorpusData cd) throws JDOMException, URISyntaxException, MalformedURLException {
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString());
        XPath xp1;
        // in exbs: <referenced-file url="ChND_99_Barusi_flkd.wav"/>        
        xp1 = XPath.newInstance("/basic-transcription/head/meta-information/referenced-file/@url");
        List allAbsolutePaths = xp1.selectNodes(doc);
        if (allAbsolutePaths.isEmpty()) {
            report.addWarning("RemoveAbsolutePaths", "no paths found in file " + cd.getURL().getFile());
        }
        return allAbsolutePaths;
    }

    public List findAllAbsolutePathsComa(CorpusData cd) throws JDOMException, URISyntaxException {
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString());
        XPath xp1;
        // in Coma: NSLinks and relPaths <NSLink>narrative/KBD_71_Fish_nar/KBD_71_Fish_nar_s.exs</NSLink>
        //  <relPath>narrative/KBD_71_Fish_nar/NG_6_1971_506-507_KBD_71_Fish_nar.pdf</relPath>
        xp1 = XPath.newInstance("/Corpus/CorpusData/Communication/File/relPath | /Corpus/CorpusData/Communication/File/absPath | /Corpus/CorpusData/Communication/Transcription/NSLink | /Corpus/CorpusData/Communication/Transcription/Description/Key[@Name=\"# EXB-SOURCE\"]");
        List allAbsolutePaths = xp1.selectNodes(doc);
        if (allAbsolutePaths.isEmpty()) {
            report.addWarning("RemoveAbsolutePaths", "no paths found in file " + cd.getURL().getFile());
        }
        return allAbsolutePaths;
    }

    public static Path trimFilePathBeforeDirectory(Path filepath, String directory) {
        //find the index where the directoryname occurs
        for (int i = 0; i < filepath.getNameCount() - 1; i++) {
            if (filepath.getName(i).toString().equals(directory)) {
                Path trimmedPath = filepath.subpath(i + 1, filepath.getNameCount());
                return trimmedPath;
            }
        }
        return null;
    }
}
