/*
 * Util.java
 *
 * Created on 10 December 2001, 12:31
 */

package gilbert.extractor;

import java.net.*;
import java.util.*;
import java.io.*;
import org.apache.regexp.*;

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
        sysProps.setProperty("sun.net.client.defaultReadTimeout", "1000");
    }
     
    public static final int IP_ADDRESS = 1;
    public static final int HOST_NAME = 2;   
    /// Output Stream for logging purposes
    protected static PrintStream logStream = System.err;
    /// Code for debugging log level
    public static final int LOG_DEBUG = 10;
    /// Code for normal operation log level
    public static final int LOG_NORMAL = 3;
    /// Code for "error" log level
    public static final int LOG_ERROR = 1;
    /// Current logging level
    protected static int logLevel = LOG_NORMAL;
    
    /**
     * Returns whether the given string has an IP Adress format or a
     * hostname format. This will not check IPv6 addresses, and make
     * no sanity checks.
     * @param host A hostname or ip address.
     * @return int Whether the host string is an IP Address or 
     *             a hostname. This uses the internal constants as codes.
     */
    // FIXME: This method does NOT work correctly
    public static int hostnameType(String host) {
        RE regexp = null;
        try {
            regexp = new RE("^(\\d{1,4}\\.){3}\\d{1,4}$");
        } catch (RESyntaxException e) {
            System.err.println("RE Syntax: " + e.getMessage());
            return -1;
        }
        if (regexp.match(host)) {
            return IP_ADDRESS;
        } else {
            return HOST_NAME;
        }
    }
    
    /**
     * Check if the given URL is alive. <i>Alive</i> means that an 
     * Http server responds to a HEAD request with a non-errorcode.
     */
    public static boolean isAlive(String host) {
        URL u = null;
        HttpURLConnection conn = null;
        host = "http://" + host + "/";
        
        try {
            Util.logMessage("Trying " + host + "... ", Util.LOG_DEBUG);
            u = new URL(host);
            conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("HEAD");
            conn.connect();
            int resCode = conn.getResponseCode();
            Util.logMessage("Got return code: " + resCode, Util.LOG_DEBUG);
            conn.disconnect();
            if (resCode >= 400) { // If the code is not an ok or redirect
                return false;
            }
        } catch (MalformedURLException e) {
            logMessage(host + " is not a valid URL.", LOG_ERROR);
            return false;
        } catch (IOException e) {
            Util.logMessage("", Util.LOG_DEBUG);
            return false;
        } 
        return true;
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
        currentDate.append(cal.get(Calendar.MONTH));
        currentDate.append(".");
        currentDate.append(cal.get(Calendar.YEAR));
        return currentDate.toString();
    }
}
