/*
 * SimpleExtractor.java
 *
 * Created on 06 March 2002, 11:29
 */

package gilbert.extractor.extractors;

import gilbert.extractor.*;
import java.util.*;
import org.xml.sax.*;
import org.apache.log4j.*;

/**
 * Finds a start URL by simply using the visit's source.
 *
 * @author  daniel
 */
public class SimpleExtractor extends Extractor {
    /// Logger for this class.
    protected Logger logger;
    
    /** Creates a new instance of SimpleExtractor */
    public SimpleExtractor() {
        logger = Logger.getLogger(this.getClass());
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
        if (logger.isInfoEnabled()) logger.info("Handling host: " + host);
        if (Util.hostnameType(host) == Util.HOST_NAME) {
            if (logger.isDebugEnabled()) logger.debug("New hostname: " + host);
            startTag("url");
            printTag("name", "http://" + host + "/");
            String location = v.getProperty("visit.location_code");
            if (location != null) printTag("location_code", location);
            String time = v.getProperty("visit.timestamp");
            if (time != null) printTag("timestamp", time);
            String referer = v.getProperty("visit.referer.url");
            if (referer != null) printTag("referer", referer);
            printTag("degree", "0");
            endTag("url");
        }
    }
    
    public void extract(String uri) {
        visitHash = new Hashtable();
        logger.info("Initializing.");
        super.extract(uri);
    }
    
    public void extract(InputSource input) {
        visitHash = new Hashtable();
        logger.info("Initializing.");
        super.extract(input);
    }
}
