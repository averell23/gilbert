/*
 * DocumentTypeURLFilter.java
 *
 * Created on 30 January 2002, 17:59
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * Filters unwanted document types. This will now properly check the encoding
 * supplied by the URLConnection, and use the Util class to allow caching.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class DocumentTypeURLFilter implements URLFilter {
    /** Allowed document types for this filter. */
    protected Vector types;
    /// Logger for this class
    protected Logger logger = Logger.getLogger(this.getClass());
    
    /**
     * Creates a new instance of DocumentTypeURLFilter.
     */
    public DocumentTypeURLFilter() {
        types = new Vector();
    }
    
    /**
     * Adds a new allowed document type. Failing to add types
     * will result in all sites being rejected.
     */
    public void addDocumentType(String type) {
        types.add(type);
    }
    
    /** Returns true if, and only if, the given url is accpeted by the filter.
     */
    public boolean accept(VisitorURL url) {
        String hostname = url.getProperty("url.name");
        SiteInfo info = Util.siteStatus(hostname);
        
        Enumeration typeList = types.elements();
        boolean accepted = false;
        if (info.getContentType() != null) {
            while (typeList.hasMoreElements()) {
                accepted = accepted || info.getContentType().equals((String) typeList.nextElement());
            }
        }
        if (logger.isInfoEnabled() && !accepted) {
            logger.info("URL dropped by filter, unknown type: " + info.getContentType());
        }
        return accepted;
    }
    
    /// Getter Method for document types. Treats document types as comma-separated list.
    public String getDocumentTypes() {
        StringBuffer retVal = new StringBuffer();
        Enumeration typesE = types.elements();
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
        types = new Vector();
        while (sTok.hasMoreTokens()) {
            addDocumentType(sTok.nextToken());
        }
    }
}
