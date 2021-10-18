package de.shellfire.vpn.webservice.model;

public class GetServerBackgroundImageFilenameRequest {

	private int serverId;

	public GetServerBackgroundImageFilenameRequest(int serverId) {
		this.serverId = serverId;
	}
	
	public int getServerId() {
		return this.serverId;
	}
	
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

}
