/*
 * RefinerChain.java
 *
 * Created on 18 January 2002, 13:49
 */

package gilbert.extractor;
import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.apache.log4j.*;
import gilbert.io.*;

/**
 * Chain of refiners. The data will be passed through all of the refiners.
 * Each Refiner will be started as a separate thread.
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class RefinerChain {
    /// Vector containing all Refiners that make up this chain
    protected Vector refinerChain;
    /// Output Stream for the chain
    protected PrintStream outputStream;
    /// Input source for the first Refiner
    protected InputSource input;
    /// Logger for this class
    protected Logger logger;
    
    /** Creates new RefinerChain */
    public RefinerChain() {
        refinerChain = new Vector();
        outputStream = System.out;
        logger = Logger.getLogger(this.getClass());
    }
    
    /**
     * Creates a new RefinerChain with given Input.
     */
    public RefinerChain(InputSource input) {
        this();
        this.input = input;
    }
    
    /**
     * Creates a new RefinerChain with given Input.
     */
    public RefinerChain(String uri) {
        this(new InputSource(uri));
    }
    
    /**
     * Starts the refinement process. This will invoke all Refiners in
     * turn, the last Refiner in the chain will write to the chain's
     * output stream.
     */
    public void refine() throws IOException {
        refine(input);
    }
    
    /**
     * Does the actual refining. This method will block until the last
     * Refiner has returned.
     */
    public void refine(InputSource inSrc) throws IOException {
        // Check if we're clear to go
        if (inSrc == null) {
            logger.error("Refiner chain has no input source.");
            return;
        }
        if (refinerChain.size() == 0) {
            logger.error("Refiner chain is empty.");
            return;
        }
        
        
        InputSource tmpInput = inSrc;
        Enumeration refinerE = refinerChain.elements();
        // Set up all refiners
        while (refinerE.hasMoreElements()) {
            Refiner current = (Refiner) refinerE.nextElement();
            logger.info("Preparing Refiner from chain: " + current.getClass().getName());
            current.setInputSource(tmpInput);
            if (refinerE.hasMoreElements()) {
                BufConnectOutputStream outPipe = new BufConnectOutputStream();
                current.setOutputStream(outPipe);
                InputStream inPipe = new BufConnectInputStream(outPipe);
                tmpInput = new InputSource(inPipe);
            } else { // This is the last refiner
                current.setOutputStream(outputStream);
            }
        }
        // Starting the Refiners
        refinerE = refinerChain.elements();
        Refiner.WaitObject wob = null;
        while (refinerE.hasMoreElements()) {
            Refiner current = (Refiner) refinerE.nextElement();
            logger.info("Executing Refiner from chain: " + current.getClass().getName());
            wob = current.getWaitObject();
            current.start();
        }
        logger.debug("Wating for Refiners to finish.");
        synchronized (wob) {
            while (!wob.isFinished()) { // Quick hack to wait for last refiner to finish.
                try {
                    wob.wait();
                } catch (InterruptedException e) { }
            }
        }
        logger.info("RefinerChain exiting.");
    }
    
    /**
     * Sets the output stream for the chain.
     */
    public void setOutputStream(PrintStream out) {
        outputStream = out;
    }
    
    /**
     * Sets the output stream for the chain.
     */
    public void setOutputStream(OutputStream out) {
        setOutputStream(new PrintStream(out));
    }
    
    /**
     * Adds a new Refiner to the chain
     */
    public void addRefiner(Refiner ref) {
        if (ref != null) {
            refinerChain.add(ref);
            logger.info("Added Refiner: " + ref.getClass().getName());
        }
    }
    
    /**
     * Resets the refiner chain.
     */
    public void resetChain() {
        refinerChain = new Vector();
    }
    
    /**
     * Sets the input source.
     */
    public void setInputSource(InputSource input) {
        this.input = input;
    }
    
    /**
     * Sets the input source.
     */
    public void setInputSource(String uri) {
        setInputSource(new InputSource(uri));
    }
}
