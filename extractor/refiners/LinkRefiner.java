/*
 * LinkRefiner.java
 *
 * Created on 07 March 2002, 14:54
 */

package gilbert.extractor.refiners;

import gilbert.extractor.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * Finds all URLs linked from the current URL.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class LinkRefiner extends Refiner {
    /// Logger for this class
    Logger logger;
    
    /** Creates a new instance of LinkRefiner */
    public LinkRefiner() {
        passing = true;
        maxHandlers = 20;
        logger = Logger.getLogger(this.getClass());
        logger.debug("Created.");
    }
    
    /** Must be overridden by child classes to handle each of
     * the URLs.
     *
     * It's up to the child class to use the proper methods or the
     * <code>outStream</code> for printing the resutls and to honour
     * the postfilters.
     */
    public void handleURL(VisitorURL url) {
        String urlStr = url.getProperty("url.name");
        logger.debug("handleURL()");
        SiteInfo info = Util.siteStatus(urlStr);
        Vector links = info.getLinks();
        if (links.size() > 0) {
            int degree = 0;
            try {
                String degreeStr = (String) url.getProperty("url.degree");
                degree = Integer.parseInt(degreeStr);
            } catch (NumberFormatException e) {
                logger.warn("LinkRefiner: Unable to determine degree for URL "
                + url + " (" + e.getMessage() + ")" );
            }
            Enumeration linksE = links.elements();
            while (linksE.hasMoreElements()) {
                String link = (String) linksE.nextElement();
                if (logger.isInfoEnabled()) logger.info("LinkRefiner: Added linked URL " + link);
                synchronized (outStream) {
                    startTag("url");
                    printTag("name", link);
                    printTag("timestamp", (String) url.getProperty("url.timestamp"));
                    printTag("degree", "" + (degree + 1));
                    endTag("url");
                }
            }
        }
    }
    
}
