/*
 * LinkRefiner.java
 *
 * Created on 07 March 2002, 14:54
 */

package gilbert.extractor.refiners;

import gilbert.extractor.*;

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
        String url = url.getProperty("url.name");
        Util.logMessage("LinkRefiner: Handling URL " + url, Util.LOG_DEBUG);
        SiteInfo info = Util.siteStatus(url);
        Vector links = info.getLinks();
        if (links.size() > 0) {
            Enumeration linksE = links.elements();
            while (linksE.hasMoreElements()) {
                String linked = (String) linksE.nextElement();
                Ut
            }
        }
    }
    
}
