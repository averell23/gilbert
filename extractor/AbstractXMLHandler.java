/*
 * AbstractXMLHandler.java
 *
 * Created on 14 December 2001, 16:36
 */

package gilbert.extractor;
import java.util.*;
import org.apache.log4j.*;

/**
 * Abstract superclass for all XML Handlers. This just implements some core
 * methods. 
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public abstract class AbstractXMLHandler 
implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {
    /// A stack that holds the currently open tags and data.
    protected Stack tagStack;
    /// The logger for this class
    protected static Logger logger = Logger.getLogger(AbstractXMLHandler.class);
    
    /** Creates new AbstractXMLHandler */
    public AbstractXMLHandler() {
    }

    public void startDocument() throws org.xml.sax.SAXException {
        tagStack = new Stack();
        logger.debug("startDocument()");
    }
    
    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
        if (logger.isDebugEnabled()) logger.debug("characters(): -" + new String(ch, start, length) + "-");
        String chS = new String(ch, start, length);
        if (!chS.trim().equals("")) {
            Object top = tagStack.peek();
            if (top instanceof StringBuffer) {
                StringBuffer topB = (StringBuffer) top;
                topB.append(chS);
                if (logger.isDebugEnabled()) logger.debug("Top String now: " + topB + " <<-");
            } else {
                tagStack.push(new StringBuffer(chS));
            }
        }
    }
    
    public void ignorableWhitespace(char[] values, int param, int param2) throws org.xml.sax.SAXException {
    }
    
    public void processingInstruction(java.lang.String str, java.lang.String str1) throws org.xml.sax.SAXException {
    }
    
    public void startPrefixMapping(java.lang.String str, java.lang.String str1) throws org.xml.sax.SAXException {
    }
    
    public void endDocument() throws org.xml.sax.SAXException {
        logger.debug("endDocument()");
        // Check for sanity
        if (!tagStack.empty()) {
            logger.error("*** Warning: Stack not empty at end of document!");
            logger.error(tagStack);
        }
    }
    
    public void skippedEntity(java.lang.String str) throws org.xml.sax.SAXException {
    }
    
    public void setDocumentLocator(org.xml.sax.Locator locator) {
    }
   
    public void endPrefixMapping(java.lang.String str) throws org.xml.sax.SAXException {
    }
    
    public void startElement(java.lang.String str, java.lang.String str1, java.lang.String str2, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
    }
    
    public void endElement(java.lang.String str, java.lang.String str1, java.lang.String str2) throws org.xml.sax.SAXException {
    }
    
    public void warning(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        logger.warn("XML Warning in line: " + e.getLineNumber());
        logger.warn("Exception was: " + e.getMessage());
    }
    
    public void error(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        logger.error("XML error (recoverable) in line: " + e.getLineNumber());
        logger.error("Exception was: " + e.getMessage());
    }
    
    public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        logger.error("XML fatal error in line: " + e.getLineNumber(), e);
        throw(new org.xml.sax.SAXException(e.getMessage()));
    }
    
    /**
     * Resets the Handler.
     */
    public void reset() {
        tagStack = new Stack();
        logger.debug("Reset.");
    }
}
