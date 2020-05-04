/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.Corpus;
import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.Report;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ozzy
 */
public class ComaUpdateSegmentCountsTest {
    
    public ComaUpdateSegmentCountsTest() {
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
     * Test of fix method, of class ComaUpdateSegmentCounts.
     */
    @Test
    public void testFix() throws Exception {
            
            System.out.println("fix");
            String corpusFolder = "K:\\Kamas\\KamasCorpus";
            URL corpusURL = Paths.get(corpusFolder).toUri().toURL();
            Corpus corp = new Corpus(corpusURL);
            ComaUpdateSegmentCounts instance = new ComaUpdateSegmentCounts();
            instance.report = new Report();
            Collection<CorpusData> cdc;
            //what happens when we fix coma files
            for (CorpusData cd : corp.getMetadata()){
                assertNotNull(instance.fix(cd));
            }
    }

    /**
     * Test of getIsUsableFor method, of class ComaUpdateSegmentCounts.
     */
    @Test
    public void testGetIsUsableFor() {
        System.out.println("getIsUsableFor");
        ComaUpdateSegmentCounts instance = new ComaUpdateSegmentCounts();
        //Collection<Class> expResult = null;
        Collection<Class<? extends CorpusData>> result = instance.getIsUsableFor();
        //no null object here
        assertNotNull(result);
    }
    
    
}
