/*
 * Visit.java
 *
 * Created on 04 December 2001, 16:59
 */

package gilbert.extractor;

import java.util.*;

/**
 * Contains all information about a visit to the site. This is, at the moment,
 * a <code>Properties</code> object and pretty much a dummy class.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class Visit extends java.util.Properties {
    /// Vector with all keywords in this visit record
    protected Vector keywords;
    
    /** Creates new Visit */
    public Visit() {
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
