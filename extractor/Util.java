/*
 * Util.java
 *
 * Created on 10 December 2001, 12:31
 */

package gilbert.extractor;

import java.net.*;
import java.util.*;
import java.io.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

/**
 * Class with some neat static utility mehtods on can use.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class Util {
    
    /*
     * Init the system properties. That needs to be done only once.
     */
    static {
        Properties sysProps = System.getProperties();
        sysProps.setProperty("http.proxyHost", "wwwcache.lancs.ac.uk");
        sysProps.setProperty("http.proxyPort", "8080");
        sysProps.setProperty("sun.net.client.defaultConnectTimeout", "1000");
        sysProps.setProperty("sun.net.client.defaultReadTimeout", "5000");
    }
    
    public static final int IP_ADDRESS = 1;
    public static final int HOST_NAME = 2;
    /// Output Stream for logging purposes
    protected static PrintStream logStream = System.err;
    /// Code for debugging log level
    public static final int LOG_DEBUG = 10;
    /// Code for non-critical message log level
    public static final int LOG_MESSAGE = 8;
    /// Code for normal operation log level
    public static final int LOG_NORMAL = 3;
    /// Code for "warning" messages
    public static final int LOG_WARN = 2;
    /// Code for "error" log level
    public static final int LOG_ERROR = 1;
    /// Current logging level
    protected static int logLevel = LOG_NORMAL;
    /// Cache for site status of different urls...
    protected static Hashtable siteCache = new Hashtable();
    /// Timeout for live cache entries in seconds.
    protected static int siteCacheTimeout = 5 * 60 * 60; // 5 hours should do...
    /// Last cleanup of the siteCache
    protected static long cacheTimestamp = System.currentTimeMillis();
    /// Inteverval for cach cleanups in minutes
    protected static int cleanupInterval = 15; // Thrice the time of the cache timeout
    
    
    /**
     * Returns whether the given string has an IP Adress format or a
     * hostname format. This will not check IPv6 addresses, and make
     * no sanity checks.
     * @param host A hostname or ip address.
     * @return int Whether the host string is an IP Address or
     *             a hostname. This uses the internal constants as codes.
     */
    public static int hostnameType(String host) {
        if (host.matches("\\A(\\d{1,3}\\.){3}\\d{1,3}\\Z")) {
            return IP_ADDRESS;
        } else {
            return HOST_NAME;
        }
    }
    /**
     * Check if the given URL is alive.
     * @deprecated Has been replaced with { @link #siteStatus(String) }
     */
    public static boolean isAlive(String host) {
        return siteStatus("http://" + host + "/").getAlive();
    }
    
    /**
     * Check the status of the give web site. This will return a
     * <code>SiteInfo</code> object containig the status.
     */
    public static SiteInfo siteStatus(String urlStr) {
        Util.logMessage("Checking site status for url: " + urlStr, Util.LOG_MESSAGE);
        URL u = null;
        HttpURLConnection conn = null;
        
        // check the cache
        SiteInfo retVal = checkSiteCache(urlStr);
        
        if (retVal == null) {
            Util.logMessage("No cache entry: Creating site status for: " + urlStr, Util.LOG_MESSAGE);
            retVal = new SiteInfo(urlStr);
            try {
                u = new URL(urlStr);
                conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("HEAD");
                conn.connect();
                int resCode = conn.getResponseCode();
                Util.logMessage("Got return code: " + resCode, Util.LOG_DEBUG);
                String contentType = conn.getContentType();
                Util.logMessage("Got content type: " + contentType, Util.LOG_DEBUG);
                retVal.setContentType(contentType);
                // Parse the page if it's HTML
                if ((contentType != null) && contentType.equals("text/html")) {
                    DocumentParser parser = new DocumentParser(DTD.getDTD("HTML"));
                    HTMLEditorKit.ParserCallback pc = new Util.InternalParserCallback(retVal);
                    parser.parse(new InputStreamReader(conn.getInputStream()), pc, true);
                } else {
                    Util.logMessage("Site Status parsing: Ignored URL with non-html type: " + contentType + "(" + urlStr + ")", Util.LOG_MESSAGE);
                }
                // End parsing code
                conn.disconnect();
                if (resCode >= 400) { // If the code is not an ok or redirect
                    Util.logMessage("Host was not alive", Util.LOG_MESSAGE);
                    retVal.setAlive(false);
                } else {
                    Util.logMessage("Host found alive", Util.LOG_MESSAGE);
                    retVal.setAlive(true);
                }
            } catch (MalformedURLException e) {
                logMessage(urlStr + " is not a valid URL.", LOG_WARN);
                retVal.setAlive(false);
            } catch (IOException e) {
                Util.logMessage("IOException checking for host status: " + e.getMessage(), Util.LOG_MESSAGE);
                retVal.setAlive(false);
            }
            addCacheEntry(retVal);
        }
        return retVal;
    }
    
    /**
     * Sets a new log level. <code>logMessage()</code> will
     * log all messages with a severity of that level or higher.
     */
    public static void setLogLevel(int logL) {
        logLevel = logL;
    }
    
    /**
     * Sets the log stream. <code>logMessage()</code> will print all
     * log messages on that stream.
     */
    public static void setLogStream(PrintStream ls) {
        logStream = ls;
    }
    
    /**
     * Logs a message. This writes the given string to the log stream
     * if the severity given is as (or more) severe than the current
     * logging level.
     */
    public static void logMessage(String message, int severity) {
        if (severity <= logLevel) {
            message = "[" + dateToString(System.currentTimeMillis()) + "] " + message;
            logStream.println(message);
        }
    }
    
    /**
     * Converts the given system time (in milliseconds) to
     * a printable date String.
     */
    public static String dateToString(long millis) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        StringBuffer currentDate = new StringBuffer();
        currentDate.append(cal.get(Calendar.HOUR_OF_DAY));
        currentDate.append(":");
        currentDate.append(cal.get(Calendar.MINUTE));
        currentDate.append(":");
        currentDate.append(cal.get(Calendar.SECOND));
        currentDate.append(" ");
        currentDate.append(cal.get(Calendar.DAY_OF_MONTH));
        currentDate.append(".");
        currentDate.append(cal.get(Calendar.MONTH) + 1);
        currentDate.append(".");
        currentDate.append(cal.get(Calendar.YEAR));
        return currentDate.toString();
    }
    
    /**
     * Adds a host to the liveCache.
     * @param alive Indicates if the host is alive or not.
     */
    protected static void addCacheEntry(SiteInfo status) {
        cleanupSiteCache();
        siteCache.put(status.getUrl(), status);
    }
    
    /**
     * Convenience method to get at peek at the cache.
     */
    public static Hashtable geSiteCache() {
        return siteCache;
    }
    
    
    /**
     * Checks the given host against the site cache.
     *
     * @return SiteInfo This retuns the <code>SiteInfo</code> object
     *                  of the host, or null if there is no cache entry.
     */
    protected static SiteInfo checkSiteCache(String host) {
        SiteInfo retval = null;
        
        if (siteCache.containsKey(host)) {
            SiteInfo current = (SiteInfo) siteCache.get(host);
            if ((current.getTimestamp() + (siteCacheTimeout * 1000)) < System.currentTimeMillis()) {
                siteCache.remove(host); // Lazy cleanup
            } else {
                Util.logMessage("Found valid cache entry for host: " + host, Util.LOG_MESSAGE);
                retval = current;
            }
        }
        cleanupSiteCache();
        return retval;
    }
    
    /**
     * Cleans up zombie entries from the cache.
     */
    protected static void cleanupSiteCache() {
        if  ((cacheTimestamp + (cleanupInterval * 60000)) < System.currentTimeMillis()) {
            Util.logMessage("Util: Cleaning up site cache. (" + siteCache.size() + "entries)", Util.LOG_MESSAGE);
            Enumeration keys = siteCache.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                SiteInfo current = (SiteInfo) siteCache.get(key);
                if ((current.getTimestamp() + (siteCacheTimeout * 1000)) < System.currentTimeMillis()) {
                    siteCache.remove(key);
                    Util.logMessage("Util: Stale cache entry removed: " + key, Util.LOG_DEBUG);
                }
            }
            cacheTimestamp = System.currentTimeMillis();
            Util.logMessage("Util: Cache cleanup complete.", Util.LOG_MESSAGE);
        }
    }
    
    /**
     * Internal Parser Callback class, for parsing documents for additional
     * SiteStatus information
     */
    protected static class InternalParserCallback extends HTMLEditorKit.ParserCallback {
        /// Indicates if the title is being parsed at the moment.
        boolean parseTitle = false;
        /// StringBuffer for the title string.
        StringBuffer titleBuffer;
        /// SiteInfo object this parser puts the data into
        SiteInfo info;
        
        /**
         * Creates a new InternalParserCallback.
         * @param info The <code>SiteInfo</code> object that takes the
         *             information from this parser.
         */
        public InternalParserCallback(SiteInfo info) {
            this.info = info;
        }
        
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
            // check for the <title> tag
            if (tag.equals(HTML.Tag.TITLE)) {
                Util.logMessage("Site Info Parser: Started Title Tag.", Util.LOG_DEBUG);
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
                        info.addMetaKeyword(keylist[i]);
                        Util.logMessage("Site Info Parser: Added META keyword: " + keylist[i], Util.LOG_DEBUG);
                    }
                }
                if (attrib.equals("description")) {
                    info.setMetaDescription(a.getAttribute(HTML.Attribute.CONTENT).toString());
                    Util.logMessage("Site Info parser added META description: " + a.getAttribute(HTML.Attribute.CONTENT).toString(), Util.LOG_DEBUG);
                }
            }
        }
        
        public void handleEndTag(HTML.Tag tag, int pos) {
            if (tag.equals(HTML.Tag.TITLE)) {
                parseTitle = false;
                info.setMetaTitle(titleBuffer.toString());
                Util.logMessage("Site Info parser: Stopped title parsing", Util.LOG_DEBUG);
                Util.logMessage("Put title string " + titleBuffer, Util.LOG_DEBUG);
            }
        }
        
        public void handleText(char[] data, int pos) {
            if (parseTitle) {
                titleBuffer.append(data);
            }
        }
        
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
            if (tag.equals(HTML.Tag.A) && a.isDefined(HTML.Attribute.HREF)) {
                String attrib = a.getAttribute(HTML.Attribute.HREF).toString().toLowerCase();
                if (attrib.startsWith("http:")) {
                    Util.logMessage("Site Info Parser: Added link to " + attrib, Util.LOG_MESSAGE);
                    info.addLink(attrib);
                } else if (attrib.startsWith("#")) {
                    // Link to an internal anchor
                    /* Uncomment if you really want to follow internal links
                    String url = info.getUrl();
                    int idx = url.indexOf("?");
                    if (idx != -1) url = url.substring(0, idx);
                    attrib = url + attrib; 
                    info.addLink(attrib);
                     */
                    Util.logMessage("Site Info Parser: Internal link (ignored):" + attrib, Util.LOG_MESSAGE);
                } else if (attrib.matches("^:.*(tml$|tml#.*)")) {
                    // seems to be a relative link to another page.
                    String url = info.getUrl();
                    int idx = url.indexOf("?");
                    if (idx != -1) url = url.substring(0,idx);
                    if (url.endsWith("/")) {
                        attrib = url + attrib;
                    } else {
                        attrib = url + "/" + attrib;
                    }
                    info.addLink(attrib);
                    Util.logMessage("Site Info Parser: Constructed link from relative: " + attrib, Util.LOG_MESSAGE);
                } else {
                    Util.logMessage("Site Info Parser: Ignoring unknown link: " + attrib, Util.LOG_MESSAGE);
                } 
            }
        }
        
    } // End of inner class InternalParserCallback
    
}
