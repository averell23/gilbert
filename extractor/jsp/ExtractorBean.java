/*
 * ExtractorBean.java
 *
 * Created on 21 January 2002, 16:47
 */

package gilbert.extractor.jsp;

import java.util.*;
import java.io.*;
import java.net.*;
import org.xml.sax.*;
import gilbert.extractor.*;

/**
 * Java Bean to be used as an interface between the Extractor classes
 * and JSP pages. This holds the current state information and should
 * be used by all JSP pages trying to get at extracted URLs.<br>
 * <b>Note:</b> Be sure to set the right scope for this bean in your JSP,
 * or you may end up with different objects of this class.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class ExtractorBean {
    /// Vector with pages that have been found previously
    protected Vector currentSet;
    /// Fallback URL
    protected String fallbackUrl = "http://127.0.0.1/";
    /// URL for the data source.
    protected String dataSource = "http://127.0.0.1/marco/url.xml";
    /// Timeout for reloading the data
    protected long timeOut  = 5 * 60 * 1000; // 5 Minutes for the start
    /// Timestamp of the last reload
    protected long timestamp;
    /// Extraction chain for the extraction
    protected ExtractingChain extractor;
    /// VectorRefiner for the results
    protected VectorRefiner endRef;
    
    /** Creates new ExtractorBean */
    public ExtractorBean() {
        currentSet = new Vector();
        currentSet.add(fallbackUrl);
        extractor = new ExtractingChain(dataSource);
        extractor.setExtractor(new StraightExtractor());
        extractor.addRefiner(new SearchingRefiner(true, "ubicomp,handheld,context"));
        endRef = new VectorRefiner();
        extractor.addRefiner(endRef);
        timestamp = 0;
    }
    
    public void setFallbackUrl(String fbu) {
        fallbackUrl = fbu;
    }
    
    public String getFallbackUrl() {
        return fallbackUrl;
    }
    
    public void setDataSource(String ds) {
        dataSource = ds;
        extractor.setInputSource(ds);
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    
    public void setTimeout(long to) {
        timeOut = to;
    }
    
    public long getTimeout() {
        return timeOut;
    }
    
    /**
     * This gets the Vector containing the Urls
     */
    public Vector getUrls() {
        return currentSet;
    }
    
    /** 
     * This gets the timestamp of the last extraction
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * This will update the internal data. If the timeout is reached, the
     * extraction process will be started anew.
     */
    public void update() throws IOException { 
        if ((timestamp + timeOut) < System.currentTimeMillis()) {
            timestamp = System.currentTimeMillis();
            extractor.extract();
            Vector tmpSet = endRef.getUrlList();
            if (tmpSet.size() != 0) {
                currentSet = tmpSet;
            }
        }
    }
}
