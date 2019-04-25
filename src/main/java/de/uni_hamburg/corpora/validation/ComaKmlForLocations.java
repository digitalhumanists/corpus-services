package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class that identifies and lists fields which contain location information;
 * creates a list of different location names; gets geo-coordinates for the
 * location names via Google API.
 */
public class ComaKmlForLocations extends Checker implements CorpusFunction {

    String comaLoc = "";
    HashMap<String, String> birthPlace; // hash map for holding the birthplaces of speakers
    HashMap<String, String> domicile; // hash map for storing the residences of speakers 
    HashMap<String, String> commLocation; // hash map for holding locations where the communications took place

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, comaLoc + ": Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, comaLoc + ": Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, comaLoc + ": Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, comaLoc + ": Unknown file reading error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature;
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        NodeList communications = doc.getElementsByTagName("Communication"); // get all the communications in the corpus
        NodeList speakers = doc.getElementsByTagName("Speaker"); // get all the speakers in the corpus
        Report stats = new Report(); //create a new report
        if (birthPlace == null) {
            birthPlace = new HashMap<>();
        }
        if (domicile == null) {
            domicile = new HashMap<>();
        }
        if (commLocation == null) {
            commLocation = new HashMap<>();
        }
        for (int i = 0; i < speakers.getLength(); i++) { //iterate through speakers
            Element speaker = (Element) speakers.item(i);
            Element sigle = (Element) speaker.getElementsByTagName("Sigle").item(0);
            String sigleString = sigle.getTextContent();
            NodeList locations = speaker.getElementsByTagName("Location");
            for (int j = 0; j < locations.getLength(); j++) {
                Element location = (Element) locations.item(j);
                if (location.getAttribute("Type").equals("Basic biogr. data")) {
                    NodeList keys = location.getElementsByTagName("Key");
                    String placeOfBirth = null;
                    String region = null;
                    String country = null;
                    String domicileStr = null;
                    for (int k = 0; k < keys.getLength(); k++) {
                        Element key = (Element) keys.item(k);
                        switch (key.getAttribute("Name")) {
                            case "1a Place of birth":
                                placeOfBirth = key.getTextContent();
                                break;
                            case "2 Region":
                                region = key.getTextContent();
                                break;
                            case "3 Country":
                                country = key.getTextContent();
                                break;
                            case "7a Domicile":
                                domicileStr = key.getTextContent();
                                break;
                        }
                    }
                    birthPlace.put(sigleString, new String(placeOfBirth + ", " + region + ", " + country));
                    domicile.put(sigleString, domicileStr);
                    break;
                }
            }
        }
        for (int i = 0; i < communications.getLength(); i++) { //iterate through communications
            Element communication = (Element) communications.item(i);
            Element location = (Element) communication.getElementsByTagName("Location").item(0); // get the location of the communication
            String communicationID = communication.getAttribute("Id"); // get communication id 
            //String communicationName = communication.getAttribute("Name"); // get communication name 
            NodeList keys = location.getElementsByTagName("Key");
            String country = "";
            String region = "";
            String settlement = "";
            for (int j = 0; j < keys.getLength(); j++) {
                Element key = (Element) keys.item(j);
                switch (key.getAttribute("Name")) {
                    case "Country":
                        country = key.getTextContent();
                        break;
                    case "Region":
                        region = key.getTextContent();
                        break;
                    case "Settlement":
                        settlement = key.getTextContent();
                        break;
                }
            }
            commLocation.put(communicationID, new String(settlement + ", " + region + ", " + country));
        }
        return stats; // return the report with warnings
    }

    /**
     * Fixing the conflicts in coma file with regards to this feature is not
     * supported yet.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ComaKmlForLocations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return IsUsableFor;
    }
}
