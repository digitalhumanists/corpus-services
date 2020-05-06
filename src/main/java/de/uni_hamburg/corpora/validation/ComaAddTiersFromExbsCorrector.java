/**
 * @file ComaErrorChecker.java
 *
 * Collection of checks for coma errors for HZSK repository purposes.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.CorpusIO;
import de.uni_hamburg.corpora.Report;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.BasicBody;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;



/**
 * This class loads coma data and for all communications adds all tiers found
 * in the linked exb as a key value pairs to the description. 
 */
public class ComaAddTiersFromExbsCorrector extends Checker implements CorpusFunction{

    File comafile;
    ValidatorSettings settings;
    String tierNameFormat = "Tier %2$s (%1$s):";
    String tierTextFormat = "%s";
    String comaLoc = "";

    public ComaAddTiersFromExbsCorrector() {
        super("ComaAddTiersFromExbsCorrector");
    }

    /**
     * Uses the list of known abbreviations to add existing tiers from exb to
     * coma. Comes from old catalog file that is used somewhere?
     *
     * <table>
     * <thead>
     * <tr>
     * <th>TIERS</th>
     * <th>Comments</th>
     * <th>Type</th>
     * </tr>
     * </thead><tbody>
     * <tr>
     * <th colspan="3">SFB 538 / DiK / Skand Semiko</th>
     * </tr><tr>
     * <td>k</td>
     * <td>Free comment</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>sup</td>
     * <td>Suprasegmental information</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>akz</td>
     * <td>Accentuation/Stress</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>en</td>
     * <td>English translation</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>de</td>
     * <td>German translation</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>hd</td>
     * <td>Standard German translation</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>nv</td>
     * <td>Non-verbal</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>cs</td>
     * <td>Codeswitching</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <th colspan="3">HAMATAC</th>
     * </tr><tr>
     * <td>pos</td>
     * <td>Part of Speech</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>pos-sup</td>
     * <td>Superordinate part of Speech</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>pho</td>
     * <td>Phonetic annotation</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>lemma</td>
     * <td>Lemma</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>disfluency</td>
     * <td>Disfluency</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>c</td>
     * <td>Indicates that the automatic pos-annotation is incorrect</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <th colspan="3">some others</th>
     * </tr><tr>
     * <td>lang</td>
     * <td>Language of utterance</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>type</td>
     * <td>Type (spontaneous vs. imitated) of utterance</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>pho-adult</td>
     * <td>Phonetic target structure</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>syll</td>
     * <td>Syllable structure</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>word</td>
     * <td>Orthographuc form of tokens</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>c</td>
     * <td>Indicates that the automatic pos-annotation is incorrect</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <th colspan="3">NSLC</th>
     * </tr><tr>
     * <td>ref</td>
     * <td>Name of the communication</td>
     * <td><code>d</code></td>
     * </tr><tr>
     * <td>st</td>
     * <td>Source texts: normally in Cyrillic transliteration</td>
     * <td><code>d</code></td>
     * </tr><tr>
     * <td>ts</td>
     * <td>Transcription (what is heard)</td>
     * <td><code>d</code></td>
     * </tr><tr>
     * <td>tx</td>
     * <td>Tier for interlinearization</td>
     * <td>transcription</td>
     * </tr><tr>
     * <td>mb</td>
     * <td>Morpheme break</td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>mp</td>
     * <td>Morphophonemes, underlying forms</td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>gr</td>
     * <td>Morphological <code>a</code>: Russian gloss of each morpheme </td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>ge</td>
     * <td>Morphological <code>a</code>: English gloss of each morpheme</td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>mc</td>
     * <td>Part of speech of each morpheme</td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>ps</td>
     * <td>Part of speech of each word</td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>SeR</td>
     * <td>Annotation of semantic roles </td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>SyF</td>
     * <td>Annotation of syntactic function</td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>IST</td>
     * <td>Annotation of information status </td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>CW</td>
     * <td>Annotation of code switching</td>
     * <td><code>a</code></td>
     * </tr><tr>
     * <td>fr</td>
     * <td>Russian free translation</td>
     * <td><code>d</code></td>
     * </tr><tr>
     * <td>fe</td>
     * <td>English free translation</td>
     * <td><code>d</code></td>
     * </tr><tr>
     * <td>fg</td>
     * <td>German free translation</td>
     * <td><code>d</code></td>
     * </tr><tr>
     * <td>nt</td>
     * <td>Notes on the text unit</td>
     * <td><code>d</code></td>
     * </tr><tr>
     * <th colspan="3">Unsourced / I came up with this</th>
     * </tr><tr>
     * <td>v</td>
     * <td>Verbal</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>no</td>
     * <td>Numbering</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>anno</td>
     * <td>Anonymisation</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * <td>nn</td>
     * <td>Action from non-specific source</td>
     * <td><code>?</code></td>
     * </tr><tr>
     * </table>
     *
     */
    public Report fix() {
        Report stats = new Report();
        try {
            stats = exceptionalFix();
        } catch(JexmaraldaException je) {
            stats.addException(je, function, cd, "Unknown parsing error");
        } catch(JDOMException jdome) {
            stats.addException(jdome, function, cd, "Unknown parsing error");
        } catch(SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch(IOException ioe) {
            stats.addException(ioe, function, cd, "Reading/writing error");
        }
        return stats;
    }


    private Report exceptionalFix() throws
            SAXException, JDOMException, IOException, JexmaraldaException {
        Map<String, String> tiers = new HashMap<String, String>();
        tiers.put("akz", "Accentuation/stress");
        tiers.put("c", "Indicates that the automatic pos-annotation is " +
                "incorrect");
        tiers.put("cs", "Codeswitching");
        tiers.put("CW", "Annotation of code switching");
        tiers.put("de", "German translation");
        tiers.put("disfluency", "Disfluency");
        tiers.put("en", "English translation");
        tiers.put("fe", "English free translation");
        tiers.put("fg", "German free translation");
        tiers.put("fr", "Russian free translation");
        tiers.put("fh", "Hungarian free translation");
        tiers.put("so", "Source origin");
        tiers.put("ge", "Morphological annotation: " +
                "English gloss of each morpheme");
        tiers.put("gr", "Morphological annotation: " +
                "Russian gloss of each morpheme");
        tiers.put("hd", "Standard German translation");
        tiers.put("IST", "Annotation of information status");
        tiers.put("k", "Free Comment");
        tiers.put("lang", "Language of utterance");
        tiers.put("lemma", "Lemma");
        tiers.put("mb", "Morpheme break");
        tiers.put("mc", "Part of speech of each morpheme");
        tiers.put("mp", "Morphophonemes, underlying forms");
        tiers.put("mT", "Morphological transliteration");
        tiers.put("nt", "Notes on the text unit");
        tiers.put("nv", "Non-verbal");
        tiers.put("pho-adult", "Phonetic target structure");
        tiers.put("pho", "Phonetic annnotation");
        tiers.put("pos", "Part of Speech");
        tiers.put("pos-sup", "Superordinate part of Speech");
        tiers.put("ps", "Part of speech of each word");
        tiers.put("ref", "Name of the communication");
        tiers.put("SeR", "Annotation of semantic roles");
        tiers.put("st", "Source texts: normally in Cyrillic transliteration");
        tiers.put("sup", "suprasegmental information");
        tiers.put("SyF", "Annotation of syntactic function");
        tiers.put("syll", "Syllable structure");
        tiers.put("ts", "Transcription (what is heard)");
        tiers.put("tx", "Tier for interlinearization");
        tiers.put("type", "Type (spontaneous vs. imitated) of utterance");
        tiers.put("word", "Orthographic form of tokens");
        // These are NOT in the catalogue...?
        tiers.put("v", "Verbal");
        tiers.put("no", "Numbering");
        tiers.put("anno", "Anonymisation");
        tiers.put("nn", "Action by unspecified source");

        Set<String> skipTiers = new HashSet<String>();
        skipTiers.add("COLUMN-LABEL");
        skipTiers.add("ROW-LABEL");
        skipTiers.add("SUB-ROW-LABEL");
        skipTiers.add("EMPTY");
        skipTiers.add("EMPTY-EDITOR");
        Report stats = new Report();
        org.jdom.Document corpus =
            org.exmaralda.common.jdomutilities.
                    IOUtilities.readDocumentFromLocalFile(
                            comafile.getAbsolutePath());
        XPath xpCommunications = XPath.newInstance("//Communication");
        List allCommunications = xpCommunications.selectNodes(corpus);
        for (Object o : allCommunications) {
            org.jdom.Element communication = (org.jdom.Element) o;
            //retrieve the communication name
            String communicationName = communication.getAttributeValue("Name");
            //pick up basic transcriptions
            XPath xpBasTrans = XPath.newInstance("Transcription[Description" +
                    "/Key[@Name='segmented']/text()='false']");
            List allBasTrans = xpBasTrans.selectNodes(communication);
            for (Object oB : allBasTrans) {
                org.jdom.Element basTrans = (org.jdom.Element) oB;
                String relPath = basTrans.getChildText("NSLink");
                String filePath = comafile.getParent() + File.separator +
                    relPath;
                File file = new File(filePath);
                if (!file.isFile()) {
                    // we already checked validity of files in other checks
                    continue;
                }
                org.jdom.Element desc = basTrans.getChild("Description");
                BasicTranscription bt = new BasicTranscription(filePath);
                BasicBody bb = bt.getBody();
                String[] tierIDs = bb.getAllTierIDs();
                Set<String> addedTiers = new HashSet<String>();
                for (String tierID : tierIDs) {
                    if (skipTiers.contains(tierID)) {
                        stats.addNote(function,
                                    "Skipped a tier: " + tierID,
                                    "This tier does not need to be included in "
                                    + "coma file");
                        continue;
                    }
                    Tier tier = null;
                    try {
                        tier = bb.getTierWithID(tierID);
                    } catch (JexmaraldaException je) {
                        System.out.println("ERRORR: tier with ID " + tierID +
                                " is lost...");
                        continue;
                    }
                    String displayName = tier.getDisplayName();
                    String category = tier.getCategory();
                    String tierType = tier.getType();
//                    System.out.println("DEBUG: id,disp,cat" +
//                            tierID + " , " + displayName + " , " + category);
                    org.jdom.Element keyElement = new org.jdom.Element("Key");
                    boolean alreadyAdded = false;
                    for (String added : addedTiers) {
                        if (added.equals(category)) {
                            // no need to add twice?
                            alreadyAdded = true;
                        }
                    }
                    if (alreadyAdded) {
                        continue;
                    } else if (tiers.containsKey(category)) {
                        String describeTierType = "Unknown";
                        if (tierType.equals("a")) {
                            describeTierType = "Annotation";
                        } else if (tierType.equals("d")) {
                            describeTierType = "Description";
                        } else if (tierType.equals("t")) {
                            describeTierType = "Transcription";
                        } else {
                            describeTierType = "Unknown";
                        }
                        keyElement.setAttribute("Name",
                                String.format(tierNameFormat,
                                    describeTierType, category));
                        keyElement.setText(String.format(tierTextFormat,
                                    tiers.get(category)));
                        desc.addContent(keyElement);
                        stats.addNote(function,
                                    "Tier was missing from COMA: "
                                    + tierID,
                                    "The default description has been added.");
                        addedTiers.add(category);
                    } else {
                        stats.addWarning(function,
                                    "Unrecognised tier category: "
                                    + category,
                                    "Tier must be added manually to coma");
                    }
                }
            }
        }
        try {
            CorpusIO cio = new CorpusIO();
            cio.write(corpus, settings.getOutputFile().toURI().toURL());           
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "unknown transformer error");
        } catch (ParserConfigurationException ex) {
            stats.addException(ex, function, cd, "unknown transformer error");
        } catch (UnsupportedEncodingException ex) {
            stats.addException(ex, function, cd, "unknown transformer error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "unknown transformer error");
        }
        return stats;
    }

    /**
    * Default check function which calls the exceptionalCheck function so that the
    * primal functionality of the feature can be implemented, and additionally 
    * checks for parser configuration, SAXE and IO exceptions.
    */   
    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        Report stats = new Report();
        try {
            stats = exceptionalCheck(cd);
        } catch (JexmaraldaException je) {
            stats.addException(je, function, cd, "Unknown parsing error");
        } catch (JDOMException jdome) {
            stats.addException(jdome, function, cd, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cd, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cd, "Reading/writing error");
        }
        return stats;
    }
    
