/*
 * ConnectTest.java
 *
 * Created on 28 March 2002, 11:46
 */

package gilbert.io;
import org.apache.log4j.*;
import java.io.*;

/**
 *
 * @author  daniel
 */
public class ConnectTest {
    
    public static void main(String args[]) {
        BasicConfigurator.configure();
        int counter = 3 * 4096;
        try {
            BufConnectOutputStream out = new BufConnectOutputStream();
            for (int i = 0 ; i < counter ; i ++) {
                out.write(i % 256);
            }
            out.close();
            BufConnectInputStream in = new BufConnectInputStream(out);
            int b = 0;
            int count = 0;
            while (b != -1) {
                b = in.read();
                System.out.println(b);
                count++;
            }
            System.out.println("Count: " + (count - 1) + " of " + counter);
            if ((count - 1) != counter) {
                System.out.println("Read != write.");
                System.exit(0);
            }
            out = new BufConnectOutputStream();
            in = new BufConnectInputStream(out);
            for (int i = 0 ; i < counter ; i++) {
                out.write(i % 128);
                int x = in.read();
                if (x != (i % 128)) {
                    System.out.println("Was " + x + " should be " + (i % 128));
                    System.exit(0);
                }
            }
            out.close();
            System.out.println(in.read());
            try {
                out.write(1);
                System.out.println("Should have thrown execption.");
                System.exit(1);
            } catch (java.io.IOException e) {
                System.out.println("Ok. " + e.getMessage());
            }
            try {
                in.read();
                System.out.println("Should have thrown execption.");
                System.exit(1);
            } catch (java.io.IOException e) {
                System.out.println("Ok. " + e.getMessage());
            }
            out = new BufConnectOutputStream();
            in = new BufConnectInputStream(out);
            BufferedReader inR = new BufferedReader(new InputStreamReader(in));
            PrintStream test = new PrintStream(out);
            String testString =  "test string";
            for (int i = 0 ; i < 5 ; i++) {
                test.print(testString);
                System.out.println("Avail: " + in.available());
                String result = inR.readLine();
                if (!result.equals(testString)) {
                    System.out.println("String was: "+ result + " should have been " + testString);
                    System.exit(1);
                }
            }
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
    
}
