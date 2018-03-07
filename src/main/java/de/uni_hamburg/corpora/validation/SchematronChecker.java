/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;
import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.CorpusFunction;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.jdom.JDOMException;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.xml.sax.SAXException;

/**
 *
 * @author Daniel Jettka, daniel.jettka@uni-hamburg.de
 */
public class SchematronChecker extends Checker implements CorpusFunction{

    @Override
    public Report fix(CorpusData cd) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report fix(Collection<CorpusData> cdc) throws SAXException, JDOMException, IOException, JexmaraldaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report execute(Corpus c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report check(CorpusData cd) throws SAXException, JexmaraldaException {
        
        Report r = new Report();
        
        //for now choosing fix schematron file
        ClassLoader classLoader = getClass().getClassLoader();
        File schematronFile = new File(classLoader.getResource("/schematron/nslc-exb.sch").getFile());

        final ISchematronResource aResSCH = SchematronResourceSCH.fromFile (schematronFile);
        if (!aResSCH.isValidSchematron ())            
            r.addCritical("", new IllegalArgumentException ("Invalid Schematron!"), "Schematron validation not executed!");
        
        try {
            SchematronOutputType sot = aResSCH.applySchematronValidationToSVRL (TypeConverter.String2StreamSource(cd.toSaveableString()));
            
            List<String> schNoteList = sot.getText();
            for (int i = 0; i < schNoteList.size(); i++) {
                r.addNote("schematron", schNoteList.get(i)); 
            }
                   
        } catch (Exception ex) {
            r.addCritical("", ex, "Schematron validation not executed!");            
        }
        
        return r;
        
    }

    @Override
    public Report check(Collection<CorpusData> cdc) throws SAXException, JexmaraldaException, IOException, JDOMException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

    @Override
    public Collection<Class> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");   
            IsUsableFor.add(cl);
            Class cl2 = Class.forName("de.uni_hamburg.corpora.UnspecifiedXMLData");
            IsUsableFor.add(cl2);
             Class cl3 = Class.forName("de.uni_hamburg.corpora.ComaData");
            IsUsableFor.add(cl2);
            IsUsableFor.add(cl3);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PrettyPrintData.class.getName()).log(Level.SEVERE, null, ex);
        }
    return IsUsableFor;
    }
}
