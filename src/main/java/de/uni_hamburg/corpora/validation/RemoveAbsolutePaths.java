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
import java.io.IOException;
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

/**
 *
 * @author fsnv625
 */
public class RemoveAbsolutePaths extends Checker implements CorpusFunction {

    Document doc = null;
    Path pathRelative = null;

    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            Class cl2 = Class.forName("de.uni_hamburg.corpora.ComaData");
            if (cl.isInstance(cd)) {
                List al = findAllAbsolutePathsExb(cd);
                //if there is no autosave, nothing needs to be done
                if (al.isEmpty()) {
                    report.addCorrect("RemoveAbsolutePaths", "there is no absolute path left, nothing to do");
                } else {
                    report.addCritical("RemoveAbsolutePaths", "absolute path info needs to be removed in " + cd.getURL().getFile());
                }
            } else if (cl2.isInstance(cd)) {
                List al = findAllAbsolutePathsComa(cd);
                //if there is no autosave, nothing needs to be done
                if (al.isEmpty()) {
                    report.addCorrect("RemoveAbsolutePaths", "there is no absolute path left, nothing to do");
                } else {
                    report.addCritical("RemoveAbsolutePaths", "absolute path info needs to be removed in " + cd.getURL().getFile());
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
                //if there is no autosave, nothing needs to be done
                if (!al.isEmpty()) {
                    for (Object o : al) {
                        Attribute a = (Attribute) o;
                        //System.out.println(a);
                        //make the path relative
                        a.setValue(pathRelative.toString());
                    }
                    //then save file
                    //add a report message
                    CorpusIO cio = new CorpusIO();
                    cio.write(doc, cd.getURL());
                    report.addCorrect("RemoveAutoSaveExb", "removed AutoSave info in " + cd.getURL().getFile());
                } else {
                    report.addCorrect("RemoveAutoSaveExb", "there is no autosave info left, nothing to do");
                }
            } else if (cl2.isInstance(cd)) {
                List al = findAllAbsolutePathsComa(cd);
                if (!al.isEmpty()) {
                    for (Object o : al) {
                        Element e = (Element) o;
                        //System.out.println(e);
                        //make the path relative
                        e.setText(pathRelative.toString());
                    }
                    //then save file
                    //add a report message
                    CorpusIO cio = new CorpusIO();
                    cio.write(doc, cd.getURL());
                    report.addCorrect("RemoveAutoSaveExb", "removed AutoSave info in " + cd.getURL().getFile());
                } else {
                    report.addCorrect("RemoveAutoSaveExb", "there is no autosave info left, nothing to do");
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

    public List findAllAbsolutePathsExb(CorpusData cd) throws JDOMException, URISyntaxException {
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString());
        XPath xp1;
        // in exbs: <referenced-file url="ChND_99_Barusi_flkd.wav"/>        
        xp1 = XPath.newInstance("/basic-transcription/head/meta-information/referenced-file/@url");
        List allAbsolutePaths = xp1.selectNodes(doc);
        if(!allAbsolutePaths.isEmpty()){
        for (int i = 0; i < allAbsolutePaths.size(); i++) {
            Object o = allAbsolutePaths.get(i);
            Attribute a = (Attribute) o;
            Path pabs = Paths.get(a.getValue());
            //System.out.println(pabs.toString());
            if (pabs.isAbsolute()) {
                URL url = cd.getURL();
                Path pabsFile = Paths.get(url.toURI());
                //System.out.println(pabsFile.toString());
                pathRelative = pabsFile.getParent().relativize(pabs);
                //System.out.println(pathRelative);
                //if it already is absolute, do nothing
            } else {
                report.addCorrect("RemoveAbsolutePaths", "paths are already relative, nothing to do");
                allAbsolutePaths.clear();
            }
        }
        }
        else {
            report.addWarning("RemoveAbsolutePaths", "no paths found in file " + cd.getURL().getFile());
        }

        return allAbsolutePaths;
    }

    public List findAllAbsolutePathsComa(CorpusData cd) throws JDOMException, URISyntaxException {
        doc = TypeConverter.String2JdomDocument(cd.toSaveableString());
        XPath xp1;
        // in exbs: <referenced-file url="ChND_99_Barusi_flkd.wav"/>
        // in Coma: NSLinks and relPaths <NSLink>narrative/KBD_71_Fish_nar/KBD_71_Fish_nar_s.exs</NSLink>
        //  <relPath>narrative/KBD_71_Fish_nar/NG_6_1971_506-507_KBD_71_Fish_nar.pdf</relPath>
        xp1 = XPath.newInstance("/Corpus/CorpusData/Communication/(File/relPath|Transcription/NSLink)");
        List allAbsolutePaths = xp1.selectNodes(doc);
         for (int i = 0; i < allAbsolutePaths.size(); i++) {
            Object o = allAbsolutePaths.get(i);
            Element e = (Element) o;
            Path pabs = Paths.get(e.getText());
            if (pabs.isAbsolute()) {
                URL url = cd.getURL();
                Path pabsFile = Paths.get(url.toURI());
                pathRelative = pabsFile.relativize(pabs.getParent());
                //System.out.println(pathRelative);
            } //if it already is absolute, do nothing
            else {
                report.addCorrect("RemoveAbsolutePaths", "paths are already relative, nothing to do");
                allAbsolutePaths.clear();
            }
        }
        return allAbsolutePaths;
    }

}
