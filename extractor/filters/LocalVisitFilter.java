/*
 * LocalVisitFilter.java
 *
 * Created on 25 January 2002, 15:46
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;
import org.apache.log4j.*;

/**
 * Simple filter that will only accept visits which are <b>not</b> of
 * class <i>local</i>
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class LocalVisitFilter implements VisitFilter {
    /// Logger for this class
    protected Logger logger = Logger.getLogger(this.getClass());
    
    /**
     * Returns true if, and only if, the give Visit is accepted by the filter.
     */
    public boolean accept(Visit v) {
        String val = v.getProperty("visit.visitor.class.local");
        if ((val != null) && (val.equals("true"))) {
            if (logger.isInfoEnabled()) {
                logger.info("Local Visit dropped by filter: " + v.getProperty("visit.host"));
            }
            return false;
        }
        return true;
    }
    
}