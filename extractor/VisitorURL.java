/*
 * URL.java
 *
 * Created on 14 December 2001, 15:56
 */

package gilbert.extractor;

import java.util.*;

/**
 * This is basically a <code>java.util.Properties</code> object - the class
 * exists for extensability and type safety. 
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class VisitorURL extends java.util.Properties {
    /// Vector for the keywords
    protected Vector keywords;
    
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
}

