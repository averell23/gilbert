/*
 * TextOutputRunner.java
 *
 * Created on 26 March 2002, 17:48
 */

package gilbert.ui;
import java.util.*;
import javax.swing.*;
import org.apache.log4j.*;
import java.io.*;
import javax.swing.text.*;


/**
 * Runner Thread class for updating the text output window.
 *
 * @author Daniel Hahn
 * @version CVS $Revision$
 */
public class TextOutputRunner implements Runnable {
    /// Logger for this class.
    Logger logger = Logger.getLogger(this.getClass());
    // These will be manipulated.
    JTextPane theField;
    JButton theButton;
    BufferedReader input;
    PrintWriter out;
    
    /** Creates a new instance of TextOutputRunner */
    public TextOutputRunner(JTextPane updateField, JButton tButton, BufferedReader input, PrintWriter out) {
        theField = updateField;
        theButton = tButton;
        this.input = input;
        this.out = out;
    }
    
    public void run() {
        logger.info("Starting Text Output Frame.");
        try {
            String line = input.readLine();
            logger.debug("Entering read loop");
            while (line != null) {
                if (out != null) out.println(line);
                Document doc = theField.getDocument();
                try {
                    doc.insertString(doc.getLength(), line + "\n", null);
                    theField.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    logger.warn("Exception updating.", e);
                }
                //theField.append(line);
                line = input.readLine();
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            logger.error("Cannot read from input stream: " + e.getMessage(), e);
        }
        logger.info("Text Output Frame: Read complete.");
        theButton.setEnabled(true);
    }
    
    public void start() {
        logger.debug("Thread starter");
        Thread t = new Thread(this);
        t.start();
        logger.debug("Thread started.");
    }
    
}
