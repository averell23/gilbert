/*
 * SiteInfo.java
 *
 * Created on 01 February 2002, 17:01
 */

package gilbert.extractor;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import org.apache.log4j.*;
import java.io.*;
import java.net.*;


/**
 * This encapsulates information about a site. This is meant to be a cache
 * object: The information will be retrieved by this object when and if it
 * is necessary.
 *
 * @author Daniel Hahn
 * @version CVS $revision$
 */
public class SiteInfo {
    /// Logger for this class.
    protected Logger logger;
    /// URL of the site
    protected String url;
    /// Timestamp when the entry was created
    protected long timestamp;
    /// If the URL was found alive
    protected boolean alive;
    /// The content type served by that url
    protected String contentType;
    /// META keywords from the page
    protected Vector metaKeywords;
    /// META description from the page
    protected String metaDescription;
    /// META title from the page
    protected String metaTitle;
    /// URLs linked from this site
    protected Vector links;
    /// If the primary info has been loaded into this object.
    protected boolean primaryLoaded = false;
    /// If the secondary info has been loaded into this object.
    protected boolean secondaryLoaded = false;
    
    /** Creates a new instance of SiteInfo */
    protected SiteInfo(String url) {
        logger = Logger.getLogger(this.getClass());
        this.url = url;
        timestamp = System.currentTimeMillis();
        metaKeywords = new Vector();
        links = new Vector();
    }
    
    public String getContentType() {
        if (!primaryLoaded) loadPrimaryInfo();
        return contentType;
    }
    
    protected void setContentType(String content) {
        contentType = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getUrl() {
        return url;
    }
    
    protected void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    protected boolean getAlive() {
        if (!primaryLoaded) loadPrimaryInfo();
        return alive;
    }
    
    public Vector getMetaKeywords() {
        if (!secondaryLoaded) loadSecondaryInfo();
        return metaKeywords;
    }
    
    protected void addMetaKeyword(String keyword) {
        metaKeywords.add(keyword);
    }
    
    protected String getMetaDescription() {
        if (!secondaryLoaded) loadSecondaryInfo();
        return metaDescription;
    }
    
    protected void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }
    
    public String getMetaTitle() {
        if (!secondaryLoaded) loadSecondaryInfo();
        return metaTitle;
    }
    
