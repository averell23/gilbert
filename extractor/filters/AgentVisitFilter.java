/*
 * AgentVisitFilter.java
 *
 * Created on 25 January 2002, 15:53
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;
import org.apache.log4j.*;

/**
 * Accepts only visits that are not of class <i>agent</i>.
 *
 * @author  Daniel Hahn
 * @version CVS $Revison$
 */
public class AgentVisitFilter implements VisitFilter {
    /// Logger for this class.
    Logger logger = Logger.getLogger(this.getClass());
    
    /**
     * Returns true if, and only if, the give Visit is accepted by the filter.
     */
    public boolean accept(Visit v) {
        String val = v.getProperty("visit.visitor.class.agent");
        if ((val != null) && (val.equals("true"))) {
            if (logger.isInfoEnabled()){
                    logger.info("Agent visit dropped by filter: " + v.getProperty("visit.host"));
            }
            return false;
        }
        return true;
    }
    
}
