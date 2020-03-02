
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import static de.uni_hamburg.corpora.CorpusMagician.exmaError;
import de.uni_hamburg.corpora.XMLData;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author fsnv625
 */
public class RemoveAbsolutePaths extends Checker implements CorpusFunction {

    Document doc = null;
    Path pathRelative = null;
    String nameOfCorpusFolder;
    String nameOfExbFolder;

    public RemoveAbsolutePaths() {
        super("RemoveAbsolutePaths");
    }

    
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            Class cl3 = Class.forName("de.uni_hamburg.corpora.SegmentedTranscriptionData");
            Class cl2 = Class.forName("de.uni_hamburg.corpora.ComaData");
            if (cl.isInstance(cd) || cl3.isInstance(cd)) {
                List al = findAllAbsolutePathsExbAttribute(cd);
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
                            report.addCritical(function, cd, "absolute path info needs to be replaced");
                            if (cl.isInstance(cd)) {
                                exmaError.addError("RemoveAbsolutePaths", cd.getURL().getFile(), "", "", false, "absolute path info needs to be replaced");
                            }
                        } else {
                            al.remove(o);
                            report.addCorrect(function, cd, "path is already relative, nothing to do");
                        }
                    }
                }
                List ale = findAllAbsolutePathsExbElement(cd);
                if (!ale.isEmpty()) {
                    for (int i = 0; i < ale.size(); i++) {
                        Object o = ale.get(i);
                        Element ae = (Element) o;
                        //System.out.println(a);
                        String refurl = ae.getText();
                        Path pabs;
                        if (refurl.startsWith("file")) {
                            URL refurlurl = new URL(refurl);
                            pabs = Paths.get(refurlurl.toURI());
                        } else {
                            pabs = Paths.get(refurl);
                        }
                        if (pabs.isAbsolute()) {
                            report.addCritical(function, cd, "absolute path info needs to be replaced");
                            if (cl.isInstance(cd)) {
                                exmaError.addError("RemoveAbsolutePaths", cd.getURL().getFile(), "", "", false, "absolute path info needs to be replaced");
                            }
                        } else {
                            al.remove(o);
                            report.addCorrect(function, cd, "path is already relative, nothing to do");
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
                        String refurl = e.getText();
                        Path pabs;
                        if (refurl.startsWith("file")) {
                            URL refurlurl = new URL(refurl);
                            pabs = Paths.get(refurlurl.toURI());
                        } else {
                            pabs = Paths.get(refurl);
                        }
                        //Path pabs = Paths.get(e.getText());
                        if (pabs.isAbsolute()) {
                            report.addCritical(function, cd, "absolute path info needs to be replaced");                           
                        } else {
                            al.remove(o);
                            report.addCorrect(function, cd, "path is already relative, nothing to do");
                        }

                    }
                }
            } else {
                report.addCritical(function, cd, "File is neither coma nor exb nor exs file");
            }
        } catch (ClassNotFoundException ex) {
            report.addException(ex, function, cd, "unknown class not found error");
        } catch (JDOMException ex) {
            report.addException(ex, function, cd, "unknown reading error");
        } catch (URISyntaxException ex) {
            report.addException(ex, function, cd, "unknown URI syntax error");
        } catch (MalformedURLException ex) {
            report.addException(ex, function, cd, "unknown malformed URL error");
        } catch (TransformerException ex) {
            report.addException(ex, function, cd, "unknown reading error");
        } catch (ParserConfigurationException ex) {
            report.addException(ex, function, cd, "unknown reading error");
        } catch (IOException ex) {
            report.addException(ex, function, cd, "unknown reading error");
        } catch (XPathExpressionException ex) {
            report.addException(ex, function, cd, "unknown reading error");
        }
        return report;
    }

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {       
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            Class cl3 = Class.forName("de.uni_hamburg.corpora.SegmentedTranscriptionData");
            Class cl2 = Class.forName("de.uni_hamburg.corpora.ComaData");
            if (cl.isInstance(cd) || cl3.isInstance(cd)) {
                List al = findAllAbsolutePathsExbAttribute(cd);
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
                                cd.updateUnformattedString(TypeConverter.JdomDocument2String(doc));
                                cio.write(cd, cd.getURL());
                                report.addCorrect(function, cd, "removed absolute path");
                            } else {
                                report.addCritical(function, cd,
                                        "relative path " + pabs.toString() + " cannot be figured out");
                                if (cl.isInstance(cd)) {
                                    exmaError.addError("RemoveAbsolutePaths", cd.getURL().getFile(), "", "", false, "absolute path needs to be replaced manually");
                                }
                            }
                        } else {
                            report.addCorrect(function, cd, "path is already relative");
                        }
                    }
                } else {
                    report.addCorrect(function, cd, "there is no absolute path left, nothing to do");
                }
                List ale = findAllAbsolutePathsExbElement(cd);
                if (!ale.isEmpty()) {
                    for (int i = 0; i < ale.size(); i++) {
                        Object o = ale.get(i);
                        Element ae = (Element) o;
                        String refurl = ae.getText();
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
                                ae.setText(pathRelative.toString());
                                //then save file
                                //add a report message
                                CorpusIO cio = new CorpusIO();
                                cd.updateUnformattedString(TypeConverter.JdomDocument2String(doc));
                                cio.write(cd, cd.getURL());
                                report.addCorrect(function, cd, "removed absolute path");
                            } else {
                                report.addCritical(function, cd,
                                        "relative path " + pabs.toString() + " cannot be figured out");
                                if (cl.isInstance(cd)) {
                                    exmaError.addError("RemoveAbsolutePaths", cd.getURL().getFile(), "", "", false, "absolute path needs to be replaced manually");
                                }
                            }
                        } else {
                            report.addCorrect(function, cd, "path is already relative");
                        }
                    }
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
                                cd.updateUnformattedString(TypeConverter.JdomDocument2String(doc));
                                XMLData xml = (XMLData) cd;
                                xml.setJdom(doc);
                                cd = (CorpusData) xml;
                                cd.updateUnformattedString(TypeConverter.JdomDocument2String(doc));
                                cio.write(cd, cd.getURL());
                                report.addCorrect(function, cd, "removed absolute path");
                            } else {
                                report.addCritical(function, cd,
                                        "relative path " + pabs.toString() + " cannot be figured out");
                            }

                        } else {
                            al.remove(o);
                            report.addCorrect(function, cd, "path is already relative, nothing to do");
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
                report.addCritical(function, cd, "File is neither coma nor exb nor exs file");
            }
        } catch (ClassNotFoundException ex) {
            report.addException(ex, function, cd, "unknown class not found error");
        } catch (JDOMException ex) {
            report.addException(ex, function, cd, "unknown reading error");
        } catch (URISyntaxException ex) {
            report.addException(ex, function, cd, "unknown URI syntax error");
        } catch (TransformerException ex) {
             report.addException(ex, function, cd, "unknown reading error");
        } catch (ParserConfigurationException ex) {
             report.addException(ex, function, cd, "unknown reading error");
        } catch (UnsupportedEncodingException ex) {
             report.addException(ex, function, cd, "unknown reading error");
        } catch (XPathExpressionException ex) {
             report.addException(ex, function, cd, "unknown reading error");
        }
        return report;
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
            Class cl2 = Class.forName("de.uni_hamburg.corpora.SegmentedTranscriptionData");
            IsUsableFor.add(cl2);
            Class cl3 = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl3);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "usable class not found error");
        }
        return IsUsableFor;
    }

    public List findAllAbsolutePathsExbAttribute(CorpusData cd) throws JDOMException, URISyntaxException, MalformedURLException, TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString());
        XPath xp1;
        // in exbs: <referenced-file url="ChND_99_Barusi_flkd.wav"/>  
        //working for exs too
        xp1 = XPath.newInstance("//head/meta-information/referenced-file/@url");
        List allAbsolutePaths = xp1.selectNodes(doc);
        if (allAbsolutePaths.isEmpty()) {
            report.addWarning(function, cd, "no paths found");
        }
        return allAbsolutePaths;
    }

    public List findAllAbsolutePathsExbElement(CorpusData cd) throws JDOMException, URISyntaxException, MalformedURLException, TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString());
        XPath xp1;
        // in exbs: <referenced-file url="ChND_99_Barusi_flkd.wav"/>  
        //working for exs too
        xp1 = XPath.newInstance("//ud-meta-information/ud-information[@attribute-name='# EXB-SOURCE']");
        List allAbsolutePaths = xp1.selectNodes(doc);
        if (allAbsolutePaths.isEmpty()) {
            report.addWarning(function, cd, "no paths found");
        }
        return allAbsolutePaths;
    }

    public List findAllAbsolutePathsComa(CorpusData cd) throws JDOMException, URISyntaxException, MalformedURLException, TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString());
        XPath xp1;
        // in Coma: NSLinks and relPaths <NSLink>narrative/KBD_71_Fish_nar/KBD_71_Fish_nar_s.exs</NSLink>
        //  <relPath>narrative/KBD_71_Fish_nar/NG_6_1971_506-507_KBD_71_Fish_nar.pdf</relPath>
        xp1 = XPath.newInstance("/Corpus/CorpusData/Communication/File/relPath | /Corpus/CorpusData/Communication/File/absPath | /Corpus/CorpusData/Communication/Transcription/NSLink | /Corpus/CorpusData/Communication/Transcription/Description/Key[@Name='# EXB-SOURCE'] | /Corpus/CorpusData/Communication/Recording/Media/NSLink");
        List allAbsolutePaths = xp1.selectNodes(doc);
        if (allAbsolutePaths.isEmpty()) {
            report.addWarning(function, cd, "no paths found");
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

    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class finds paths that are absolute"
                + " in files and replaces them with paths relative to the corpus folder. ";
        return description;
    }
}
