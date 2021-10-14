package de.shellfire.vpn.webservice.model;

public class SendLogToShellfireRequest {

	private String clientLogString;
	private String serviceLogString;
	private String installLogString;
	private String wireguardLogString;
	
	public SendLogToShellfireRequest(String serviceLogString, String clientLogString, String wireguardLogString, String installLogString) {
		this.serviceLogString = serviceLogString;
		this.clientLogString = clientLogString;
		this.installLogString = installLogString;
		this.wireguardLogString = wireguardLogString;
	}

}
