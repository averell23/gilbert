/*
 * MetaKInterestRefiner.java
 *
 * Created on 14 March 2002, 15:53
 */

package gilbert.extractor.refiners;
import java.util.*;
import gilbert.extractor.*;
import org.apache.log4j.*;

/**
 * Assigns an interestingness to a URL by checking the META keywords of that
 * Url against an internal list of keywords.
 *
 * @author Daniel Hahn
 * @version CVS $Revision$
 */
public class MetaKInterestRefiner extends Refiner {
    /// Internal keyword list
    protected Vector keywords;
    /// Weight of this interest function
    protected double weight;
    /// Logger for this class
    protected Logger logger = Logger.getLogger(this.getClass());
    
    
    /** Creates a new instance of MetaKInterestRefiner */
    public MetaKInterestRefiner() {
        super();
        passing = true;
        keywords = new Vector();
        weight = 1;
        maxHandlers = 12;
    }
    
    /**
     * Adds a keyword to the refiner.
     */
    public void addKeyword(String keyword) {
        keywords.add(keyword.toLowerCase());
        if (logger.isDebugEnabled()) logger.debug("Added keyword: " + keyword);
    }
    
        /// Weight setter
    public void setWeight(double weight) {
        if (logger.isDebugEnabled()) logger.debug("Weight setter: " + weight);
        if (weight > 1) weight = 1;
        if (weight < 0) weight = 0;
        this.weight = weight;
    }
    
    /// Weight getter
    public double getWeight() {
        logger.debug("Weight getter");
        return weight;
    }
    
    /// Getter Method for keywords types. Treats keywords types as comma-separated list.
    public String getKeywords() {
        StringBuffer retVal = new StringBuffer();
        Enumeration typesE = keywords.elements();
        if (typesE.hasMoreElements()) {
            retVal.append(typesE.nextElement());
        }
        while (typesE.hasMoreElements()) {
            retVal.append(",");
            retVal.append(typesE.nextElement());
        }
        return retVal.toString();
    }
    
    /**
     * Sets keywords from a comma-separated list. 
     * The keyword list will be completely re-set.
     */
    public void setKeywords(String typeStr) {
        StringTokenizer sTok = new StringTokenizer(typeStr, ",");
        keywords = new Vector();
        while (sTok.hasMoreTokens()) {
            addKeyword(sTok.nextToken());
        }
    }
    
    /** Must be overridden by child classes to handle each of
     * the URLs.
     *
     * It's up to the child class to use the proper methods or the
     * <code>outStream</code> for printing the resutls and to honour
     * the postfilters.
     */
    public void handleURL(VisitorURL url) {
        if (logger.isDebugEnabled()) logger.debug("Handling URL: " + url);
        String interestStr = url.getProperty("url.interest");
        String urlStr = url.getProperty("url.name");
        double interest = 0;
        
        Enumeration keywordsE = keywords.elements();
        Vector urlKeywords = Util.siteStatus(urlStr).getMetaKeywords();
        
        while (keywordsE.hasMoreElements()) {
            String current = (String) keywordsE.nextElement();
            if (urlKeywords.contains(current)) {
                interest += ((double) 1 / (double) keywords.size()); 
            }
        }
        interest *= weight;
        if (logger.isDebugEnabled()) logger.debug("Calculated interest to: " + interest);
        
        if (interestStr != null) {
            logger.debug("Old interst found.");
            double oldInterest = 0;
            try {
                oldInterest = Double.parseDouble(interestStr);
                interest = (interest + oldInterest) / (double) 2;
                if (logger.isDebugEnabled()) logger.debug("Old interest: " + oldInterest);
            } catch (NumberFormatException e) {
                logger.warn("Cannot parse Interest Number: " + interestStr);
            }
        }
        if (logger.isDebugEnabled()) logger.debug("Setting interest to: " + interest);
        url.setProperty("url.interest", "" + interest);
    }    
    

}
