/*
 * SimpleExtractor.java
 *
 * Created on 06 March 2002, 11:29
 */

package gilbert.extractor.extractors;

import gilbert.extractor.*;
import java.util.*;
import org.xml.sax.*;

/**
 * Finds a start URL by simply using the visit's source.
 *
 * @author  daniel
 */
public class SimpleExtractor extends Extractor {
    /** Hashtable containing alredy visited hosts */
    Hashtable visitHash;
    
    /** Creates a new instance of SimpleExtractor */
    public SimpleExtractor() {
        visitHash = new Hashtable();
    }
    
    /**
     * This is the method that child classes should override
     * to handle each visit. This will be called from
     * <code>recieveVisit()</code> if the visit passed the
     * filters.
     *
     * It's up to the child class to use the <code>outStream</code>
     * and the proper methods to write the results and to honor the
     * postfilters.
     */
    protected void handleVisit(Visit v) {
         String host = v.getProperty("visit.host");
        Util.logMessage("SimpleExtractor: Handling host: " + host, Util.LOG_MESSAGE);
        if ((!visitHash.containsKey(host)) && (Util.hostnameType(host) == Util.HOST_NAME)) {
            visitHash.put(host, "visited");
            Util.logMessage("New hostname: " + host, Util.LOG_DEBUG);
            startTag("url");
            printTag("name", "http://" + host + "/");
            String location = v.getProperty("visit.location_code");
            if (location != null) printTag("location_code", location);
            String time = v.getProperty("visit.timestamp");
            if (time != null) printTag("timestamp", time);
            endTag("url");
        }
    }
    
    public void extract(String uri) {
        visitHash = new Hashtable();
        Util.logMessage("SimpleExtractor initialized.", Util.LOG_MESSAGE);
        super.extract(uri);
    }
    
    public void extract(InputSource input) {
        visitHash = new Hashtable();
        Util.logMessage("SimpleExtractor initialized.", Util.LOG_MESSAGE);
        super.extract(input);
    }
}
