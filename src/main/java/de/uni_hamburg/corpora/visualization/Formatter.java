/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.AbstractResourceProcessor;
import de.uni_hamburg.corpora.validation.ErrorMessage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public class Formatter extends AbstractResourceProcessor {

    @Override
    public Collection<ErrorMessage> exceptionalFix(File fileToBeFixed) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 
    /**
     * 
     * 
     */
}
