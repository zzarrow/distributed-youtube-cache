package edu.upenn.cis.cis555.youtube;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.geo.impl.GeoRssWhere;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaPlayer;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeMediaRating;
import com.google.gdata.data.youtube.YtPublicationState;
import com.google.gdata.data.youtube.YtStatistics;
import com.google.gdata.util.ServiceException;

public class YouTubeData {
	public static String getResults(String _query) throws IOException, ServiceException{
		YouTubeService service = new YouTubeService("*****REDACTED_FOR_GITHUB*****");
		YouTubeQuery query = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/videos"));
		query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
		query.setMaxResults(25);
		query.setFullTextQuery(_query);
		query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
		query.setLanguageRestrict("en");

		VideoFeed videoFeed = service.query(query, VideoFeed.class);
		return constructXML(videoFeed);
	}
	
	public static String constructXML(VideoFeed videoFeed) {
		  StringBuilder sb = new StringBuilder();
		  sb.append("<searchresults numresults=" + videoFeed.getEntries().size() + ">\n");
		  for(VideoEntry videoEntry : videoFeed.getEntries())
		    sb.append("<result>" + getVideoEntryHtml(videoEntry) + "</result>\n");
		  
		  sb.append("</searchresults>");
		  
		  return sb.toString();
	}

	public static String getVideoEntryHtml(VideoEntry videoEntry) {
			StringBuilder html = new StringBuilder();

			YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
			MediaPlayer mediaPlayer = mediaGroup.getPlayer();
			List<YouTubeMediaContent> mediaContent = mediaGroup.getYouTubeContents();
			MediaKeywords keywords = mediaGroup.getKeywords();
			
			//thumbnail, duration
			if(mediaGroup.getThumbnails().size() > 0){
				MediaThumbnail mediaThumbnail = mediaGroup.getThumbnails().get(0);
				html.append("<td><center><a href=\"" + mediaPlayer.getUrl() + "\"><img src=\"" + mediaThumbnail.getUrl() + "\"></a>");
			} else
				html.append("<td><center><i>No thumbnail available</i>");
			
		    if(!mediaContent.isEmpty())
		    	html.append("<br />Duration: " + (mediaContent.get(0).getDuration() / 60) + ":" + (mediaContent.get(0).getDuration() % 60));
		    html.append("</center></td>");
			
			//title/link, author
			html.append("<td><b><a href=\"" + mediaPlayer.getUrl() + "\">" + videoEntry.getTitle().getPlainText() + "</a></b>" +
					"<br />Uploaded by: <b>" + mediaGroup.getUploader() + "</b></td>");
			
			//Description
			if(mediaGroup.getDescription() != null)
				html.append("<td><center>" + mediaGroup.getDescription().getPlainTextContent() + "</center></td>");
			else
				html.append("<td><center><i>No description available</i></center></td>");
			
			
			//View count, rating
			if(videoEntry.getStatistics() != null)
				html.append("<td>Views: <b>" + videoEntry.getStatistics().getViewCount() + "</b><br />");
			else
				html.append("<td><i>View count not available</i><br /></td>");
			
			if(videoEntry.getRating() != null)
				html.append("Rating: " + videoEntry.getRating().getAverage() + "</td>");
			else
				html.append("<i>Rating not available</i></td>");

			//Keywords
			html.append("<td>");
			boolean hadAtLeastOne = false;
			for(String kw : keywords.getKeywords()){
				if(hadAtLeastOne)
					html.append(", ");
				html.append(kw);
				hadAtLeastOne = true;
			}
			html.append("</td>");
			
			return html.toString();
	}
	
	public static void main(String[] args) throws IOException, ServiceException{
		System.out.println(YouTubeData.getResults("magic"));
	}
}
