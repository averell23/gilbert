/*
 * VectorRefiner.java
 *
 * Created on 18 January 2002, 12:19
 */

package gilbert.extractor.refiners;

import java.util.*;
import java.net.*;
import gilbert.extractor.*;

/**
 * Refines into an internal <code>Vector</code>, rather than to the output
 * stream. This will take all URLs from the input source, und create a
 * Vector of <code>VisitorURL</code> objects of this.
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
     * Creates new VectorRefiner.
     * @param passing If the input URL should be passed to the output.
     */
    public VectorRefiner(boolean passing) {
        this();
        this.passing = passing;
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
        urlList.add(url);
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
     * Returns the internal data as a Vector of <code>VisitorURL</code>
     * objects.
     */
    public Vector getUrlList() {
        return urlList;
    }
    
    /**
     * Prints the internal URLs to the output stream again.
     */
    public void printAllURLs() {
        Enumeration list = urlList.elements();
        while(list.hasMoreElements()) {
            printURL((VisitorURL) list.nextElement());
        }
    }
    
}
