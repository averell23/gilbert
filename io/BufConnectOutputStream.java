/*
 * BufConnectOutputStream.java
 *
 * Created on 28 March 2002, 10:49
 */

package gilbert.io;
import org.apache.log4j.*;

/**
 * Output Stream can be connected to an BufConnectInputStream.
 * This works much like the PipedStreams, only that it can work completely
 * asynchronously and does not need a live Thread at each end.
 * <p>
 * The data will be buffered internally, the buffer will be grown if
 * necessary.
 * </p>
 *
 * @author Daniel Hahn
 * @version CVS $Revision$
 */
public class BufConnectOutputStream extends java.io.OutputStream {
    // Default size of initial Buffer in bytes;
    protected int bufSize = 4096;
    // Increment size for the buffer
    protected int bufferIncrement = 4096;
    // Position of the Read cursor
    protected int read = 0;
    // Position of the write cursor
    protected int write = 0;
    // Buffer
    protected byte[] buf;
    // If the stream has been closed.
    protected boolean closed = false;
    // Logger for this class
    Logger logger = Logger.getLogger(this.getClass());
    
    /** Creates a new instance of BufConnectOutputStream */
    public BufConnectOutputStream() {
        buf = new byte[bufSize];
    }
    
    public synchronized void write(int param) throws java.io.IOException {
        if (logger.isDebugEnabled()) logger.debug("Write byte, read " + read + ", write " + write + ", value " + param);
        if (closed) throw(new java.io.IOException("Stream closed"));
        if (write == (read - 1) || ((write == (bufSize - 1)) && (read == 0))) {
            logger.debug("Increasing array");
            // Buffer full, increase size
            int newBufSize = bufSize + bufferIncrement;
            byte[] newBuf = new byte[newBufSize];
            int newWrite= 0;
            while (read != write) {
                newBuf[newWrite] = buf[read];
                read = (read + 1) % bufSize;
                newWrite++;
            }
            bufSize = newBufSize;
            buf = newBuf;
            read = 0;
        }
        logger.debug("Writing to array");
        byte b = 0;
        if (param < 0 || param > 255) throw(new java.io.IOException("Illegal value: " + param));
        if (param < 128) {
            b = (byte) param;
        } else {
            b = (byte) -(param - 256);
        }
        buf[write] = (byte) param;
        write = (write + 1) % bufSize;
        notifyAll();
    }
    
    /** Allows the connected input buffer to read a byte */
    protected synchronized int readByte() {
        if (logger.isDebugEnabled()) logger.debug("Byte is fetched, read " + read + ", write " + write);
        while (read == write) {
            // If this is closed and everything has been read...
            if (closed) return -1;
            // Buffer is empty, wait for a new byte to arrive.
            try {
                wait(200);
            } catch (InterruptedException e) { }
        }
        byte b = buf[read];
        int retVal = 256;
        if (b >= 0) {
            retVal = b;
        } else {
            retVal = b + 256;
        }
        read = (read + 1) % bufSize;
        if (logger.isDebugEnabled()) logger.debug("Returning " + retVal);
        return retVal;
    }
    
    /** Checks if the stream is closed. */
    public boolean isClosed() {
        return closed;
    }
    
    /**
     * Gets available number of bytes.
     */
    protected synchronized int available() {
        int avail = 0;
        if (write >= read) {
            avail = write - read;
        } else {
            avail = (bufSize - read) + write;
        }
        return avail;
    }
    
    /** 
     * Tries to skip n bytes in the buffer. Will never attempt
     * to skip more bytes than are available in the buffer.
     */
    protected synchronized long skipBuf(long n) {
        if (logger.isDebugEnabled()) logger.debug("Skipping " + n + " bytes.");
        if (n < 0) return 0;
        int avail = available();
        int num = (int) n;
        if ((num > avail) || (num < 0))  num = avail;
        read = (read + num) % bufSize;
        if (logger.isDebugEnabled()) logger.debug(num + " bytes actually skipped.");
        return num;
    }
    
    /**
     * Closing the stream writes EOS to the Array. Note that
     * this means the connected stream will <i>only</i>
     * detect EOS after this stream was closed.
     */
    public synchronized void close() throws java.io.IOException {
        super.close();
        closed = true;
    }
    
}
