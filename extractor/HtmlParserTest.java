/*
 * HtmlParserTest.java
 *
 * Created on 12 December 2001, 10:42
 */

package gilbert.extractor;

import java.net.*;
import java.util.*;
import java.io.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

/**
 *
 * @author  daniel
 * @version 
 */
public class HtmlParserTest {

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        if (args.length == 0) {
            System.out.println("Please give URL for connection.");
            System.exit(1);
        }
        
        String host = args[0];
        URL u = null;
        
        Properties sysProps = System.getProperties();
        sysProps.setProperty("http.proxyHost", "wwwcache.lancs.ac.uk");
        sysProps.setProperty("http.proxyPort", "8080");
        
        try {
            // System.out.print("Trying " + host + "... ");
            u = new URL(host);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 4.0");
            conn.connect();
            System.out.println("Content encoding: " + conn.getContentEncoding());
            System.out.println("Content type: " + conn.getContentType());
            DocumentParser parser = new DocumentParser(DTD.getDTD("HTML"));
            HTMLEditorKit.ParserCallback pc = new HTMLEditorKit.ParserCallback() {
                public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
                    System.out.println("Tag started: " + tag + ", pos " + pos);
                    Enumeration x = a.getAttributeNames();
                    while (x.hasMoreElements()) {
                        // System.out.println(x.nextElement().getClass().getName());
                        Object y = x.nextElement();
                        System.out.println("Attribute: " +  y + " - " + a.getAttribute(HTML.Attribute.NAME));
                    }
                    if ((a.isDefined(HTML.Attribute.NAME) && a.isDefined(HTML.Attribute.CONTENT))
                        && a.getAttribute(HTML.Attribute.NAME).toString().toLowerCase().equals("keywords")) {
                            System.out.println("Keywords: " + a.getAttribute(HTML.Attribute.CONTENT));
                    }
                }
                
                public void handleEndTag(HTML.Tag tag, int pos) {
                    System.out.println("Tag ended: " + tag + ", pos " + pos);
                }
                
                public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
                    System.out.println("Simple Tag: " + tag + ", pos" + pos);
                }
                
                public void handleText(char[] data, int pos) {
                    System.out.println("Text at pos " + pos +": " + new String(data));
                }
                
            };
            parser.parse(new InputStreamReader(conn.getInputStream()), pc, true);
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.err.println(host + " is not a valid URL.");
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        } 
    }
}
