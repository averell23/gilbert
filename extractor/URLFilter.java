/*
 * URLFilter.java
 *
 * Created on 17 December 2001, 11:37
 */

package gilbert.extractor;

/**
 * Interface for classes that can filter <code>VisitorURL</code> objects.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public interface URLFilter {
    /**
     * Returns true if, and only if, the given url is accpeted by the filter.
     */
    public boolean accept(VisitorURL url);
}

