/*
 * ExtractorTest.java
 *
 * Created on 05 December 2001, 17:04
 */

package gilbert.extractor;
import gilbert.extractor.extractors.*;
import gilbert.extractor.filters.*;
import org.apache.log4j.*;
import org.apache.log4j.xml.*;

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
        Logger mainlogger = Logger.getLogger("main");
        PropertyConfigurator.configure("gilbert/extractor/log4j.properties");
        
        if (args.length == 0) {
            System.err.println("Please give the uri to open.");
            System.exit(1);
        }
        mainlogger.info("Starting...");
        Extractor x = new StraightExtractor();
        x.addPrefilter(new LocalVisitFilter());
        long timestamp = System.currentTimeMillis();
        x.extract(args[0]);
        x.getOutputStream().flush();
        long time = (System.currentTimeMillis() - timestamp) / 1000;
        mainlogger.info("Extracting time was " + time + " seconds.");
        // System.out.println(Util.liveCache);
    }

}
