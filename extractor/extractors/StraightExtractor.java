/*
 * StraightExtractor.java
 *
 * Created on 10 December 2001, 17:20
 */

package gilbert.extractor.extractors;
import org.xml.sax.*;
import gilbert.extractor.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * This is a simple Extractor, taking URLs directly from hostnames. This
 * will try the original host, then most parts of the name, prefixed with
 * "www". IP Adresses will not be used for extraction
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class StraightExtractor extends Extractor {
    /// Hash for all entries that have already been extracted.
    Hashtable visitHash;
    /// HTTP connection timeout backup
    String connectTO;
    /// HTTP read timeout backup
    String readTO;
    /// Logger for this class
    protected Logger logger;
    
    /**
     * Creates a new StraightExtractor.
     * @param dumpMode If true, the Extractor will not do Web lookups,
     */
    public StraightExtractor() {
        super();
        logger = Logger.getLogger(this.getClass());
        visitHash = new Hashtable();
        logger.debug("Created.");
    }
    
    /**
     * Recieves record of visit information from the XMLReader.
     * Override this to do the actual extraction work.
     */
    protected void handleVisit(Visit v) {
        String host = v.getProperty("visit.host");
        if (logger.isInfoEnabled()) logger.info("Handling host: " + host);
        if ((!visitHash.containsKey(host)) && (Util.hostnameType(host) == Util.HOST_NAME)) {
            visitHash.put(host, "visited");
            if (logger.isDebugEnabled()) logger.debug("New hostname: " + host);
            if (Util.siteStatus("http://" + host + "/").getAlive()) {
                startTag("url");
                printTag("name", "http://" + host + "/");
                String location = v.getProperty("visit.location_code");
                if (location != null) printTag("location_code", location);
                String time = v.getProperty("visit.timestamp");
                if (time != null) printTag("timestamp", time);
                endTag("url");
            }
            StringBuffer buf = new StringBuffer(host);
            // Find the last dot in the hostname
            int lastPart = buf.lastIndexOf(".");
            // current position
            int pos = buf.indexOf(".");
            while ((pos < lastPart) && (pos != -1)) {
                String subdom = "www" + buf.substring(pos);
                if (!visitHash.containsKey(subdom)) {
                    if (Util.siteStatus("http://" + subdom + "/").getAlive()) {
                        if (logger.isDebugEnabled()) logger.debug("Found URL: " + subdom);
                        startTag("url");
                        printTag("name", "http://" + subdom + "/");
                        String location = v.getProperty("visit.location_code");
                        if (location != null) printTag("location_code", location);
                        String time = v.getProperty("visit.timestamp");
                        if (time != null) printTag("timestamp", time);
                        printTag("degree", "0");
                        endTag("url");
                    }
                    visitHash.put(subdom, "visited");
                }
                pos = buf.indexOf(".", pos + 1);
            }
        }
    }
    
    public void extract(String uri) {
        visitHash = new Hashtable();
        logger.info("Initialized.");
        super.extract(uri);
    }
    
    public void extract(InputSource input) {
        visitHash = new Hashtable();
        logger.info("Initialized.");
        super.extract(input);
    }
}