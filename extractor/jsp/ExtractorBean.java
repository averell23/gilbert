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
import org.apache.log4j.*;

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
    /// Tree with pages that have been found previously
    protected TreeSet currentSet;
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
    /// Counter for failed connections to the data source
    protected int failures = 0;
    /// The max. number of failed extractions that the backoff takes into account.
    protected int maxBackoff = 6;
    /// Logger for this class
    protected Logger logger = Logger.getLogger(ExtractorBean.class);
    /// Counter for the beans
    protected static long beanCount = 0;
    /// Name for current Bean
    protected String beanName;
    /// Static keywords...
    protected String keywords = "ubicomp,wearable,ubiquiotous,context,awareness,ambient";
    
    /** Creates new ExtractorBean */
    public ExtractorBean() {
        logger.debug("Creating ExtractorBean");
        // Create the Beans name
        synchronized (ExtractorBean.class) {
            beanName = "ExtractorBean:" + beanCount;
            beanCount++;
        }
        NDC.push(beanName);
        currentSet = new TreeSet();
        fallbackUrl = new VisitorURL();
        fallbackUrl.setProperty("url.name", "nothing.html");
        currentSet.add(fallbackUrl);
        extractor = createExtractingChain();
        
        timestamp = 0;
        NDC.pop();
    }
    
    protected ExtractingChain createExtractingChain() {
        extractor = new ExtractingChain(dataSource);
        
        Extractor ext = new StraightExtractor();
        ext.addPrefilter(new LocalVisitFilter());
        ext.addPrefilter(new AgentVisitFilter());
        RTypeVisitFilter rtf = new RTypeVisitFilter();
        rtf.setDocumentTypes(".gif,.jpg,.pdf,.tif,.png");
        ext.addPrefilter(rtf);
        extractor.setExtractor(ext);
        
        SearchingRefiner search = new SearchingRefiner();
        search.setKeywords(keywords);
        extractor.addRefiner(search);
        
        extractor.addRefiner(new LinkRefiner());
        
        MetaKInterestRefiner mkr = new MetaKInterestRefiner();
        mkr.setKeywords(keywords);
        mkr.setMaxHandlers(12);
        extractor.addRefiner(mkr);
        
        KWInterestRefiner kwr = new KWInterestRefiner();
        kwr.setKeywords(keywords);
        kwr.setMaxHandlers(12);
        kwr.setWeight(0.5);
        extractor.addRefiner(kwr);
        
        Refiner meta = new MetaRefiner();
        DocumentTypeURLFilter docFilter = new DocumentTypeURLFilter();
        docFilter.addDocumentType("text/html");
        meta.addPrefilter(docFilter);
        extractor.addRefiner(meta);
        
        endRef = new VectorRefiner();
        endRef.addPrefilter(new AliveFilter());
        extractor.addRefiner(endRef);
        
        return extractor;
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
    
    
    /**
     * Returns the URLs
     */
    public Collection getUrls() {
        return currentSet.tailSet(currentSet.first());
    }
    
    public void setTimeout(long to) {
        timeOut = to;
    }
    
    public long getTimeout() {
        return timeOut;
    }
      
    /**
     * Gets the timestamp of the last extraction
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Updates the internal data. If the timeout is reached, the
     * extraction process will be started anew.
     */
    public synchronized void update() throws IOException {
        /* Calculate the real time out. Take into account the failures for backoff time. */
        long realTimeout = (failures == 0)?timeOut:((long) (timeOut * ((failures - 1) * 0.25)));
        logger.debug("Real timeout calculated to: " + (realTimeout / 1000));
        if ((timestamp + realTimeout) < System.currentTimeMillis()) {
            if (logger.isDebugEnabled() && (failures != 0)) {
                logger.debug("Last extraction failed, retry no. " + failures
                + " after " + ((System.currentTimeMillis() - timestamp) / 1000)
                + " seconds.");
            }
            endRef.reset();
            extractor.extract();
            Vector tmpSet = endRef.getUrlList();
            currentSet = new TreeSet();
            if (tmpSet.size() != 0) {
                Enumeration tmpE = tmpSet.elements();
                if (logger.isDebugEnabled()) logger.debug("Trying to add " + tmpSet.size() + " elements.");
                while (tmpE.hasMoreElements()) {
                    VisitorURL v = (VisitorURL) tmpE.nextElement();
                    if (!currentSet.contains(v)) {
                        /* double interest = 0;
                        try {
                            String interestStr = v.getProperty("url.interest");
                            if (interestStr != null) {
                                interest = Double.parseDouble(interestStr);
                            } else {
                                logger.warn("Extracted URL without interest found.");
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("Refined URL without proper interest found");
                        } */
                        currentSet.add(v);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Did not add duplicate URL: " + v.getProperty("url.name"));
                        }
                    }
                }
                if (logger.isDebugEnabled()) logger.debug(currentSet.size() + " URLs present in Bean.");
                
                timestamp = System.currentTimeMillis();
                failures = 0;
            } else {
                currentSet.add(fallbackUrl);
                logger.warn("ExtractorBean: Extraction failed (no results)");
                if (failures < maxBackoff) failures++; // increase the failure counter..
                timestamp = System.currentTimeMillis();
            }
        }
    }
}
