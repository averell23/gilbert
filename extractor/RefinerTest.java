/*
 * RefinerTest.java
 *
 * Created on 18 December 2001, 12:19
 */

package gilbert.extractor;

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
        if (args.length == 0) {
            System.err.println("Please give the uri to open.");
            System.exit(1);
        }
        // Util.setLogLevel(Util.LOG_DEBUG);
        Refiner x = new SearchingRefiner(false, "ubicomp,handheld");
        x.refine(args[0]);
    }

}
