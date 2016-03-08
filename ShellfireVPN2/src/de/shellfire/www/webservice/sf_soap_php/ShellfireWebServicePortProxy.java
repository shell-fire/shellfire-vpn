package de.shellfire.www.webservice.sf_soap_php;

public class ShellfireWebServicePortProxy implements de.shellfire.www.webservice.sf_soap_php.ShellfireWebServicePort {
  private String _endpoint = null;
  private de.shellfire.www.webservice.sf_soap_php.ShellfireWebServicePort shellfireWebServicePort = null;
  
  public ShellfireWebServicePortProxy() {
    _initShellfireWebServicePortProxy();
  }
  
  public ShellfireWebServicePortProxy(String endpoint) {
    _endpoint = endpoint;
    _initShellfireWebServicePortProxy();
  }
  
  private void _initShellfireWebServicePortProxy() {
    try {
      shellfireWebServicePort = (new de.shellfire.www.webservice.sf_soap_php.ShellfireWebServiceServiceLocator()).getShellfireWebServicePort();
      if (shellfireWebServicePort != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)shellfireWebServicePort)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)shellfireWebServicePort)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (shellfireWebServicePort != null)
      ((javax.xml.rpc.Stub)shellfireWebServicePort)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public de.shellfire.www.webservice.sf_soap_php.ShellfireWebServicePort getShellfireWebServicePort() {
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort;
  }
  
  public de.shellfire.www.webservice.sf_soap_php.WsLoginResult login(java.lang.String lang, java.lang.String user, java.lang.String pass) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.login(lang, user, pass);
  }
  
  public de.shellfire.www.webservice.sf_soap_php.WsVpn[] getAllVpnDetails(java.lang.String user, java.lang.String pass) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getAllVpnDetails(user, pass);
  }
  
  public de.shellfire.www.webservice.sf_soap_php.WsVpn createNewFreeVpn(java.lang.String user, java.lang.String pass) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.createNewFreeVpn(user, pass);
  }
  
  public de.shellfire.www.webservice.sf_soap_php.WsServer[] getServerList() throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getServerList();
  }
  
  public int maySwitchToServer(java.lang.String user, java.lang.String pass, int vpnProductId, int vpnServerId) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.maySwitchToServer(user, pass, vpnProductId, vpnServerId);
  }
  
  public int setServerTo(java.lang.String user, java.lang.String pass, int vpnProductId, int vpnServerId) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.setServerTo(user, pass, vpnProductId, vpnServerId);
  }
  
  public int setProductTypeToOpenVpn(java.lang.String user, java.lang.String pass, int vpnProductId) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.setProductTypeToOpenVpn(user, pass, vpnProductId);
  }
  
  public int setProtocolTo(java.lang.String user, java.lang.String pass, int vpnProductId, java.lang.String protocol) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.setProtocolTo(user, pass, vpnProductId, protocol);
  }
  
  public java.lang.String getParametersForOpenVpn(java.lang.String user, java.lang.String pass, int vpnProductId) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getParametersForOpenVpn(user, pass, vpnProductId);
  }
  
  public de.shellfire.www.webservice.sf_soap_php.WsFile[] getCertificatesForOpenVpn(java.lang.String user, java.lang.String pass, int vpnProductId) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getCertificatesForOpenVpn(user, pass, vpnProductId);
  }
  
  public java.lang.String getLocalIpAddress() throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getLocalIpAddress();
  }
  
  public de.shellfire.www.webservice.sf_soap_php.WsGeoPosition getLocalLocation() throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getLocalLocation();
  }

  public de.shellfire.www.webservice.sf_soap_php.WsRegistrationResult registerNewFreeAccountMac(java.lang.String lang, java.lang.String email, java.lang.String password, int subscribeToNewsletter, int isResend) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.registerNewFreeAccountMac(lang, email, password, subscribeToNewsletter, isResend);
  }

  public de.shellfire.www.webservice.sf_soap_php.WsRegistrationResult registerNewFreeAccount(java.lang.String lang, java.lang.String email, java.lang.String password, int subscribeToNewsletter, int isResend) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.registerNewFreeAccount(lang, email, password, subscribeToNewsletter, isResend);
  }
  
  public int accountActive(java.lang.String token) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.accountActive(token);
  }
  
  public de.shellfire.www.webservice.sf_soap_php.VpnAttributeList getComparisonTableData(java.lang.String language) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getComparisonTableData(language);
  }
  
  public de.shellfire.www.webservice.sf_soap_php.TrayMessage[] getTrayMessages(java.lang.String language) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getTrayMessages(language);
  }
  
  public int getLatestVersion() throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getLatestVersion();
  }  
  
  public int getLatestVersionMac() throws java.rmi.RemoteException{
	    if (shellfireWebServicePort == null)
	      _initShellfireWebServicePortProxy();
	    return shellfireWebServicePort.getLatestVersionMac();
	  }
  
  public java.lang.String getLatestInstaller() throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getLatestInstaller();
  }  
  
  public java.lang.String getLatestInstallerMac() throws java.rmi.RemoteException{
	    if (shellfireWebServicePort == null)
	      _initShellfireWebServicePortProxy();
	    return shellfireWebServicePort.getLatestInstallerMac();
	  }
  
  public java.lang.String getUrlSuccesfulConnect() throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getUrlSuccesfulConnect();
  }
  
  public java.lang.String getUrlHelp() throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getUrlHelp();
  }
  
  public java.lang.String getUrlPremiumInfo() throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getUrlPremiumInfo();
  }
  
  public java.lang.String getUrlPasswordLost() throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.getUrlPasswordLost();
  }
  
  public de.shellfire.www.webservice.sf_soap_php.WsUpgradeResult upgradeVpnToPremiumWithCobiCode(java.lang.String user, java.lang.String pass, int vpnProductId, java.lang.String serialNumber) throws java.rmi.RemoteException{
    if (shellfireWebServicePort == null)
      _initShellfireWebServicePortProxy();
    return shellfireWebServicePort.upgradeVpnToPremiumWithCobiCode(user, pass, vpnProductId, serialNumber);
  }
  
  
}