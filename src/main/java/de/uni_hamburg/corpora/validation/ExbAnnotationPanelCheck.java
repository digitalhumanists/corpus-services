/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import org.exmaralda.common.corpusbuild.AbstractBasicTranscriptionChecker;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.Event;
import org.exmaralda.partitureditor.jexmaralda.Tier;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class ExbAnnotationPanelCheck {
//    public class AnnotationPanelChecker extends AbstractBasicTranscriptionChecker {
//
//    public BasicTranscription basictranscription;
//    public File annotationSpecificationFile;
//
//    /**
//     *
//     * @author fsnv625
//     */
//    public AnnotationPanelChecker() {
//        super();
//    }
//
//    //get the possible tags from the annotationSpecification
//    public ArrayList<String> getAllTagsFromAnnotationSpecification(File annotationSpecificationFile) throws JDOMException, IOException {
//        //this array list allTagStrings stores all the possible tags from the annotationSpecificationFile
//        ArrayList<String> allTagStrings = new ArrayList<String>();
//        //read all the tags from file
//        Document doc = org.exmaralda.exakt.utilities.FileIO.readDocumentFromLocalFile(annotationSpecificationFile);
//        Iterator i = doc.getRootElement().getDescendants(new ElementFilter("tag"));
//        //adds every tag to the array list
//        while (i.hasNext()) {
//            Element tag = (Element) (i.next());
//            String tagString = tag.getAttributeValue("name");
//            allTagStrings.add(tagString);
//            //System.out.println(tagString);
//            if (tagString == null) {
//                System.out.println("There are no tags specified in the file");
//            }
//        }
//        return allTagStrings;
//    }
//
//    //get every annotation event and check if it is identical with one of the possible tags
//    public void CheckBasicTranscriptionAnnotationTags(BasicTranscription basictranscription, File annotationSpecificationFile, String currentFilename)
//            throws JDOMException, IOException, URISyntaxException {
//        for (int pos = 0; pos < basictranscription.getBody().getNumberOfTiers(); pos++) {
//            Tier tier = basictranscription.getBody().getTierAt(pos);
//            //single out only the annotation tiers
//            if (tier.getType().equals("a")) {
//                //go trough every event of that tier
//                for (int pos2 = 0; pos2 < tier.getNumberOfEvents(); pos2++) {
//                    //get the event
//                    Event event = tier.getEventAt(pos2);
//                    //convert the event content to a string
//                    String content = event.getDescription();
//                    //System.out.println(content);
//                    //get every possible tag
//                    ArrayList<String> allTagStrings = getAllTagsFromAnnotationSpecification(annotationSpecificationFile);
//                    //check if the content is contained in the possible tags
//                    if (!(allTagStrings.contains(content))) {
//                        //write the errors to errors document
//                        String text = "Incompatible tag";
//                        addError(currentFilename, tier.getID(), event.getStart(), text);
//                        System.out.println(content);
//                        System.out.println(currentFilename + " " + tier.getID() + " " + event.getStart() + " " + text);
//                    }
//
//                }
//            }
//        }
//        System.out.println(getErrorsDocoument());
//    }
//
//    @Override
//    public void processTranscription(BasicTranscription bt, String currentFilename) throws URISyntaxException, SAXException {
//
//    }
}
