/*
 * MetaRefiner.java
 *
 * Created on 25 January 2002, 17:05
 */

package gilbert.extractor.refiners;
import java.net.*;
import java.util.*;
import java.io.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import gilbert.extractor.*;

/**
 * A refiner that tries to read meta information from URLs and writes it to
 * the output.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class MetaRefiner extends Refiner {
    
    
    /** Creates a new instance of MetaRefiner */
    public MetaRefiner() {
        passing = true;
    }
    
    /**
     * Must be overridden by child classes to handle each of
     * the URLs.
     *
     * It's up to the child class to use the proper methods or the
     * <code>outStream</code> for printing the resutls and to honour
     * the postfilters.
     */
    public void handleURL(VisitorURL url) {
        String urlName = url.getProperty("url.name");
        SiteInfo sInfo = Util.siteStatus(urlName);
        String title = sInfo.getMetaTitle();
        String description = sInfo.getMetaDescription();
        Vector keywords = sInfo.getMetaKeywords();
        if ((title != null) && !title.equals("")) {
            url.setProperty("url.title", title);
        }
        if ((description != null) && !description.equals("")) {
            url.setProperty("url.description", description);
        }
        if (keywords.size() > 0) {
            Enumeration keywordsE = keywords.elements();
            while (keywordsE.hasMoreElements()) {
                url.addKeyword((String) keywordsE.nextElement());
            }
        }
    }
    
}
