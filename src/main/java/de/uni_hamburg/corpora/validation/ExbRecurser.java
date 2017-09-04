/**
 * @file ExbErrorChecker.java
 *
 * A command-line tool / non-graphical interface
 * for checking errors in exmaralda's EXB files.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;

import java.io.IOException;
import java.io.File;
import java.util.Hashtable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
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

/**
 * A command-line tool for checking EXB files.
 */
public class ExbRecurser {

    public static Collection<File> getReferencedFilesAsFiles(File f)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        NodeList reffiles = doc.getElementsByTagName("referenced-file");
        Set<File> refs = new HashSet<File>();
        for (int i = 0; i < reffiles.getLength(); i++) {
            Element reffile = (Element)reffiles.item(i);
            String url = reffile.getAttribute("url");
            File justFile = new File(url);
            if (justFile.exists()) {
                refs.add(justFile);
            }
            String relfilename = url;
            if (url.lastIndexOf("/") != -1) {
                relfilename = url.substring(url.lastIndexOf("/"));
            }
            String referencePath = f.getParentFile().getCanonicalPath();
            String absPath = referencePath + File.separator + relfilename;
            File absFile = new File(absPath);
            if (absFile.exists()) {
                refs.add(absFile);
            }
        }
        return refs;
    }


}
