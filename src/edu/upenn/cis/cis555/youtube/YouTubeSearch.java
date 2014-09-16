package edu.upenn.cis.cis555.youtube;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class YouTubeSearch extends HttpServlet{
	
	private String dhtDaemon = "127.0.0.1"; //Might as well have something for the default
	private int dhtPort = 8080; //Again, default here in case the servlet is misconfigured
	
	public void init(ServletConfig cfg){
		dhtDaemon = cfg.getServletContext().getInitParameter("cacheServer");
		dhtPort = Integer.parseInt(cfg.getServletContext().getInitParameter("cacheServerPort"));
	}
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>YouTube Video Search</title></head>" +
				"<body><h2 align=\"center\"><font face=\"Arial\"><b>YouTube Video" +
				" Search</b></font></h2><h4 align=\"center\"><font face=\"Verdana\" color=\"blue\">" +
				"Developed by Zachary Zarrow for CIS-455/555<br /><i>ENIAC Username: zzarrow</i>" +
				"</font></h4><hr><br /><br /><br /><br /><br /><br /><br /><center>" +
				"<form method=\"post\" action=\"/youtube\">" +
				"Query: &nbsp;<input type=\"text\" name=\"query\">&nbsp; <input type=\"submit\"" +
				" name=\"submit\" value=\"Search\"> &nbsp; <input type=\"reset\" name=\"reset\"" +
				" value=\"Clear\">" +
				"</form></center>" +
				"</body>" +
				"</html>");	
	}
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		if((request.getParameter("query") == null) ||
				(request.getParameter("query").equals(""))){
			out.println("<html><body><h2><font color=\"red\"><b>Error</b></font></h2>" +
					"<hr><li><font face=\"Arial\">The query you entered is invalid.</li><li>Please" +
					" <a href=\"javascript:history.back(1)\">go back</a> and try again.</font></li></body></html>");

			return;
		}
		
		//Else, send a REST request to the DHT @ dhtDaemon:dhtPort
		/**
		Socket sock = new Socket(dhtDaemon, dhtPort);
		System.out.println("Is sock connected: " + sock.isConnected());
		DataInputStream fromServer = new DataInputStream(sock.getInputStream());
		DataOutputStream toServer = new DataOutputStream(sock.getOutputStream());
		System.out.println("Sending request for query " + request.getParameter("query"));
		toServer.writeBytes("GET videos?query=" + request.getParameter("query"));
		System.out.println("Sent query");
		toServer.flush();
		System.out.println("Stream flushed.");
		//StringBuilder serverResponse = new StringBuilder();
		//String line = "";
		
		byte[] b = new byte[10000];
		String serverResponse = "";
		
		fromServer.readFully(b);
		serverResponse = new String(b);
		
		//while((line = fromServer.readLine()) != null) //CHECK THIS
			//serverResponse.append(line);
		**/
		
		URL u = new URL("http://" + dhtDaemon + ":" + dhtPort + "/videos?query=" + request.getParameter("query"));
		URLConnection uc = u.openConnection();
		uc.connect();
		//String serverResponse = uc.getContent();
		InputStreamReader in = new InputStreamReader((InputStream) uc.getContent());
	    BufferedReader buf = new BufferedReader(in);
	    StringBuilder resp = new StringBuilder();
	    String line = buf.readLine();
	    while(line != null){
	    	resp.append(line + "\n");
	    	line = buf.readLine();
	    }
	    
	    String serverResponse = resp.toString();
		
		System.out.println("Received response: \n\n" + serverResponse);
		
		if(serverResponse.contains("<error>") || serverResponse.equals("")){
			int start = serverResponse.indexOf("<error>") + 7;
			int end = serverResponse.indexOf("</error>");
			//start == 6 means that serverResponse has to be ""
			//Else parse the server's error from the response
			String errText = (start == 6) ? 
					"The server returned a blank response."
					: serverResponse.substring(start, end);
			out.println("<html><body><h2><b><font color=\"red\">Error</font></b></h2><hr>" +
					"<li>An error occurred when running your search query.</li>" +
					"<li>Error details: <i>" + errText + "</i></body></html>");
			return;
		}
		if(serverResponse.contains("numresults=0")){
			out.println("<html><body><h2><b><font color=\"blue\">No Videos Found</font></b></h2><hr>" +
					"<li>Sorry, your query returned <b>0 results</b> from YouTube.</li>" +
					"<li><a href=\"javascript:history.back(1)\">Click here</a> to try another query.</li></body></html>");
			return;
		}
		
		//Process xml
		//out.println("XML from server:\n");
		out.println(serverResponse);
		
		
		//toServer.flush();
		//fromServer.close();
		//toServer.close();
		//sock.close();
	}
}
