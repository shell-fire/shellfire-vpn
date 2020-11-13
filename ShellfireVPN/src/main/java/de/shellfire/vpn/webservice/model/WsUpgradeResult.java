/**
 * WsUpgradeResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class WsUpgradeResult implements java.io.Serializable {
  private java.lang.String error;

  private java.lang.String eAccountType;

  private int iUpgradeUntil;

  private int upgradeSuccesful;

  public WsUpgradeResult() {
  }

  public WsUpgradeResult(java.lang.String error, java.lang.String eAccountType, int iUpgradeUntil, int upgradeSuccesful) {
    this.error = error;
    this.eAccountType = eAccountType;
    this.iUpgradeUntil = iUpgradeUntil;
    this.upgradeSuccesful = upgradeSuccesful;
  }

  /**
   * Gets the error value for this WsUpgradeResult.
   * 
   * @return error
   */
  public java.lang.String getError() {
    return error;
  }

  /**
   * Sets the error value for this WsUpgradeResult.
   * 
   * @param error
   */
  public void setError(java.lang.String error) {
    this.error = error;
  }

  /**
   * Gets the eAccountType value for this WsUpgradeResult.
   * 
   * @return eAccountType
   */
  public java.lang.String getEAccountType() {
    return eAccountType;
  }

  /**
   * Sets the eAccountType value for this WsUpgradeResult.
   * 
   * @param eAccountType
   */
  public void setEAccountType(java.lang.String eAccountType) {
    this.eAccountType = eAccountType;
  }

  /**
   * Gets the iUpgradeUntil value for this WsUpgradeResult.
   * 
   * @return iUpgradeUntil
   */
  public int getIUpgradeUntil() {
    return iUpgradeUntil;
  }

  /**
   * Sets the iUpgradeUntil value for this WsUpgradeResult.
   * 
   * @param iUpgradeUntil
   */
  public void setIUpgradeUntil(int iUpgradeUntil) {
    this.iUpgradeUntil = iUpgradeUntil;
  }

  /**
   * Gets the upgradeSuccesful value for this WsUpgradeResult.
   * 
   * @return upgradeSuccesful
   */
  public int getUpgradeSuccesful() {
    return upgradeSuccesful;
  }

  /**
   * Sets the upgradeSuccesful value for this WsUpgradeResult.
   * 
   * @param upgradeSuccesful
   */
  public void setUpgradeSuccesful(int upgradeSuccesful) {
    this.upgradeSuccesful = upgradeSuccesful;
  }

}
