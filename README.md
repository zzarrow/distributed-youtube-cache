##Distributed Caching System for YouTube Searches##
####Developed by Zach Zarrow to fulfill an academic assignment in Spring 2011.####

This is a P2P distributed caching server that runs across a virtual network of server nodes.  It serves a simple form via web that asks the user for a YouTube search query. When the form is submitted, the system returns the YouTube search results and caches them in the distributed hash table to speed future response times.

Below is the README information originaly submitted with the assignment:

Description of features implemented:
  - All specified features are implemented, and the program correctly distributes itself across multiple nodes

  - Some notes:
	- You can monitor each node on stdout; they will indicate numerous pieces of information; namely, if they are pulling a query from cache or from YouTube.

	- I made the design decision to have my bootstrap node double as a client node in the ring. This allowed for seamless integration of the web service and the distributed cache.

	- Please see below for instructions on running. 

List of source files included (consider using `find src | grep java`)
src/edu/upenn/cis/cis555/youtube/P2PCache.java
src/edu/upenn/cis/cis555/youtube/YouTubeData.java
src/edu/upenn/cis/cis555/youtube/YouTubeSearch.java
src/edu/upenn/cis/cis555/youtube/DHTMessage.java
src/test/edu/upenn/cis/cis555/RunAllTests.java
src/test/edu/upenn/cis/cis555/WebServiceTest.java
src/test/edu/upenn/cis/cis555/CacheTest.java
web.xml (sample)

Outside sources used:
  Lecture slides.

Special instructions for building or running:
  - Please use 555-build.sh hw3 to build the code.
  - When running P2PCache as the bootstrap node, please specify 127.0.0.1 or localhost as the IP address
  - I added an argument to P2PCache that takes the port number on which to run/connect to the pastry node. For the bootstrap node, use the port you want to run the Pastry listener on. For client nodes, use the port number you specified for the bootstrap node.
  - Please edit web.xml to set the correct parameters for your cache server and cache port before running the web server.
  - Here is the command to launch a cache node:

	java -cp target/WEB-INF/lib/hw3.jar:target/WEB-INF/lib/* edu.upenn.cis.cis555.youtube.P2PCache [num_nodes] [bootstrap node ip/hostname] [cache server port] [Pastry port]
