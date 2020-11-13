package de.shellfire.vpn.webservice.model;

public class SendLogToShellfireRequest {

	private String clientLogString;
	private String serviceLogString;
	private String installLogString;

	public SendLogToShellfireRequest(String serviceLogString, String clientLogString, String installLogString) {
		this.serviceLogString = serviceLogString;
		this.clientLogString = clientLogString;
		this.installLogString = installLogString;
	}

}
