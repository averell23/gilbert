/*
 * RefinerSelectDialog.java
 *
 * Created on 25 March 2002, 13:48
 */

package gilbert.ui;
import org.apache.log4j.*;
import java.util.*;

/**
 * Allows the user to select some class. The list of valid classes will be
 * submitted when constructing the Dialog, and the result will be a new
 * object of the selected class.
 *
 * @author Daniel Hahn
 * @version CVS $Revision$
 */
class ClassSelectDialog extends javax.swing.JDialog {
    /// Indicates if the dialog was cancelled by the user.
    protected boolean cancelled = false;
    /// Name of the selected Refiner
    protected Class selection;
    /// Logger for this class
    Logger logger = Logger.getLogger(this.getClass());
    /// Data for this selection
    Vector selectData;
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public Object getSelection() {
        Class selClass = (Class) selection;
        Object retVal = null;
        try {
            retVal = selClass.newInstance();
        } catch (InstantiationException e) {
            logger.warn("Could not instanciate object of " + selClass.getName(), e);
        } catch (IllegalAccessException e) {
            logger.warn("Could not instanciate object of " + selClass.getName(), e);
        }
        return retVal;
    }
    
    /** 
     * Creates new form ClassSelectDialog.
     * @param classlist Vector containing the classes to select from.
     */
    public ClassSelectDialog(java.awt.Frame parent, boolean modal, Vector classlist) {
        super(parent, modal);
        selectData = classlist;
        initComponents();
        initForm();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        refinerList = new javax.swing.JList();

        setModal(true);
        setName("selectDialog");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        cancelButton.setText("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel1.add(cancelButton);

        selectButton.setText("Select");
        selectButton.setActionCommand("select");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        jPanel1.add(selectButton);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        refinerList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(refinerList);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents
    
    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        cancelled = false;
        selection = (Class) refinerList.getSelectedValue();
        setVisible(false);
        dispose();
    }//GEN-LAST:event_selectButtonActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelled = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        cancelled = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * Initializes the form.
     */
    private void initForm() {
        refinerList.setListData(selectData);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList refinerList;
    private javax.swing.JButton selectButton;
    private javax.swing.JButton cancelButton;
    // End of variables declaration//GEN-END:variables
    
}