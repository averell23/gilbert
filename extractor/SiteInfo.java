/*
 * SiteInfo.java
 *
 * Created on 01 February 2002, 17:01
 */

package gilbert.extractor;

/**
 * This encapsulates information about a site. 
 *
 * @author Daniel Hahn
 * @version CVS $revision$
 */
public class SiteInfo {
    /// URL of the site
    protected String url;
    /// Timestamp when the entry was created
    protected long timestamp;
    /// If the URL was found alive
    protected boolean alive;
    /// The content type served by that url
    protected String contentType;

    /** Creates a new instance of SiteInfo */
    protected SiteInfo(String url) {
        this.url = url;
        timestamp = System.currentTimeMillis();
    }
    
    /** Creates a new instance of SiteInfo */
    protected SiteInfo() {
        this("");
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String content) {
        contentType = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public boolean getAlive() {
        return alive;
    }

}
