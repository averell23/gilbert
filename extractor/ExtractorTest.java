/*
 * ExtractorTest.java
 *
 * Created on 05 December 2001, 17:04
 */

package gilbert.extractor;
import gilbert.extractor.extractors.*;
import gilbert.extractor.filters.*;

/**
 *
 * @author  daniel
 * @version 
 */
public class ExtractorTest {

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        if (args.length == 0) {
            System.err.println("Please give the uri to open.");
            System.exit(1);
        }
        Util.setLogLevel(Util.LOG_MESSAGE);
        Util.logMessage("Starting...", Util.LOG_ERROR);
        Extractor x = new StraightExtractor();
        x.addPrefilter(new LocalVisitFilter());
        long timestamp = System.currentTimeMillis();
        x.extract(args[0]);
        x.getOutputStream().flush();
        long time = (System.currentTimeMillis() - timestamp) / 1000;
        System.out.println("Extracting time was " + time + " seconds.");
        // System.out.println(Util.liveCache);
    }

}
