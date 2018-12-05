/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.conversion;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.validation.PrettyPrintData;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Vector;
import org.jdom.Document;
import org.jdom.Element;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fsnv625
 */
public class EXB2INELISOTEITest {
    
    public EXB2INELISOTEITest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of check method, of class EXB2INELISOTEI.
     */
    //we expect a class cast exception because we run it on a coma file even if we know that doesnT work
    @Test(expected = ClassCastException.class)
    public void testCheck() throws Exception {
        System.out.println("check");
        String corpusFolder = "src/test/java/de/uni_hamburg/corpora/resources/example";
            URL corpusURL = Paths.get(corpusFolder).toUri().toURL();
            Corpus corp = new Corpus(corpusURL);
            EXB2INELISOTEI instance = new EXB2INELISOTEI();
            instance.report = new Report();
            //what happens when we check coma files
            Collection<CorpusData> cdc;
            //what happens when we check coma files
            for (CorpusData cd : corp.getMetadata()){
                assertNull(instance.check(cd));
                //shouldn't be pretty printed yet
                //assertFalse(instance.CorpusDataIsAlreadyPretty(cd));
            }
            //what happens when we check exb files
            for (CorpusData cd : corp.getContentdata()){
                assertNotNull(instance.check(cd));
                assertNotNull(instance.report.getSummaryLines());
                //shouldn't be pretty printed yet
                //assertTrue(instance.CorpusDataIsAlreadyPretty(cd));
            }
            //what happens when we check annotation files
            for (CorpusData cd : corp.getAnnotationspecification()){
                assertNotNull(instance.check(cd));
                //shouldn't be pretty printed yet
                //assertFalse(instance.CorpusDataIsAlreadyPretty(cd));
            }
        /* CorpusData cd = null;
        EXB2INELISOTEI instance = new EXB2INELISOTEI();
        Report expResult = null;
        Report result = instance.check(cd);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of fix method, of class EXB2INELISOTEI.
     */
    @Test
    public void testFix() throws Exception {
        System.out.println("fix");
        /* CorpusData cd = null;
        EXB2INELISOTEI instance = new EXB2INELISOTEI();
        Report expResult = null;
        Report result = instance.fix(cd);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of getIsUsableFor method, of class EXB2INELISOTEI.
     */
    @Test
    public void testGetIsUsableFor() {
        System.out.println("getIsUsableFor");
        System.out.println("getIsUsableFor");
        EXB2INELISOTEI instance = new EXB2INELISOTEI();
        //Collection<Class> expResult = null;
        Collection<Class<? extends CorpusData>> result = instance.getIsUsableFor();
        //no null object here
        assertNotNull(result);
    }

    /**
     * Test of setIsUsableFor method, of class EXB2INELISOTEI.
     */
    @Test
    public void testSetIsUsableFor() {
        System.out.println("setIsUsableFor");
        /* Collection<Class<? extends CorpusData>> cdc = null;
        EXB2INELISOTEI instance = new EXB2INELISOTEI();
        instance.setIsUsableFor(cdc);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }
    
}
