/*
 * DocumentTypeURLFilter.java
 *
 * Created on 30 January 2002, 17:59
 */

package gilbert.extractor.filters;
import gilbert.extractor.*;
import java.util.*;

/**
 * Filters unwanted document types. This will check the extension (== the
 * end of the URL name), since <code>URLConnection.getEncoding()</code> 
 * doesn't seem to work properly...
 * 
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class DocumentTypeURLFilter implements URLFilter {
    /** Vector with endings for this filter. */
    protected Vector endings;
    
    /**
     * Creates a new instance of DocumentTypeURLFilter. 
     * @param endings List of endings that, when matched will
     *        allow a url name to pass (e.g. ".html"). The
     *        directory entry ("/") will always pass.
     */
    public DocumentTypeURLFilter(Vector endings) {
        this.endings = endings;
    }

    /** Returns true if, and only if, the given url is accpeted by the filter.
     */
    public boolean accept(VisitorURL url) {
        String hostname = url.getProperty("url.name");
        Enumeration urlList = endings.elements();
        boolean accepted = false;
        while (urlList.hasMoreElements()) {
            String current = (String) urlList.nextElement();
            accepted = accepted || hostname.endsWith(current);
        }
        accepted = accepted || hostname.endsWith("/");
        if (!accepted) {
            Util.logMessage("URL dropped by filter, unknown type: " + hostname, Util.LOG_MESSAGE);
        }
        return accepted;
    }
    
}
