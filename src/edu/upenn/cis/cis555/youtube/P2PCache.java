package edu.upenn.cis.cis555.youtube;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gdata.util.ServiceException;

import rice.p2p.commonapi.Id; 
import rice.p2p.commonapi.Message; 
import rice.p2p.commonapi.Node; 
import rice.p2p.commonapi.NodeHandle; 
import rice.p2p.commonapi.Endpoint; 
import rice.p2p.commonapi.Application; 
import rice.p2p.commonapi.RouteMessage; 

import rice.p2p.commonapi.Id;

public class P2PCache implements Application {

	NodeFactory nodeFactory; 
	Node node; 
	Endpoint endpoint; 
	
	Socket conn;
	DataInputStream in;
	DataOutputStream out;
	
	//Set some defaults just in case
	int numNodes = 0;
	String ip;
	int port = 8080;
	boolean isDaemon = false;
	
	Map<String, String> mCache;
	
	//NodeFactory pastryChef;
	LinkedList<P2PCache> nodes;
	
	public P2PCache(int _numNodes, String _ip, int _port, NodeFactory nodeFactory, boolean _isDaemon) throws IOException{
		numNodes = _numNodes;
		ip = _ip;
		//ip = "192.168.148.221";
		port = _port;
		isDaemon = _isDaemon;
		
		mCache = new HashMap<String, String>(); //Maps query to cached result
		
		System.out.println("Constructing P2PCache");
		
		if(ip.equals("127.0.0.1") || ip.equals("localhost")){
			//Look for a network interface that isn't a loopback interface
			//If we can't find one, stick with 127.0.0.1 (likely to make Pastry fail)
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()){
				NetworkInterface ni = interfaces.nextElement();
				//System.out.println("New network interface: " + ni.toString());
				if(!ni.isLoopback()){
					Enumeration<InetAddress> addresses = ni.getInetAddresses();
					if(!addresses.hasMoreElements())
						continue;
					//Sometimes we'll get an IPV6 address, sometimes we'll get an
					//IPV4 address, usually we'll get both.  Check for this.
					ip = addresses.nextElement().getHostAddress();
					if(ip.length() > 15){
						//ipV6
						if(addresses.hasMoreElements())
							ip = addresses.nextElement().getHostAddress(); //get the ipv4
						else
							continue; //no ipv4 for that interface
					}
					//We're going to insert a "programmatic bias"... Usually, eth1 is what
					//we're going to want to use, so if we see eth1 just go with it.  If not,
					//we'll just pick a non-loopback interface.
					if(ni.getDisplayName().equals("eth1"))
						break;
				}
			}
		}
		
		if(nodeFactory != null){
			this.nodeFactory = nodeFactory; 
			this.node = nodeFactory.getNode(); 
			this.endpoint = node.registerApplication(this, "YouTube Cache"); 
		}
			
