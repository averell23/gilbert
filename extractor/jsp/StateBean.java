/*
 * StateBean.java
 *
 * Created on 22 January 2002, 12:02
 */

package gilbert.extractor.jsp;

/**
 * A Bean that store some semi-permanent state information for the extraction
 * JSP pages.
 *
 * @author Daniel Hahn
 * @version CVS $Revision
 */
public class StateBean {
    /// If the page should automatically reload at the moment.
    protected boolean autoReload;
    /// The base URI for the top page.
    protected String baseURI;
    /// The frequency of the page reloads (in seconds).
    protected int reloadFrequency;

    /** Creates new StateBean */
    public StateBean() {
    }
    
    public void setAutoReload(boolean rel) {
        autoReload = rel;
    }
    
    public boolean getAutoReload() {
        return autoReload;
    }
    
    public void setBaseURI(String base) {
        baseURI = base;
    }
    
    public String getBaseURI() {
        return baseURI;
    }
    
    public void setReloadFrequency(int freq) {
        reloadFrequency = freq;
    }
    
    public int getReloadFrequency() {
        return reloadFrequency;
    }

}
