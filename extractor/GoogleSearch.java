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
/**
 * This is a web search using the Google search engine (http://www.google.com/).
 * Current version supports only the search.keywords parameter.
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
    
    /** Creates new GoogleSearch */
    public GoogleSearch() {
    }
    
    /**
     * Creates a new GoogleSearch with the given Parameters.
     * The search will be performed immediately, and the constructor
     * will block until the search is complete.
     */
    public GoogleSearch(Properties params) {
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
            System.err.println("No keywords were specified for search.");
            return results;
        }
        keywords = keywords.replace(',', '+');
        String domainStr = parameters.getProperty("search.domains");
        String noOfResults = parameters.getProperty("search.resultcount");
        if ((domainStr == null) || keywords.equals("")) {
            searchGoogle(keywords, "", noOfResults);
        } else {
            Vector domains = splitString(domainStr);
            Enumeration domEnum = domains.elements();
            while (domEnum.hasMoreElements()) {
                searchGoogle(keywords, (String) domEnum.nextElement(), noOfResults);
            }
        }
        return results;
    }
    
    /**
     * Creates the search URL and calls the search.
     */
    protected void searchGoogle(String keywords, String domain, String numberOfResults) {
        parseResults = false;
        paragraphParsed = false;
        doneParsing = false;
        
        Util.logMessage("Searching domain " + domain, Util.LOG_DEBUG);
        URL sUrl = null;
        
        StringBuffer searchBuf = new StringBuffer(GOOGLE_HOME);
        searchBuf.append("search?as_q=" + keywords);
        if ((numberOfResults == null) || numberOfResults.equals("")) {
            numberOfResults = "10";
        }
        searchBuf.append("&num=" + numberOfResults);
        if ((domain != null) && (!domain.equals(""))) {
            searchBuf.append("&as_sitesearch=" + domain);
        }
        searchBuf.append("&btnG=Google+Search");
        Util.logMessage("Using URL: " + searchBuf, Util.LOG_DEBUG);
        try {
            sUrl = new URL(searchBuf.toString());
        } catch (MalformedURLException e) {
            System.err.println("Error, malformed search URL: " + searchBuf);
            return;
        }
        callSearch(sUrl);
        Util.logMessage("GoogleSearch: Search call returned.", Util.LOG_DEBUG);
    }
    
    void handleHTMLEndTag(HTML.Tag t,int pos) {
        // This would be the proper handling, if DIV wouldn't be handled
        // as a simple tag
        /* if (t.equals(HTML.Tag.DIV)) {
            parseResults = false;
            doneParsing = true;
            Util.logMessage("Leaving parseable area", Util.LOG_DEBUG);
        } */
    }
    
    /*
     * Obviously the DIV Tag seems to be handled as "Simple"
     * as is the A Tag.
     */
    void handleHTMLSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (t.equals(HTML.Tag.DIV)) {
            Util.logMessage("DIV", Util.LOG_DEBUG);
            if  (!(parseResults || doneParsing)) {
                Util.logMessage("Start Parsing", Util.LOG_DEBUG);
                parseResults = true;
            } else {
                Util.logMessage("Stop parsing", Util.LOG_DEBUG);
                parseResults = false;
                doneParsing = true;
            }
        }
        if (t.equals(HTML.Tag.A) && (!paragraphParsed) && parseResults) { // Get a new URL
            String urlName = (String) a.getAttribute(HTML.Attribute.HREF);
            Util.logMessage("GoogleSearch: Trying to capture: " + urlName, Util.LOG_DEBUG);
            URL myUrl = null;
            try {
                myUrl = new URL(urlName);
                results.add(myUrl);
            } catch(MalformedURLException e) {
                System.err.println("Malformed URL: " + urlName);
            }
            paragraphParsed = true;
        }
    }
    
    void handleHTMLStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        // This would be the proper handling if the DIV wasn't
        // handled as a simple tag...
        /* if (t.equals(HTML.Tag.DIV) && (!doneParsing)) { // The first Div is the start of the results
            Util.logMessage("Entering parseable area", Util.LOG_DEBUG);
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
                    System.err.println("Malformed URL: " + urlName);
                }
                paragraphParsed = true;
                results.add(myUrl);
            } */
        }
    }
}
