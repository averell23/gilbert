/*
 * UrlXMLHandler.java
 *
 * Created on 14 December 2001, 16:56
 */

package gilbert.extractor;

import java.util.*;
import org.apache.log4j.*;

/**
 * Handles the SAX events for a URL list.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class UrlXMLHandler extends AbstractXMLHandler {
    /// Url currently being worked on.
    VisitorURL currentUrl;
    /// The <code>Refiner</code> this Hanlder reports to.
    Refiner refiner;
    /// Logger for this class
    Logger logger;
    
    /** Creates new UrlXMLHandler */
    public UrlXMLHandler(Refiner refiner) {
        this.refiner = refiner;
        logger = Logger.getLogger(this.getClass());
        logger.debug("Created.");
    }
    
    public void startElement(java.lang.String namespaceURI, java.lang.String localName, java.lang.String qName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        if (logger.isDebugEnabled()) logger.debug("UrlXMLHandler.startElement(): " + localName);
        if (localName.equals("url")) {
            if (currentUrl!= null) {
                throw(new org.xml.sax.SAXException("Wrong format: url not closed before starting new"));
            } else {
                currentUrl = new VisitorURL();
            }
        }
        if (!localName.equals("url_list")) tagStack.push(localName);
    }

    public void endElement(java.lang.String namespaceURI, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
        if (logger.isDebugEnabled()) logger.debug("UrlXMLHandler.endElement(): " + localName);
        if (localName.equals("url_list")) return;
        Object top = tagStack.pop();
        StringBuffer data = new StringBuffer();
        StringBuffer key = new StringBuffer();
        
        if (localName.equals("keyword")) {
            // Handle the "normal" entries
            while (top instanceof StringBuffer) {
                data.append((StringBuffer) top);
                top = tagStack.pop();
            }
            String startTag = (String) top;
            if (!startTag.equals(localName)) {
                throw(new org.xml.sax.SAXException("Tag <" + startTag + "> ended by <" + localName + ">"));
            }
            currentUrl.addKeyword(data.toString());
            if (logger.isDebugEnabled()) logger.debug("Added keyword: " + data);
        } else if (!localName.equals("url")) {
            while (top instanceof StringBuffer) {
                data.append((StringBuffer) top);
                top = tagStack.pop();
            }
            String startTag = (String) top;
            if (!startTag.equals(localName)) {
                throw(new org.xml.sax.SAXException("Tag <" + startTag + "> ended by <" + localName + ">"));
            }
            // Now create the key string for the Properties object
            Enumeration open = tagStack.elements();
            if (logger.isDebugEnabled()) logger.debug("Tag stack: " + tagStack);
            if (open.hasMoreElements()) {
                key.append((String) open.nextElement());
            }
            while (open.hasMoreElements()) {
                key.append('.');
                key.append((String) open.nextElement());
            }
            key.append('.');
            key.append(localName);
            currentUrl.setProperty(key.toString(), data.toString());
            if (logger.isDebugEnabled()) logger.debug("Adding new entry: " + key + "," + data);
        } else {
            if (!((String) top).equals("url")) {
                throw(new org.xml.sax.SAXException("URL record not read correctly, popped " + top));
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Completed URL: " + currentUrl.getProperty("url.name"));
                }
                refiner.recieveURL(currentUrl);
                currentUrl = null;
            }
        }
        logger.debug("*Done*");
    }
    
    /// Resets the handler.
    public void reset() {
        currentUrl = null;
        logger.debug("Reset.");
        super.reset();
    }
}