		if(isDaemon){
			initPastryNodes();
			runListener();
		} else {
			runAsPastryNode();
		}
	}
	
	private void runListener() throws IOException{
		ServerSocket sock = new ServerSocket(port);
		while(true){
			Socket conn = sock.accept();
			handleConnection(conn);
		}
	}
	
	private void handleConnection(Socket _conn) throws IOException{
		conn = _conn;
		in = new DataInputStream(conn.getInputStream());
		out = new DataOutputStream(conn.getOutputStream());
		
		while(in.available() < 10);
		byte[] b = new byte[in.available()];
		in.readFully(b);

		String userRequest = new String(b);
		if((!userRequest.startsWith("GET")) || (userRequest.length() <= 4) || (!userRequest.contains("videos"))){
			out.writeBytes("HTTP/1.1 200 OK\nContent-Type: text/xml; charset=utf-8\n\n<error>Malformed REST request.</error>");
			out.flush();
			in.close();
			out.close();
			return;
		}
		
		String query = userRequest.substring(userRequest.indexOf("videos") + 13, userRequest.indexOf("HTTP/1"));
		System.out.println("Query from client: " + query);

		//P2PCache rootNode = new P2PCache(numNodes, ip, port, nodeFactory, false);
		
		
		Id targetId = nodeFactory.getIdFromBytes(query.getBytes());
		endpoint.route(targetId, new DHTMessage(node.getLocalNodeHandle(),"GETVIDEO:::" + query), null);

		
		/** Response structure will be:
		 * 	<searchresults numresults=#>
		 *     <result>DATA</result>
		 * 	   <result>DATA</result>
		 * 	   ...
		 *  </searchresults>
		 */
		
		//For now, send a dummy response so we can test the client/server comm.
		
		/**
		out.writeBytes("HTTP/1.1 200 OK\nContent-Type: text/xml; charset=utf-8\n\n");
		out.writeBytes("<searchresults numresults=3>");
		out.writeBytes("<result>Test result 1</result>");
		out.writeBytes("<result>Test result 2</result>");
		out.writeBytes("<result>Test result 3</result>");
		out.writeBytes("</searchresults>");
		
		out.flush();
		
		in.close();
		out.close();
		conn.close();
		**/
		return;
	}
	
	private void initPastryNodes() throws IOException{
		//Start Pastry nodes (Create class for Pastry app?)
		//for i = 0 to numNodes
		//create and start node on port (_port + i)
		
		//pastryChef = new NodeFactory(9100);
		nodes = new LinkedList<P2PCache>();
		nodes.add(this);
		
		for(int i = 1; i < numNodes; i++)
			nodes.add(new P2PCache(numNodes, ip, port, nodeFactory, false));
	}
	
	private void runAsPastryNode(){
		
	}
	
	public boolean forward(RouteMessage routeMessage) { 
		// This method will always return true in your assignment 
		return true; 
	}
	  
	void sendMessage(Id idToSendTo, String msgToSend){
		DHTMessage m = new DHTMessage(node.getLocalNodeHandle(), msgToSend);
		endpoint.route(idToSendTo, m, null);
	}
	  
	public void deliver(Id id, Message message){
		DHTMessage om = (DHTMessage) message;
		System.out.println("Received DHT message of length " + om.content.length() + " from " + om.from);
		//System.out.println("Message: \n" + om.content + "\n\n");
		
		//Need this first condition because the daemon is setup as a client node too
		if((om.from != node.getLocalNodeHandle()) && om.wantResponse){
			DHTMessage reply = new DHTMessage(node.getLocalNodeHandle(), generateResponse(om.from.toString(), om.content));
			reply.wantResponse = false;
			endpoint.route(null, reply, om.from);
		}
		
		if(isDaemon){
			try {
				sendResponseToClient(generateResponse(om.from.toString(), om.content));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
	}
	
	public String generateResponse(String fromId, String message){
		//Parse message
		//if search query is in hash table
			//return from hash table
		//else
			//do YouTube lookup, store in hashtable, return
		
		if(message.startsWith("GETVIDEO")){
			String query = message.substring(message.indexOf(":::") + 3, message.length());
			System.out.println("DHT received video search for: " + query);
			//If it's in the cache, pull it and return it
			if(mCache.containsKey(query)){
				System.out.println("Query found in cache.  Retreiving cached data.");
				return mCache.get(query);
			}
			System.out.println("Query not found in cache: Pulling data from YouTube.");
			String response = getXmlDataFromYoutube(query);
			mCache.put(query, response);
			System.out.println("Response data stored in cache.  Sending data to requester.");
			return response;
		}
		
		return message;
	}
	
	private static String getXmlDataFromYoutube(String query) {
		//Download data from YouTube
		
		try {
			return YouTubeData.getResults(query);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return "<error>A fatal error occurred when trying to extract data from YouTube: " + e.getMessage() + "</error>";
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			return "<error>A fatal error occurred when trying to extract data from YouTube: " + e.getMessage() + "</error>";
		}
		
		//return "<searchresults numresults=3>\n<result>Test result 1</result>\n<result>Test result 2</result>\n<result>Test result 3</result>\n</searchresults>";
	}

	private void sendResponseToClient(String _response) throws IOException{
		//Response is in XML
		//Convert to HTML here
		
		out.writeBytes("HTTP/1.1 200 OK\nContent-Type: text/html; charset=utf-8\n\n");
		
		if(_response.contains("<error>"))
			out.writeBytes(_response);
		else
			out.writeBytes(xmlToHtml(_response));
				
		
		/**
		out.writeBytes("<searchresults numresults=3>");
		out.writeBytes("<result>Test result 1</result>");
		out.writeBytes("<result>Test result 2</result>");
		out.writeBytes("<result>Test result 3</result>");
		out.writeBytes("</searchresults>");
		**/
		
		out.flush();
		
		in.close();
		out.close();
		conn.close();
	}
	
	
	private static String xmlToHtml(String response) {
		// Convert XML to HTML
		/** XML format:
		 	<searchresults numresults=#>
		 	<result>REsult 1</result>
		 	<result>Result 2</result>
		 	...
		 	</searchresults>
		 
		 **/
	
		int start = response.indexOf("numresults=") + 11;
		int end = response.indexOf(">", start);
		int numResults = Integer.parseInt(response.substring(start, end));
		
		if(numResults == 0)
			return response; //Servlet knows how to handle that xml
		
		StringBuilder html = new StringBuilder();
		html.append("<html><head><title>YouTube Search Results</title></head>");
		html.append("<body><center><font face = \"Arial\"><h2>Search Results</h2></font>" +
				"Your query returned <b>" + numResults + "</b> results.<br />" +
						"<a href=\"javascript:history.back(1)\">Click here</a> to return to the form and search again.<hr>");
		html.append("<table border=1><tr><td><b>Thumbnail<b></td><td><b>Title</b></td><td><b>Description</b></td><td><b>Statistics</b></td><td><b>Keywords</b></td></tr>");
		
		start = response.indexOf("<result>");
		end = response.lastIndexOf("</result>");
		response = response.substring(start, end + 9);
		
		String arrResults[] = response.split("<result>");
		for(int i = 0; i < arrResults.length; i++){
			if((arrResults[i] == null) || arrResults[i].equals(""))
				continue;
			arrResults[i] = arrResults[i].substring(0, arrResults[i].indexOf("</result>"));
			html.append("<tr>" + arrResults[i] + "</tr>");
		}
		
		html.append("</table><hr></center></body></html>");
		
		return html.toString();
	}

	public void update(NodeHandle handle, boolean joined) { 
		// This method will always be empty in your assignment 
	}
	
	public String jUnitExposeString(String _input){
		if(_input.equals("ip"))
			return ip;
		return null;
	}
	
	public static String jUnitExposeString(String _input, String _parm){
		if(_input.equals("xmlToHtml"))
			return xmlToHtml(_parm);
		if(_input.equals("getXmlDataFromYouTube"))
			return getXmlDataFromYoutube(_parm);
		return null;
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		if(args.length == 0){
			System.out.println("Zach Zarow (ENIAC Username: zzarrow)");
			return;
		}
		
		if(args.length != 4){
			System.out.println("Usage: P2PCache [number of virtual nodes] [bootstrap server IP] [cache server port] [Pastry listener port]");
			return;
		}
		
		boolean isDaemon = ((args[1].equals("127.0.0.1")) || (args[1].equals("localhost")));
		NodeFactory nf = null;
		
		//System.out.println("Before create node factory");
		nf = new NodeFactory(args[1], Integer.parseInt(args[3]));
		//System.out.println("After create node factory");
		
		System.out.println("Starting cache...");
		P2PCache cache = new P2PCache(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]), nf, isDaemon);
			

	}

	public Map<String, String> jUnitExposeCache() {
		return mCache;
	}

}
