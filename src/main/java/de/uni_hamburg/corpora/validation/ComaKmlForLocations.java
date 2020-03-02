package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
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
    String kmlFile;
    HashMap<String, String> birthPlace; // hash map for holding the birthplaces of speakers
    HashMap<String, String> domicile; // hash map for storing the residences of speakers 
    HashMap<String, String> commLocation; // hash map for holding locations where the communications took place
    HashMap<String, String> lngLat; // hash map for holding coordinates of locations
    Report stats = new Report(); //create a new report
     
    public ComaKmlForLocations() {
        super("coma-kml-for-locations");
    }

    /**
     * Default check function which calls the exceptionalCheck function so that
     * the primal functionality of the feature can be implemented, and
     * additionally checks for parser configuration, SAXE and IO exceptions.
     */
    public Report check(CorpusData cd) {
        try {
            if (kmlFile != null) {
            getCoordinates();
            stats = exceptionalCheck(cd);
            } else {
            stats.addCritical(function, "No KML file path supplied");    
            }
        } catch (ParserConfigurationException pce) {
            stats.addException(pce, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cd, "Unknown file reading error");
        } catch (URISyntaxException ex) {
            stats.addException(ex, function, cd, "Unknown file reading error");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown transformer error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown XPath error");
        } catch (JDOMException ex) {
            stats.addException(ex, function, cd, "Unknown Jdom error");
        }
        return stats;
    }

    /**
     * Main functionality of the feature;
     */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerConfigurationException, TransformerException, XPathExpressionException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
        NodeList communications = doc.getElementsByTagName("Communication"); // get all the communications in the corpus
        NodeList speakers = doc.getElementsByTagName("Speaker"); // get all the speakers in the corpus    
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
            String languageCode = speaker.getElementsByTagName("LanguageCode").item(0).getTextContent();
            for (int j = 0; j < locations.getLength(); j++) {
                Element location = (Element) locations.item(j);
                if (location.getAttribute("Type").equals("Basic biogr. data")) {
                    NodeList keys = location.getElementsByTagName("Key");
                    String placeOfBirth = "";
                    String region = null;
                    String country = null;
                    String domicileStr = "";
                    Element ref = null;
                    Element domRef = null;
                    for (int k = 0; k < keys.getLength(); k++) {
                        Element key = (Element) keys.item(k);
                        switch (key.getAttribute("Name")) {
                            case "1a Place of birth":
                                placeOfBirth = key.getTextContent();
                                break;
                            case "2 Region":
                                region = key.getTextContent();
                                ref = key;
                                break;
                            case "3 Country":
                                country = key.getTextContent();
                                break;
                            case "7a Domicile":
                                domicileStr = key.getTextContent();
                                break;
                            case "8a Other information":
                                domRef = key;
                                break;
                        }
                    }
                    if (!placeOfBirth.equals("...") && !placeOfBirth.equals("")) {
                        if (placeOfBirth.endsWith("(?)")) {
                            placeOfBirth = placeOfBirth.substring(0, placeOfBirth.indexOf(" (?)"));
                        }
                        if (placeOfBirth.endsWith("`")) {
                            placeOfBirth = placeOfBirth.substring(0, placeOfBirth.indexOf("`"));
                        }
                        if (!lngLat.containsKey(placeOfBirth + "-" + languageCode)) {
                            String message = "KML (" + kmlFile + ") does not contain the birthplace '" + placeOfBirth + "' "
                                    + "from speaker '" + sigleString + "'";
                            System.out.println(message);
                            stats.addWarning(function, cd, message);
                        }
                    }
                    if (!domicileStr.equals("...") && !domicileStr.equals("")) {
                        if (domicileStr.endsWith("(?)")) {
                            domicileStr = domicileStr.substring(0, domicileStr.indexOf(" (?)"));
                        }
                        if (domicileStr.endsWith("`")) {
                            domicileStr = domicileStr.substring(0, domicileStr.indexOf("`"));
                        }
                        if (!lngLat.containsKey(domicileStr + "-" + languageCode)) {
                            String message = "KML (" + kmlFile + ") does not contain the domicile '" + domicileStr + "' "
                                    + "from speaker '" + sigleString + "'";
                            System.out.println(message);
                            stats.addWarning(function, cd, message);
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
            String communicationName = communication.getAttribute("Name"); // get communication name 
            NodeList keys = location.getElementsByTagName("Key");
            String country = "";
            String region = "";
            String settlement = "";
            String languageCode = communication.getElementsByTagName("LanguageCode").item(0).getTextContent();
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
            if (!settlement.equals("...") && !settlement.equals("")) {
                if (settlement.endsWith("(?)")) {
                    settlement = settlement.substring(0, settlement.indexOf(" (?)"));
                }
                if (settlement.endsWith("`")) {
                    settlement = settlement.substring(0, settlement.indexOf("`"));
                }
                if (!lngLat.containsKey(settlement + "-" + languageCode)) {
                    String message = "KML (" + kmlFile + ") does not contain the settlement '" + settlement + "' "
                            + "from communication '" + communicationName + "'";
                    System.out.println(message);
                    stats.addWarning(function, cd, message);
                }
            }
            commLocation.put(communicationID, new String(settlement + ", " + region + ", " + country));
        }
        return stats; // return the report with warnings
    }

    // sets the KML file path which is provided as input
    public void setKMLFilePath(String path) {
        this.kmlFile = path;
    }

    // the method for getting coordinates of locations in the kml file
    public void getCoordinates() throws ParserConfigurationException, SAXException, IOException, JDOMException, URISyntaxException {
        Document doc = null;
        CorpusIO cio = new CorpusIO();
        if (kmlFile != null) {
            String kmlString = cio.readExternalResourceAsString(kmlFile);
            if (kmlString != null) {
                doc = TypeConverter.String2W3cDocument(kmlString);
                if (lngLat == null) {
                    lngLat = new HashMap<>();
                }
                if (doc != null) {
                    NodeList placeMarks = doc.getElementsByTagName("Placemark");
                    for (int i = 0; i < placeMarks.getLength(); i++) { //iterate through place marks
                        Element placeMark = (Element) placeMarks.item(i);
                        Element name = (Element) placeMark.getElementsByTagName("name").item(0);
                        String nameOfPlace = name.getTextContent();
                        String language = "";
                        NodeList data = placeMark.getElementsByTagName("Data");
                        for (int j = 0; j < data.getLength(); j++) {
                            Element datum = (Element) data.item(j);
                            if (datum.getAttribute("name").equals("lang")) {
                                Element value = (Element) datum.getElementsByTagName("value").item(0);
                                language = value.getTextContent();
                            }
                        }
                        String coordinatesWithAltitude = placeMark.getElementsByTagName("coordinates").item(0).getTextContent();
                        String coordinate = coordinatesWithAltitude.trim().substring(0, coordinatesWithAltitude.trim().lastIndexOf(","));
                        lngLat.put(nameOfPlace + "-" + language, coordinate);
                    }
                } else {
                    stats.addCritical(function, "KML file cannot be read");
                }
            } else {
                stats.addCritical(function, "KML file cannot be read");
            }
        } else {
            stats.addCritical(function, "No KML file path supplied");
        }
    }

    /**
     * Adds coordinates of the locations to the coma file using the kml file.
     */
    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        Report stats = new Report(); //create a new report

        try {
            getCoordinates();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(TypeConverter.String2InputStream(cd.toSaveableString())); // get the file as a document
            NodeList communications = doc.getElementsByTagName("Communication"); // get all the communications in the corpus
            NodeList speakers = doc.getElementsByTagName("Speaker"); // get all the speakers in the corpus
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
                String languageCode = speaker.getElementsByTagName("LanguageCode").item(0).getTextContent();
                for (int j = 0; j < locations.getLength(); j++) {
                    Element location = (Element) locations.item(j);
                    if (location.getAttribute("Type").equals("Basic biogr. data")) {
                        NodeList keys = location.getElementsByTagName("Key");
                        String placeOfBirth = "";
                        String region = null;
                        String country = null;
                        String domicileStr = "";
                        boolean coorFlag = false;
                        boolean domCoor = false;
                        Element ref = null;
                        Element domRef = null;
                        for (int k = 0; k < keys.getLength(); k++) {
                            Element key = (Element) keys.item(k);
                            switch (key.getAttribute("Name")) {
                                case "1a Place of birth":
                                    placeOfBirth = key.getTextContent();
                                    break;
                                case "1c Place of birth (LngLat)":
                                    coorFlag = true;
                                    break;
                                case "2 Region":
                                    region = key.getTextContent();
                                    ref = key;
                                    break;
                                case "3 Country":
                                    country = key.getTextContent();
                                    break;
                                case "7a Domicile":
                                    domicileStr = key.getTextContent();
                                    break;
                                case "7c Domicile (LngLat)":
                                    domCoor = true;
                                    break;
                                case "8a Other information":
                                    domRef = key;
                                    break;
                            }
                        }
                        if (!placeOfBirth.equals("...") && !placeOfBirth.equals("")) {
                            if (placeOfBirth.endsWith("(?)")) {
                                placeOfBirth = placeOfBirth.substring(0, placeOfBirth.indexOf(" (?)"));
                            }
                            if (placeOfBirth.endsWith("`")) {
                                placeOfBirth = placeOfBirth.substring(0, placeOfBirth.indexOf("`"));
                            }
                            if (coorFlag == false && lngLat.containsKey(placeOfBirth + "-" + languageCode)) {
                                Element coordinatesKey = doc.createElement("Key");
                                coordinatesKey.setAttribute("Name", "1c Place of birth (LngLat)");
                                coordinatesKey.setTextContent(lngLat.get(placeOfBirth + "-" + languageCode));
                                Element loc = (Element) location.getElementsByTagName("Description").item(0);
                                loc.insertBefore(coordinatesKey, ref);
                            } else if (!lngLat.containsKey(placeOfBirth + "-" + languageCode)) {
                                String message = "KML (" + kmlFile + ") does not contain the birthplace '" + placeOfBirth + "' "
                                        + "from speaker '" + sigleString + "'";
                                System.out.println(message);
                                stats.addWarning(function, cd, message);
                            }
                        }
                        if (!domicileStr.equals("...") && !domicileStr.equals("")) {
                            if (domicileStr.endsWith("(?)")) {
                                domicileStr = domicileStr.substring(0, domicileStr.indexOf(" (?)"));
                            }
                            if (domicileStr.endsWith("`")) {
                                domicileStr = domicileStr.substring(0, domicileStr.indexOf("`"));
                            }
                            if (domCoor == false && lngLat.containsKey(domicileStr + "-" + languageCode)) {
                                Element coordinatesKey = doc.createElement("Key");
                                coordinatesKey.setAttribute("Name", "7c Domicile (LngLat)");
                                coordinatesKey.setTextContent(lngLat.get(domicileStr + "-" + languageCode));
                                Element loc = (Element) location.getElementsByTagName("Description").item(0);
                                loc.insertBefore(coordinatesKey, domRef);
                            } else if (!lngLat.containsKey(domicileStr + "-" + languageCode)) {
                                String message = "KML (" + kmlFile + ") does not contain the domicile '" + domicileStr + "' "
                                        + "from speaker '" + sigleString + "'";
                                System.out.println(message);
                                stats.addWarning(function, cd, message);
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
                String communicationName = communication.getAttribute("Name"); // get communication name
                NodeList keys = location.getElementsByTagName("Key");
                String country = "";
                String region = "";
                String settlement = "";
                String languageCode = communication.getElementsByTagName("LanguageCode").item(0).getTextContent();
                boolean coorFlag = false;
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
                        case "Settlement (LngLat)":
                            coorFlag = true;
                            break;
                    }
                }
                if (!settlement.equals("...") && !settlement.equals("")) {
                    if (settlement.endsWith("(?)")) {
                        settlement = settlement.substring(0, settlement.indexOf(" (?)"));
                    }
                    if (settlement.endsWith("`")) {
                        settlement = settlement.substring(0, settlement.indexOf("`"));
                    }
                    if (coorFlag == false && lngLat.containsKey(settlement + "-" + languageCode)) {
                        Element coordinatesKey = doc.createElement("Key");
                        coordinatesKey.setAttribute("Name", "Settlement (LngLat)");
                        coordinatesKey.setTextContent(lngLat.get(settlement + "-" + languageCode));
                        Element loc = (Element) location.getElementsByTagName("Description").item(0);
                        loc.appendChild(coordinatesKey);
                    } else if (!lngLat.containsKey(settlement + "-" + languageCode)) {
                        String message = "KML (" + kmlFile + ") does not contain the settlement '" + settlement + "' "
                                + "from communication '" + communicationName + "'";
                        System.out.println(message);
                        stats.addWarning(function, cd, message);
                    }
                }
                commLocation.put(communicationID, new String(settlement + ", " + region + ", " + country));
            }

            CorpusIO cio = new CorpusIO();
            cd.updateUnformattedString(TypeConverter.W3cDocument2String(doc));
            cio.write(cd, cd.getURL());

            return stats; // return the report with warnings
        } catch (ParserConfigurationException ex) {
            stats.addException(ex, function, cd, "The KML file could not be parsed.");
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "Unknown Transformer error.");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "Unknown XPath error.");
        } catch (IOException ex) {
            stats.addException(ex, function, cd, "The KML file could not be parsed.");
        } catch (URISyntaxException ex) {
            stats.addException(ex, function, cd, "URI syntax Exception.");
        }
        return stats;
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
            stats.addException(ex, " usable class not found");
        }
        return IsUsableFor;
    }

    /**
     * Default function which returns a two/three line description of what this
     * class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class identifies and lists fields which contain"
                + " location information; creates a list of different location names;"
                + " gets geo-coordinates for the location names via Google API.";
        return description;
    }
}
