/*
 * AgentVisitFilter.java
 *
 * Created on 25 January 2002, 15:53
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;

/**
 * Accepts only visits that are not of class <i>agent</i>.
 *
 * @author  Daniel Hahn
 * @version CVS $Revison$
 */
public class AgentVisitFilter implements VisitFilter {


    /**
     * Returns true if, and only if, the give Visit is accepted by the filter.
     */
    public boolean accept(Visit v) {
        String val = v.getProperty("visit.visitor.class.agent");
        if ((val != null) && (val.equals("true"))) {
            Util.logMessage("Agent visit dropped by filter: " + v.getProperty("visit.host"), Util.LOG_MESSAGE);
            return false;
        }
        return true;
    }
    
}
