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
import org.apache.log4j.*;

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
        Logger logger = Logger.getLogger(Util.class);
        Properties sysProps = System.getProperties();
        sysProps.setProperty("http.proxyHost", "wwwcache.lancs.ac.uk");
        sysProps.setProperty("http.proxyPort", "8080");
        sysProps.setProperty("sun.net.client.defaultConnectTimeout", "1000");
        sysProps.setProperty("sun.net.client.defaultReadTimeout", "4000");
        logger.debug("Util static initialization complete.");
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
    /// Logger for Util class
    protected static Logger logger = Logger.getLogger(Util.class);
    
    
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
        if (logger.isInfoEnabled()) logger.info("Checking site status for url: " + urlStr);
        URL u = null;
        HttpURLConnection conn = null;
        
        // check the cache
        SiteInfo retVal = checkSiteCache(urlStr);
        
        if (retVal == null) {
            logger.info("No cache entry: Creating site status for: " + urlStr);
            retVal = new SiteInfo(urlStr);
            addCacheEntry(retVal);
        }
        return retVal;
    }
    
    /**
     * Sets a new log level. <code>logMessage()</code> will
     * log all messages with a severity of that level or higher.
     *
     * @deprecated Use log4j API instead.
     */
    public static void setLogLevel(int logL) {
        logLevel = logL;
    }
    
    /**
     * Sets the log stream. <code>logMessage()</code> will print all
     * log messages on that stream.
     *
     * @deprecated Use log4j API instead.
     */
    public static void setLogStream(PrintStream ls) {
        logStream = ls;
    }
    
    /**
     * Logs a message. This writes the given string to the log stream
     * if the severity given is as (or more) severe than the current
     * logging level.
     * @deprecated Use the log4j interface instead.
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
                logger.info("Found valid cache entry for host: " + host);
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
            logger.info("Util: Cleaning up site cache. (" + siteCache.size() + "entries)");
            Enumeration keys = siteCache.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                SiteInfo current = (SiteInfo) siteCache.get(key);
                if ((current.getTimestamp() + (siteCacheTimeout * 1000)) < System.currentTimeMillis()) {
                    siteCache.remove(key);
                    if (logger.isDebugEnabled()) logger.debug("Util: Stale cache entry removed: " + key);
                }
            }
            cacheTimestamp = System.currentTimeMillis();
            logger.info("Util: Cache cleanup complete.");
        }
    }
}
