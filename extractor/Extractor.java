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
            System.exit(1);
        }
        prefilters = new Vector();
        postfilters = new Vector();
    }
    
    /**
     * This actually starts the extraction process.
     * @param uri The URI of the XML source to read.
     */
    public void extract(String uri) {
        outStream.println("<url_list>");
        try {
            parser.parse(uri);
        } catch (SAXException e) {
            System.err.println("*** Extractor aborting due to error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (java.io.IOException e) {
            System.err.println("*** Error opening location: " + uri);
            System.exit(1);
        }
        outStream.println("</url_list>");
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
            boolean accepted = true;
            Enumeration filters = prefilters.elements();
            while (filters.hasMoreElements()) {
                accepted = accepted && ((VisitFilter) filters.nextElement()).accept(v);
            }
            if (!accepted) return;
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
