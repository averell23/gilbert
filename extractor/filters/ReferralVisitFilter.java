/*
 * ReferralFilter.java
 *
 * Created on 20 March 2002, 11:58
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;
import org.apache.log4j.*;
import java.util.*;

/**
 * Drops visits when the referer string contains one of the given strings.
 * This is intended to be used for filtering out referrals within the site,
 * but may have other uses.
 *
 * @author Daniel Hahn
 * @version CVS $revision$
 */
public class ReferralVisitFilter implements VisitFilter {
    /// List of Strings to match against
    Vector matchList;
    /// If the visit should pass if there's no referer
    boolean emptyPass;
    /// Logger for this class
    Logger logger = Logger.getLogger(this.getClass());
    
    /** Creates a new instance of ReferralFilter */
    public ReferralVisitFilter() {
         matchList = new Vector();
         emptyPass = true;
    }
    
    /**
     * Add a String to match against. This may is treated as a regular
     * epression wich will be matched against the referer entry.
     */
    public void addMatch(String match) {
        matchList.add(match);
    }
    
    /**
     * If set to false, visits without referer information will be dropped.
     * (Default is true).
     */
    public void setEmptyPass(boolean pass) {
        emptyPass = pass;
    }

    /** Returns true if, and only if, the give Visit is accepted by the filter.
     */
    public boolean accept(Visit v) {
        boolean retVal = false;
        String referer = v.getProperty("visit.referer.url");
        if (referer != null) {
            Enumeration matchE = matchList.elements();
            while (matchE.hasMoreElements()) {
                String match = (String) matchE.nextElement();
                if (referer.matches(match)) {
                    if (logger.isInfoEnabled()) logger.info("Visit with refer match dropped by filter: " + v.getProperty("visit.host"));
                    return false;
                }
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("Visit did not contain referer information, acceptance is set to: " + emptyPass);
            retVal = emptyPass;
        }
        return retVal;
    }
    
}
