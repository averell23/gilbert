/*
 * RefinerTest.java
 *
 * Created on 18 December 2001, 12:19
 */

package gilbert.extractor;
import java.util.*;
import gilbert.extractor.refiners.*;
import gilbert.extractor.filters.*;
import org.apache.log4j.*;

/**
 *
 * @author  daniel
 * @version 
 */
public class RefinerTest {

    /** Creates new RefinerTest */
    public RefinerTest() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        BasicConfigurator.configure();
        if (args.length == 0) {
            System.err.println("Please give the uri to open.");
            System.exit(1);
        }
        Util.setLogLevel(Util.LOG_DEBUG);
       // Refiner x = new SearchingRefiner(true, "ubicomp, handheld,context");
        Refiner x = new LinkRefiner();
        x.refine(args[0]);
        /* Enumeration e = x.getUrlList().elements();
        while (e.hasMoreElements()) {
            System.out.println(e.nextElement());
        } */
    }

}
