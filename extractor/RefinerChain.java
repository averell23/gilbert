/*
 * RefinerChain.java
 *
 * Created on 18 January 2002, 13:49
 */

package gilbert.extractor;
import java.util.*;
import java.io.*;
import org.xml.sax.*;

/**
 * Chain of refiners. The data will be passed through all of the refiners.
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

    /** Creates new RefinerChain */
    public RefinerChain() {
        refinerChain = new Vector();
        outputStream = System.out;
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
     * Does the actual refining.
     */
    public void refine(InputSource inSrc) throws IOException {
        // Check if we're clear to go
        if (inSrc == null) {
            Util.logMessage("Error: Refiner chain has no input source.", Util.LOG_ERROR);
            return;
        }
        if (refinerChain.size() == 0) {
            Util.logMessage("Error: Refiner chain is empty.", Util.LOG_ERROR);
            return;
        }
        
        /* These will buffer the results of each step. It may not 
         * be effective for large numbers of Refiners, but it should
         * be working safely. We need two Streams, since each
         * Refiner will read from one ByteArray and write 
         * to another one.
         */
        ByteArrayOutputStream standIn = new ByteArrayOutputStream();
        ByteArrayOutputStream standOut = new ByteArrayOutputStream();
        
        InputSource tmpInput = inSrc;
        Enumeration refinerE = refinerChain.elements();
        while (refinerE.hasMoreElements()) {
            // standIn has the array the refiner will read from
            // standOut has the array the refiner will write to
            // in the first round we read from input instead
            standOut.reset();            // resets the output buffer
            Refiner current = (Refiner) refinerE.nextElement();
            Util.logMessage("Starting Refiner from chain: " + current.getClass().getName(), Util.LOG_MESSAGE);
            current.setOutputStream(standOut); // write to the output buffer
            current.refine(tmpInput); // read from the current input
            standOut.flush();
            ByteArrayInputStream tmpIS = new ByteArrayInputStream(standOut.toByteArray());
            tmpInput = new InputSource(tmpIS); // this will be the input for next round
            // Swap the buffers
            ByteArrayOutputStream tmp = standOut;
            standOut = standIn;
            standIn = tmp;
            // The output of the last round is now in standIn
        }
        // Dump the results to the output Stream
        // FIXME: The last Refiner could write to the output directly for efficiency
        standIn.writeTo(outputStream);
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
            Util.logMessage("Added Refiner: " + ref.getClass().getName(), Util.LOG_MESSAGE);
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
