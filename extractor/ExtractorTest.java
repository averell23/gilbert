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
        Util.setLogLevel(Util.LOG_DEBUG);
        Extractor x = new ResolvingExtractor();
        x.addPrefilter(new LocalVisitFilter());
        x.extract(args[0]);
    }

}
