/*
 * AliveFilter.java
 *
 * Created on 14 March 2002, 13:57
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;
import org.apache.log4j.*;

/**
 * Drops URLS which are not alive.
 *
 * @author Daniel Hahn
 * @version CVS $Revision$
 */
public class AliveFilter implements URLFilter {
        
    /// Logger for this class
    protected Logger logger = Logger.getLogger(this.getClass());

    /** Returns true if, and only if, the given url is accpeted by the filter.
     */
    public boolean accept(VisitorURL url) {
        if (!Util.siteStatus(url.getProperty("url.name")).getAlive()) {
            if (logger.isDebugEnabled()) logger.info("Non-alive URL dropped: "
                                         + url.getProperty("url.name"));
            return false;
        }
        return true;
    }    

}
