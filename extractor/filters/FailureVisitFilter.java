/*
 * FailureVisitFilter.java
 *
 * Created on 20 March 2002, 11:46
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;
import org.apache.log4j.*;

/**
 * Drops all visits that were not successful. (i.e the requested resource was 
 * succesfully served to the visitor).
 *
 * @author Daniel Hahn
 * @version CVS $revision$
 */
public class FailureVisitFilter implements VisitFilter {
    /// Logger for this class
    Logger logger = Logger.getLogger(this.getClass());
    
    /** Creates a new instance of FailureVisitFilter */
    public FailureVisitFilter() {
    }

    /** Returns true if, and only if, the give Visit is accepted by the filter.
     */
    public boolean accept(Visit v) {
        String result = v.getProperty("visit.result");
        boolean retVal = false;
        if (result != null) {
            if (result.equals("success")) {
                retVal = true;
            } else {
                if (logger.isInfoEnabled()) logger.info("Unsuccessful visit dropped: " 
                                            + v.getProperty("visit.host"));
            }
        } else {
            logger.warn("Visit without result information dropped: " + v.getProperty("visit.host"));
        }
        return retVal;
    }
    
}
