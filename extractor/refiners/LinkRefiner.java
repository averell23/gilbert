/*
 * LinkRefiner.java
 *
 * Created on 07 March 2002, 14:54
 */

package gilbert.extractor.refiners;

import gilbert.extractor.*;
import java.util.*;

/**
 * Finds all URLs linked from the current URL. 
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class LinkRefiner extends Refiner {
    
    /** Creates a new instance of LinkRefiner */
    public LinkRefiner() {
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
        Util.logMessage("LinkRefiner: Handling URL " + url, Util.LOG_DEBUG);
        SiteInfo info = Util.siteStatus(urlStr);
        Vector links = info.getLinks();
        if (links.size() > 0) {
            int degree = 0;
            try {
                String degreeStr = (String) url.getProperty("url.degree");
                degree = Integer.parseInt(degreeStr);
            } catch (NumberFormatException e) {
                Util.logMessage("LinkRefiner: Unable to determine degree for URL " + url, Util.LOG_WARN);
            }
            Enumeration linksE = links.elements();
            while (linksE.hasMoreElements()) {
                String link = (String) linksE.nextElement();
                Util.logMessage("LinkRefiner: Added linked URL " + link, Util.LOG_MESSAGE);
                startTag("url");
                printTag("url.timestamp", (String) url.getProperty("url.timestamp"));
                printTag("url.degree", "" + (degree + 1));
                endTag("url");
            }
        }
    }
    
}
