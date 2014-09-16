package edu.upenn.cis.cis555.youtube;

import rice.p2p.commonapi.NodeHandle;

public class DHTMessage implements rice.p2p.commonapi.Message {
	NodeHandle from;
	String content;
	boolean wantResponse = true;
	
	public DHTMessage(NodeHandle from, String content){
		this.from = from;
		this.content = content;
	}

	//Make the Message interface happy...
	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}
}
