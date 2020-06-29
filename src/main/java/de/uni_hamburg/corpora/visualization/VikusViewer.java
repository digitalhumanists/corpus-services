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
import de.uni_hamburg.corpora.ComaData;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
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
    String corpusname;
    ArrayList<String> keywordblacklist = new ArrayList<>();
    URL vikusviewerurl;
    String licence;
    String version;
    String corpusPrefix;
    String title;
    String description;

    @Override
    public Report function(CorpusData cd) throws NoSuchAlgorithmException, ClassNotFoundException, FSMException, URISyntaxException, SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException, JDOMException {
        Report stats = new Report();
        ComaData coma = (ComaData) cd;
        keywordBlacklist();
        vikusviewerurl = new URL(cd.getParentURL() + "resources/vikus-viewer");
        File vikusviewerfolder = new File((vikusviewerurl).getFile());
        if (!vikusviewerfolder.exists()) {
            //the curation folder it not there and needs to be created
            vikusviewerfolder.mkdirs();
        }

        Element comadescription = coma.getCorpusDescription();
        Element descriptioncoma = (Element) XPath.selectSingleNode(comadescription, "descendant::Key[@Name='DC:description']");
        description = descriptioncoma.getText();
        Element elcorpusPrefix = (Element) XPath.selectSingleNode(comadescription, "descendant::Key[@Name='hzsk:corpusPrefix']");
        corpusPrefix = elcorpusPrefix.getText();
        Element eltitle = (Element) XPath.selectSingleNode(comadescription, "descendant::Key[@Name='DC:title']");
        title = eltitle.getText();
        Element elversion = (Element) XPath.selectSingleNode(comadescription, "descendant::Key[@Name='hzsk:corpusVersion']");
        version = elversion.getText();
        Element ellicence = (Element) XPath.selectSingleNode(comadescription, "descendant::Key[@Name='DC:rights']");
        licence = ellicence.getText();
        stats.merge(createDataCSV(cd));
        stats.merge(createConfigJSON(cd));
        stats.merge(createInfoMD(cd));
        return stats;
    }

    public void keywordBlacklist() {
        keywordblacklist.add("and");
        keywordblacklist.add("a");
        keywordblacklist.add("the");
        keywordblacklist.add("I");
    }

    public Report createDataCSV(CorpusData cd) throws FileNotFoundException, IOException, JDOMException {
        //id,keywords,year,_dialect,_country,_region,_language,_speaker,_transcription,_scorehtml,_listhtml,_pdf,_audio,_genre,_description
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
        String transrepourl = "https://corpora.uni-hamburg.de/repository/transcript:" + corpusPrefix + "-" + version + "_";
        String filerepourl = "https://corpora.uni-hamburg.de/repository/file:" + corpusPrefix + "-" + version + "_";
        String recrepourl = "https://corpora.uni-hamburg.de/repository/recording:" + corpusPrefix + "-" + version + "_";
        for (Element communication : coma.getCommunications()) {
            String[] comrow = new String[15];
            //id
            Attribute id = (Attribute) XPath.selectSingleNode(communication, "@Name");
            comrow[0] = id.getValue();
            data.add(comrow);
            //keyword - year, genre, Title splitted by spaces
            Element year = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[contains(@Name,'Date of recording')]");
            System.out.println(year.getText());
            Element descriptiondesc = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[contains(@Name,'Title')]");
            System.out.println(descriptiondesc.getText());
            Element genre = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[contains(@Name,'Genre')]");
            System.out.println(genre.getText());
            Element region = (Element) XPath.selectSingleNode(communication, "descendant::Location/Description/Key[contains(@Name,'Region')]");
            System.out.println(region.getText());
            Element speaker = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[contains(@Name,'Speakers')]");
            System.out.println(speaker.getText());
            String keywords = "\"";
            for (String s : descriptiondesc.getText().split(" ")) {
                if (!keywordblacklist.contains(s)) {
                    keywords += s + ",";
                }
            }
            keywords += year.getText() + "," + genre.getText() + "," + region.getText() + "," + speaker.getText() + "\"";
            comrow[1] = keywords;
            //year - Description Date of Recording
            comrow[2] = year.getText();
            //dialect
            Element dialect = (Element) XPath.selectSingleNode(communication, "descendant::Description/Key[contains(@Name,'Dialect')]");
            System.out.println(dialect.getText());
            comrow[3] = dialect.getText();
            //country
            Element country = (Element) XPath.selectSingleNode(communication, "descendant::Location/Description/Key[contains(@Name,'Country')]");
            System.out.println(country.getText());
            comrow[4] = country.getText();
            //region
            comrow[5] = region.getText();
            //language
            Element language = (Element) XPath.selectSingleNode(communication, "descendant::Language/LanguageCode");
            System.out.println(language.getText());
            comrow[6] = language.getText();
            //speaker
            comrow[7] = "\"" + speaker.getText() + "\"";
            //transcription url
            //needs to look like https://corpora.uni-hamburg.de/repository/transcript:selkup-1.0_DN_196X_Bread_nar/EXB/DN_196X_Bread_nar.exb 
            String transcrurl = transrepourl + id.getValue() + "/EXB/" + id.getValue() + ".exb";
            //Element transcription = (Element) XPath.selectSingleNode(communication, "descendant::Transcription/NSLink");
            //System.out.println(transcription.getText());
            //comrow[8] = transcription.getText();
            comrow[8] = transcrurl;
            //scorehtml url
            //needs to look like 
            //https://corpora.uni-hamburg.de/repository/transcript:selkup-1.0_AGS_1964_SnakeInMouth_flk/SCORE/AGS_1964_SnakeInMouth_flk-score.html 
            String scoreurl = transrepourl + id.getValue() + "/SCORE/" + id.getValue() + "-score.html";
            comrow[9] = scoreurl;
            //listhtml url
            //needs to look like 
            //https://corpora.uni-hamburg.de/repository/transcript:selkup-1.0_AGS_1964_SnakeInMouth_flk/LIST/AGS_1964_SnakeInMouth_flk-list.html
            String listurl = transrepourl + id.getValue() + "/LIST/" + id.getValue() + "-list.html";
            comrow[10] = listurl;
            //pdf url
            Element pdf = (Element) XPath.selectSingleNode(communication, "descendant::File[mimetype='application/pdf']/relPath']");
            if (pdf != null) {
                String pdfrurl = filerepourl + id.getValue() + "/PDF/" + id.getValue() + ".pdf";
                //System.out.println(pdf.getText());
                //comrow[9] = pdf.getText();
                comrow[11] = pdfrurl;
            } else {
                comrow[11] = "no pdf";
            }
            //audio url
            Element audio = (Element) XPath.selectSingleNode(communication, "descendant::Recording/Media/NSLink");
            if (audio != null) {
                String audiourl = recrepourl + id.getValue() + "/MP3/" + id.getValue() + ".mp3";
                //System.out.println(audio.getText());
                //comrow[10] = audio.getText();
                comrow[12] = audiourl;
                //TODO
                //now save the audio image in the folder with the correct name
            } else {
                comrow[12] = "no audio";
            }
            //genre
            System.out.println(genre.getText());
            comrow[13] = genre.getText();
            //description
            System.out.println(descriptiondesc.getText());
            comrow[14] = descriptiondesc.getText();

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

    public Report createInfoMD(CorpusData cd) throws JDOMException, IOException {
        Report stats = new Report();
        CorpusIO cio = new CorpusIO();
        ComaData coma = (ComaData) cd;
        String info = cio.readInternalResourceAsString(INFO_PATH);
        String corpusnameandversion = title + " " + version;
        info = info.replaceAll("_CORPUSNAME_", corpusnameandversion);
        //_DESCRIPTION_  <Key Name="DC:description">
        info = info.replaceAll("_DESCRIPTION_", description);
        //_LICENCE_      <Key Name="DC:rights">CC BY-NC-SA 4.0</Key>
        info = info.replaceAll("_LICENCE_", licence);
        //now save the string array as csv
        URL infoMDlocation = new URL(vikusviewerurl + "/info.md");
        cio.write(info, infoMDlocation);
        stats.addCorrect(function, cd, "vikus-viewer info.md successfully created at " + infoMDlocation.toString());
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

    @Override
    public String getDescription() {
        String description = "This class creates an config files needed "
                + "for the vikus-viewer software. ";
        return description;
    }

}
