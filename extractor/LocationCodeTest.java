/*
 * LocationCodeTest.java
 *
 * Created on 06 March 2002, 14:26
 */

package gilbert.extractor;

/**
 *
 * @author  daniel
 */
public class LocationCodeTest {

    /** Creates a new instance of LocationCodeTest */
    public LocationCodeTest() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        String host = args[0];
        int code = LocationCode.getLocationCode(host);
        System.out.println("Location code for " + host + ":" + code);
        String desc = LocationCode.getLocation(code);
        System.out.println("Description for code " + code + ": " + desc);
    }

}
