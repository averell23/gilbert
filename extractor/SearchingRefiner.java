/*
 * SearchingRefiner.java
 *
 * Created on 14 January 2002, 11:11
 */

package gilbert.extractor;
import java.util.*;
import java.net.*;

/**
 * This takes a existing list of URLs and searches their domains
 * for some pre-defined keywords.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class SearchingRefiner extends Refiner {
    /** Keywords that will be used for the search */
    protected String keywordList;
    /** Wether to always include the original URLs */
    protected boolean keepOriginal;
    /** The search Properties */
    protected Properties sProps;
    /** The search object */
    WebSearch mySearch;
    /** Track the domains that have already been refined */
    protected Hashtable refHash;
    
    /**
     * Creates a new instance of SearchingRefiner.
     * @param keepOriginal If true, the original URL will always be in
     *                     the output.
     * @param keywordList A comma-separated list of keywords.
     */
    public SearchingRefiner(boolean keepOriginal, String keywordList) {
        // initialize the search Object
        mySearch = new GoogleSearch();
        sProps = new Properties();
        sProps.setProperty("search.keywords", keywordList);
    }
    
    public void refine(String uri) {
        refHash = new Hashtable();
        super.refine(uri);
    }
    
    /** This method must be overridden by child classes to handle each of
     * the URLs.
     *
     * It's up to the child class to use the proper methods or the
     * <code>outStream</code> for printing the resutls and to honour
     * the postfilters.
     */
    public void handleURL(VisitorURL url) {
        Util.logMessage("Refiner: Handling new URL", Util.LOG_DEBUG);
        String urlStr = url.getProperty("url.name");
        URL tURL = null;
        try {
            tURL = new URL(urlStr);
            String hostStr = tURL.getHost();
            Util.logMessage("Refiner: Found host name: " + hostStr, Util.LOG_DEBUG);
            String[] parts = hostStr.split("\\.");
            if ((parts == null) || (parts.length == 0)) {
                System.err.println("Error splitting hostname");
                return;
            }
            String domainName = parts[parts.length - 2] + "." + parts[parts.length -1];
            Util.logMessage("Refiner: Trying to search domain: " + domainName, Util.LOG_DEBUG);
            sProps.setProperty("search.domains", domainName);
            mySearch.setParameters(sProps);
            Vector results = mySearch.search();
            if (results != null) {
                Enumeration resultsE = results.elements();
                while (resultsE.hasMoreElements()) {
                    URL curUrl = (URL) resultsE.nextElement();
                    String curUrlStr = curUrl.toExternalForm();
                    if (!refHash.containsKey(curUrlStr)) {
                        startTag("url");
                        printTag("name", curUrlStr);
                        endTag("url");
                        refHash.put(curUrlStr, "refined");
                    }
                }
            }
        } catch(MalformedURLException e) {
            System.err.println("Source URL malformed: " + urlStr);
        }
        if (keepOriginal) {
            startTag("url");
            printTag("name", urlStr);
            endTag("url");
        }
    }
    
}
