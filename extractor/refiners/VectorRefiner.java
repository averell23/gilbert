/*
 * VectorRefiner.java
 *
 * Created on 18 January 2002, 12:19
 */

package gilbert.extractor.refiners;

import java.util.*;
import java.net.*;
import gilbert.extractor.*;
import org.apache.log4j.*;

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
    /// logger for this class.
    protected static Logger logger = Logger.getLogger(VectorRefiner.class);

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
        if (logger.isDebugEnabled()) logger.debug("Adding URL to Vector: " + url.getProperty("url.name"));
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
    
    /// Sets the passing property.
    public void setPassing(boolean passing) {
        this.passing = passing;
    }
    
    /// Gets the passing property
    public boolean getPassing() {
        return passing;
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