    /**
    * Main functionality of the feature; loads coma data and checks for potential 
    * problems with HZSK repository depositing.
    */
    private Report exceptionalCheck(CorpusData cd)
            throws SAXException, JDOMException, IOException, JexmaraldaException{
        Report stats = new Report();
                Map<String, String> tiers = new HashMap<String, String>();
        tiers.put("akz", "Accentuation/stress");
        tiers.put("c", "Indicates that the automatic pos-annotation is " +
                "incorrect");
        tiers.put("cs", "Codeswitching");
        tiers.put("CW", "Annotation of code switching");
        tiers.put("de", "German translation");
        tiers.put("disfluency", "Disfluency");
        tiers.put("en", "English translation");
        tiers.put("fe", "English free translation");
        tiers.put("fg", "German free translation");
        tiers.put("fr", "Russian free translation");
        tiers.put("fh", "Hungarian free translation");
        tiers.put("so", "Source origin");
        tiers.put("ge", "Morphological annotation: " +
                "English gloss of each morpheme");
        tiers.put("gr", "Morphological annotation: " +
                "Russian gloss of each morpheme");
        tiers.put("hd", "Standard German translation");
        tiers.put("IST", "Annotation of information status");
        tiers.put("k", "Free Comment");
        tiers.put("lang", "Language of utterance");
        tiers.put("lemma", "Lemma");
        tiers.put("mb", "Morpheme break");
        tiers.put("mc", "Part of speech of each morpheme");
        tiers.put("mp", "Morphophonemes, underlying forms");
        tiers.put("mT", "Morphological transliteration");
        tiers.put("nt", "Notes on the text unit");
        tiers.put("nv", "Non-verbal");
        tiers.put("pho-adult", "Phonetic target structure");
        tiers.put("pho", "Phonetic annnotation");
        tiers.put("pos", "Part of Speech");
        tiers.put("pos-sup", "Superordinate part of Speech");
        tiers.put("ps", "Part of speech of each word");
        tiers.put("ref", "Name of the communication");
        tiers.put("SeR", "Annotation of semantic roles");
        tiers.put("st", "Source texts: normally in Cyrillic transliteration");
        tiers.put("sup", "suprasegmental information");
        tiers.put("SyF", "Annotation of syntactic function");
        tiers.put("syll", "Syllable structure");
        tiers.put("ts", "Transcription (what is heard)");
        tiers.put("tx", "Tier for interlinearization");
        tiers.put("type", "Type (spontaneous vs. imitated) of utterance");
        tiers.put("word", "Orthographic form of tokens");
        // These are NOT in the catalogue...?
        tiers.put("v", "Verbal");
        tiers.put("no", "Numbering");
        tiers.put("anno", "Anonymisation");
        tiers.put("nn", "Action by unspecified source");
        Set<String> skipTiers = new HashSet<String>();
        skipTiers.add("COLUMN-LABEL");
        skipTiers.add("ROW-LABEL");
        skipTiers.add("SUB-ROW-LABEL");
        skipTiers.add("EMPTY");
        skipTiers.add("EMPTY-EDITOR");
        comafile = new File(cd.getURL().toString());
        String str = comafile.getPath().substring(6);
        org.jdom.Document corpus =
            org.exmaralda.common.jdomutilities.
                    IOUtilities.readDocumentFromLocalFile(
                            str);
        XPath xpCommunications = XPath.newInstance("//Communication");
        List allCommunications = xpCommunications.selectNodes(corpus);
                for (Object o : allCommunications) {
            org.jdom.Element communication = (org.jdom.Element) o;
            //retrieve the communication name
            String communicationName = communication.getAttributeValue("Name");
            //pick up basic transcriptions
            XPath xpBasTrans = XPath.newInstance("Transcription[Description" +
                    "/Key[@Name='segmented']/text()='false']");
            List allBasTrans = xpBasTrans.selectNodes(communication);
            for (Object oB : allBasTrans) {
                org.jdom.Element basTrans = (org.jdom.Element) oB;
                String relPath = basTrans.getChildText("NSLink");
                String filePath = comafile.getParent() + File.separator +
                    relPath;
                filePath = filePath.substring(6);
                File file = new File(filePath);
                if (!file.isFile()) {
                    // we already checked validity of files in other checks
                    continue;
                }
                org.jdom.Element desc = basTrans.getChild("Description");
                List keys = desc.getChildren("Key");
                Set<String> addedTiers = new HashSet<String>();
                // add tiers that are already in the coma file to the set so that they are not added to the coma file
                // again from the exbs files
                for(Object key : keys){
                    org.jdom.Element keyElement = (org.jdom.Element) key;
                    if(keyElement.getAttributeValue("Name").startsWith("Tier")){
                        int fIndex = keyElement.getAttributeValue("Name").indexOf(" ");
                        int lIndex = keyElement.getAttributeValue("Name").lastIndexOf(" ");
                        addedTiers.add(keyElement.getAttributeValue("Name").substring(fIndex+1, lIndex));
                    }
                }
                BasicTranscription bt = new BasicTranscription(filePath);
                BasicBody bb = bt.getBody();
                String[] tierIDs = bb.getAllTierIDs();
                
                for (String tierID : tierIDs) {
                    if (skipTiers.contains(tierID)) {
                        stats.addNote(function,
                                    "Skipped a tier: " + tierID,
                                    "This tier does not need to be included in "
                                    + "coma file");
                        continue;
                    }
                    Tier tier = null;
                    try {
                        tier = bb.getTierWithID(tierID);
                    } catch (JexmaraldaException je) {
                        System.out.println("ERRORR: tier with ID " + tierID +
                                " is lost...");
                        continue;
                    }
                    String displayName = tier.getDisplayName();
                    String category = tier.getCategory();
                    String tierType = tier.getType();
//                    System.out.println("DEBUG: id,disp,cat" +
//                            tierID + " , " + displayName + " , " + category);
                    org.jdom.Element keyElement = new org.jdom.Element("Key");
                    boolean alreadyAdded = false;
                    for (String added : addedTiers) {
                        if (added.equals(category)) {
                            // no need to add twice?
                            alreadyAdded = true;
                        }
                    }
                    if (alreadyAdded) {
                        continue;
                    } else if (tiers.containsKey(category)) {
                        String describeTierType = "Unknown";
                        if (tierType.equals("a")) {
                            describeTierType = "Annotation";
                        } else if (tierType.equals("d")) {
                            describeTierType = "Description";
                        } else if (tierType.equals("t")) {
                            describeTierType = "Transcription";
                        } else {
                            describeTierType = "Unknown";
                        }
                        keyElement.setAttribute("Name",
                                String.format(tierNameFormat,
                                    describeTierType, category));
                        keyElement.setText(String.format(tierTextFormat,
                                    tiers.get(category)));
                        desc.addContent(keyElement);
                        stats.addNote(function,
                                    "Tier is missing from COMA: "
                                    + tierID);
                        addedTiers.add(category);
                    } else {
                        stats.addWarning(function,
                                    "Unrecognised tier category: "
                                    + category,
                                    "Tier must be added manually to coma");
                    }
                }
            }
        }
        return stats;
    }
    
