/*
 * VectorRefiner.java
 *
 * Created on 18 January 2002, 12:19
 */

package gilbert.extractor;

import java.util.*;
import java.net.*;

/**
 * Refines into an internal <code>Vector</code>, rather than to the output
 * stream. This will take all URLs from the input source, und create a
 * Vector of <code>URL</code> objects of this.
 * <p>
 * <b>Please Note:</b> This version will <i>not</i> write anything to the
 * output stream and should be only used as the last Refiner in a chain. 
 * It is also not suitable for large input sources (since all data ist
 * stored internally).
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class VectorRefiner extends Refiner {
    /** The internal URL Vector */
    protected Vector urlList;

    /** Creates new VectorRefiner */
    public VectorRefiner() {
        urlList = new Vector();
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
        String urlStr = url.getProperty("url.name");
        URL tUrl = null;
        try {
            tUrl = new URL(urlStr);
            urlList.add(tUrl);
        } catch (MalformedURLException e) {
            Util.logMessage("VectorRefiner: Malformed URL found: " + urlStr, Util.LOG_ERROR);
        }
    }
    
    /**
     * Resets the internal vector. This way, a new <code>refine()</code>
     * call will not add to the existing vector, but will start with 
     * a blank one.
     */
    public void reset() {
        urlList = new Vector();
    }
    
    /**
     * Returns the internal data as a Vector of <code>URL</code>
     * objects.
     */
    public Vector getUrlList() {
        return urlList;
    }
    
}
