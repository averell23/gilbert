/*
 * DumpExtractor.java
 *
 * Created on 10 December 2001, 10:49
 */

package gilbert.extractor.extractors;
import gilbert.extractor.*;

/**
 *
 * @author  daniel
 * @version 
 */
public class DumpExtractor extends gilbert.extractor.Extractor {
    
    protected void handleVisit(Visit v) {
        System.out.println("Recieved visit from: " + v.getProperty("visit.host"));
    }
}
