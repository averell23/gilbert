/*
 * SimpleFileFilter.java
 *
 * Created on 26 March 2002, 15:33
 */

package gilbert.ui;

/**
 *
 * @author  daniel
 */
public class SimpleFileFilter extends javax.swing.filechooser.FileFilter {
    // Type for the filter.
    String filetype;
    // Description for the filter
    String description;
    
    /** Creates a new instance of SimpleFileFilter */
    public SimpleFileFilter(String filetype, String description) {
        this.filetype = filetype;
        this.description = description;
    }

    public boolean accept(java.io.File file) {
        if (file.isDirectory()) return true;
        if (file.getName().endsWith(filetype)) 
            return true;
        else
            return false;
    }
    
    public String getDescription() {
        return description;
    }
    
}
