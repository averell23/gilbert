/*
 * AbstractXMLHandler.java
 *
 * Created on 14 December 2001, 16:36
 */

package gilbert.extractor;
import java.util.*;

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
    
    /** Creates new AbstractXMLHandler */
    public AbstractXMLHandler() {
    }

    public void startDocument() throws org.xml.sax.SAXException {
        tagStack = new Stack();
        Util.logMessage("*XMLHandler.startDocument()", Util.LOG_DEBUG);
    }
    
    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
        Util.logMessage("*XMLHandler.characters: -" + new String(ch, start, length) + "-", Util.LOG_DEBUG);
        String chS = new String(ch, start, length);
        if (!chS.trim().equals("")) {
            tagStack.push(new StringBuffer(chS));
        }
    }
    
    public void ignorableWhitespace(char[] values, int param, int param2) throws org.xml.sax.SAXException {
    }
    
    public void processingInstruction(java.lang.String str, java.lang.String str1) throws org.xml.sax.SAXException {
    }
    
    public void startPrefixMapping(java.lang.String str, java.lang.String str1) throws org.xml.sax.SAXException {
    }
    
    public void endDocument() throws org.xml.sax.SAXException {
        Util.logMessage("*XMLHandler.endDocument()", Util.LOG_DEBUG);
        // Check for sanity
        if (!tagStack.empty()) {
            System.err.println("*** Warning: Stack not empty at end of document!");
            System.err.println(tagStack);
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
        Util.logMessage("XML Warning in line: " + e.getLineNumber(), Util.LOG_WARN);
        Util.logMessage("Exception was: " + e.getMessage(), Util.LOG_WARN);
    }
    
    public void error(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        Util.logMessage("XML error (recoverable) in line: " + e.getLineNumber(), Util.LOG_ERROR);
        Util.logMessage("Exception was: " + e.getMessage(), Util.LOG_ERROR);
    }
    
    public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        Util.logMessage("XML fatal error in line: " + e.getLineNumber(), Util.LOG_ERROR);
        e.printStackTrace();
        throw(new org.xml.sax.SAXException(e.getMessage()));
    }
}
