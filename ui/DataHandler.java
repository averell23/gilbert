/*
 * DataHandler.java
 *
 * Created on 25 March 2002, 14:03
 */

package gilbert.ui;
import java.util.*;
import gilbert.extractor.*;
import gilbert.extractor.extractors.*;
import gilbert.extractor.refiners.*;
import gilbert.extractor.filters.*;

/**
 * Contains methods to get data etc.
 *
 * @author Daniel Hahn
 * @version CVS $Revision$
 */
public class DataHandler {

    /** 
     * Returns all available Extractors 
     */
    public static Vector getExtractors() {
        Vector retVal = new Vector();
        retVal.add(SimpleExtractor.class);
        retVal.add(StraightExtractor.class);
        retVal.add(ResolvingExtractor.class);
        return retVal;
    }
    
    /**
     * Returns all available Refiners
     */
    public static Vector getRefiners() {
        Vector retVal = new Vector();
        retVal.add(KWInterestRefiner.class);
        retVal.add(LinkRefiner.class);
        retVal.add(MetaKInterestRefiner.class);
        retVal.add(MetaRefiner.class);
        retVal.add(SearchingRefiner.class);
        return retVal;
    }

    /**
     * Returns all available VisitFilters
     */
    public static Vector getVisitFilters() {
        Vector retVal = new Vector();
        retVal.add(AgentVisitFilter.class);
        retVal.add(FailureVisitFilter.class);
        retVal.add(LocalVisitFilter.class);
        retVal.add(RTypeVisitFilter.class);
        retVal.add(ReferralVisitFilter.class);
        return retVal;
    }
    
    /**
     * Returns all available URLFilters/
     */
    public static Vector getURLFilters() {
        Vector retVal = new Vector();
        retVal.add(AliveFilter.class);
        retVal.add(DocumentTypeURLFilter.class);
        return retVal;
    }
}
