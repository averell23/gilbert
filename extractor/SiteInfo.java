/*
 * SiteInfo.java
 *
 * Created on 01 February 2002, 17:01
 */

package gilbert.extractor;
import java.util.*;

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
    /// META keywords from the page
    protected Vector metaKeywords;
    /// META description from the page
    protected String metaDescription;
    /// META title from the page
    protected String metaTitle;
    /// URLs linked from this site
    protected Vector links;

    /** Creates a new instance of SiteInfo */
    protected SiteInfo(String url) {
        this.url = url;
        timestamp = System.currentTimeMillis();
        metaKeywords = new Vector();
        links = new Vector();
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
    
    public Vector getMetaKeywords() {
        return metaKeywords;
    }
    
    public void addMetaKeyword(String keyword) {
        metaKeywords.add(keyword);
    }
    
    public String getMetaDescription() {
        return metaDescription;
    }
    
    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }
    
    public String getMetaTitle() {
        return metaTitle;
    }
    
    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }
    
    public Vector getLinks() {
        return links;
    }
    
    public void addLink(String link) {
        links.add(link);
    }

}
