package de.shellfire.vpn.webservice.model;

public class SendLogToShellfireRequest {

  private String clientLogString;
  private String serviceLogString;

  public SendLogToShellfireRequest(String serviceLogString, String clientLogString) {
    this.serviceLogString = serviceLogString;
    this.clientLogString = clientLogString;
  }

}
