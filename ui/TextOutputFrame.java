/*
 * TextOutputFrame.java
 *
 * Created on 26 March 2002, 16:02
 */

package gilbert.ui;
import java.io.*;
import org.apache.log4j.*;

/**
 * This prints all the contents of the given Reader to the 
 * TextArea.
 *
 * @author  Daniel hahn
 * @version CVS $Revision$
 */
public class TextOutputFrame extends javax.swing.JFrame  {
    /// Reader for the incoming data.
    Reader input;
    /// Output Writer
    PrintWriter output;
    /// Logger for this class
    Logger logger = Logger.getLogger(this.getName());
    
    /** Creates new form TextOutputFrame */
    public TextOutputFrame(Reader input, Writer output) {
        initComponents();
        this.input = input;
        if (output == null) {
            // Create dummy writer
            this.output = new PrintWriter(new Writer(){
                public void flush() { };
                public void close() { };
                public void write(char[] cbuf, int off, int len) { };
            });
        } else {
            this.output = new PrintWriter(output);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        dismissButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        dismissButton.setText("Dismiss");
        dismissButton.setActionCommand("dismiss");
        dismissButton.setEnabled(false);
        dismissButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dismissButtonActionPerformed(evt);
            }
        });

        jPanel1.add(dismissButton);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        textPane.setEditable(false);
        jScrollPane1.setViewportView(textPane);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void dismissButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dismissButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_dismissButtonActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        setVisible(false);
        dispose();
    }//GEN-LAST:event_exitForm

    public void go() {
        pack();
        show();
        logger.debug("Showing text output now.");
        TextOutputRunner runner = new TextOutputRunner(textPane, dismissButton, new BufferedReader(input), output);
        runner.start();
        logger.debug("Thread started.");
    }

    // For debugging.
    public static void main(String[] args) {
        TextOutputFrame test = new TextOutputFrame(new InputStreamReader(System.in), new OutputStreamWriter(System.out));
        test.go();
    }
       

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextPane textPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton dismissButton;
    // End of variables declaration//GEN-END:variables

}
