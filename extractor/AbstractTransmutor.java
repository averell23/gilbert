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
    
    public void setOutputStream(OutputStream outStream) {
        this.outStream = new PrintStream(outStream);
    }
    
    /** Returns the output stream of this Transmutor */
    public PrintStream getOutputStream() {
        return outStream;
    }
    
    
    /**
     * Prints a XML tag with the data as character data. This will
     * print to the <code>outStream</code>
     * @param name Name of the tag to be printed.
     * @param data Name of the data to be inserted into the tag.
     */
    protected void printTag(String name, String data) {
        outStream.print("<" + name + ">");
        normalizeAndPrint(data);
        outStream.println("</" + name + ">");
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
    
    /** Normalizes and prints the given string. */
    protected void normalizeAndPrint(String s) {
        
        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            normalizeAndPrint(c);
        }
        
    } // normalizeAndPrint(String)
    
    /** Normalizes and prints the given array of characters. */
    protected void normalizeAndPrint(char[] ch, int offset, int length) {
        for (int i = 0; i < length; i++) {
            normalizeAndPrint(ch[offset + i]);
        }
    } // normalizeAndPrint(char[],int,int)
    
    /** Normalizes and print the given character. */
    protected void normalizeAndPrint(char c) {
        // Try and squash illegal control characters and such
        // (e.g. japanese encoding)
        if (c > 255) return;
        if ((c >= 128) && (c <= 159)) return;
        // Try to escape others 
        if (c > 159) {
            int cVal = c;
            outStream.print("&#" + cVal + ";");
            return;
        }
        
        switch (c) {
            case '<': {
                outStream.print("&lt;");
                break;
            }
            case '>': {
                outStream.print("&gt;");
                break;
            }
            case '&': {
                outStream.print("&amp;");
                break;
            }
            case '"': {
                outStream.print("&quot;");
                break;
            }
            case '?': {
                outStream.print("&#63;");
                break;
            }
            case '\r':
            case '\n': {
                // we shall always print this the canonical way...
                outStream.print("&#");
                outStream.print(Integer.toString(c));
                outStream.print(';');
                break;
                // else, default print char
            }
            default: {
                outStream.print(c);
            }
        }
        
    } // normalizeAndPrint(char)
    
   public String toString() {
       return this.getClass().getName();
   }
   
}
