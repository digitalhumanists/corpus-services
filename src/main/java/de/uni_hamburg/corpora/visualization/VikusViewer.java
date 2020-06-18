/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.net.URL;

/**
 *
 * @author anne
 */
public class VikusViewer extends Visualizer {

    private static final String CONFIG_PATH = "/vikus-viewer/config.json";
    private static final String DATA_PATH = "/vikus-viewer/data.csv";
    private static final String INFO_PATH = "/vikus-viewer/info.md";
    private static final String TIMELINE_PATH = "/vikus-viewer/timeline.csv";
    String corpusname = "Corpus";
    String vikusviewerfolder = "";
    URL vikusviewerurl;

    @Override
    public Report function(CorpusData cd) throws NoSuchAlgorithmException, ClassNotFoundException, FSMException, URISyntaxException, SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException, JDOMException {
        Report stats = new Report();
        vikusviewerurl = new URL(cd.getParentURL() + "resources/vikus-viewer");
        File vikusviewerfolder = new File((vikusviewerurl).getFile());
        if (!vikusviewerfolder.exists()) {
            //the curation folder it not there and needs to be created
            vikusviewerfolder.mkdirs();
        }
        stats.merge(createDataCSV(cd));
        stats.merge(createConfigJSON(cd));
        stats.merge(createInfoMD(cd));
        return stats;
    }

    public Report createDataCSV(CorpusData cd) {
        Report stats = new Report();
        return stats;
    }

    public Report createConfigJSON(CorpusData cd) throws JDOMException, IOException {
        Report stats = new Report();
        CorpusIO cio = new CorpusIO();
        String config = cio.readInternalResourceAsString(CONFIG_PATH);
        JsonElement jelement = new JsonParser().parse(config);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("project");
        jobject.addProperty("name", corpusname);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonString = gson.toJson(jelement);
        //System.out.println(prettyJsonString);
        //now save it pretty printed
        URL configJSONlocation = new URL(vikusviewerurl + "/config.json");
        cio.write(prettyJsonString, configJSONlocation);
        stats.addCorrect(function, cd, "vikus-viewer config successfully created at " + configJSONlocation.toString());
        return stats;
    }

    public Report createInfoMD(CorpusData cd) {
        Report stats = new Report();
        return stats;
    }

    @Override
    public Report function(Corpus c) throws NoSuchAlgorithmException, ClassNotFoundException, FSMException, URISyntaxException, SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException, JDOMException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, "Usable class not found.");
        }
        return IsUsableFor;
    }

    public void setCorpusName(String s) {
        corpusname = s;
    }

    @Override
    public String getDescription() {
        String description = "This class creates an config files needed "
                + "for the vikus-viewer software. ";
        return description;
    }

}
