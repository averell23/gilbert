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
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class ExtractorTest {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Logger mainlogger = Logger.getLogger("main");
        PropertyConfigurator.configure("gilbert/extractor/log4j.properties");
        if (args.length == 0) {
            System.err.println("Usage: [extractor] <source uri>");
            System.exit(1);
        }
        String extractor = "DumpExtractor";
        String uri = "";
        if (args.length > 1) {
            extractor = args[0];
            uri = args[1];
        } else {
            uri = args[0];
        }
        
        mainlogger.info("Starting...");
        Class extClass = null;
        Extractor x = null;
        long timestamp = System.currentTimeMillis();
        try {
            extClass = Class.forName("gilbert.extractor.extractors." + extractor);
            x = (Extractor) extClass.newInstance();
            LocalVisitFilter loc = new LocalVisitFilter();
            FailureVisitFilter fail = new FailureVisitFilter();
            RTypeVisitFilter rtype = new RTypeVisitFilter();
            rtype.addDocType("gif");
            rtype.addDocType("pdf");
            rtype.addDocType("jpg");
            rtype.addDocType("zip");
            rtype.addDocType("gz");
            x.addPrefilter(loc);
            x.addPrefilter(fail);
            x.addPrefilter(rtype);
            timestamp = System.currentTimeMillis();
            x.extract(uri);
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
        mainlogger.info("Number of visits: " + x.getCount());
        mainlogger.info("Number of distinctive visits: " + x.getDistinctiveCount());
        mainlogger.info("Number of visit handlings: " + x.getHandledCount());
        // System.out.println(Util.liveCache);
    }
    
}
