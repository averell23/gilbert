/*
 * DumpRefiner.java
 *
 * Created on 18 December 2001, 12:09
 */

package gilbert.extractor.refiners;
import gilbert.extractor.*;

/**
 * This only dumps the origianl list to a new stream (probably using the
 * Prefileters.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class DumpRefiner extends Refiner {

    /** Creates new DumpRefiner */
    public DumpRefiner() {
    }

    /**
     * This method must be overridden by child classes to handle each of
     * the URLs.
     *
     * It's up to the child class to use the proper methods or the
     * <code>outStream</code> for printing the resutls and to honour
     * the postfilters.
     */
    public void handleURL(VisitorURL url) {
        startTag("url");
        printTag("name", url.getProperty("url.name"));
        printTag("location_code", url.getProperty("url.location_code"));
        endTag("url");
    }
    
}
