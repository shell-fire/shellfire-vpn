/**
 * ShellfireWebServicePort.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;

import com.google.gson.reflect.TypeToken;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.exception.VpnException;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.model.ActivationStatus;
import de.shellfire.vpn.webservice.model.CryptoMinerConfigResponse;
import de.shellfire.vpn.webservice.model.EndPoint;
import de.shellfire.vpn.webservice.model.GeoPositionResponse;
import de.shellfire.vpn.webservice.model.GetActivationStatusRequest;
import de.shellfire.vpn.webservice.model.GetAllVpnDetailsRequest;
import de.shellfire.vpn.webservice.model.GetCertificatesForOpenVpnRequest;
import de.shellfire.vpn.webservice.model.GetComparisonTableDataRequest;
import de.shellfire.vpn.webservice.model.GetCryptoCurrencyVpnRequest;
import de.shellfire.vpn.webservice.model.GetCryptoMinerConfigRequest;
import de.shellfire.vpn.webservice.model.GetLatestInstallerRequest;
import de.shellfire.vpn.webservice.model.GetLatestVersionRequest;
import de.shellfire.vpn.webservice.model.GetLocalIpAddressRequest;
import de.shellfire.vpn.webservice.model.GetLocalLocationRequest;
import de.shellfire.vpn.webservice.model.GetParametersForOpenVpnRequest;
import de.shellfire.vpn.webservice.model.GetServerListRequest;
import de.shellfire.vpn.webservice.model.GetTrayMessagesRequest;
import de.shellfire.vpn.webservice.model.GetUrlHelpRequest;
import de.shellfire.vpn.webservice.model.GetUrlPasswordLostRequest;
import de.shellfire.vpn.webservice.model.GetUrlPremiumInfoRequest;
import de.shellfire.vpn.webservice.model.GetUrlSuccesfulConnectRequest;
import de.shellfire.vpn.webservice.model.GetWebServiceEndPointList;
import de.shellfire.vpn.webservice.model.GetWebServiceEndPointListResponse;
import de.shellfire.vpn.webservice.model.InstallerResponse;
import de.shellfire.vpn.webservice.model.LocalIPResponse;
import de.shellfire.vpn.webservice.model.LoginResponse;
import de.shellfire.vpn.webservice.model.OpenVpnParamResponse;
import de.shellfire.vpn.webservice.model.RegisterRequest;
import de.shellfire.vpn.webservice.model.SendLogToShellfireRequest;
import de.shellfire.vpn.webservice.model.SetProtocolToRequest;
import de.shellfire.vpn.webservice.model.SetServerToRequest;
import de.shellfire.vpn.webservice.model.TrayMessage;
import de.shellfire.vpn.webservice.model.UrlResponse;
import de.shellfire.vpn.webservice.model.VersionResponse;
import de.shellfire.vpn.webservice.model.VpnAttributeList;
import de.shellfire.vpn.webservice.model.WsFile;
import de.shellfire.vpn.webservice.model.WsGeoPosition;
import de.shellfire.vpn.webservice.model.WsLoginRequest;
import de.shellfire.vpn.webservice.model.WsServer;
import de.shellfire.vpn.webservice.model.WsVpn;

public class WebServiceBroker {

	private static Logger log = Util.getLogger(WebServiceBroker.class.getCanonicalName());
	private final static String ENDPOINT_TEMPLATE = "https://%s/webservice/json.php?action=";
	private String endPoint = null;

	/**
	 * The token for the login session. null if logged out.
	 */
	private static String sessionToken = null;

	private static WebServiceBroker instance;

	private WebServiceBroker() {
	}

	public static WebServiceBroker getInstance() {
		if (instance == null) {
			instance = new WebServiceBroker();
		}

		return instance;
	}

	/**
	 * function used to login and start a session on the webservice.
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public Response<LoginResponse> login(String user, String pass) throws ClientProtocolException, IOException, VpnException {
		log.debug("login ({}, xxx) - start", user);

		String lang = getLangKey();
		WsLoginRequest request = new WsLoginRequest(lang, user, pass);

		Type theType = new TypeToken<Response<LoginResponse>>() {
		}.getType();
		Response<LoginResponse> resp = new JsonHttpRequest<WsLoginRequest, LoginResponse>().call(request, theType);

		// if login okay, store token
		if (resp != null) {
			if (resp.isSuccess()) {
				LoginResponse token = resp.getData();
				if (token != null) {
					log.debug("login() succesful, storing sessionToken");
					WebServiceBroker.sessionToken = token.getToken();
				}
			}
		}

		// return full response to GUI
		log.debug("login() - finishd, returning response");
		return resp;
	}

	/**
	 * function used to retrieve details for all vpns for a given login
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public List<WsVpn> getAllVpnDetails() throws ClientProtocolException, IOException, VpnException {
		log.debug("getAllVpnDetails() - start");
		GetAllVpnDetailsRequest request = new GetAllVpnDetailsRequest();

		Type theType = new TypeToken<Response<List<WsVpn>>>() {
		}.getType();
		Response<List<WsVpn>> resp = new JsonHttpRequest<GetAllVpnDetailsRequest, List<WsVpn>>().call(request, theType);

		// ensure no errors occured and data is available. throws VPNException otherwise
		resp.validate();

		log.debug("getAllVpnDetails() - finished, returning data");
		return resp.getData();
	}

	/**
	 * function used to retrieve the list of all shellfire vpn servers
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public List<WsServer> getServerList() throws ClientProtocolException, IOException, VpnException {
		log.debug("getServerList() - start");
		GetServerListRequest request = new GetServerListRequest();

		Type theType = new TypeToken<Response<List<WsServer>>>() {
		}.getType();
		Response<List<WsServer>> resp = new JsonHttpRequest<GetServerListRequest, List<WsServer>>().call(request, theType);

		// ensure no errors occured and data is available. throws VPNException otherwise
		resp.validate();
		log.debug("getAllVpnDetails() - finished, returning data");
		return resp.getData();
	}

	public List<String> getWebServiceEndPointList() throws ClientProtocolException, IOException, VpnException {
		log.debug("getWebServiceEndPointList() - start");
		GetWebServiceEndPointList request = new GetWebServiceEndPointList();

		Type theType = new TypeToken<Response<GetWebServiceEndPointListResponse>>() {
		}.getType();
		Response<GetWebServiceEndPointListResponse> resp = new JsonHttpRequest<GetWebServiceEndPointList, GetWebServiceEndPointListResponse>()
				.call(request, theType);

		// ensure no errors occured and data is available. throws VPNException otherwise
		resp.validate();

		List<String> stringList = new LinkedList<String>();
		for (EndPoint endPoint : resp.getData().aliaslist) {
			stringList.add(endPoint.host + ":" + endPoint.port);
		}

		log.debug("getWebServiceEndPointList() - finished, returning data");
		return stringList;
	}

	/**
	 * function used to set the server of the vpn
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public Boolean setServerTo(int vpnProductId, int vpnServerId) throws ClientProtocolException, IOException, VpnException {
		log.debug("setServerTo ({}, {}) - start", vpnProductId, vpnServerId);
		SetServerToRequest request = new SetServerToRequest(vpnProductId, vpnServerId);

		Type theType = new TypeToken<Response<Void>>() {
		}.getType();
		Response<Void> resp = new JsonHttpRequest<SetServerToRequest, Void>().call(request, theType);

		Boolean result = resp != null && resp.isSuccess();

		log.debug("setServerTo () - returning result: {}", result);
		return result;
	}

	/**
	 * function used to set the protocol of the users vpn
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public Boolean setProtocolTo(int vpnProductId, String protocol) throws ClientProtocolException, IOException, VpnException {
		log.debug("setProtocolTo ({}, {}) - start", vpnProductId, protocol);

		SetProtocolToRequest request = new SetProtocolToRequest(vpnProductId, protocol);

		Type theType = new TypeToken<Response<Void>>() {
		}.getType();
		Response<Void> resp = new JsonHttpRequest<SetProtocolToRequest, Void>().call(request, theType);

		Boolean result = resp != null && resp.isSuccess();

		log.debug("setProtocolTo () - returning result: {}", result);
		return result;
	}

	/**
	 * function used to get the parameters for a users openvpn connection (parameters to openvpn.exe)
	 * 
	 * @throws VpnException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public String getParametersForOpenVpn(int vpnProductId) throws VpnException, ClientProtocolException, IOException {
		log.debug("getParametersForOpenVpn ({}) - start", vpnProductId);
		GetParametersForOpenVpnRequest request = new GetParametersForOpenVpnRequest(vpnProductId);

		Type theType = new TypeToken<Response<OpenVpnParamResponse>>() {
		}.getType();
		Response<OpenVpnParamResponse> resp = new JsonHttpRequest<GetParametersForOpenVpnRequest, OpenVpnParamResponse>().call(request,
				theType);
		resp.validate();

		String result = resp.getData().getParams();
		log.debug("getParametersForOpenVpn () - returning result: {}", result);
		return result;
	}

	/**
	 * function used to get the certificates for a users openvpn connection (stored in config folder)
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public List<WsFile> getCertificatesForOpenVpn(int vpnProductId) throws ClientProtocolException, IOException, VpnException {
		log.debug("getCertificatesForOpenVpn ({}) - start", vpnProductId);
		GetCertificatesForOpenVpnRequest request = new GetCertificatesForOpenVpnRequest(vpnProductId);

		Type theType = new TypeToken<Response<List<WsFile>>>() {
		}.getType();
		Response<List<WsFile>> resp = new JsonHttpRequest<GetCertificatesForOpenVpnRequest, List<WsFile>>().call(request, theType);
		resp.validate();

		List<WsFile> result = resp.getData();
		log.debug("getCertificatesForOpenVpn () - returning result: {}", result);
		return result;
	}

	/**
	 * function used to get the local machines current ip address. this can then be used to determine whether or not a vpn connection is
	 * active
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public String getLocalIpAddress() throws ClientProtocolException, IOException, VpnException {
		log.debug("getLocalIpAddress () - start");
		GetLocalIpAddressRequest request = new GetLocalIpAddressRequest();

		Type theType = new TypeToken<Response<LocalIPResponse>>() {
		}.getType();
		Response<LocalIPResponse> resp = new JsonHttpRequest<GetLocalIpAddressRequest, LocalIPResponse>().call(request, theType);
		resp.validate();

		String result = resp.getData().getIp();
		log.debug("getLocalIpAddress () - returning result: {}", result);
		return result;
	}

	/**
	 * gets the geocoordinates of the calling IP address. this can be used in the sf vpn map to show the users current place and to draw the
	 * line to the server he is connected to
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public WsGeoPosition getLocalLocation() throws ClientProtocolException, IOException, VpnException {
		log.debug("getLocalLocation () - start");
		GetLocalLocationRequest request = new GetLocalLocationRequest();

		Type theType = new TypeToken<Response<GeoPositionResponse>>() {
		}.getType();
		Response<GeoPositionResponse> resp = new JsonHttpRequest<GetLocalLocationRequest, GeoPositionResponse>().call(request, theType);
		resp.validate();

		GeoPositionResponse result = resp.getData();
		WsGeoPosition geoPosition = result.getLocation();
		log.debug("getLocalLocation () - returning geoPosition: {}", geoPosition);
		return geoPosition;
	}

	private String getLangKey() {
		String key = VpnI18N.getLanguage().getKey();

		return key;
	}

	/*
	 * Response to registration is in same format as login -> either error status with messages or a token to login with.
	 */
	public Response<LoginResponse> register(String email, String password, int subscribeToNewsletter)
			throws ClientProtocolException, IOException, VpnException {
		log.debug("register ({}, {}, {}) - start", email, password, subscribeToNewsletter);

		String lang = getLangKey();

		RegisterRequest request = new RegisterRequest(lang, email, password, subscribeToNewsletter);

		Type theType = new TypeToken<Response<LoginResponse>>() {
		}.getType();
		Response<LoginResponse> resp = new JsonHttpRequest<RegisterRequest, LoginResponse>().call(request, theType);

		if (resp.isSuccess()) {
			LoginResponse data = resp.getData();
			if (data != null) {
				log.debug("registration succesful - storing sessionToken for immediate login");
				LoginResponse token = data;
				WebServiceBroker.sessionToken = token.getToken();
			}
		}

		log.debug("register() - returning");
		return resp;
	}

	/**
	 * Find out, if a shellfire account is active after calling registerNewFreeAccount(...)
	 * 
	 * @throws VpnException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public boolean getIsActive() throws VpnException, ClientProtocolException, IOException {
		log.debug("getActivationStatus () - start");
		GetActivationStatusRequest request = new GetActivationStatusRequest();

		Type theType = new TypeToken<Response<ActivationStatus>>() {
		}.getType();
		Response<ActivationStatus> resp = new JsonHttpRequest<GetActivationStatusRequest, ActivationStatus>().call(request, theType);
		resp.validate();

		ActivationStatus status = resp.getData();
		log.debug("ActiovationStatus = {}", status.status);
		boolean response = status.status.equals("active");
		log.debug("getActivationStatus () - finished, returning {}", response);
		return response;
	}

	/**
	 * param String the language key the table should be returned in
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public VpnAttributeList getComparisonTableData() throws ClientProtocolException, IOException, VpnException {
		log.debug("getComparisonTableData() - start");

		String lang = getLangKey();
		GetComparisonTableDataRequest request = new GetComparisonTableDataRequest(lang);

		JsonHttpRequest<GetComparisonTableDataRequest, VpnAttributeList> jsonReq = new JsonHttpRequest<GetComparisonTableDataRequest, VpnAttributeList>();

		Type theType = new TypeToken<Response<VpnAttributeList>>() {
		}.getType();
		Response<VpnAttributeList> resp = jsonReq.call(request, theType);

		resp.validate();

		VpnAttributeList result = resp.getData();
		log.debug("getComparisonTableData () - returning result");
		return result;
	}

	/**
	 * param String the language key the table should be returned in
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public List<TrayMessage> getTrayMessages() throws ClientProtocolException, IOException, VpnException {
		log.debug("getTrayMessages() - start");

		String lang = getLangKey();
		GetTrayMessagesRequest request = new GetTrayMessagesRequest(lang);

		Type theType = new TypeToken<Response<List<TrayMessage>>>() {
		}.getType();
		Response<List<TrayMessage>> resp = new JsonHttpRequest<GetTrayMessagesRequest, List<TrayMessage>>().call(request, theType);
		resp.validate();

		List<TrayMessage> result = resp.getData();
		log.debug("getTrayMessages () - returning result");
		return result;
	}

	/**
	 * return int the latest available version
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public int getLatestVersion() throws ClientProtocolException, IOException, VpnException {
		log.debug("getLatestVersion () - start");
		GetLatestVersionRequest request = new GetLatestVersionRequest();

		Type theType = new TypeToken<Response<VersionResponse>>() {
		}.getType();
		Response<VersionResponse> resp = new JsonHttpRequest<GetLatestVersionRequest, VersionResponse>().call(request, theType);
		resp.validate();

		int latestVersion = resp.getData().getVersion();

		log.debug("getLatestVersion () - finished, returning {}", latestVersion);
		return latestVersion;
	}

	/**
	 * return String the filename of the latest installer
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public String getLatestInstaller() throws ClientProtocolException, IOException, VpnException {
		log.debug("getLatestInstaller () - start");
		GetLatestInstallerRequest request = new GetLatestInstallerRequest();

		Type theType = new TypeToken<Response<InstallerResponse>>() {
		}.getType();
		Response<InstallerResponse> resp = new JsonHttpRequest<GetLatestInstallerRequest, InstallerResponse>().call(request, theType);
		resp.validate();

		String installer = resp.getData().getInstaller();

		log.debug("getLatestInstaller () - finished, returning {}", installer);
		return installer;
	}

	/**
	 * return String the url of the website that is displayed after a succesful connection has been established
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public String getUrlSuccesfulConnect() throws ClientProtocolException, IOException, VpnException {
		log.debug("getUrlSuccesfulConnect () - start");
		GetUrlSuccesfulConnectRequest request = new GetUrlSuccesfulConnectRequest();

		Type theType = new TypeToken<Response<UrlResponse>>() {
		}.getType();
		Response<UrlResponse> resp = new JsonHttpRequest<GetUrlSuccesfulConnectRequest, UrlResponse>().call(request, theType);
		resp.validate();

		String url = resp.getData().getUrl();

		log.debug("getUrlSuccesfulConnect () - finished, returning {}", url);
		return url;
	}

	/**
	 * return String the url of the website showing vpn help
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public String getUrlHelp() throws ClientProtocolException, IOException, VpnException {
		log.debug("getUrlHelp () - start");
		GetUrlHelpRequest request = new GetUrlHelpRequest();

		Type theType = new TypeToken<Response<UrlResponse>>() {
		}.getType();
		Response<UrlResponse> resp = new JsonHttpRequest<GetUrlHelpRequest, UrlResponse>().call(request, theType);
		resp.validate();

		String url = resp.getData().getUrl();

		log.debug("getUrlHelp () - finished, returning {}", url);
		return url;
	}

	/**
	 * return String the url of the webpage where premium infos can be found
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public String getUrlPremiumInfo() throws ClientProtocolException, IOException, VpnException {
		log.debug("getUrlPremiumInfo () - start");
		GetUrlPremiumInfoRequest request = new GetUrlPremiumInfoRequest();

		Type theType = new TypeToken<Response<UrlResponse>>() {
		}.getType();
		Response<UrlResponse> resp = new JsonHttpRequest<GetUrlPremiumInfoRequest, UrlResponse>().call(request, theType);
		resp.validate();

		String url = resp.getData().getUrl();

		log.debug("getUrlPremiumInfo () - finished, returning {}", url);
		return url;
	}

	/**
	 * return String the url of the webpage where lost passwords can be retrieved
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public String getUrlPasswordLost() throws ClientProtocolException, IOException, VpnException {
		log.debug("getUrlPasswordLost () - start");
		GetUrlPasswordLostRequest request = new GetUrlPasswordLostRequest();

		Type theType = new TypeToken<Response<UrlResponse>>() {
		}.getType();
		Response<UrlResponse> resp = new JsonHttpRequest<GetUrlPasswordLostRequest, UrlResponse>().call(request, theType);
		resp.validate();

		String url = resp.getData().getUrl();

		log.debug("getUrlPasswordLost () - finished, returning {}", url);
		return url;
	}

	/**
	 * return String the json string to configure the crypto miner
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws VpnException
	 */
	public String getCryptoMinerConfig() throws ClientProtocolException, IOException, VpnException {
		log.debug("getCryptoMinerConfig () - start");
		GetCryptoMinerConfigRequest request = new GetCryptoMinerConfigRequest();

		Type theType = new TypeToken<Response<CryptoMinerConfigResponse>>() {
		}.getType();
		Response<CryptoMinerConfigResponse> resp = new JsonHttpRequest<GetCryptoMinerConfigRequest, CryptoMinerConfigResponse>()
				.call(request, theType);
		resp.validate();

		String config = resp.getData().getConfig();

		log.debug("getCryptoMinerConfig () - finished, returning {}", config);
		return config;
	}

	public static boolean isLoggedIn() {
		return sessionToken != null;
	}

	public static String getSessionToken() {
		return sessionToken;
	}

	public Boolean sendLogToShellfire(String serviceLogString, String clientLogString, String installLogString)
			throws ClientProtocolException, IOException, VpnException {
		log.debug("sendLogToShellfire() - start");

		serviceLogString = Util.encodeBase64(serviceLogString);
		clientLogString = Util.encodeBase64(clientLogString);
		installLogString = Util.encodeBase64(installLogString);

		SendLogToShellfireRequest request = new SendLogToShellfireRequest(serviceLogString, clientLogString, installLogString);

		Type theType = new TypeToken<Response<Void>>() {
		}.getType();
		Response<Void> resp = new JsonHttpRequest<SendLogToShellfireRequest, Void>().call(request, theType);

		Boolean result = resp != null && resp.isSuccess();

		log.debug("sendLogToShellfire () - returning result: {}", result);
		return result;
	}

	public void setEndPoint(String endpoint) {
		this.endPoint = String.format(ENDPOINT_TEMPLATE, endpoint);
	}

	public String getEndPoint() {
		if (endPoint == null) {
			setEndPoint(EndpointManager.getInstance().getPreferredEndPointFromProperties());
		}
		return this.endPoint;
	}

	public List<String> getCryptoCurrencyVpn() throws ClientProtocolException, IOException, VpnException {
		log.debug("getCryptoCurrencyVpn() - start");

		GetCryptoCurrencyVpnRequest request = new GetCryptoCurrencyVpnRequest();

		Type theType = new TypeToken<Response<List<String>>>() {
		}.getType();
		Response<List<String>> resp = new JsonHttpRequest<GetCryptoCurrencyVpnRequest, List<String>>().call(request, theType);
		resp.validate();

		List<String> result = resp.getData();
		log.debug("getCryptoCurrencyVpn () - returning result");
		return result;
	}
}
