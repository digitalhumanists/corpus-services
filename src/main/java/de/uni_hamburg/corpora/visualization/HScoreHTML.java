/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.visualization;

import de.uni_hamburg.corpora.CorpusData;
import de.uni_hamburg.corpora.Report;
import de.uni_hamburg.corpora.utilities.TypeConverter;
import de.uni_hamburg.corpora.utilities.XSLTransformer;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Daniel Jettka
 */
public class HScoreHTML extends Visualizer {

    // resources loaded from directory supplied in pom.xml
    private static final String STYLESHEET_PATH = "/xsl/EXB2hScoreHTML.xsl";
    private final String SERVICE_NAME = "HScoreHTML";


    public HScoreHTML(String btAsString){
        createFromBasicTranscription(btAsString);
    }


	 /**
	 * This method deals performs the transformation of EXB to horizontal Score HTML
	 *
	 * @param  btAsString  the EXB file represented in a String object
	 * @return
	 */
    public void createFromBasicTranscription(String btAsString){

        basicTranscriptionString = btAsString;
        basicTranscription = TypeConverter.String2BasicTranscription(btAsString);

        String result = null;

        try {

            String xsl = TypeConverter.InputStream2String(getClass().getResourceAsStream(STYLESHEET_PATH));

            // perform XSLT transformation
            XSLTransformer xt = new XSLTransformer();
            xt.setParameter("EMAIL_ADDRESS", EMAIL_ADDRESS);
            xt.setParameter("WEBSERVICE_NAME", SERVICE_NAME);
            xt.setParameter("HZSK_WEBSITE", HZSK_WEBSITE);
            result = xt.transform(basicTranscriptionString, xsl);


        } catch (TransformerException ex) {
            Logger.getLogger(HScoreHTML.class.getName()).log(Level.SEVERE, null, ex);
        }

        setHTML(result);
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Usage: " + HScoreHTML.class.getName() +
                        "EXB [HTML]");
                System.exit(1);
            } else {
                byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
                String btString = new String(encoded, "UTF-8");
                HScoreHTML score = new HScoreHTML(btString);
                if (args.length >= 2) {
                    PrintWriter htmlOut = new PrintWriter(args[1]);
                    htmlOut.print(score.getHTML());
                    htmlOut.close();
                } else {
                    System.out.println(score.getHTML());
                }
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public Report visualize(CorpusData cd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Report doMain(String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
