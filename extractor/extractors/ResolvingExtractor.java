/*
 * ResolvingExtractor.java
 *
 * Created on 25 January 2002, 11:56
 */

package gilbert.extractor.extractors;
import java.net.*;
import gilbert.extractor.*;
import org.apache.log4j.*;

/**
 * This should work exactly as the <code>StraightExtractor</code>, except
 * that it will try to resolve all IP addresses to proper hostnames.<br>
 * Please note that the <i>prefilters</i> will of course be applied 
 * <i>before</i> resolving the address.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class ResolvingExtractor extends StraightExtractor {
    /// Logger for this class
    protected Logger logger;

    protected void handleVisit(Visit v) {
        logger = Logger.getLogger(this.getClass());
        String host = v.getProperty("visit.host");
        if (logger.isDebugEnabled()) logger.debug("Original hostname is: " + host);
        try {
            InetAddress addy = InetAddress.getByName(host);
            host = addy.getHostName();
            if (logger.isDebugEnabled()) logger.debug("Reset hostname to: " + host);
            v.setProperty("visit.host", host);
        } catch (UnknownHostException e) {
            logger.info("Cannot resolve host " + host + " (" + e.getMessage() + ")");
        }
        super.handleVisit(v);
    }

}
