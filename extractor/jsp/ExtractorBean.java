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
import gilbert.extractor.extractors.*;
import gilbert.extractor.refiners.*;
import gilbert.extractor.filters.*;

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
    protected VisitorURL fallbackUrl;
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
        Util.setLogLevel(Util.LOG_MESSAGE);
        currentSet = new Vector();
        fallbackUrl = new VisitorURL();
        fallbackUrl.setProperty("url.name", "nothing.html");
        currentSet.add(fallbackUrl);
        extractor = new ExtractingChain(dataSource);
        StraightExtractor ext = new StraightExtractor();
        ext.addPrefilter(new LocalVisitFilter());
        ext.addPrefilter(new AgentVisitFilter());
        extractor.setExtractor(ext);
        extractor.addRefiner(new SearchingRefiner(true, "ubicomp,handheld,context"));
        extractor.addRefiner(new MetaRefiner());
        endRef = new VectorRefiner();
        extractor.addRefiner(endRef);
        timestamp = 0;
    }
    
    public void setFallbackUrl(String fbu) {
        fallbackUrl.setProperty("url.name", fbu);
    }
    
    public String getFallbackUrl() {
        return fallbackUrl.getProperty("url.name");
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
            endRef.reset();
            extractor.extract();
            Vector tmpSet = endRef.getUrlList();
            if (tmpSet.size() != 0) {
                currentSet = tmpSet;
                timestamp = System.currentTimeMillis();
            }
        }
    }
}