    protected void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }
    
    public Vector getLinks() {
        if (!secondaryLoaded) loadSecondaryInfo();
        return links;
    }
    
    protected void addLink(String link) {
        links.add(link);
    }
    
    /**
     * Retrieves the <i>primary</i> information about an URL. This will only
     * issue a HEAD request, the body of the document does not need to be
     * retrieved.
     */
    protected void loadPrimaryInfo() {
        if (primaryLoaded) {
            logger.debug("Primary info already loaded.");
            return;
        }
        // Additional debug info: The settings of the System properties
        if (logger.isDebugEnabled()) {
            Properties sysProps = System.getProperties();
            logger.debug("Proxy Host: " + sysProps.getProperty("http.proxyHost"));
            logger.debug("Proxy Port: " + sysProps.getProperty("http.proxyPort"));
            logger.debug("Connection Timeout: " + sysProps.getProperty("sun.net.client.defaultConnectTimeout"));
            logger.debug("Read Timeout: " + sysProps.getProperty("sun.net.client.defaultReadTimeout"));
        }
        try {
            if (logger.isInfoEnabled()) logger.info("Loading primary information on " + url);
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("HEAD");
            conn.connect();
            int resCode = conn.getResponseCode();
            if (logger.isDebugEnabled()) logger.debug("Got return code: " + resCode);
            String contentType = conn.getContentType();
            if (logger.isDebugEnabled()) logger.debug("Got content type: " + contentType);
            setContentType(contentType);
            // End parsing code
            conn.disconnect();
            if (resCode >= 400) { // If the code is not an ok or redirect
                logger.info("Host was not alive");
                setAlive(false);
            } else {
                logger.info("Host found alive");
                setAlive(true);
            }
        } catch (MalformedURLException e) {
            logger.warn(url + " is not a valid URL.");
            setAlive(false);
        } catch (IOException e) {
            logger.info("IOException checking for host status: " + e.getMessage());
            setAlive(false);
        }
        primaryLoaded = true;
    }
    
    /**
     * Gets the <i>secondary</i> info about the page. This will issue a
     * GET request, retrieve and parse the whole page (if it is text/html).
     */
    public void loadSecondaryInfo() {
        if (secondaryLoaded) {
            logger.debug("Secondary info already loaded.");
            return;
        }
        if (!primaryLoaded) {
            loadPrimaryInfo();
        }
        if (getAlive() && getContentType().startsWith("text/html")) {
            try {
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                String contentType = conn.getContentType();
                logger.debug("Got content type: " + contentType);
                // Parse the page if it's HTML
                if ((contentType != null) && contentType.startsWith("text/html")) {
                    logger.debug("Parsing document");
                    DocumentParser parser = new DocumentParser(DTD.getDTD("HTML"));
                    HTMLEditorKit.ParserCallback pc = new InternalParserCallback();
                    parser.parse(new InputStreamReader(conn.getInputStream()), pc, true);
                    secondaryLoaded = true;
                } else {
                    logger.warn("Unexpected content type: " + contentType + "(" + url + ")");
                    logger.info("Invalidating Object.");
                    primaryLoaded = false;
                }
                // End parsing code
                conn.disconnect();
            } catch (MalformedURLException e) {
                logger.warn(url + " is not a valid URL.");
                setAlive(false);
            } catch (IOException e) {
                logger.info("IOException checking for host status: " + e.getMessage());
                setAlive(false);
            }
        }
    }
    
    /**
     * Internal Parser Callback class, for parsing documents for additional
     * SiteStatus information
     */
    protected class InternalParserCallback extends HTMLEditorKit.ParserCallback {
        /// Indicates if the title is being parsed at the moment.
        boolean parseTitle = false;
        /// StringBuffer for the title string.
        StringBuffer titleBuffer;
        
        /**
         * Creates a new InternalParserCallback.
         * @param info The <code>SiteInfo</code> object that takes the
         *             information from this parser.
         */
        public InternalParserCallback() {
            logger.debug("Internal Callback initializing...");
        }
        
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
            logger.debug("Parser: Start Tag Handler called.");
            // check for the <title> tag
            if (tag.equals(HTML.Tag.TITLE)) {
                logger.debug("Parser: Started Title Tag.");
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
                        addMetaKeyword(keylist[i]);
                        if (logger.isDebugEnabled()) logger.debug("Parser: Added META keyword: " + keylist[i]);
                    }
                }
                if (attrib.equals("description")) {
                    setMetaDescription(a.getAttribute(HTML.Attribute.CONTENT).toString());
                    if (logger.isDebugEnabled()) logger.debug("Parser added META description: " + a.getAttribute(HTML.Attribute.CONTENT).toString());
                }
            }
        }
        
        public void handleEndTag(HTML.Tag tag, int pos) {
            logger.debug("Parser: End tag handler called");
            if (tag.equals(HTML.Tag.TITLE)) {
                parseTitle = false;
                setMetaTitle(titleBuffer.toString());
                logger.debug("Parser: Stopped title parsing");
                if (logger.isDebugEnabled()) logger.debug("Parser: Put title string " + titleBuffer);
            }
        }
        
        public void handleText(char[] data, int pos) {
            logger.debug("Parser: Text Handler called");
            if (parseTitle) {
                titleBuffer.append(data);
            }
        }
        
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
            logger.debug("Parser: Simple Tag Handler called.");
            if (tag.equals(HTML.Tag.A) && a.isDefined(HTML.Attribute.HREF)) {
                String attrib = a.getAttribute(HTML.Attribute.HREF).toString().toLowerCase();
                if (attrib.startsWith("http:")) {
                    if (logger.isDebugEnabled()) logger.debug("Parser: Added link to " + attrib);
                    addLink(attrib);
                } else if (attrib.startsWith("#")) {
                    // Link to an internal anchor
                    /* Uncomment if you really want to follow internal links
                    String url = getUrl();
                    int idx = url.indexOf("?");
                    if (idx != -1) url = url.substring(0, idx);
                    attrib = url + attrib;
                    addLink(attrib);
                     */
                    if (logger.isDebugEnabled()) logger.debug("Parser: Internal link (ignored):" + attrib);
                } else if (attrib.startsWith("/")) {
                    // Internal absolute link
                    String url = getUrl();
                    int idx = url.indexOf("/", 8);
                    if (idx != -1) url = url.substring(0,idx);
                    attrib = url + attrib;
                    if (logger.isDebugEnabled()) logger.debug("Parser: Constructed link to internal document: " + attrib);
                    addLink(attrib);
                } else if (attrib.matches("[^:].*")) { // At the moment, match everything
                    // seems to be a relative link to another page.
                    String url = getUrl();
                    int idx = url.indexOf("?");
                    if (idx != -1) url = url.substring(0,idx);
                    if (url.endsWith("/")) {
                        attrib = url + attrib;
                    } else {
                        attrib = url + "/" + attrib;
                    }
                    addLink(attrib);
                    if (logger.isDebugEnabled()) logger.debug("Parser: Constructed link from relative: " + attrib);
                } else {
                    if (logger.isDebugEnabled()) logger.debug("Parser: Ignoring unknown link: " + attrib);
                }
            }
        }
    } // End of inner class InternalParserCallback
    
}
