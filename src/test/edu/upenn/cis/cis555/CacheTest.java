package test.edu.upenn.cis.cis555;

import java.io.IOException;
import java.util.Map;

//import org.junit.Test;

import edu.upenn.cis.cis555.youtube.NodeFactory;
import edu.upenn.cis.cis555.youtube.P2PCache;
import junit.framework.TestCase;

public class CacheTest extends TestCase {
	
	private String ip = "127.0.0.1";
	
	//In order for some of these tests to work (the ones that call non-static methods),
	//a daemon needs to be started on port 9000 somewhere.  Fill in that somewhere's
	//IP adress into the "ip" field above.  This daemon cannot be started as part of
	//the test suite because it will wait for requests, and the JUnit will time out.
	
	//Alternatively, you can just change the port numbers below so that each client
	//"kind of" runs as its own daemon (will work for the purposes of JUnit).
	
	/**
	public void testInstantiateDaemon() throws IOException{
		P2PCache cache = new P2PCache(1, "127.0.0.1", 6000, new NodeFactory("127.0.0.1", 9000), true);
		ip = cache.jUnitExposeString("ip");
		assert(true); //Meaning no exceptions were thrown and it didn't hang
	}**/
	
	//The annotated exception is expected; it's just the client node doing virtual binding
	//@Test(expected=java.lang.IllegalStateException.class)
	public void testInstantiateClient() throws IOException{
		P2PCache cache = new P2PCache(1, ip, 9000, new NodeFactory(ip, 9000), false);
		assert(true); //Doesn't hang - gets to this point
	}
	
	
	
	//@Test(expected=java.lang.IllegalStateException.class)
	public void testGenerateResponse() throws IOException{
		P2PCache cache = new P2PCache(1, ip, 9000, new NodeFactory(ip, 9000), false);
		String response = cache.generateResponse("dummyId", "GETVIDEO:::dogs");
		assertTrue(response.contains("<searchresults"));
		assertTrue(response.contains("<result>"));
	}
	
	//@Test(expected=java.lang.IllegalStateException.class)
	public void testCache() throws IOException{
		P2PCache cache = new P2PCache(1, ip, 9000, new NodeFactory(ip, 9000), false);
		String resp = cache.generateResponse("dummyId", "GETVIDEO:::python");
		Map<String, String> cacheMap = cache.jUnitExposeCache();
		assertTrue(cacheMap.containsKey("python"));
		assertSame(resp, cacheMap.get("python"));
	}
	
	//@Test(expected=java.lang.IllegalStateException.class)
	public void testCache2() throws IOException{
		P2PCache cache = new P2PCache(1, ip, 9000, new NodeFactory(ip, 9000), false);
		Map<String, String> cacheMap = cache.jUnitExposeCache();
		cache.generateResponse("dummyId", "GETVIDEO:::linux");
		int sizeBefore = cacheMap.size();
		cache.generateResponse("dummyId", "GETVIDEO:::linux");
		int sizeAfter = cacheMap.size();
		assertEquals(sizeBefore, sizeAfter);		
	}

}
