package test.edu.upenn.cis.cis555;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase {
        @SuppressWarnings("unchecked")
        public static Test suite() {        	
                Class[] testClasses = {
                    WebServiceTest.class,
                    CacheTest.class
                };
                return new TestSuite(testClasses);
        }
}
