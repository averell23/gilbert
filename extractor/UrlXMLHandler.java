/*
 * UrlXMLHandler.java
 *
 * Created on 14 December 2001, 16:56
 */

package gilbert.extractor;

import java.util.*;

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
    
    /** Creates new UrlXMLHandler */
    public UrlXMLHandler(Refiner refiner) {
        this.refiner = refiner;
    }
    
    public void startElement(java.lang.String namespaceURI, java.lang.String localName, java.lang.String qName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        // System.out.println("Starting Element: " + localName);
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
        if (localName.equals("url_list")) return;
        Object top = tagStack.pop();
        StringBuffer data = new StringBuffer();
        StringBuffer key = new StringBuffer();
        Util.logMessage("Ending element: " + localName, Util.LOG_DEBUG);
        
        if (!localName.equals("url")) {
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
            Util.logMessage("Tag stack: " + tagStack, Util.LOG_DEBUG);
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
            Util.logMessage("Adding new entry: " + key + "," + data, Util.LOG_DEBUG);
        } else {
            if (!((String) top).equals("url")) {
                throw(new org.xml.sax.SAXException("URL record not read correctly, popped " + top));
            } else {
                refiner.recieveURL(currentUrl);
                currentUrl = null;
            }
        }
        Util.logMessage("*Done*", Util.LOG_DEBUG);
    }
}
