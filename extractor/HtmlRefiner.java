/*
 * HtmlRefiner.java
 *
 * Created on 08 January 2002, 11:05
 */

package gilbert.extractor;
import org.xml.sax.*;

/**
 * Refiner that turns an URL list into a XHTML page
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class HtmlRefiner extends Refiner {

    /** Creates new HtmlRefiner */
    public HtmlRefiner() {
    }
    
    /**
     * Overridden by this class since we don't want to print a standard
     * URL list.
     */
    public void refine(InputSource input) {
        Util.logMessage("HtmlRefiner: Starting HTML writing", Util.LOG_DEBUG);
        startTag("html");
        startTag("head");
        printTag("title", "URL list created by HtmlRefiner");
        endTag("head");
        startTag("body");
        refineBlank(input);
        endTag("body");
        endTag("html");
    }
    
    public void refine(String uri) {
        refine(new InputSource(uri));
    }

    /**
     * This method must be overridden by child classes to handle each of
     * the URLs.
     *
     * It's up to the child class to use the proper methods or the
     * <code>outStream</code> for printing the resutls and to honour
     * the postfilters.
     */
    public void handleURL(VisitorURL url) {
        String urlString = url.getProperty("url.name");
        outStream.print("<a href=\"" + urlString + "\">");
        outStream.print(urlString);
        outStream.print("</a>");
        outStream.println("<br/>");
    }
    
}
