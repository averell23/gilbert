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


/**
 * This is the superclass for an URL extractor. It takes an XML source
 * with visit information and tries to find related URLs. The found URLs
 * should then be printed as an XML URL list. (The mode of output is not 
 * enforced by this class, though.)
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public abstract class Extractor extends AbstractTransmutor {
    /// Vector containing the prefilters (visit filters)
    protected Vector prefilters;
    /// Indicates if prefiltering is on
    protected boolean prefiltering = false;
    /// Vector containing the postfilters (url filters)
    protected Vector postfilters;
    /// Indicates if postfiltering is off
    protected boolean postfiltering = false;
    
    /**
     * Creates a new extractor.
     */
    public Extractor() {
        try {
            parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            VisitXMLHandler tHandler = new VisitXMLHandler(this);
            parser.setContentHandler(tHandler);
            parser.setErrorHandler(tHandler);
        } catch (SAXException e) {
            System.err.println("*** Extractor aborting due to error: " + e.getMessage());
            e.printStackTrace();
            // System.exit(1);
        }
        prefilters = new Vector();
        postfilters = new Vector();
    }
    
    /**
     * This actually starts the extraction process.
     * @param input The <code>org.xml.sax.InputSource</code> from
     *              which to read the XML data.
     */
    // FIXME: Throws null pointer when file is not of proper format..
    public void extract(InputSource input) {
        VisitXMLHandler tHandler = new VisitXMLHandler(this);
        parser.setContentHandler(tHandler);
        Util.logMessage("Extractor: Parser re-initialized.", Util.LOG_DEBUG);
        outStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        outStream.println("<url_list>");
        try {
            parser.parse(input);
        } catch (SAXException e) {
            Util.logMessage("*** Extractor aborting due to error: " + e.getMessage(), Util.LOG_ERROR);
            e.printStackTrace();
            // System.exit(1);
        } catch (java.io.IOException e) {
            Util.logMessage("*** Error opening location: " + input, Util.LOG_ERROR);
            Util.logMessage("*** Input public Id was: " + input.getPublicId(), Util.LOG_ERROR);
	    e.printStackTrace();
            // System.exit(1);
        }
        outStream.println("</url_list>");
    }
    
    /** 
     * Extrats the XML Data from a given URI.
     */
    public void extract(String uri) {
        InputSource src = new InputSource(uri);
	src.setPublicId(uri);
        extract(src);
    }

    /** 
     * Adds a prefilter to the Extractor. Prefilters will automatically
     * be applied to each visit by the <code>recieveVisit</code> method.
     */
    public void addPrefilter(VisitFilter filter) {
        if (filter != null) {
            prefiltering = true;
            prefilters.add(filter);
        }
    }
    
    /** 
     * Adds a postfilter to the Extractor. Postfilters should be honoured
     * by the child classes, but this may not always be the case.
     */
    public void addPostfilter(URLFilter filter) {
        if (filter != null) {
            postfiltering = true;
            postfilters.add(filter);
        }
    }
    
    /**
     * Recieves record of visit information from the XMLReader.
     * This does the prefiltering and hands the visit on to
     * the <code>handleVisit</code> method.
     */
    protected void recieveVisit(Visit v) {
        if (prefiltering) {
            Util.logMessage("Extractor: Executing prefilters.", Util.LOG_DEBUG);
            boolean accepted = true;
            Enumeration filters = prefilters.elements();
            while (filters.hasMoreElements()) {
                VisitFilter cFilter = (VisitFilter) filters.nextElement();
                Util.logMessage("Filtering: Executing filter: " + cFilter.getClass().getName(), Util.LOG_DEBUG);
                accepted = accepted && cFilter.accept(v);
            }
            if (!accepted) {
                Util.logMessage("Extractor: Some filter rejected the visit. Returning to Handler.", Util.LOG_DEBUG);
                return;
            }
        } 
        handleVisit(v);
    }
 
    /** 
     * This is the method that child classes should override
     * to handle each visit. This will be called from
     * <code>recieveVisit()</code> if the visit passed the
     * filters.
     *
     * It's up to the child class to use the <code>outStream</code>
     * and the proper methods to write the results and to honor the
     * postfilters.
     */
    protected abstract void handleVisit(Visit v);
}
