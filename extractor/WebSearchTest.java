/*
 * WebSearchTest.java
 *
 * Created on 11 January 2002, 17:32
 */

package gilbert.extractor;
import java.util.*;

/**
 *
 * @author  hahnd
 */
public class WebSearchTest {
    
    /** Creates a new instance of WebSearchTest */
    public WebSearchTest() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Util.setLogLevel(Util.LOG_DEBUG);
        if (args.length == 0) {
            System.out.println("Please give a search string");
            System.exit(1);
        }
        String keystring = args[0];
        for (int i = 1 ; i < args.length ; i++) {
            keystring = keystring + "," + args[i];
        }
        Properties parms = new Properties();
        parms.setProperty("search.keywords", keystring);
        parms.setProperty("search.domains", "at");
        parms.setProperty("search.resultcount", "50");
        System.out.println("Search with keywords:" + keystring);
        WebSearch testSearch = new GoogleSearch();
        testSearch.setParameters(parms);
        Vector results = testSearch.search();
        Enumeration resultsE = results.elements();
        while (resultsE.hasMoreElements()) {
            System.out.println(resultsE.nextElement());
        }
        System.out.println("Number of Results: " + results.size());
    }
    
}
