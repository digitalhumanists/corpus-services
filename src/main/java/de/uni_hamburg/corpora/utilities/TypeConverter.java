/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uni_hamburg.corpora.utilities;

import java.io.InputStream;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.xml.sax.SAXException;

/**
 *
 * @author Daniel Jettka
 */
public class TypeConverter {
    
    public static String InputStream2String (InputStream is){
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    } 
    
    public static String BasicTranscription2String (BasicTranscription bt){        
        return bt.toXML();
    } 
    
    public static BasicTranscription String2BasicTranscription (String btAsString) throws SAXException, 
                                                                                          JexmaraldaException{
        BasicTranscription bt = new BasicTranscription();
        bt.BasicTranscriptionFromString(btAsString);
        return bt;
    }
    
    public static StreamSource String2StreamSource(String s){
        StreamSource ss = new StreamSource(new StringReader(s));
        return ss;
    }
}
