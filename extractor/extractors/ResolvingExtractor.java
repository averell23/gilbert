/*
 * ResolvingExtractor.java
 *
 * Created on 25 January 2002, 11:56
 */

package gilbert.extractor.extractors;
import java.net.*;
import gilbert.extractor.*;

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

    protected void handleVisit(Visit v) {
        String host = v.getProperty("visit.host");
        Util.logMessage("Resolver: Original hostname is: " + host, Util.LOG_DEBUG);
        try {
            InetAddress addy = InetAddress.getByName(host);
            host = addy.getHostName();
            Util.logMessage("Reset hostname to: " + host, Util.LOG_DEBUG);
            v.setProperty("visit.host", host);
        } catch (UnknownHostException e) {
            Util.logMessage("ResolvingExtractor: Warning: Cannot resolve host " + host, Util.LOG_WARN);
            Util.logMessage("Exception: " + e.getMessage(), Util.LOG_WARN);
        }
        super.handleVisit(v);
    }

}
