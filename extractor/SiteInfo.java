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
    /// The tree holding the word count for the words in the document
    protected TreeMap wordTree;
    
    /** Creates a new instance of SiteInfo */
    protected SiteInfo(String url) {
        logger = Logger.getLogger(this.getClass());
        this.url = url;
        timestamp = System.currentTimeMillis();
        metaKeywords = new Vector();
        links = new Vector();
        wordTree = new TreeMap();
    }
    
    public synchronized String getContentType() {
        if (!primaryLoaded) loadPrimaryInfo();
        return contentType;
    }
    
    protected synchronized void setContentType(String content) {
        contentType = content;
    }
    
    public synchronized long getTimestamp() {
        return timestamp;
    }
    
    public synchronized String getUrl() {
        return url;
    }
    
    protected synchronized void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public synchronized boolean getAlive() {
        if (!primaryLoaded) loadPrimaryInfo();
        return alive;
    }
    
    public synchronized Vector getMetaKeywords() {
        if (!secondaryLoaded) loadSecondaryInfo();
        return metaKeywords;
    }
    
    protected synchronized void addMetaKeyword(String keyword) {
        metaKeywords.add(keyword);
    }
    
    public synchronized String getMetaDescription() {
        if (!secondaryLoaded) loadSecondaryInfo();
        return metaDescription;
    }
    
    protected synchronized void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }
    
    public synchronized String getMetaTitle() {
        if (!secondaryLoaded) loadSecondaryInfo();
        return metaTitle;
    }
    
    protected synchronized void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }
    
    public synchronized Vector getLinks() {
        if (!secondaryLoaded) loadSecondaryInfo();
        return links;
    }
    
    public synchronized Iterator getDocWords() {
        if (!secondaryLoaded) loadSecondaryInfo();
        return wordTree.keySet().iterator();
    }
    
    public synchronized long getDocWordCount(String word) {
        if (!secondaryLoaded) loadSecondaryInfo();
        word = word.toLowerCase();
        long retVal = 0;
        Object cnt = wordTree.get(word);
        if (cnt != null) retVal = ((MyLong) cnt).getValue();
        return retVal;
    }
    
    protected synchronized void addLink(String link) {
        links.add(link);
    }
    
    /**
     * Retrieves the <i>primary</i> information about an URL. This will only
     * issue a HEAD request, the body of the document does not need to be
     * retrieved.
     */
    protected synchronized void loadPrimaryInfo() {
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
    public synchronized void loadSecondaryInfo() {
        if (secondaryLoaded) {
            logger.debug("Secondary info already loaded.");
            return;
        }
        if (!primaryLoaded) {
            loadPrimaryInfo();
        }
        if (getAlive() && (getContentType() != null) && getContentType().startsWith("text/html")) {
            logger.info("Retrieving secondary information.");
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
            if (logger.isDebugEnabled()) logger.debug("Parser: Start tag handler called for "+ tag);
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
            if (logger.isDebugEnabled()) logger.debug("Parser: End tag handler called for "+ tag);
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
                if (logger.isDebugEnabled()) logger.debug("Parser: Title Text Handler with text: " + new String(data) + " <<-");
                titleBuffer.append(data);
            } else {
                if (logger.isDebugEnabled()) logger.debug("Parser: Default Text Handler with text: " + new String(data) + " <<-");
                String dataStr = new String(data);
                String[] dataTok = dataStr.split("[^\\w]");
                for (int i=0 ; i < dataTok.length ; i++) {
                    String tok = dataTok[i];
                    if (tok.length() > 3) { // Very short words can be discarded
                        tok = tok.toLowerCase();
                        Object cnt = wordTree.get(tok);
                        if (cnt != null) {
                            ((MyLong) cnt).inc();
                        } else {
                            wordTree.put(tok, new MyLong(1));
                        }
                    }
                }
                logger.debug("Parse: Default Text Handler finished.");
            }
        }
        
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
            if (logger.isDebugEnabled()) logger.debug("Parser: Simple tag handler called for "+ tag);
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
        
        public void handleError(String errorMsg, int pos) {
            if (logger.isDebugEnabled()) logger.debug("HTML parser error: " + errorMsg + " at pos " + pos);
        }
    } // End of inner class InternalParserCallback
    
    /**
     * Class that contains a single long value, that can be reset (unlike
     * Java's <code>Long</code> class.
     */
    protected class MyLong {
        // value of this instance
        protected long value;
        
        /// Create a new instance.
        public MyLong(long init) {
            value = init;
        }
        
        /// Get the value.
        public long getValue() {
            return value;
        }
        
        /// Sets a new value
        public void setValue(long newVal) {
            value = newVal;
        }
        
        /// Increases the value by 1
        public void inc() {
            value++;
        }
    }
}
