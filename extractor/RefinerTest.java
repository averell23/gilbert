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

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        Logger mainlogger = Logger.getLogger("main");
        PropertyConfigurator.configure("gilbert/extractor/log4j.properties");
        if (args.length == 0) {
            System.err.println("Usage: RefinerTest [Refiner] <source uri>");
            System.exit(1);
        }
        String refiner = "DumpRefiner";
        String uri = "";
        if (args.length > 1) {
            refiner = args[0];
            uri = args[1];
        } else {
            uri = args[0];
        }
        Refiner x = null;
        Class refClazz = null;
        long timestamp = System.currentTimeMillis();
        try {
            if (refiner.equals("SearchingRefiner")) {
                x = new SearchingRefiner(true, "ubicomp, handheld, context");
            } else {
                refClazz = Class.forName("gilbert.extractor.refiners." + refiner);
                x = (Refiner) refClazz.newInstance();
            }
            timestamp = System.currentTimeMillis();
            x.refine(uri);
            x.getOutputStream().flush();
        } catch (ClassNotFoundException e) {
            mainlogger.error("Cannot find class: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            mainlogger.error("Cannot instantiate class: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            mainlogger.error("Access violation: " + e.getMessage(), e);
        }
        long time = (System.currentTimeMillis() - timestamp) / 1000;
        mainlogger.info("Extracting time was " + time + " seconds.");
    }

}
