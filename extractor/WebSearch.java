/*
 * WebSearch.java
 *
 * Created on 09 January 2002, 14:30
 */

package gilbert.extractor;

import java.util.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.html.*;
import javax.swing.text.*;
import java.io.*;
import java.net.*;
import org.apache.log4j.*;

/**
 * Abstract base class for a Web search. This will connect to a particular
 * Web search engine, and return the URLs that have been found.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public abstract class WebSearch {
    /** Vector containing the URLs found by the search */
    protected Vector results;
    /** Parameters for the search */
    protected Properties parameters;
    /** Callback class for HTML parsing */
    protected ParserCallback myCallback;
    /// Logger for this class
    protected Logger logger;
    
    /**
     * Default constructor
     */
    public WebSearch() {
        logger = Logger.getLogger(this.getClass());
        // Kludge for Proxy settings...
        // FIXME: Proxy settings should go to global setting class
        Properties sysProps = System.getProperties();
        sysProps.setProperty("http.proxyHost", "wwwcache.lancs.ac.uk");
        sysProps.setProperty("http.proxyPort", "8080");
        logger.debug("System Properties set for proxy");
        myCallback = new ParserCallback();
    }
    
    /**
     * Performs the search. This method should block until all the results
     * are retrieved.
     * @return Vector A vector containing the results as
     *                <code>java.net.URL</code> objects. This should be
     *                <code>null</code> if the search was unsuccessful.
     *
     */
    public abstract Vector search();
    
    /**
     * This sets the search parameters. The Properties of the search should
     * be
     * <ul>
     * <li><i>search.keywords</i> A comma-separated list of the search
     *     words.</li>
     * <li><i>search.domains</i> A comma-separated list of the domains
     *     that will be searched.</li>
     * <li><i>search.languages</i> A comma-separated list of the desired
     *     languages of the resulting pages. (Languages are given by
     *     their standard code.</li>
     * <li><i>search.resultcount</i> The maximum number of results that
     *     the search should return. This may be limited by the search engine, and
     * also note this number is returned for <i>each</i> of the
     * domains that are searched.</li>
     * <li><i>serach.orCombined</i> The keywords will be combined by OR 
     * rather than AND. (This should be the default)</li>
     * </ul>
     * <b>Note:</b> Due to differences in the capabilities of the search engines,
     * not all of the parameters may be honoured.
     */
    public void setParameters(Properties params) {
        parameters = params;
    }
    
    /**
     * Returns the result vector of the last search.
     */
    public Vector getResults() {
        return results;
    }
    
    /**
     * Calls the search engine. This does <b>re-initialize</b> the result vector!
     */
    protected void callSearch(URL searchURL) {
        try {
            if (logger.isDebugEnabled()) logger.debug("Trying to open search connection for: " + searchURL);
            HttpURLConnection conn = (HttpURLConnection) searchURL.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 4.0");
            conn.connect();
            DocumentParser parser = new DocumentParser(DTD.getDTD("HTML"));
            parser.parse(new InputStreamReader(conn.getInputStream()), myCallback, true);
            conn.disconnect();
        } catch (IOException e) {
            logger.error("Unable to get search result page.");
            logger.error("I/O Exception: " + e.getMessage(), e);
            results = null;
        }
    }
    
    /**
     * Splits a comma-separated string into a vector of strings. This is a
     * convenience method for handling the search properties.
     */
    protected Vector splitString(String s) {
        StringTokenizer sTok = new StringTokenizer(s, ",");
        Vector retVal = new Vector();
        while (sTok.hasMoreElements()) {
            retVal.add(sTok.nextToken());
        }
        return retVal;
    }
    
    /** Called when handling a HTML comment in the result page */
    void handleHTMLComment(char[] data, int pos) {
    }
    
    /** Called when handling a HTML End Tag */
    void handleHTMLEndTag(HTML.Tag t, int pos) {
    }
    
    /** Called when handling a simple HTML tag */
    void handleHTMLSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
    }
    
    /** Called when handling a HTML start tag. */
    void handleHTMLStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
    }
    
    /** Called when handlin text in the HTML file */
    void handleHTMLText(char[] data, int pos) {
    }
    
    /**
     * Parser callback class. This will call the handler functions in the
     * WebSearch class.
     */
    protected class ParserCallback extends HTMLEditorKit.ParserCallback {
        public void handleComment(char[] data, int pos) {
            handleHTMLComment(data, pos);
        }
        
        public void handleEndTag(HTML.Tag t, int pos) {
            handleHTMLEndTag(t, pos);
        }
        
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            handleHTMLSimpleTag(t, a, pos);
        }
        
        public void handleText(char[] data, int pos) {
            handleHTMLText(data, pos);
        }
        
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            handleHTMLStartTag(t, a, pos);
        }
    }
    
}
