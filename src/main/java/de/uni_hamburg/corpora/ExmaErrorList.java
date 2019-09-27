/**
 *
 * Auxiliary data structure for creating Exmaralda error list files. 
 *
 */

package de.uni_hamburg.corpora;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ExmaErrorList {

    /**
     * the data structure holding all statistics.
     */
    private static Map<String, Collection<ExmaErrorListItem>> statistics;

    /**
     * Create an empty error list.
     */
    public ExmaErrorList() {
        statistics = new HashMap<String, Collection<ExmaErrorListItem>>();
    }

    /**
     * convenience function to create new statistic set if missing or get old.
     */
    private Collection<ExmaErrorListItem> getOrCreateStatistic(String statId) {
        if (!statistics.containsKey(statId)) {
            statistics.put(statId, new ArrayList<ExmaErrorListItem>());
        }
        return statistics.get(statId);
    }

    /**
     * Merge two error lists. Efficiently adds statistics from other error list
     * to this one.
     */
    public void merge(ExmaErrorList sr) {
        for (Map.Entry<String, Collection<ExmaErrorListItem>> kv
                : sr.statistics.entrySet()) {
            if (statistics.containsKey(kv.getKey())) {
                Collection<ExmaErrorListItem> c
                        = statistics.get(kv.getKey());
                c.addAll(kv.getValue());
                statistics.put(kv.getKey(), c);
            } else {
                statistics.put(kv.getKey(), kv.getValue());
            }
        }
    }

    /**
     * Add an error in named statistics bucket.
     * @todo change input filename to corpusdata object
     */
    public void addError(String statId, String fileName, String tierID, String eventStart, boolean done, String description) {
        Collection<ExmaErrorListItem> stat = getOrCreateStatistic(statId);
        stat.add(new ExmaErrorListItem(fileName, tierID, eventStart, done, description));
    }

    /**
     * Create the error list xml file from all the errors.
     */
    public static Document createFullErrorList() throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement;
        rootElement = doc.createElement("error-list");
        doc.appendChild(rootElement);
        Element secondElement = doc.createElement("errors");
        rootElement.appendChild(secondElement);
        for (Collection<ExmaErrorListItem> col : statistics.values()) {
            for (ExmaErrorListItem item : col) {
                Element error = doc.createElement("error");
                Attr fl = doc.createAttribute("file");
                fl.setValue(item.getFileName());
                error.setAttributeNode(fl);
                Attr tier = doc.createAttribute("tier");
                tier.setValue(item.getTierID());
                error.setAttributeNode(tier);
                Attr start = doc.createAttribute("start");
                start.setValue(item.getEventStart());
                error.setAttributeNode(start);
                Attr done = doc.createAttribute("done");
                if(item.isDone())
                    done.setValue("yes");
                else
                    done.setValue("no");
                error.setAttributeNode(done);
                error.appendChild(doc.createTextNode(item.getDescription()));
                secondElement.appendChild(error);
            }
        }
        return doc;
        /* TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        File f = new File(location.getFile());
        URI u = f.toURI();
        StreamResult result = new StreamResult(new File(u));
        transformer.transform(source, result); */
    }
}