/*
 * RTypeVisitFilter.java
 *
 * Created on 20 March 2002, 11:22
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;
import org.apache.log4j.*;
import java.util.*;

/**
 * Drops all visits where the resource is (or is not) of a specified type.
 *
 * @author Daniel Hahn
 * @version CVS $revision$
 */
public class RTypeVisitFilter implements VisitFilter {
    /// The document types for filter for
    protected Vector doctypes;
    /// If the filtering should be inverted
    protected boolean inverseFilter;
    /// Logger for this class
    Logger logger = Logger.getLogger(this.getClass());
    
    
    /** Creates a new instance of RTypeVisitFilter */
    public RTypeVisitFilter() {
        doctypes = new Vector();
        inverseFilter = false;
    }
    
    /**
     * Add a new document type. A document type is the requested document's 
     * suffix. (e.g. .html or .txt)
     */
    public void addDocType(String doctype) {
        doctypes.add(doctype);
    }
    
    /**
     * Inverse the filtering. If this is set to <i>true</i>, the filter
     * will drop all visits <i>but</i> those with the specified document
     * types. The default behavior is to drop all visits <i>with</i> the
     * specified document types.
     */
    public void setInverse(boolean inverse) {
        inverseFilter = inverse;
    }
    
    /**
     * Gets the inverse filtering property.
     */
    public boolean getInverse() {
        return inverseFilter;
    }

    /** Returns true if, and only if, the give Visit is accepted by the filter.
     */
    public boolean accept(Visit v) {
        String resource = v.getProperty("visit.resource");
        boolean retVal = !inverseFilter;
        if (resource != null) {
            Enumeration doctypeE = doctypes.elements();
            while (doctypeE.hasMoreElements()) {
                String doctype = (String) doctypeE.nextElement();
                if (resource.endsWith(doctype)) {
                   if (logger.isInfoEnabled()) 
                       logger.info("Document Type matched, filter acceptance of visit " 
                                   + v.getProperty("visit.host") 
                                   + " is " + inverseFilter);
                       return inverseFilter;
                }
            }
        } else {
            logger.warn("Visit had no resource, was dropped by filter: " + v.getProperty("visit.host"));
        }
        if (logger.isInfoEnabled()) logger.info("Document types not matched for visit "
                                    + v.getProperty("visit.host") + ", acceptance is " 
                                    + retVal);
        return retVal;
    }
    
    /// Getter Method for document types. Treats document types as comma-separated list.
    public String getDocumentTypes() {
        StringBuffer retVal = new StringBuffer();
        Enumeration typesE = doctypes.elements();
        if (typesE.hasMoreElements()) {
            retVal.append(typesE.nextElement());
        }
        while (typesE.hasMoreElements()) {
            retVal.append(",");
            retVal.append(typesE.nextElement());
        }
        return retVal.toString();
    }
    
    /**
     * Sets document types from a comma-separated list. 
     * The type list will be completely re-set.
     */
    public void setDocumentTypes(String typeStr) {
        StringTokenizer sTok = new StringTokenizer(typeStr, ",");
        doctypes = new Vector();
        while (sTok.hasMoreTokens()) {
            addDocType(sTok.nextToken());
        }
    }
    
}
