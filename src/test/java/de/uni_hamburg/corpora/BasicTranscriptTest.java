/**
 * @file BasicTranscriptionData.java
 *
 * Connects BasicTranscription from Exmaralda to HZSK corpus services.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */



import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import de.uni_hamburg.corpora.BasicTranscriptionData;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;

public class BasicTranscriptTest {


    @Test
    public void readWriteBT() {
        try {
            File exbFile = new File("src/test/java/de/uni_hamburg/corpora/resoruces/example.exb");
            BasicTranscriptionData btd = new BasicTranscriptionData();
            btd.loadFile(exbFile);
            String prettyXML = btd.toSaveableString();
            assertNotNull(prettyXML);
        } catch (SAXException saxe) {
            saxe.printStackTrace();
            fail("Unexpected exception " + saxe);
        } catch (JexmaraldaException je) {
            je.printStackTrace();
            fail("Unexpected exception " + je);
        }
    }
}
