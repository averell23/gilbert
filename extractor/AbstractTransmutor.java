/*
 * AbstractTransmutor.java
 *
 * Created on 14 December 2001, 16:00
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

/**
 * Abstract Superclass for classes that read from an XML source and write
 * back results to an XML source. 
 *
 * @author  Daniel Hahn 
 * @version CVS $Revision$
 */
public abstract class AbstractTransmutor {
    /// The XML Parser for this instance
    XMLReader parser;
    /// The output <code>PrintStream</code>
    protected PrintStream outStream = System.out;
    
    /** Creates new AbstractTransmutor */
    public AbstractTransmutor() {
    }
    
    /**
     * Prints a XML tag with the data as character data. This will
     * print to the <code>outStream</code>
     * @param name Name of the tag to be printed.
     * @param data Name of the data to be inserted into the tag.
     */
    protected void printTag(String name, String data) {
        outStream.println("<" + name + ">" + data + "</" + name + ">");
    }
    
    /**
     * Prints a starting XML Tag of that name.
     */
    protected void startTag(String name) {
        outStream.println("<" + name + ">");
    }
    
    /**
     * Prints an ending XML Tag of that name.
     */
    protected void endTag(String name) {
        outStream.println("</" + name + ">");
    }
}
