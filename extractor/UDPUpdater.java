/*
 * UDPUpdater.java
 *
 * Created on 31 January 2002, 17:00
 */

package gilbert.extractor;

import java.util.*;
import java.io.*;
import java.net.*;
import org.xml.sax.*;
import gilbert.extractor.*;
import gilbert.extractor.extractors.*;
import gilbert.extractor.refiners.*;
import gilbert.extractor.filters.*;
import org.apache.log4j.*;

/**
 * Updates Albrecht's VB client by sending UDP packets with the 
 * URLs in them.
 *
 * @author Daniel Hahn
 * @version CVS $REVISION$
 */
public class UDPUpdater {
    /// Vector with pages that have been found previously
    protected static Vector currentSet;
    /// Fallback URL
    protected static VisitorURL fallbackUrl;
    /// URL for the data source.
    protected static String dataSource = "http://ubicomp.lancs.ac.uk/~dhahn/cgi-bin/weblog.pl";
    /// Extraction chain for the extraction
    protected static ExtractingChain extractor;
    /// VectorRefiner for the results
    protected static VectorRefiner endRef;
    /// Name of the log file
    protected static String logfileName = "udp.log";
    /// Name of the host that runs the VB client
    protected static String host = "localhost";
    /// Name of the port that the VB client uses
    protected static int port = 2345;
    /// The socket which handles the communication
    protected static DatagramSocket dSock;
    /// Verbose logging flag
    protected static boolean verbose = false;
    /// Update delay in seconds
    protected static int delay = 5;
    /// Logger
    protected static Logger logger;

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        Logger logger = Logger.getLogger(UDPUpdater.class);
        PropertyConfigurator.configure("gilbert/extractor/log4j.properties");
        
        UDPUpdater waitObject = new UDPUpdater(); // I know it's a kludge but we need an object to wait on. sorry.
        parseCommandLine(args);
        if (verbose) {
            System.out.println("Parameters:");
            System.out.println("  Destination host: " + host);
            System.out.println("  Destination port: " + port);
            System.out.println("  Update delay....: " + delay);
            System.out.println();
        }
        System.out.println("UDP Update Server starting up...");
        init();
        System.out.println("Internal objects initialized...");
        setupCom();
        System.out.println("Communication socket initialized...");
        System.out.println("Starting update process.");
        while (true) {
            logger.info("UDPUpdate: Entering update loop.");
            try {
                endRef.reset();
                extractor.extract();
                Vector urls = endRef.getUrlList();
                Enumeration urlsL = urls.elements();
                while (urlsL.hasMoreElements()) {
                    String current = ((VisitorURL) urlsL.nextElement()).getProperty("url.name");
                    byte[] data = current.getBytes();
                    DatagramPacket datagram = new DatagramPacket(data, data.length);
                    dSock.send(datagram);
                    waitObject.waitTime(5000);
                    logger.info("Sent Data: " + current);
                }
            } catch (IOException e) {
                System.out.println("* Warning: Uncaught IOException: " + e.getMessage());
            }
            waitObject.waitTime(5000);
        }
    }
    
    /**
     * Yeah, that's part of the kludge too. Sue me.
     */
    synchronized void waitTime(long time) {
        try {
            wait(time);
        } catch (InterruptedException e) { }
    }
    
    /**
     * Initializes the communication.
     */
    static void setupCom() {
        try {
            dSock = new DatagramSocket();
            InetAddress ia = InetAddress.getByName(host);
            dSock.connect(ia, port);
        } catch (SocketException e) {
            System.out.println("*** Error creating socket: " + e.getMessage());
            System.out.println("*** Aborting.");
            System.exit(1);
        } catch (UnknownHostException e) {
            System.out.println("*** Could not resolve hostname: " + host);
            System.out.println("*** Aborting.");
            System.exit(1);
        }
    }
    
    /**
     * Initializes the inernal objects.
     */
    static void init() {
        // FIXME: All  below should be configurable via the command line
        currentSet = new Vector();
        fallbackUrl = new VisitorURL();
        fallbackUrl.setProperty("url.name", "nothing.html");
        currentSet.add(fallbackUrl);
        extractor = new ExtractingChain(dataSource);
        StraightExtractor ext = new StraightExtractor();
        ext.addPrefilter(new LocalVisitFilter());
        ext.addPrefilter(new AgentVisitFilter());
        extractor.setExtractor(ext);
        SearchingRefiner sRef = new SearchingRefiner();
        sRef.setKeywords("ubicomp,handheld,context");
        sRef.setPassing(true);
        extractor.addRefiner(sRef);
        Refiner meta = new MetaRefiner();
        DocumentTypeURLFilter docFilter = new DocumentTypeURLFilter();
        docFilter.addDocumentType("text/html");
        meta.addPrefilter(docFilter);
        extractor.addRefiner(meta);
        endRef = new VectorRefiner();
        extractor.addRefiner(endRef);
        // create a dummy output for the last refiner
        OutputStream dummy = new OutputStream() { public void write(int b) {} };
        endRef.setOutputStream(dummy);
    }
    
    static void parseCommandLine(String[] args) {
        for (int i=0 ; i < args.length ; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].equals("-help")) {
                    System.out.println("UDP Updater Tool V0.1");
                    System.out.println("Usage: UDPUpdater [options] [host]");
                    System.out.println();
                    System.out.println("Where options are:");
                    System.out.println("  -datasource <src uri> - URI of the data source.");
                    System.out.println("  -port <port>          - Port to connect to (" + port + ")");
                    System.out.println("  -delay <update delay> - Minimum delay between updates in sec. (" + delay + ")");
                    System.out.println("  -help                 - Print this message.");
                    System.out.println();
                    System.out.println("Logging can be configured through the log4.properties.");
                    System.out.println();
                    System.exit(0);
                } else if (args[i].equals("-datasource")) {
                    i++;
                    if ((i < args.length) && !args[i].startsWith("-")) {
                        dataSource = args[i];
                    } else {
                        System.out.println("*** -datasource must be followed by URI");
                        System.exit(1);
                    }
                } else if (args[i].equals("-port")) {
                    i++;
                    if ((i < args.length) && !args[i].startsWith("-")) {
                        try {
                         port = Integer.parseInt(args[i]);
                        } catch (NumberFormatException e) {
                            System.out.println("*** Not a proper (port) number: " + args[i]);
                            System.exit(1);
                        }
                    } else {
                        System.out.println("*** -port must be followed by port number");
                        System.exit(1);
                    }
                } else if (args[i].equals("-delay")) {
                    i++;
                    if ((i < args.length) && !args[i].startsWith("-")) {
                        try {
                            delay = Integer.parseInt(args[i]);
                        } catch (NumberFormatException e) {
                            System.out.println("*** Not a proper number: " + args[i]);
                            System.exit(1);
                        }
                    } else {
                        System.out.println("*** -delay must be followed by delay interval");
                        System.exit(1);
                    }
                } else {
                    System.out.println("*** Unknown option: " + args[i]);
                    System.exit(1);
                }
            } else {
                host = args[i];
            }
        }
    }
    
}
