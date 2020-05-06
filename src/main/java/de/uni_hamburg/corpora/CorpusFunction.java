/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.io.IOException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.partitureditor.fsm.FSMException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.jdom.transform.XSLTransformException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 */
public interface CorpusFunction {

public Report execute(CorpusData cd);
 
public Report execute(Corpus c);

public Report execute(CorpusData cd, boolean fix);

public Report execute(Corpus c, boolean fix);

public Collection<Class<? extends CorpusData>> getIsUsableFor();

public void setIsUsableFor(Collection<Class<? extends CorpusData>> cdc);

public String getDescription();

public String getFunction();
}
