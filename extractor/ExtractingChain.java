/*
 * ExtractingChain.java
 *
 * Created on 18 January 2002, 13:27
 */

package gilbert.extractor;
import org.xml.sax.*;
import java.util.*;
import java.io.*;
import gilbert.io.*;
import org.apache.log4j.*;

/**
 * A chain of an <code>Extractor</code> and multiple <code>Refiner</code>s. 
 * The chain will read from the original input source, and the results will
 * be passed to the entire chain. The last Refiner in the chain will print
 * to the chain's output stream.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class ExtractingChain extends RefinerChain implements Runnable {
    /// The Extractor that starts the chain
    protected Extractor extractor;
    /// The logger for this class
    
    /**
     * Creates new ExtractingChain.
     */
    public ExtractingChain() {
        super();
        logger = Logger.getLogger(this.getClass());
    }
    
    /**
     * Creates new ExtractingChain with given input.
     */
    public ExtractingChain(InputSource input) {
        super(input);
        logger = Logger.getLogger(this.getClass());
    }
    
    /**
     * Creates new ExtractingChain with given input
     */
    public ExtractingChain(String uri) {
        super(new InputSource(uri));
        logger = Logger.getLogger(this.getClass());
    }
    
    /**
     * Creates new ExtractingChain with given input and
     * initial extractor.
     */
    public ExtractingChain(String uri, Extractor extractor) {
        super(new InputSource(uri));
        this.extractor = extractor;
        logger = Logger.getLogger(this.getClass());
    }
    
    /**
     * Starts the extraction process. This will call the Extractor and
     * all Refiners in the chain, and print the result on the 
     * chain's output stream.
     */
    public void extract() throws IOException {
        if (extractor == null) {
            logger.error("Cannot start extraction chain: No input source");
            return;
        }
        BufConnectOutputStream pipeOut = new BufConnectOutputStream();
        extractor.setOutputStream(pipeOut);
        extractor.setInputSource(input);
        InputStream inPipe = new BufConnectInputStream(pipeOut);
        logger.debug("About to start extractor thread.");
        extractor.start();
        logger.debug("Extractor thread started.");
        refine(new InputSource(inPipe));
    }
    
    
    /**
     * Starts an extraction in a separate thread.
     */
    public void start() {
        Thread t = new Thread(this);
        t.start();
    }
    
    /**
     * Set the Extractor for this chain.
     */
    public void setExtractor(Extractor extractor) {
        this.extractor = extractor;
    }
    
    public void run() {
        try {
            extract();
        } catch (IOException e) {
            logger.error("IO Exception in extractor thread.", e);
        }
    }
    
}
