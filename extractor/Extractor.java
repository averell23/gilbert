/*
 * Extractor.java
 *
 * Created on 05 December 2001, 10:16
 */

package gilbert.extractor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * This is the superclass for an URL extractor. It takes an XML source
 * with visit information and tries to find related URLs. The found URLs
 * should then be printed as an XML URL list. (The mode of output is not
 * enforced by this class, though.)
 *
 * <p>
 * <b>Multiple Handler threads:</b> A separate thread will be started
 * for each visit, up to <code>maxHandlers</code>. </code>maxHandlers = 1</code>
 * is equivalent to a sequential behaviour. To make your Refiner thread-safe,
 * <b>synchronize all write operations on <code>outStream</code></b>, to enforce
 * that the writing of an XML Tag will not be interrupted by a different
 * visit Handler. If you don't do this your Extractor will behave erratically
 * with more than one handler thread.
 * </p>
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public abstract class Extractor extends AbstractTransmutor implements Runnable {
    /// Vector containing the prefilters (visit filters)
    protected Vector prefilters;
    /// Indicates if prefiltering is on
    protected boolean prefiltering = false;
    /// Visit XML Handler
    protected VisitXMLHandler tHandler;
    /** Hashtable containing alredy visited hosts */
    protected Hashtable visitHash;
    /// Logger for this class
    protected static Logger logger = Logger.getLogger(Extractor.class);
    /// Visit counter
    protected long counter;
    /**
     * Counts the number of times a unique visit (i.e. a new hostname string)
     * was encountered.
     */
    protected long distinctCounter;
    /// Counts the number of times handleVisit() is called.
    protected long handledCounter;
    /// Extraction uri
    protected InputSource source;
    /// Maximum number of handler threads.
    protected int maxHandlers = 1;
    /// Current number of handler threads
    protected int currentHandlers = 0;
    /// Dummy object to synchronize Handler threads.
    protected Object sync;
    
    /**
     * Creates a new extractor.
     */
    public Extractor() {
        try {
            parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            tHandler = new VisitXMLHandler(this);
            parser.setContentHandler(tHandler);
            parser.setErrorHandler(tHandler);
            logger.debug("Parser created.");
        } catch (SAXException e) {
            logger.error("SAX parser exception, aborting: " + e.getMessage(), e);
            // System.exit(1);
        }
        sync = new Object();
        prefilters = new Vector();
    }
    
    /**
     * This actually starts the extraction process.
     * @param input The <code>org.xml.sax.InputSource</code> from
     *              which to read the XML data.
     */
    // FIXME: Throws null pointer when file is not of proper format..
    public synchronized void extract(InputSource input) {
        counter = 0;
        distinctCounter = 0;
        handledCounter = 0;
        logger.debug("Extracting.");
        tHandler.reset();
        outStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        outStream.println("<url_list>");
        try {
            parser.parse(input);
        } catch (SAXException e) {
            logger.error("SAX Parser exception: " + e.getMessage(), e);
            outStream.close();
            // System.exit(1);
        } catch (java.io.IOException e) {
            logger.error("Input public Id was: " + input.getPublicId());
            logger.error("Could not open input: " + input + " (" + e.getMessage() + ")" , e);
            outStream.close();
            // System.exit(1);
        }
        logger.debug("Waiting for extract handlers to finish.");
        synchronized (sync) {
            while (currentHandlers > 0) {
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    logger.info("Wait Interrupt");
                }
            }
        }
        outStream.println("</url_list>");
    }
    
    /**
     * Extrats the XML Data from a given URI.
     */
    public synchronized void extract(String uri) {
        InputSource src = new InputSource(uri);
        src.setPublicId(uri);
        extract(src);
    }
    
    /**
     * Adds a prefilter to the Extractor. Prefilters will automatically
     * be applied to each visit by the <code>recieveVisit</code> method.
     */
    public synchronized void addPrefilter(VisitFilter filter) {
        if (filter != null) {
            prefiltering = true;
            prefilters.add(filter);
        }
    }
    
    /**
     * Gets the prefilters.
     */
    public synchronized Vector getPrefilters() {
        return prefilters;
    }
    
    /**
     * Recieves record of visit information from the XMLReader.
     * This does the prefiltering and hands the visit on to
     * the <code>handleVisit</code> method.
     */
    protected void recieveVisit(Visit v) {
        counter++;
        String host = v.getProperty("visit.host");
        boolean accepted = true; // Indicates if the host was accepted by the filters.
        if (!visitHash.containsKey(host)) {
            if (prefiltering) {
                logger.debug("Extractor: Executing prefilters.");
                Enumeration filters = prefilters.elements();
                while (accepted && filters.hasMoreElements()) {
                    VisitFilter cFilter = (VisitFilter) filters.nextElement();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Filtering: Executing filter: " + cFilter.getClass().getName());
                    }
                    accepted = accepted && cFilter.accept(v);
                }
                
            }
            visitHash.put(host, "visited");
            distinctCounter++;
            if (accepted) {
                logger.debug("About to call handler now...");
                handledCounter++;
                // Start a new handler thread.
                synchronized (sync) {
                    while (currentHandlers >= maxHandlers) {
                        try {
                            sync.wait();
                        } catch (InterruptedException e) {
                            logger.info("Wait Interrupt");
                        }
                    }
                    currentHandlers++;
                }
                if (logger.isDebugEnabled()) logger.debug("About to start visit handler thread " + currentHandlers);
                ExtractorRunner r = new ExtractorRunner(v);
                r.start();
                logger.debug("Visit handler thread started.");
            } else {
                if (logger.isDebugEnabled()) logger.debug("Extractor: Some filter rejected the visit. Returning to Handler.");
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("Ignored previously encontered host: " + host);
        }
    }
    
    /**
     * Get the number of visits parsed.
     */
    public synchronized long getCount() {
        return counter;
    }
    
    /**
     * Get the number of distinctive visits handled.
     */
    public synchronized long getDistinctiveCount() {
        return distinctCounter;
    }
    
    /**
     * Get the number of visits processed.
     */
    public synchronized long getHandledCount() {
        return handledCounter;
    }
    
    /**
     * This is the method that child classes should override
     * to handle each visit. This will be called from
     * <code>recieveVisit()</code> if the visit passed the
     * filters.
     *
     * It's up to the child class to use the <code>outStream</code>
     * and the proper methods to write the results.
     */
    protected abstract void handleVisit(Visit v);
    
    /**
     * Runs the extraction as separate thread.
     */
    public void run() {
        if (source != null) {
            extract(source);
        } else {
            logger.error("Error: Tried to run extractor Thread without source.");
        }
        logger.debug("Finalizing Extractor thread.");
        outStream.flush();
        outStream.close();
    }
    
    /**
     * Sets the input source for threaded execution.
     */
    public void setInputSource(InputSource input) {
        this.source = input;
    }
    
    /**
     * Starts extraction in a separate thread.
     */
    public void start() {
        Thread t = new Thread(this);
        logger.debug("About to start Extractor thread.");
        t.start();
        logger.info("Extractor thread started.");
    }
  
    public synchronized int getMaxHandlers() {
        return maxHandlers;
    }
    
    public synchronized void setMaxHandlers(int handlers) {
        if (handlers < 1) handlers = 1;
        maxHandlers = handlers;
    }
    
    protected class ExtractorRunner extends Thread {
        /// Visit handled by the thread
        Visit v;
        public ExtractorRunner(Visit v) {
            this.v = v;
        }
        
        /// Runs a new handler.
        public void run() {
            logger.debug("About to call handleVisit()");
            handleVisit(v);
            synchronized (sync) {
                currentHandlers--;
                sync.notifyAll();
            }
            synchronized (outStream) {
                outStream.flush();
            }
            if (logger.isDebugEnabled()) logger.debug("Thread finalizing: " + currentHandlers);
        }
    }
}
