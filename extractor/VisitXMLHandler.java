/*
 * VisitXMLHandler.java
 *
 * Created on 05 December 2001, 15:58
 */

package gilbert.extractor;

import java.util.*;

/**
 * This is the XML Content Handler to read visit records from the XML source. It
 * will then pass <code>Visit</code> objects on to the <code>Extractor</code> that
 * called it.
 *
 * @author  daniel
 * @version
 */
public class VisitXMLHandler extends AbstractXMLHandler {
    /// The extractor this Handler delivers elements to
    protected Extractor extractor;
    /// The visit that is currently build
    private Visit currentVisit;
    
    /**
     * Creates new VisitXMLHandler.
     * @param extractor The Extractor that recieves the assebled <code>Visit</code>
     *                  objects.
     */
    protected VisitXMLHandler(Extractor extractor) {
        this.extractor = extractor;
    }
    
    public void startElement(java.lang.String namespaceURI, java.lang.String localName, java.lang.String qName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        // System.out.println("Starting Element: " + localName);
        if (localName.equals("visit")) {
            if (currentVisit != null) {
                throw(new org.xml.sax.SAXException("Wrong format: Visit not closed before starting new"));
            } else {
                currentVisit = new Visit();
            }
        }
        if (!localName.equals("visitlist")) tagStack.push(localName);
    }
    
    public void endElement(java.lang.String namespaceURI, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
        if (localName.equals("visitlist")) return;
        Object top = tagStack.pop();
        StringBuffer data = new StringBuffer();
        StringBuffer key = new StringBuffer();
        Util.logMessage("Ending element: " + localName, Util.LOG_DEBUG);
        
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
            currentVisit.addKeyword(data.toString());
            Util.logMessage("Added keyword: " + data, Util.LOG_DEBUG);
        } else if (localName.equals("visit")) {
            if (!((String) top).equals("visit")) {
                throw(new org.xml.sax.SAXException("Visit record not read correctly, popped " + top));
            } else {
                extractor.recieveVisit(currentVisit);
                currentVisit = null;
            }
        } else {
            // Handle the "normal" entries
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
            if (localName.equals("class")) {
                key.append('.');
                key.append(data);
                currentVisit.setProperty(key.toString(), "true");
                Util.logMessage("Adding new class: " + key, Util.LOG_DEBUG);
            } else {
                currentVisit.setProperty(key.toString(), data.toString());
                Util.logMessage("Adding new entry: " + key + "," + data, Util.LOG_DEBUG);
            }
        }
        Util.logMessage("*Done*", Util.LOG_DEBUG);
    }
    
}
