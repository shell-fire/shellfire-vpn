/**
 * WsVpn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class WsVpn {
  int iVpnId;
  int iProductTypeId;
  int iServerId;
  String sUserName;
  String sPassword;
  String eAccountType;
  String sListenHost;
  String eProtocol;
  long iPremiumUntil;

  public int getVpnId() {
    return iVpnId;
  }

  public void setVpnId(int iVpnId) {
    this.iVpnId = iVpnId;
  }

  public int getProductTypeId() {
    return iProductTypeId;
  }

  public void setProductTypeId(int iProductTypeId) {
    this.iProductTypeId = iProductTypeId;
  }

  public int getServerId() {
    return iServerId;
  }

  public void setServerId(int iServerId) {
    this.iServerId = iServerId;
  }

  public String getUserName() {
    return sUserName;
  }

  public void setUserName(String sUserName) {
    this.sUserName = sUserName;
  }

  public String getPassword() {
    return sPassword;
  }

  public void setPassword(String sPassword) {
    this.sPassword = sPassword;
  }

  public String getAccountType() {
    return eAccountType;
  }

  public void setAccountType(String eAccountType) {
    this.eAccountType = eAccountType;
  }

  public String getListenHost() {
    return sListenHost;
  }

  public void setListenHost(String sListenHost) {
    this.sListenHost = sListenHost;
  }

  public String getProtocol() {
    return eProtocol;
  }

  public void setProtocol(String eProtocol) {
    this.eProtocol = eProtocol;
  }

  public long getPremiumUntil() {
    return iPremiumUntil;
  }

  public void setPremiumUntil(long iPremiumUntil) {
    this.iPremiumUntil = iPremiumUntil;
  }

}
