/*
 * LocalVisitFilter.java
 *
 * Created on 25 January 2002, 15:46
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;

/**
 * Simple filter that will only accept visits which are <b>not</b> of
 * class <i>local</i>
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class LocalVisitFilter implements VisitFilter {

    /**
     * Returns true if, and only if, the give Visit is accepted by the filter.
     */
    public boolean accept(Visit v) {
        String val = v.getProperty("visit.visitor.class.local");
        if ((val != null) && (val.equals("true"))) {
            Util.logMessage("Local Visit dropped by filter: " + v.getProperty("visit.host"), Util.LOG_MESSAGE);
            return false;
        }
        return true;
    }
    
}