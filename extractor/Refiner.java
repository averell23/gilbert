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
        prefilters = new Vector();
        postfilters = new Vector();
        try {
            parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            UrlXMLHandler tHandler = new UrlXMLHandler(this);
            parser.setContentHandler(tHandler);
            parser.setErrorHandler(tHandler);
        } catch (SAXException e) {
            Util.logMessage("*** Extractor aborting due to error: " + e.getMessage(), Util.LOG_ERROR);
            e.printStackTrace();
            // System.exit(1);
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
            Util.logMessage("Added prefilter: " + filter.getClass().getName(), Util.LOG_MESSAGE);
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
            Util.logMessage("Added postfilter: " + filter.getClass().getName(), Util.LOG_MESSAGE);
        }
    }
    
    /**
     * This starts the refining process. This method will only start the
     * parsing of the input stream, but write anything to the output
     * on it's own behalf.
     *
     * @param input The <code>org.xml.sax.InputSource</code> object
     *              from which to read the Input.
     */
    protected void refineBlank(InputSource input) {
        UrlXMLHandler tHandler = new UrlXMLHandler(this);
        parser.setContentHandler(tHandler);
        Util.logMessage("Refiner: Starting generic refine. (Handler initialized)", Util.LOG_MESSAGE);
        Util.logMessage("I am a " + this.getClass().getName(), Util.LOG_MESSAGE);
        try {
            parser.parse(input);
        } catch (SAXException e) {
            Util.logMessage("*** Extractor aborting due to error: " + e.getMessage(), Util.LOG_ERROR);
            e.printStackTrace();
            // System.exit(1);
        } catch (java.io.IOException e) {
            Util.logMessage("*** Error opening location: " + input, Util.LOG_ERROR);
            // System.exit(1);
        }
    }
    
    /**
     * Refines XML data from a given URI.
     */
    protected void refineBlank(String uri) {
        refineBlank(new InputSource(uri));
    }
    
    /**
     * This does the same as <code>refineBlank</code> but inserts the start
     * and end Tags for the <url_list> on the output stream. This is the
     * standard behaviour, but child class may override it.
     */
    public void refine(InputSource input) {
        outStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        startTag("url_list");
        refineBlank(input);
        endTag("url_list");
    }
    
    /**
     * Refine from a given URI.
     */
    public void refine(String uri) {
        refine(new InputSource(uri));
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
                URLFilter nextFilter = (URLFilter) filters.nextElement();
                accepted = accepted && nextFilter.accept(url);
            }
            if (!accepted) return;
        }
        handleURL(url);
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
     * <code>outStream</code> for printing the resutls and to honour
     * the postfilters.
     */
    public abstract void handleURL(VisitorURL url);
    
}
