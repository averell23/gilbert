/*
 * Refiner.java
 *
 * Created on 14 December 2001, 13:41
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
import org.apache.log4j.*;

/**
 * This refines an existing URL list. It takes URLs from a XML URL list,
 * and should write a new list of that kind to the output (this method of
 * output is not enforced). Subclasses may do either:
 * <ul>
 * <li>Insert new URLs into the list</li>
 * <li>Delete URL from the list</li>
 * <li>Insert weights for URLs, or change existing weights</li>
 * <li>Any combination of the above</li>
 * </ul>
 * The only restriction is that subclasses should honour existing weights
 * for URLs in the input data whenever applicable.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public abstract class Refiner extends AbstractTransmutor implements Runnable {
    /// Vector containing the prefilters
    protected Vector prefilters;
    /// Indicates if prefiltering is on
    protected boolean prefiltering = false;
    /// Maximum degree of relationship for incoming URLs. (0 is unbounded).
    protected int maxDegree = 0;
    /// Hash for the URLs already visited
    protected Hashtable visitCache;
    /// Url XML Handler
    protected UrlXMLHandler tHandler;
    /// logger for this class.
    protected static Logger logger = Logger.getLogger(Refiner.class);
    /// Input
    protected InputSource input;
    /// Maximum number of handler threads.
    protected int maxHandlers = 1;
    /// Current number of handler threads
    protected int currentHandlers = 0;
    /// Dummy object to synchronize Handler threads.
    protected Object sync;
    /// Object for Refiner Chain waiting object hack
    protected WaitObject wob;
    
    /*
     * If the refiner passes original the input URLs on to the output, this
     * should be true. Most Refiners <i>should</i> pass the original
     * input to the output, but there may be cases where this is not
     * desirable. Each subclass of should set this to a sensible
     * default and <b>only</b> offer a setter method when it is
     * sensible for the user to change that default.<br/>
     *
     * Changes to the original URL will be passed on as well.
     */
    protected boolean passing = false;
    
    /** Creates new Refiner */
    public Refiner() {
        visitCache = new Hashtable();
        prefilters = new Vector();
        sync = new Object();
        wob = new WaitObject();
        try {
            logger.debug("Creating parser.");
            parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            tHandler = new UrlXMLHandler(this);
            parser.setContentHandler(tHandler);
            parser.setErrorHandler(tHandler);
            logger.debug("Parser created and set.");
        } catch (SAXException e) {
            logger.error("SAX parsing error, aborting refine: " + e.getMessage(), e);
            outStream.close();
            // System.exit(1);
        }
    }
    
    /**
     * Sets the maximum degree of relationship (relative to the original URL).
     * @param degree The maximum degree of relationship. A value of 0 will
     *               disable this feature, negative values will be ignored.
     */
    public synchronized void setMaxDegree(int degree) {
        if (degree >= 0) {
            maxDegree = degree;
        }
    }
    
    /// Get the wait object.
    public WaitObject getWaitObject() {
        return wob;
    }
    
    /**
     * Returns the maximum level of relationship allowed for this refiner.
     */
    public int getMaxDegree() {
        return maxDegree;
    }
    
    public synchronized int getMaxHandlers() {
        return maxHandlers;
    }
    
    public synchronized void setMaxHandlers(int handlers) {
        if (handlers < 1) handlers = 1;
        maxHandlers = handlers;
    }
    
    /**
     * Adds a prefilter to the Extractor. Prefilters will automatically
     * be applied to each visit by the <code>recieveVisit</code> method.
     */
    public synchronized void addPrefilter(URLFilter filter) {
        if (filter != null) {
            prefiltering = true;
            prefilters.add(filter);
            logger.info("Added prefilter: " + filter.getClass().getName());
        }
    }
    
    /**
     * Gets the prefilters.
     */
    public Vector getPrefilters() {
        return prefilters;
    }
    
    /**
     * This starts the refining process. This method will only start the
     * parsing of the input stream, but write anything to the output
     * on it's own behalf.
     *
     * @param input The <code>org.xml.sax.InputSource</code> object
     *              from which to read the Input.
     */
    protected synchronized void refineBlank(InputSource input) {
        synchronized (wob) {
            visitCache = new Hashtable();
            tHandler.reset();
            if (logger.isInfoEnabled()) {
                logger.info("Refining.");
                logger.info("I am a " + this.getClass().getName());
                logger.debug("Input is: " + input);
            }
            try {
                parser.parse(input);
            } catch (SAXException e) {
                logger.error("SAX Parser error, aborting: " + e.getMessage(), e);
                // System.exit(1);
            } catch (java.io.IOException e) {
                logger.error("Could not open location: " + input, e);
                // System.exit(1);
            }
            logger.debug("Waiting for handlers to finish.");
            synchronized (sync) {
                while (currentHandlers > 0) {
                    try {
                        sync.wait();
                    } catch (InterruptedException e) {
                        logger.info("Wait Interrupt");
                    }
                }
            }
            wob.finish();
            wob.notifyAll();
            logger.debug("Finalizing Refiner Thread.");
        }
    }
    
    /**
     * Refines XML data from a given URI.
     */
    protected synchronized void refineBlank(String uri) {
        refineBlank(new InputSource(uri));
    }
    
    /**
     * This does the same as <code>refineBlank</code> but inserts the start
     * and end Tags for the <url_list> on the output stream. This is the
     * standard behaviour, but child class may override it. This will only
     * called once for each URL, subsequent occurences of the same URL will
     * be ignored.
     */
    public synchronized void refine(InputSource input) {
        outStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        startTag("url_list");
        refineBlank(input);
        endTag("url_list");
    }
    
    /**
     * Refine from a given URI.
     */
    public synchronized void refine(String uri) {
        refine(new InputSource(uri));
    }
    
    
    /**
     * This receives a URL object from the XML Handler. This applies
     * the prefiltering and then calls the <code>handleURL</code> method.
     */
    protected void recieveURL(VisitorURL url) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handling URL: " + url);
            logger.debug("URL name: " + url.getProperty("url.name"));
        }
        int degree = 0;
        String uName = url.getProperty("url.name");
        if (!visitCache.containsKey(uName)) {
            visitCache.put(uName, url);
            try {
                degree = Integer.valueOf(url.getProperty("url.degree")).intValue();
            } catch (NumberFormatException e) {
                logger.error("Could not determine degree: " + e.getMessage());
            }
            
            boolean accepted = true;
            if ((maxDegree == 0) || (degree <= maxDegree)) {
                if (prefiltering) {
                    Enumeration filters = prefilters.elements();
                    while (accepted && filters.hasMoreElements()) {
                        URLFilter nextFilter = (URLFilter) filters.nextElement();
                        accepted = accepted && nextFilter.accept(url);
                    }
                }
            } else {
                accepted = false;
                logger.info("Refiner: URL with degree " + degree + " dropped, max. degree was " + maxDegree);
            }
            if (accepted) {
                logger.debug("About to start next handler thread.");
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
                if (logger.isDebugEnabled()) logger.debug("About to start handler thread. " + currentHandlers);
                RefinerRunner r = new RefinerRunner(url);
                r.start();
                logger.debug("Handler thread started.");
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Refiner: Ignored duplicate url " + uName);
        }
    }
    
    /**
     * Prints the given URL back to the output stream. This may be less
     * efficient than directly printing the information, however it has
     * the advantage that all URL information is retained.<br>
     * <b>Note:</b> For quicker writing it is assumed that the URL
     * object has no "nested elements". (The only exception to this
     * are the URLs keywords, which are handled specially..)
     */
    protected void printURL(VisitorURL vUrl) {
        Enumeration keys = vUrl.propertyNames();
        startTag("url");
        while (keys.hasMoreElements()) {
            String curKey = (String) keys.nextElement();
            String[] splitKey = curKey.split("\\.");
            // NOTE AGAIN: We will look only at the second element
            // NO NESTED ELEMENTS WILL BE PRINTED!
            if (splitKey.length == 2) {
                printTag(splitKey[1], vUrl.getProperty(curKey));
            }
        }
        // Print the keywords
        Enumeration keyList = vUrl.getKeywords().elements();
        while (keyList.hasMoreElements()) {
            printTag("keyword", (String) keyList.nextElement());
        }
        endTag("url");
    }
    
    /**
     * Must be overridden by child classes to handle each of
     * the URLs.
     *
     * It's up to the child class to use the proper methods or the
     * <code>outStream</code> for printing the resutls.
     */
    public abstract void handleURL(VisitorURL url);
    
    
    /**
     * Returns true if the Refiner passes the input URLs to the
     * output.
     */
    public boolean isPassing() {
        return passing;
    }
    
    /** Runs the refiner thread */
    public void run() {
        if (input != null) {
            refine(input);
        } else {
            logger.error("Tried to run refiner without input.");
        }
        logger.debug("Finalizing Refiner thread.");
        outStream.flush();
        outStream.close();
    }
    
    
    /**
     * Set the input source for threaded execution.
     */
    public void setInputSource(InputSource input) {
        this.input = input;
    }
    
    /** Starts reifining in a separate thread */
    public void start() {
        logger.debug("About to start refiner thread.");
        Thread t = new Thread(this);
        t.start();
        logger.info("Started refiner thread.");
    }
    
    protected class RefinerRunner extends Thread {
        /// Visit handled by the thread
        VisitorURL url;
        public RefinerRunner(VisitorURL url) {
            this.url = url;
        }
        
        /// Runs a new handler.
        public void run() {
            logger.debug("About to call handleURL()");
            handleURL(url);
            logger.debug("Handle URL finished.");
            if (passing) {
                logger.debug("Passing URL now.");
                synchronized (outStream) {
                    printURL(url);
                }
            } else {
                logger.debug("Refiner: URL not passed, passing disabled.");
            }
            synchronized (sync) {
                currentHandlers--;
                sync.notifyAll();
            }
            if (logger.isDebugEnabled()) logger.debug("Thread finalizing: " + currentHandlers);
            synchronized (outStream) {
                outStream.flush();
            }
        }
    }
    
    // For the waiting object hack
    protected class WaitObject {
        boolean finished = false;
        
        public void finish() {
            finished = true;
        }
        
        public boolean isFinished() {
            return finished;
        }
    }
    
}