    /**
    * Fix potential problems with HZSK repository depositing.
    */
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        Map<String, String> tiers = new HashMap<String, String>();
        tiers.put("akz", "Accentuation/stress");
        tiers.put("c", "Indicates that the automatic pos-annotation is " +
                "incorrect");
        tiers.put("cs", "Codeswitching");
        tiers.put("CW", "Annotation of code switching");
        tiers.put("de", "German translation");
        tiers.put("disfluency", "Disfluency");
        tiers.put("en", "English translation");
        tiers.put("fe", "English free translation");
        tiers.put("fg", "German free translation");
        tiers.put("fr", "Russian free translation");
        tiers.put("fh", "Hungarian free translation");
        tiers.put("so", "Source origin");
        tiers.put("ge", "Morphological annotation: " +
                "English gloss of each morpheme");
        tiers.put("gr", "Morphological annotation: " +
                "Russian gloss of each morpheme");
        tiers.put("hd", "Standard German translation");
        tiers.put("IST", "Annotation of information status");
        tiers.put("k", "Free Comment");
        tiers.put("lang", "Language of utterance");
        tiers.put("lemma", "Lemma");
        tiers.put("mb", "Morpheme break");
        tiers.put("mc", "Part of speech of each morpheme");
        tiers.put("mp", "Morphophonemes, underlying forms");
        tiers.put("mT", "Morphological transliteration");
        tiers.put("nt", "Notes on the text unit");
        tiers.put("nv", "Non-verbal");
        tiers.put("pho-adult", "Phonetic target structure");
        tiers.put("pho", "Phonetic annnotation");
        tiers.put("pos", "Part of Speech");
        tiers.put("pos-sup", "Superordinate part of Speech");
        tiers.put("ps", "Part of speech of each word");
        tiers.put("ref", "Name of the communication");
        tiers.put("SeR", "Annotation of semantic roles");
        tiers.put("st", "Source texts: normally in Cyrillic transliteration");
        tiers.put("sup", "suprasegmental information");
        tiers.put("SyF", "Annotation of syntactic function");
        tiers.put("syll", "Syllable structure");
        tiers.put("ts", "Transcription (what is heard)");
        tiers.put("tx", "Tier for interlinearization");
        tiers.put("type", "Type (spontaneous vs. imitated) of utterance");
        tiers.put("word", "Orthographic form of tokens");
        // These are NOT in the catalogue...?
        tiers.put("v", "Verbal");
        tiers.put("no", "Numbering");
        tiers.put("anno", "Anonymisation");
        tiers.put("nn", "Action by unspecified source");
        
