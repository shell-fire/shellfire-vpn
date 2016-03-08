/**
 * ShellfireWebServicePort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.www.webservice.sf_soap_php;

public interface ShellfireWebServicePort extends java.rmi.Remote {

    /**
     * function used to login and start a session on the webservice.
     */
    public de.shellfire.www.webservice.sf_soap_php.WsLoginResult login(java.lang.String lang, java.lang.String user, java.lang.String pass) throws java.rmi.RemoteException;

    /**
     * function used to retrieve details for all vpns for a given
     * login
     */
    public de.shellfire.www.webservice.sf_soap_php.WsVpn[] getAllVpnDetails(java.lang.String user, java.lang.String pass) throws java.rmi.RemoteException;

    /**
     * Create a new free vpn for this account
     * Works only under the following conditions
     * 1) no vpn account exists for this user yet. if the user has a premium
     * account already, free account is not required!
     */
    public de.shellfire.www.webservice.sf_soap_php.WsVpn createNewFreeVpn(java.lang.String user, java.lang.String pass) throws java.rmi.RemoteException;

    /**
     * function used to retrieve the list of all shellfire vpn servers
     */
    public de.shellfire.www.webservice.sf_soap_php.WsServer[] getServerList() throws java.rmi.RemoteException;

    /**
     * function used to check if a serverswitch is allowed.
     */
    public int maySwitchToServer(java.lang.String user, java.lang.String pass, int vpnProductId, int vpnServerId) throws java.rmi.RemoteException;

    /**
     * function used to set the server of the vpn
     */
    public int setServerTo(java.lang.String user, java.lang.String pass, int vpnProductId, int vpnServerId) throws java.rmi.RemoteException;

    /**
     * function used to set the producttype of a users vpn to openvpnå
     */
    public int setProductTypeToOpenVpn(java.lang.String user, java.lang.String pass, int vpnProductId) throws java.rmi.RemoteException;

    /**
     * function used to set the protocol of the users vpn
     */
    public int setProtocolTo(java.lang.String user, java.lang.String pass, int vpnProductId, java.lang.String protocol) throws java.rmi.RemoteException;

    /**
     * function used to get the parameters for a users openvpn connection
     * (parameters to openvpn.exe)
     */
    public java.lang.String getParametersForOpenVpn(java.lang.String user, java.lang.String pass, int vpnProductId) throws java.rmi.RemoteException;

    /**
     * function used to get the certificates for a users openvpn connection
     * (stored in config folder)
     */
    public de.shellfire.www.webservice.sf_soap_php.WsFile[] getCertificatesForOpenVpn(java.lang.String user, java.lang.String pass, int vpnProductId) throws java.rmi.RemoteException;

    /**
     * function used to get the local machines current ip address.
     * this can then be used to determine whether or not
     * a vpn connection is active
     */
    public java.lang.String getLocalIpAddress() throws java.rmi.RemoteException;

    /**
     * gets the geocoordinates of the calling IP address.
     * this can be used in the sf vpn map to show the users current place
     * and to draw the line
     * to the server he is connected to
     */
    public de.shellfire.www.webservice.sf_soap_php.WsGeoPosition getLocalLocation() throws java.rmi.RemoteException;

    /**
     * Registers a new account only providing an email address and
     * password
     */
    public de.shellfire.www.webservice.sf_soap_php.WsRegistrationResult registerNewFreeAccountMac(java.lang.String lang, java.lang.String email, java.lang.String password, int subscribeToNewsletter, int isResend) throws java.rmi.RemoteException;

    /**
     * Registers a new account only providing an email address and
     * password
     */
    public de.shellfire.www.webservice.sf_soap_php.WsRegistrationResult registerNewFreeAccount(java.lang.String lang, java.lang.String email, java.lang.String password, int subscribeToNewsletter, int isResend) throws java.rmi.RemoteException;

    /**
     * Find out, if a shellfire account is active after calling registerNewFreeAccount(...)
     */
    public int accountActive(java.lang.String token) throws java.rmi.RemoteException;

    /**
     * param String the language key the table should be returned
     * in
     */
    public de.shellfire.www.webservice.sf_soap_php.VpnAttributeList getComparisonTableData(java.lang.String language) throws java.rmi.RemoteException;

    /**
     * param String the language key the table should be returned
     * in
     */
    public de.shellfire.www.webservice.sf_soap_php.TrayMessage[] getTrayMessages(java.lang.String language) throws java.rmi.RemoteException;

    /**
     * return int the latest available version
     */
    public int getLatestVersion() throws java.rmi.RemoteException;

    /**
     * return int the latest available version
     */
    public int getLatestVersionMac() throws java.rmi.RemoteException;

    /**
     * return String the filename of the latest installer
     */
    public java.lang.String getLatestInstaller() throws java.rmi.RemoteException;

    /**
     * return String the filename of the latest installer for mac
     */
    public java.lang.String getLatestInstallerMac() throws java.rmi.RemoteException;

    /**
     * return String the url of the website that is displayed after
     * a succesful connection has been established
     */
    public java.lang.String getUrlSuccesfulConnect() throws java.rmi.RemoteException;

    /**
     * return String the url of the website showing vpn help
     */
    public java.lang.String getUrlHelp() throws java.rmi.RemoteException;

    /**
     * return String the url of the webpage where premium infos can
     * be found
     */
    public java.lang.String getUrlPremiumInfo() throws java.rmi.RemoteException;

    /**
     * return String the url of the webpage where lost passwords can
     * be retrieved
     */
    public java.lang.String getUrlPasswordLost() throws java.rmi.RemoteException;

    /**
     * Upgrade a free VPN using a serial number
     */
    public de.shellfire.www.webservice.sf_soap_php.WsUpgradeResult upgradeVpnToPremiumWithCobiCode(java.lang.String user, java.lang.String pass, int vpnProductId, java.lang.String serialNumber) throws java.rmi.RemoteException;
}
