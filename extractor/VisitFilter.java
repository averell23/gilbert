/*
 * VisitFilter.java
 *
 * Created on 17 December 2001, 11:39
 */

package gilbert.extractor;

/**
 * Interface for classes that filter <code>Visit</code> objects.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public interface VisitFilter {
    /**
     * Returns true if, and only if, the give Visit is accepted by the filter.
     */
    public boolean accept(Visit v);
}

