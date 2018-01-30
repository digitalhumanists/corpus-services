/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.AbstractResourceProcessor;
import de.uni_hamburg.corpora.StatisticsReport;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author fsnv625
 *
 *
 * Wenn formatieren nicht teil von visualisieren ist, brauchen wir noch formatCorpus bzw. formatTranscription :)
In den alten WritePartiturs und WriteUtteranceLists werden auch Formatierungsstylesheets als Parameter genommen, wir müssen jetzt überlegen, wo und wie diese angewendet werden (und wo sie herkommen, wir hatten darüber gesprochen, stumpf ALLE dateien in Coma zu referenzieren, das auf Korpusebene sieht der Nutzer ja nicht) und wo wir defaults hinlegen für die Korpora, die keine besonderen Stylesheets wollen.
Dann müssen wir auch unterscheiden zwischen Repo-(Web-)Design und Transkript-Layout/Design und Einheitlichkeit (nur) dort erzwingen, wo es keine bzw. kaum Theorie gibt.
 *
 */
public class Formatter extends AbstractResourceProcessor {

    @Override
    public StatisticsReport exceptionalFix(File fileToBeFixed) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     *
     */
}
