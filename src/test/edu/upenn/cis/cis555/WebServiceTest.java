package test.edu.upenn.cis.cis555;

import java.io.IOException;

import com.google.gdata.util.ServiceException;

import edu.upenn.cis.cis555.youtube.P2PCache;
import edu.upenn.cis.cis555.youtube.YouTubeData;
import junit.framework.TestCase;

public class WebServiceTest extends TestCase {

	public void testXmlToHtml(){
		String xml = "<searchresults numresults=3><result>test1</result><result>test2</result><result>test3</result></searchresults>";
		String result = P2PCache.jUnitExposeString("xmlToHtml", xml);
		assertTrue(result.contains("<tr>test1</tr>"));
		assertTrue(result.contains("<tr>test2</tr>"));
		assertTrue(result.contains("<tr>test3</tr>"));
	}
	
	public void testXmlToHtmlNoResults(){
		String xml = "<searchresults numresults=0></searchresults>";
		String result = P2PCache.jUnitExposeString("xmlToHtml", xml);
		assertSame(result, xml);
		assertFalse(result.contains("<result>"));
	}
	
	public void testYouTubeData1() throws IOException, ServiceException{
		String results = YouTubeData.getResults("penn");
		assertFalse(results.contains("<error>"));
		assertTrue(results.length() > 0);
	}
	
	public void testYouTubeData2() throws IOException, ServiceException{
		String noResults = YouTubeData.getResults("thereshouldnotbeanyresultsforthislol");
		assertTrue(noResults.contains("numresults=0"));
	}
	
	public void testXmlFromYouTube(){
		String query = "java";
		String result = P2PCache.jUnitExposeString("getXmlDataFromYouTube", query);
		assertTrue(result.contains("<searchresults numresults"));
		assertTrue(result.contains("<result>"));
		assertFalse(result.contains("<error>"));
	}

}