        Set<String> skipTiers = new HashSet<String>();
        skipTiers.add("COLUMN-LABEL");
        skipTiers.add("ROW-LABEL");
        skipTiers.add("SUB-ROW-LABEL");
        skipTiers.add("EMPTY");
        skipTiers.add("EMPTY-EDITOR");
        Report stats = new Report();
        
        comafile = new File(cd.getURL().toString());
        String str = comafile.getPath().substring(6);
        org.jdom.Document corpus =
            org.exmaralda.common.jdomutilities.
                    IOUtilities.readDocumentFromLocalFile(
                            str);
        XPath xpCommunications = XPath.newInstance("//Communication");
        List allCommunications = xpCommunications.selectNodes(corpus);
        for (Object o : allCommunications) {
            org.jdom.Element communication = (org.jdom.Element) o;
            //retrieve the communication name
            String communicationName = communication.getAttributeValue("Name");
            //pick up basic transcriptions
            XPath xpBasTrans = XPath.newInstance("Transcription[Description" +
                    "/Key[@Name='segmented']/text()='false']");
            List allBasTrans = xpBasTrans.selectNodes(communication);
            for (Object oB : allBasTrans) {
                org.jdom.Element basTrans = (org.jdom.Element) oB;
                String relPath = basTrans.getChildText("NSLink");
                String filePath = comafile.getParent() + File.separator +
                    relPath;
                filePath = filePath.substring(6);
                File file = new File(filePath);
                if (!file.isFile()) {
                    // we already checked validity of files in other checks
                    continue;
                }
                org.jdom.Element desc = basTrans.getChild("Description");
                List keys = desc.getChildren("Key");
                Set<String> addedTiers = new HashSet<String>();
                // add tiers that are already in the coma file to the set so that they are not added to the coma file
                // again from the exbs files
                for(Object key : keys){
                    org.jdom.Element keyElement = (org.jdom.Element) key;
                    if(keyElement.getAttributeValue("Name").startsWith("Tier")){
                        int fIndex = keyElement.getAttributeValue("Name").indexOf(" ");
                        int lIndex = keyElement.getAttributeValue("Name").lastIndexOf(" ");
                        addedTiers.add(keyElement.getAttributeValue("Name").substring(fIndex+1, lIndex));
                    }
                }
                BasicTranscription bt = new BasicTranscription(filePath);
                BasicBody bb = bt.getBody();
                String[] tierIDs = bb.getAllTierIDs();
                
                for (String tierID : tierIDs) {
                    if (skipTiers.contains(tierID)) {
                        stats.addNote(function,
                                    "Skipped a tier: " + tierID,
                                    "This tier does not need to be included in "
                                    + "coma file");
                        continue;
                    }
                    Tier tier = null;
                    try {
                        tier = bb.getTierWithID(tierID);
                    } catch (JexmaraldaException je) {
                        System.out.println("ERRORR: tier with ID " + tierID +
                                " is lost...");
                        continue;
                    }
                    String displayName = tier.getDisplayName();
                    String category = tier.getCategory();
                    String tierType = tier.getType();
//                    System.out.println("DEBUG: id,disp,cat" +
//                            tierID + " , " + displayName + " , " + category);
                    org.jdom.Element keyElement = new org.jdom.Element("Key");
                    boolean alreadyAdded = false;
                    for (String added : addedTiers) {
                        if (added.equals(category)) {
                            // no need to add twice?
                            alreadyAdded = true;
                        }
                    }
                    if (alreadyAdded) {
                        continue;
                    } else if (tiers.containsKey(category)) {
                        String describeTierType = "Unknown";
                        if (tierType.equals("a")) {
                            describeTierType = "Annotation";
                        } else if (tierType.equals("d")) {
                            describeTierType = "Description";
                        } else if (tierType.equals("t")) {
                            describeTierType = "Transcription";
                        } else {
                            describeTierType = "Unknown";
                        }
                        keyElement.setAttribute("Name",
                                String.format(tierNameFormat,
                                    describeTierType, category));
                        keyElement.setText(String.format(tierTextFormat,
                                    tiers.get(category)));
                        desc.addContent(keyElement);
                        stats.addFix(function, cd,
                                    "Tier was missing from COMA: "
                                    + tierID +
                                    ": The default description has been added.");
                        addedTiers.add(category);
                    } else {
                        stats.addWarning(function,
                                    "Unrecognised tier category: "
                                    + category,
                                    "Tier must be added manually to coma");
                    }
                }
            }
        }
        try {
            CorpusIO cio = new CorpusIO();
            cio.write(corpus, settings.getOutputFile().toURI().toURL());                      
        } catch (TransformerException ex) {
            stats.addException(ex, function, cd, "unknown transformer error");
        } catch (ParserConfigurationException ex) {
            stats.addException(ex, function, cd, "unknown transformer error");
        } catch (UnsupportedEncodingException ex) {
            stats.addException(ex, function, cd, "unknown transformer error");
        } catch (XPathExpressionException ex) {
            stats.addException(ex, function, cd, "unknown transformer error");
        }
        return stats;
    }
    
    /**
    * Default function which determines for what type of files (basic transcription, 
    * segmented transcription, coma etc.) this feature can be used.
    */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, " usable class not found");
        }
        return IsUsableFor;
    }


    /**Default function which returns a two/three line description of what 
     * this class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class loads coma data and for all communications adds "
                + "all tiers found in the linked exb as a key value pairs to the description. ";
        return description;
    }

    @Override
    public Report check(Corpus c) {
    Report stats = new Report();
    CorpusData cdata = c.getComaData();
        try {
            stats = exceptionalCheck(cdata);
        } catch (JexmaraldaException je) {
            stats.addException(je, function, cdata, "Unknown parsing error");
        } catch (JDOMException jdome) {
            stats.addException(jdome, function, cdata, "Unknown parsing error");
        } catch (SAXException saxe) {
            stats.addException(saxe, function, cdata, "Unknown parsing error");
        } catch (IOException ioe) {
            stats.addException(ioe, function, cdata, "Reading/writing error");
        }
        return stats;    
    }

    @Override
    public Report function(CorpusData cd, Boolean fix) throws SAXException, IOException, ParserConfigurationException, JexmaraldaException, TransformerException, XPathExpressionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
