/*
 * URL.java
 *
 * Created on 14 December 2001, 15:56
 */

package gilbert.extractor;

import java.util.*;
import org.apache.log4j.*;

/**
 * This is basically a <code>java.util.Properties</code> object - the class
 * exists for extensability and type safety. 
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class VisitorURL extends java.util.Properties implements Comparable {
    /// Vector for the keywords
    protected Vector keywords;
    /// Logger for this class
    protected Logger logger = Logger.getLogger(VisitorURL.class);
    
    /** Creates new URL */
    public VisitorURL() {
        keywords = new Vector();
    }

    /** Adds a new keyword to this record */
    public void addKeyword(String x) {
        keywords.add(x);
    }
    
    /** Returns the keyword vector */
    public Vector getKeywords() {
        return keywords;
    }
    
    /** Two URLs are equal if their names are equal **/
    public boolean equals(Object o) {
        if (o instanceof VisitorURL) {
            VisitorURL vO = (VisitorURL) o;
            String thisName = this.getProperty("url.name");
            if (thisName == null) thisName = "";
            String foreignName = vO.getProperty("url.name");
            if (foreignName == null) foreignName = "";
            if (logger.isDebugEnabled()) {
                logger.debug("Comparing " + thisName + " with " + foreignName);
            }
            return thisName.equals(foreignName);
        } else {
            return false;
        }
    }
    
    public int compareTo(Object obj) {
        if (obj instanceof VisitorURL) {
            return compareTo((VisitorURL) obj);
        } else {
            throw(new ClassCastException("Incompatible Class to compare with VisitorURL: " + obj.getClass().getName()));
        }
    }
    
    public int compareTo(VisitorURL u) {
        String thisName = this.getProperty("url.name");
        if (thisName == null) thisName = "";
        String foreignName = u.getProperty("url.name");
        if (foreignName == null) foreignName = "";
        return thisName.compareTo(foreignName);
    }
}

