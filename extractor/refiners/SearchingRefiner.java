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
    /** The search Properties */
    protected Properties sProps;
    /** The search object */
    WebSearch mySearch;
    /** Track the domains that have already been refined */
    protected Hashtable refHash;
    /// Logger for this class
    protected Logger logger = Logger.getLogger(this.getClass());
    /// If the keywords in the Search should be combined by OR rather than AND (default).
    protected boolean orSearch;
    
    
    /**
     * Creates a new instance of SearchingRefiner.
     * @param passing Set to false if you don't want the original URL
     *                in the output. This would be an unexpected
     *                behaviour, but there may be reasons for this.
     * @param keywordList A comma-separated list of keywords.
     */
    public SearchingRefiner() {
        super();
        logger.debug("Creating Searching Refiner.");
        // initialize the search Object
        maxHandlers = 5;
        mySearch = new GoogleSearch();
        sProps = new Properties();
        this.passing = true;
        orSearch = true;
        sProps.setProperty("search.languages", "");
        sProps.setProperty("search.keywords", "");
        logger.debug("Refiner created.");
    }
    
    
    public void refine(InputSource input) {
        refHash = new Hashtable();
        super.refine(input);
    }
    
    /**
     * Sets the languages for the search. This should be a comma-separated
     * list of language codes.
     */
    public void setLanguages(String languages) {
        sProps.setProperty("search.languages", languages);
    }
    
    /**
     * Gets the language String.
     */
    public String getLanguages() {
        return sProps.getProperty("search.languages");
    }
    
    public void setOrSearch(boolean or) {
        orSearch = or;
    }
    
    public boolean getOrSearch() {
        return orSearch;
    }
    
    /**
     * Sets the keyword list. This is expected to be a comma-separatd list
     * of keywords.
     */
    public void setKeywords(String keywords) {
        sProps.setProperty("search.keywords", keywords);
    }
    
    /**
     * Gets the keyword list.
     */
    public String getKeywords() {
        return sProps.getProperty("search.keywords");
    }
    
    /// Sets the passing property.
    public void setPassing(boolean passing) {
        this.passing = passing;
    }
    
    /// Gets the passing property
    public boolean getPassing() {
        return passing;
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
            sProps.setProperty("search.orCombined", "" + orSearch);
            mySearch.setParameters(sProps);
            Vector results = mySearch.search();
            logger.debug("Domain search complete.");
            if (results != null) {
                Enumeration resultsE = results.elements();
                while (resultsE.hasMoreElements()) {
                    URL curUrl = (URL) resultsE.nextElement();
                    String curUrlStr = curUrl.toExternalForm();
                    if (!refHash.containsKey(curUrlStr)) {
                        synchronized (outStream) {
                            startTag("url");
                            printTag("name", curUrlStr);
                            printTag("degree", "" + (degree + 1));
                            endTag("url");
                        }
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
