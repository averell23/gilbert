/*
 * SearchingRefiner.java
 *
 * Created on 14 January 2002, 11:11
 */

package gilbert.extractor.refiners;
import java.util.*;
import java.net.*;
import org.xml.sax.*;
import gilbert.extractor.*;
import org.apache.log4j.*;

/**
 * This takes a existing list of URLs and searches the web for pages with
 * predefined keywords and the same language.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class SearchingRefiner extends Refiner {
    /** Keywords that will be used for the search */
    protected String keywordList;
    /** The search Properties */
    protected Properties sProps;
    /** The search object */
    WebSearch mySearch;
    /** Track the domains that have already been refined */
    protected Hashtable refHash;
    /** The languages for this search */
    protected String langStr;
    /// Logger for this class
    protected Logger logger;
    
    /**
     * Creates a new instance of SearchingRefiner.
     * @param passing Set to false if you don't want the original URL
     *                in the output. This would be an unexpected
     *                behaviour, but there may be reasons for this.
     * @param keywordList A comma-separated list of keywords.
     */
    public SearchingRefiner(boolean passing, String keywordList) {
        super();
        logger = Logger.getLogger(this.getClass());
        // initialize the search Object
        mySearch = new GoogleSearch();
        sProps = new Properties();
        this.passing = passing;
        sProps.setProperty("search.keywords", keywordList);
    }
    
    /**
     * Creates a new instance of SearchingRefiner. The "passing"
     * property will be set to true.
     */
    public SearchingRefiner(String keywordList) {
        this(true, keywordList);
    }
    
    public void refine(InputSource input) {
        refHash = new Hashtable();
        super.refine(input);
    }
    
    /**
     * Sets the languages for the search. This should be a comma-separated
     * list of language codes.
     */
    public void setLangStr(String languages) {
        langStr = languages;
    }
    
    public void refine(String uri) {
        refine(new InputSource(uri));
    }
    
    /** This method must be overridden by child classes to handle each of
     * the URLs.
     *
     * It's up to the child class to use the proper methods or the
     * <code>outStream</code> for printing the resutls and to honour
     * the postfilters.
     */
    public void handleURL(VisitorURL url) {
        logger.debug("Refiner: Handling new URL");
        int degree = 0;
        try {
            degree = Integer.valueOf(url.getProperty("url.degree")).intValue();
        } catch (NumberFormatException e) {
            logger.warn("SearchingRefiner: Could not determine degree : " + e.getMessage());
        }
        if (logger.isDebugEnabled()) logger.debug("SearchingRefiner: Url has degree " + degree);
        String urlStr = url.getProperty("url.name");
        URL tURL = null;
        try {
            tURL = new URL(urlStr);
            String hostStr = tURL.getHost();
            if (logger.isDebugEnabled()) logger.debug("Refiner: Found host name: " + hostStr);
            
            String[] parts = hostStr.split("\\.");
            if ((parts == null) || (parts.length < 2)) {
                logger.error("Error splitting hostname");
                return;
            }
            String domainName = parts[parts.length - 2] + "." + parts[parts.length -1];
            if (logger.isInfoEnabled()) {
                logger.info("Trying to search domain: " + domainName);
            }
            sProps.setProperty("search.domains", domainName);
            mySearch.setParameters(sProps);
            Vector results = mySearch.search();
            logger.debug("Domain search complete.");
            if (results != null) {
                Enumeration resultsE = results.elements();
                while (resultsE.hasMoreElements()) {
                    URL curUrl = (URL) resultsE.nextElement();
                    String curUrlStr = curUrl.toExternalForm();
                    if (!refHash.containsKey(curUrlStr)) {
                        startTag("url");
                        printTag("name", curUrlStr);
                        printTag("degree", "" + (degree + 1));
                        endTag("url");
                        refHash.put(curUrlStr, "refined");
                    }
                }
            }
        } catch(MalformedURLException e) {
            logger.warn("Source URL malformed: " + urlStr);
        }
        logger.debug("Url handler finished.");
    }
    
}
