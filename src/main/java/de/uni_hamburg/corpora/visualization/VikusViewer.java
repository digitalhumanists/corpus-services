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
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import de.uni_hamburg.corpora.ComaData;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.xpath.XPath;

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

    public Report createDataCSV(CorpusData cd) throws FileNotFoundException, IOException, JDOMException {
        //id,keywords,year,_dialect,_country,_region,_language,_speaker,_transcription,_pdf,_audio,_genre,_description
        //"sketch,drawing",1890,Ket,Russia,Tomsk Oblast,sel,https://corpora.uni-hamburg.de/hzsk/de/islandora/object/transcript:selkup-0.1_AR_1965_RestlessNight_transl/datastream/EXB/AR_1965_RestlessNight_transl.exb,https://corpora.uni-hamburg.de/hzsk/de/islandora/object/file:selkup-0.1_KFN_1965_BearHunting1_nar/datastream/PDF/KFN_1965_BearHunting1_nar.pdf,https://corpora.uni-hamburg.de/hzsk/de/islandora/object/recording:selkup-0.1_DN_196X_Bread_nar/datastream/MP3/DN_196X_Bread_nar.mp3,flk,Male Torso,KAI_1965_OldWitch_flk
        Report stats = new Report();
        CSVReader reader;
        CorpusIO cio = new CorpusIO();
        reader = new CSVReader(new FileReader(getClass().getResource(DATA_PATH).getPath()), ',');
        List<String[]> data = reader.readAll();
        for (String[] row : data) {
            System.out.println(Arrays.toString(row));
            //first row = keys
            //other rows = values
        }
        //create Row ForCommunications
        ComaData coma = (ComaData) cd;
        //TODO of course
        String smallcorpusname = "selkup";
        String version = "1.0";
        String transrepourl = "https://corpora.uni-hamburg.de/repository/transcript:" + smallcorpusname + "-" + version + "_";
        String filerepourl = "https://corpora.uni-hamburg.de/repository/file:" + smallcorpusname + "-" + version + "_";
        String recrepourl = "https://corpora.uni-hamburg.de/repository/recording:" + smallcorpusname + "-" + version + "_";
        for (Element communication : coma.getCommunications()) {
            String[] comrow = new String[13];
            //id
            Attribute id = (Attribute) XPath.selectSingleNode(communication, "@Name");
            comrow[0] = id.getValue();
            data.add(comrow);
            //keyword - year, genre, Title splitted by spaces
            Element year = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[@Name='2b Date of recording']");
            Element description = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[@Name='0a Title']");
            Element genre = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[@Name='1 Genre']");
            Element region = (Element) XPath.selectSingleNode(communication, "descendant::Location/Description/Key[@Name='Region']");
            Element speaker = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[@Name='4 Speakers']");
            String keywords = "\"";
            for (String s : description.getText().split(" ")) {
                keywords += s + ",";
            }
            keywords += year.getText() + "," + genre.getText() + "," + region.getText() + "," + speaker.getText() + "\"";
            comrow[1] = keywords;
            //year - Description Date of Recording
            System.out.println(year.getText());
            comrow[2] = year.getText();
            //dialect
            Element dialect = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[@Name='3b Dialect']");
            System.out.println(dialect.getText());
            comrow[3] = dialect.getText();
            //country
            Element country = (Element) XPath.selectSingleNode(communication, "descendant::Location/Description/Key[@Name='Country']");
            System.out.println(country.getText());
            comrow[4] = country.getText();
            //region
            System.out.println(region.getText());
            comrow[5] = region.getText();
            //language
            Element language = (Element) XPath.selectSingleNode(communication, "descendant::Language/LanguageCode");
            System.out.println(language.getText());
            comrow[6] = language.getText();
            //speaker
            System.out.println(speaker.getText());
            comrow[7] = "\"" + speaker.getText() + "\"";
            //transcription url
            //needs to look like https://corpora.uni-hamburg.de/repository/transcript:selkup-1.0_DN_196X_Bread_nar/EXB/DN_196X_Bread_nar.exb 
            String transcrurl = transrepourl + id.getValue() + "/EXB/" + id.getValue() + ".exb";
            //Element transcription = (Element) XPath.selectSingleNode(communication, "descendant::Transcription/NSLink");
            //System.out.println(transcription.getText());
            //comrow[8] = transcription.getText();
            comrow[8] = transcrurl;
            //pdf url
            Element pdf = (Element) XPath.selectSingleNode(communication, "descendant::File[mimetype='application/pdf']/relPath']");
            if (pdf != null) {
                String pdfrurl = filerepourl + id.getValue() + "/PDF/" + id.getValue() + ".pdf";
                //System.out.println(pdf.getText());
                //comrow[9] = pdf.getText();
                comrow[9] = pdfrurl;
            } else {
                comrow[9] = "no pdf";
            }
            //audio url
            Element audio = (Element) XPath.selectSingleNode(communication, "descendant::Recording/Media/NSLink");
            if (audio != null) {
                String audiourl = recrepourl + id.getValue() + "/MP3/" + id.getValue() + ".mp3";
                //System.out.println(audio.getText());
                //comrow[10] = audio.getText();
                comrow[10] = audiourl;
            } else {
                comrow[10] = "no audio";
            }
            //genre
            System.out.println(genre.getText());
            comrow[11] = genre.getText();
            //description
            System.out.println(description.getText());
            comrow[12] = description.getText();

        }
        String newdata = "";
        for (String[] row : data) {
            newdata += String.join(",", row) + "\n";
            //first row = keys
            //other rows = values
        }
        //now save the string array as csv
        URL configJSONlocation = new URL(vikusviewerurl + "/data.csv");
        cio.write(newdata, configJSONlocation);
        stats.addCorrect(function, cd, "vikus-viewer config successfully created at " + configJSONlocation.toString());
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
        Report stats;
        cd = c.getComaData();
        stats = function(cd);
        return stats;
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
