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
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import java.io.*;
import java.util.*;

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
public abstract class Refiner extends AbstractTransmutor {
    /// Vector containing the prefilters 
    protected Vector prefilters;
    /// Indicates if prefiltering is on
    protected boolean prefiltering = false;
    /// Vector containing the postfilters 
    protected Vector postfilters;
    /// Indicates if postfiltering is off
    protected boolean postfiltering = false;
    /** Creates new Refiner */
    
    public Refiner() {
        try {
            parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            UrlXMLHandler tHandler = new UrlXMLHandler(this);
            parser.setContentHandler(tHandler);
            parser.setErrorHandler(tHandler);
        } catch (SAXException e) {
            System.err.println("*** Extractor aborting due to error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /** 
     * Adds a prefilter to the Extractor. Prefilters will automatically
     * be applied to each visit by the <code>recieveVisit</code> method.
     */
    public void addPrefilter(URLFilter filter) {
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
     * This starts the refining process. This method will only start the
     * parsing of the input stream, but write anything to the output
     * on it's own behalf.
     *
     * @param uri The URI of the XML source to read.
     */
    protected void refineBlank(String uri) {
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
    }
    
    /**
     * This does the same as <code>refineBlank</code> but inserts the start
     * and end Tags for the <url_list> on the output stream. This is the
     * standard behaviour, but child class may override it.
     */
    public void refine(String uri) {
        startTag("url_list");
        refineBlank(uri);
        endTag("url_list");
    }
    
    /**
     * This receives a URL object from the XML Handler. This applies
     * the prefiltering and then calls the <code>handleURL</code> method.
     */
    protected void recieveURL(VisitorURL url) {
        if (prefiltering) {
            boolean accepted = true;
            Enumeration filters = prefilters.elements();
            while (filters.hasMoreElements()) {
                accepted = accepted && ((URLFilter) filters.nextElement()).accept(url);
            }
            if (!accepted) return;
        } 
        handleURL(url);
    }
    
    /**
     * This method must be overridden by child classes to handle each of
     * the URLs. 
     * 
     * It's up to the child class to use the proper methods or the 
     * <code>outStream</code> for printing the resutls and to honour
     * the postfilters.
     */
    public abstract void handleURL(VisitorURL url);
    
}
