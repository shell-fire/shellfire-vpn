/**
 * WsLoginResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class WsLoginRequest {

  public String language;
  public String email;
  public String password;

  public WsLoginRequest(String language, String email, String pass) {
    super();
    this.language = language;
    this.email = email;
    this.password = pass;
  }

}
