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
    /// Cache for pages that have already been looked up
    Hashtable requestCache;
    /// Current URL
    VisitorURL currentURL;
    
    
    /** Creates a new instance of MetaRefiner */
    public MetaRefiner() {
        requestCache = new Hashtable();
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
        currentURL = url;
        String urlName = url.getProperty("url.name");
        if (!requestCache.containsKey(urlName)) {
            try {
                URL u = new URL(urlName);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                // conn.setRequestProperty("User-Agent", "");
                conn.connect();
                DocumentParser parser = new DocumentParser(DTD.getDTD("HTML"));
                HTMLEditorKit.ParserCallback pc = new InternalParserCallback();
                String encoding = conn.getContentType();
                if ((encoding != null) && encoding.equals("text/html")) { 
                    parser.parse(new InputStreamReader(conn.getInputStream()), pc, true);
                } else {
                    Util.logMessage("Meta Refiner: Ignored URL with unknown type: " + encoding + "(" + urlName + ")", Util.LOG_MESSAGE);
                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                Util.logMessage("Meta refiner had malformed URL: " + urlName, Util.LOG_DEBUG);
            } catch (IOException e) {
                Util.logMessage("Meta refiner could not load metadata: " + e.getMessage(), Util.LOG_DEBUG);
            }
            requestCache.put(urlName, url);
        }
        printURL(url);
    }
    
    /**
     * Clears the request cache
     */
    public void clearCache() {
        requestCache = new Hashtable();
    }
    
    /**
     * Internal Parser Callback class.
     */
    protected class InternalParserCallback extends HTMLEditorKit.ParserCallback {
        /// Indicates if the title is being parsed at the moment.
        boolean parseTitle = false;
        /// StringBuffer for the title string.
        StringBuffer titleBuffer;
        
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
            // check for the <title> tag
            if (tag.equals(HTML.Tag.TITLE)) {
                Util.logMessage("Meta Refiner: Started Title Tag.", Util.LOG_DEBUG);
                parseTitle = true;
                titleBuffer = new StringBuffer();
                return;
            }
            
            // check for the Meta tag
            if (tag.equals(HTML.Tag.META)
            && a.isDefined(HTML.Attribute.NAME)
            && a.isDefined(HTML.Attribute.CONTENT)) {
                String attrib = a.getAttribute(HTML.Attribute.NAME).toString().toLowerCase();
                if (attrib.equals("keywords")) {
                    String keywords = a.getAttribute(HTML.Attribute.CONTENT).toString();
                    String[] keylist = keywords.split("\\s*,\\s*");
                    for (int i = 0 ; i < keylist.length ; i++) {
                        currentURL.addKeyword(keylist[i]);
                        Util.logMessage("MetaRefiner added keyword: " + keylist[i], Util.LOG_DEBUG);
                    }
                }
                if (attrib.equals("description")) {
                    currentURL.setProperty("url.description", a.getAttribute(HTML.Attribute.CONTENT).toString());
                    Util.logMessage("Meta Refiner added description: " + a.getAttribute(HTML.Attribute.CONTENT).toString(), Util.LOG_DEBUG);
                }
            }
        }
        
        public void handleEndTag(HTML.Tag tag, int pos) {
            if (tag.equals(HTML.Tag.TITLE)) {
                parseTitle = false;
                currentURL.setProperty("url.title", titleBuffer.toString());
                Util.logMessage("MetaRefiner: Stopped title parsing", Util.LOG_DEBUG);
                Util.logMessage("Put title string " + titleBuffer, Util.LOG_DEBUG);
            }
        }
        
        public void handleText(char[] data, int pos) {
            if (parseTitle) {
                titleBuffer.append(data);
            }
        }
        
    } // End of inne class
}
