/*
 * CheckURL.java
 *
 * Created on 10 December 2001, 11:03
 */

package gilbert.extractor;

import java.net.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author  daniel
 * @version 
 */
public class CheckURL {

    /** Creates new CheckURL */
    public CheckURL() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        if (args.length == 0) {
            System.err.println("Please give me an URL");
            System.exit(1);
        } 
        
        URL u = null;
        HttpURLConnection conn = null;
        
        Properties sysProps = System.getProperties();
        sysProps.setProperty("http.proxyHost", "wwwcache.lancs.ac.uk");
        sysProps.setProperty("http.proxyPort", "8080");
        
        try {
            u = new URL(args[0]);
            conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("HEAD");
            System.out.println("Created connection, going to connect");
            conn.connect();
            System.out.println("Connection established.");
            System.out.println("Response Code: " + conn.getResponseCode());
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.err.println(args[0] + " is not a valid URL.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error opening URL: " + e.getMessage());
        } 
        
    }

}
