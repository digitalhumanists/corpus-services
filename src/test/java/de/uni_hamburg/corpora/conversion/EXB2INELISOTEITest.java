/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.conversion;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.Report;
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
     * Test of convertCD2MORPHEMEHIATISOTEI method, of class EXB2INELISOTEI.
     */
    @Test
    public void testConvertCD2MORPHEMEHIATISOTEI_CorpusData() throws Exception {
        System.out.println("convertCD2MORPHEMEHIATISOTEI");
        /* CorpusData cd = null;
        EXB2INELISOTEI instance = new EXB2INELISOTEI();
        Report expResult = null;
        Report result = instance.convertCD2MORPHEMEHIATISOTEI(cd);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of convertCD2MORPHEMEHIATISOTEI method, of class EXB2INELISOTEI.
     */
    @Test
    public void testConvertCD2MORPHEMEHIATISOTEI_3args() throws Exception {
        System.out.println("convertCD2MORPHEMEHIATISOTEI");
        /* CorpusData cd = null;
        boolean includeFullText = false;
        String XPath2Morphemes = "";
        EXB2INELISOTEI instance = new EXB2INELISOTEI();
        Report expResult = null;
        Report result = instance.convertCD2MORPHEMEHIATISOTEI(cd, includeFullText, XPath2Morphemes);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of SegmentedTranscriptionToTEITranscription method, of class EXB2INELISOTEI.
     */
    @Test
    public void testSegmentedTranscriptionToTEITranscription() throws Exception {
        /* System.out.println("SegmentedTranscriptionToTEITranscription");
        Document segmentedTranscription = null;
        String nameOfDeepSegmentation = "";
        String nameOfFlatSegmentation = "";
        boolean includeFullText = false;
        EXB2INELISOTEI instance = new EXB2INELISOTEI();
        Document expResult = null;
        Document result = instance.SegmentedTranscriptionToTEITranscription(segmentedTranscription, nameOfDeepSegmentation, nameOfFlatSegmentation, includeFullText);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of TEIMerge method, of class EXB2INELISOTEI.
     */
    @Test
    public void testTEIMerge_3args() throws Exception {
        /* System.out.println("TEIMerge");
        Document segmentedTranscription = null;
        String nameOfDeepSegmentation = "";
        String nameOfFlatSegmentation = "";
        Vector expResult = null;
        Vector result = EXB2INELISOTEI.TEIMerge(segmentedTranscription, nameOfDeepSegmentation, nameOfFlatSegmentation);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of TEIMerge method, of class EXB2INELISOTEI.
     */
    @Test
    public void testTEIMerge_4args() throws Exception {
        System.out.println("TEIMerge");
        /* Document segmentedTranscription = null;
        String nameOfDeepSegmentation = "";
        String nameOfFlatSegmentation = "";
        boolean includeFullText = false;
        Vector expResult = null;
        Vector result = EXB2INELISOTEI.TEIMerge(segmentedTranscription, nameOfDeepSegmentation, nameOfFlatSegmentation, includeFullText);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of merge method, of class EXB2INELISOTEI.
     */
    @Test
    public void testMerge() {
        System.out.println("merge");
        /* Element e1 = null;
        Element e2 = null;
        Element expResult = null;
        Element result = EXB2INELISOTEI.merge(e1, e2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
    }

    /**
     * Test of check method, of class EXB2INELISOTEI.
     */
    @Test
    public void testCheck() throws Exception {
        System.out.println("check");
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
        /* EXB2INELISOTEI instance = new EXB2INELISOTEI();
        Collection<Class<? extends CorpusData>> expResult = null;
        Collection<Class<? extends CorpusData>> result = instance.getIsUsableFor();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype."); */
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
