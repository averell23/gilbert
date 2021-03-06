/*
 * GoogleSearch.java
 *
 * Created on 09 January 2002, 15:16
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
 * This is a web search using the Google search engine (http://www.google.com/).
 * Current version supports only the search.keywords, search.resultcount and
 * search.domain parameters. The search.languages parameter is also supported,
 * but must contain the languege code that Google uses (e.g. de for German,
 * ja for Japanese, en for English, ...)
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class GoogleSearch extends WebSearch {
    /** Home of the google search engine */
    public static final String GOOGLE_HOME = "http://www.google.com/";
    /// If we are in the part of that contains the results
    public boolean parseResults = false;
    /// If the parsing has finished
    public boolean doneParsing = false;
    /// If we have already parsed the result of the current paragraph
    public boolean paragraphParsed = false;
    /// Logger for this class.
    protected Logger logger;
    
    /** Creates new GoogleSearch */
    public GoogleSearch() {
        logger = Logger.getLogger(this.getClass());
    }
    
    /**
     * Creates a new GoogleSearch with the given Parameters.
     * The search will be performed immediately, and the constructor
     * will block until the search is complete.
     */
    public GoogleSearch(Properties params) {
        super();
        setParameters(params);
        search();
    }
    
    /**
     * Performs the search. This method should block until all the results
     * are retrieved.
     * @return Vector A vector containing the results as
     *               <code>java.net.URL</code> objects. This should be
     *               <code>null</code> if the search was unsuccessful.
     *
     */
    public Vector search() {
        results = new Vector();
        String keywords = parameters.getProperty("search.keywords");
        if ((keywords == null) || keywords.equals("")) {
            logger.error("No keywords were specified for search.");
            return results;
        }
        keywords = keywords.replace(',', '+');
        String domainStr = parameters.getProperty("search.domains");
        String noOfResults = parameters.getProperty("search.resultcount");
        String languageStr = parameters.getProperty("search.languages");
        if (logger.isDebugEnabled()) logger.debug("OrCombined value: " + parameters.getProperty("search.orCombined"));
        Boolean searchOr = new Boolean(parameters.getProperty("search.orCombined"));
        Vector domains = new Vector();
        domains.add("");
        Vector languages = new Vector();
        languages.add("");
        if ((domainStr != null) && !domainStr.equals("")) {
            domains = splitString(domainStr);
        }
        if ((languageStr != null) && !languageStr.equals("")) {
            languages = splitString(languageStr);
        }
        Enumeration domEnum = domains.elements();
        Enumeration langEnum = languages.elements();
        while (domEnum.hasMoreElements()) {
            String curDom = (String) domEnum.nextElement();
            while (langEnum.hasMoreElements()) {
                String curLang = (String) langEnum.nextElement();
                searchGoogle(keywords, curLang, curDom, noOfResults, searchOr.booleanValue());
            }
        }
        return results;
    }
    
    /**
     * Creates the search URL and calls the search.
     */
    protected void searchGoogle(String keywords, String language, String domain, String numberOfResults, boolean searchOr) {
        parseResults = false;
        paragraphParsed = false;
        doneParsing = false;
        
        logger.info("Searching domain " + domain + ", language: " + language);
        URL sUrl = null;
        
        StringBuffer searchBuf = new StringBuffer(GOOGLE_HOME);
        searchBuf.append("search?");
        if (searchOr) {
            searchBuf.append("as_oq=");
        } else {
            searchBuf.append("as_q=");
        }
        searchBuf.append(keywords);
        if ((numberOfResults == null) || numberOfResults.equals("")) {
            numberOfResults = "10";
        }
        searchBuf.append("&num=" + numberOfResults);
        if ((domain != null) && (!domain.equals(""))) {
            searchBuf.append("&as_sitesearch=" + domain);
        }
        if ((language != null) && (!language.equals(""))) {
            searchBuf.append("&lr=lang_" + language);
        }
        searchBuf.append("&btnG=Google+Search");
        if (logger.isDebugEnabled()) logger.debug("Using URL: " + searchBuf);
        try {
            sUrl = new URL(searchBuf.toString());
        } catch (MalformedURLException e) {
            logger.error("Error, malformed search URL: " + searchBuf);
            return;
        }
        callSearch(sUrl);
        logger.debug("GoogleSearch: Search call returned.");
    }
    
    void handleHTMLEndTag(HTML.Tag t,int pos) {
        // This would be the proper handling, if DIV wouldn't be handled
        // as a simple tag
        /* if (t.equals(HTML.Tag.DIV)) {
            parseResults = false;
            doneParsing = true;
            logger.debug("Leaving parseable area");
        } */
    }
    
    /*
     * Obviously the DIV Tag seems to be handled as "Simple"
     * as is the A Tag.
     */
    void handleHTMLSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (t.equals(HTML.Tag.DIV)) {
            logger.debug("DIV tag found");
            if  (!(parseResults || doneParsing)) {
                logger.debug("Start Parsing");
                parseResults = true;
            } else {
                logger.debug("Stop parsing");
                parseResults = false;
                doneParsing = true;
            }
        }
        if (t.equals(HTML.Tag.A) && (!paragraphParsed) && parseResults) { // Get a new URL
            String urlName = (String) a.getAttribute(HTML.Attribute.HREF);
            if (logger.isDebugEnabled()) {
                logger.debug("GoogleSearch: Trying to capture: " + urlName);
            }
            URL myUrl = null;
            try {
                myUrl = new URL(urlName);
                results.add(myUrl);
            } catch(MalformedURLException e) {
                logger.error("Malformed URL: " + urlName);
            }
            paragraphParsed = true;
        }
    }
    
    void handleHTMLStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        // This would be the proper handling if the DIV wasn't
        // handled as a simple tag...
        /* if (t.equals(HTML.Tag.DIV) && (!doneParsing)) { // The first Div is the start of the results
            logger.debug("Entering parseable area");
            parseResults = true;
        } */
        if (parseResults) {
            if (t.equals(HTML.Tag.P)) { // A new paragraph -> start looking for url again
                paragraphParsed = false;
            }
            // This part is never called because A is (incorrectly) considered a simple Tag...
            /* if (t.equals(HTML.Tag.A) && (!paragraphParsed)) { // Get a new URL
                String urlName = (String) a.getAttribute(HTML.Attribute.HREF);
                URL myUrl = null;
                try {
                    myUrl = new URL(urlName);
                } catch(MalformedURLException e) {
                    logger.error("Malformed URL: " + urlName);
                }
                paragraphParsed = true;
                results.add(myUrl);
            } */
        }
    }
}
