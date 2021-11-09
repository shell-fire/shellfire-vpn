package de.shellfire.vpn.webservice;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.gui.CanContinueAfterBackEndAvailableFX;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.controller.LoginController;
import de.shellfire.vpn.gui.controller.ProgressDialogController;
import de.shellfire.vpn.i18n.VpnI18N;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

public class EndpointManager {

	private static final String DELIM = ";";
	private final static String PROPERTY_ENDPOINTS = "webserviceEndPoints";
	private final static String PROPERTY_PREFERRED_ENDPOINT = "preferredWebserviceEndPoint";
	private final static String DEFAULT_PROPERTIES = "www.shellfire.de:443;www.shellfire.net:443;www.shellfire.fr;193.9.115.56:380;139.99.66.134:380;74.63.210.6:380;server58.vhorst.de:443;server13.ownz.it:380;server63.vhorst.de:380;217.182.196.58:380;server26.pow3r.de:380;server50.shellfire.co.uk:380;server45.shellfire.co.uk:380;78.46.95.38:380;server44.shellfire-vpn.de:443;server36.shellfire.fr:380;37.235.48.187:380;server52.pow3r.de:38333;server46.shellfire.fr:380;185.90.61.186:380;139.59.63.233:380;server57.ownz.it:443;server7.pow3r.de:380;203.23.128.158:380;server32.vhorst.de:45319;server35.shellfire.co.uk:380;162.252.172.100:380;server9.shellfire.co.uk:380;server27.shellfire-vpn.com:380;162.252.172.147:380;141.98.102.10:380;103.75.118.27:380;server51.anonymsurfen.de:443;server3.shellfire-vpn.com:443;server53.vhorst.de:443;136.243.72.228:380;5.9.60.246:380;server34.m-4-t-r-i-x.de:380;server64.mybouncer.de:443;23.106.80.10:380;37.120.213.50:380;37.235.55.134:380;server4.ownz.it:44598;185.224.197.84:380;server59.shellfire-vpn.de:380;server39.anonymsurfen.de:380;185.113.140.7:380;server40.m-4-t-r-i-x.de:443;158.255.215.108:380;185.150.28.32:380;server25.ownz.it:380;192.95.24.110:380;server61.shellfire.fr:380;5.254.14.162:380;server22.sixer.de:380;server38.sixer.de:443;5.9.97.217:380;185.123.101.227:380;server47.shellfirevpn.de:58650;185.150.28.29:380;server29.shellfire.co.uk:380;server43.shellfire-vpn.com:380;server60.shellfire.co.uk:380;37.235.49.49:380;92.38.163.95:380;server12.m-4-t-r-i-x.de:443;server11.anonymsurfen.de:380;51.81.51.215:380;37.143.130.168:380;server21.shellfirevpn.de:380;server54.m-4-t-r-i-x.de:380;94.76.204.84:380;185.186.79.121:380;server65.ownz.it:380;5.9.66.151:380;163.172.214.246:380;194.68.44.238:380;server6.m-4-t-r-i-x.de:380;server23.anonymsurfen.de:380;185.186.78.199:380;";
	private static I18n i18n = VpnI18N.getI18n();

	private static Logger log = Util.getLogger(EndpointManager.class.getCanonicalName());
	private static EndpointManager instance;
	private List<String> endPointList;
	private ProgressDialogController initDialogFX;
	private String preferredEndPoint;
	private boolean currentlyUsingDefaultList = false;
	private VpnProperties vpnProperties;

	private EndpointManager() {
		log.debug("EndpointManager: In the constructors");
		loadFromProperties();
	}

	private void loadFromProperties() {
		vpnProperties = VpnProperties.getInstance();

		String endPointListCsv = vpnProperties.getProperty(PROPERTY_ENDPOINTS);

		if (endPointListCsv == null || endPointListCsv.length() < 5) {
			log.debug("No Endpoints in properties file, using hard coded default list (if possible will be replaced by new list later)");
			endPointListCsv = getDefaultListCsv();
			currentlyUsingDefaultList = true;
		}

		setEndPointListFromCsv(endPointListCsv);
		setPreferredEndPoint(getPreferredEndPointFromProperties());

		// Check if JavaFX application is running. True if Platform variable is set or not null
		try {
			if (Platform.isImplicitExit() ? true : true) {
				log.debug("Dialog binding has been set");
			}
		} catch (Exception e) {
			log.error("Not Javafx Application running " + e);
		}
	}

	public String getPreferredEndPointFromProperties() {
		String preferredEndPoint = vpnProperties.getProperty(PROPERTY_PREFERRED_ENDPOINT);
		if (preferredEndPoint == null) {
			log.warn("No preferred endPoint set yet, returning default endpoint");
			return getDefaultEndPoint();
		}
		log.debug("getPreferredEndPointFromProperties() - returning: " + preferredEndPoint);
		return preferredEndPoint;
	}

	private void setPreferredEndPoint(String preferredEndPoint) {
		log.debug("setPreferredEndPoint(" + preferredEndPoint + ")");
		this.preferredEndPoint = preferredEndPoint;
		this.vpnProperties.setProperty(PROPERTY_PREFERRED_ENDPOINT, preferredEndPoint);
	}

	public static EndpointManager getInstance() {
		if (instance == null) {
			instance = new EndpointManager();
		}

		return instance;
	}

	public class FindEndpointTaskFX extends Task<String> {
		/*
		 * Main task. Executed in background thread of javaFX app.
		 */
		private CanContinueAfterBackEndAvailableFX continueFormFX;
		private boolean initDialogOriginFX;

		public FindEndpointTaskFX(CanContinueAfterBackEndAvailableFX form) {
			log.debug("FindEndpointTaskFX: Constructor of Endpoint task");
			this.continueFormFX = form;

			if (null == initDialogFX) {
				try {
					log.debug("\nFindEndpointTaskFX: In Dialog is null \n");
					initDialogFX = ProgressDialogController.getInstance("Update Check", null, LoginForms.getStage(), false);
					initDialogOriginFX = true;
					initDialogFX.getDialogStage().show();
					log.debug("Endpoint task is still null;");
				} catch (IOException ex) {
					ex.printStackTrace(System.out);
				}
			}
		}

		@Override
		protected String call() {
			log.debug("EndpointManager: start of call method");
			Platform.runLater(() -> initDialogFX.setDialogText(i18n.tr("Searching for backend connection...")));
			log.debug("Find Endpoint task method, init dialog has " + initDialogFX.toString());
			boolean result = false;

			result = testPreferredEndpoint();

			if (result) {
				log.debug("Preferred Endpoint works, using it and skipping other tests");
			} else {
				log.debug("Preferred Endpoint not working or not set yet, trying other endPoints");
				result = testEndPointList(endPointList);

				if (result) {
					log.debug("Found a working endPoint in the list, using it");
				} else {
					log.debug("Did not find an endPoint that works in the current list.");
					if (!currentlyUsingDefaultList) {
						log.debug("We're not currently using the default list, so trying the default list");
						String defaultList = getDefaultListCsv();
						setEndPointListFromCsv(defaultList);
						result = testEndPointList(endPointList);

						if (result) {
							log.debug("one of the default endPoints worked, using it");
						} else {
							log.debug("Still not found a working endPoint. know nothing else to do, giving up :-(");
						}
					}
				}
			}
			return preferredEndPoint;
		}

		/*
		 * Executed in event dispatch thread
		 */

		private boolean testPreferredEndpoint() {
			log.debug("testPreferredEndpoint() - start");
			boolean result = false;
			if (preferredEndPoint == null) {
				log.debug("No preferred endPoint set yet, not testing ");
			} else {
				log.debug("fx testing preferred endPoint {}", preferredEndPoint);
				Platform.runLater(() -> initDialogFX.setDialogText(i18n.tr("Testing endpoint that worked before...")));
				log.debug("testPreferredEndpoint - Tested endpoint that worked befores");

				result = testEndpoint(preferredEndPoint);
			}

			log.debug("testPreferredEndpoint() - finished, returning {}", result);
			return result;
		}

		public CanContinueAfterBackEndAvailableFX getContinueFormFX() {
			return continueFormFX;
		}

		public boolean isInitDialogOriginFX() {
			return initDialogOriginFX;
		}

		private boolean testEndPointList(List<String> endPointList) {
			log.debug("testEndPointList() - start");
			boolean result = false;

			for (int i = 0; i < endPointList.size() && result == false; i++) {
				// Platform.setImplicitExit(false);
				String msg = String.format(" %s / %s", (i + 1), endPointList.size());
				Platform.runLater(() -> {
					initDialogFX.setDialogText(i18n.tr("Searching for backend connection...") + msg);
				});
				String endPoint = endPointList.get(i);
				result = testEndpoint(endPoint);
			}

			log.debug("testEndPointList() - finished, returning {}", result);
			return result;
		}

		@Override
		protected void failed() {
			log.debug("testEndpointlist did not worked so failed");
		}

		@Override
		protected void succeeded() {
			log.debug("testEndpointlist worked and succeeded");
			String result = null;
			if (isInitDialogOriginFX()) {
				log.debug("end task is successfully set");
				initDialogFX.getDialogStage().hide();
			}

			log.debug("FindEndPointTask.succeeded() - calling getValue()");
			result = getValue();
			log.debug("FindEndPointTask.succeeded() - value retrieved {}", result);
			if (result == null) {
				log.debug("result is null, showing alert");
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setContentText(i18n.tr("Could not connect to the Shellfire backend - Shellfire VPN is shutting down"));
				alert.showAndWait();
				Platform.exit();
				System.exit(0);
			}
			
			if (isInitDialogOriginFX()) {
				log.debug("isInitDialogOriginFX: yes, hiding dialogStage");
				initDialogFX.getDialogStage().hide();
			}
			log.debug("calling continueFormFX.continueAfterBackEndAvailabledFX");
			this.continueFormFX.continueAfterBackEndAvailabledFX();
		}

	}

	public void ensureShellfireBackendAvailableFx(CanContinueAfterBackEndAvailableFX form) {
		log.debug("ensureShellfireBackendAvailableFx starting...");
		initDialogFX = LoginController.initProgressDialog;
		log.debug("ensureShellfireBackendAvailableFx continuation...");
		FindEndpointTaskFX taskE = new FindEndpointTaskFX(form);
		Thread t = new Thread(taskE);
		log.debug("Starting Endpoint task");
		t.start();
	}

	private boolean testEndpoint(String endPoint) {
		log.debug("testEndpoint({}) - start", endPoint);

		boolean result = false;
		WebServiceBroker broker = WebServiceBroker.getInstance();
		broker.setEndPoint(endPoint);

		try {
			String url = broker.getUrlSuccesfulConnect();
			if (url != null && url.length() > 10) {
				log.debug("successfully retrieved an answer from endPoint as a test. setting as preferred endPoint");
				setPreferredEndPoint(endPoint);
				result = true;
			} else {
				log.debug("got a response from the backend, but did not receive serverlist successfully");
			}
		} catch (Exception e) {
			log.error("Could not connect to endPoint", e);
		}

		log.debug("testEndpoint({}) - finished, returning {}", endPoint, result);
		return result;
	}

	private String getDefaultListCsv() {
		return DEFAULT_PROPERTIES;
	}

	public void setEndPointList(List<String> endPointList) {
		if (endPointList == null) {
			log.warn("setEndPointList received empty list of endpoints - not updating!");
		} else {
			this.endPointList = endPointList;
			String[] endPointArray = new String[endPointList.size()];
			endPointList.toArray(endPointArray);

			String endPointListCsv = Arrays.toString(endPointArray).replace(", ", DELIM).replaceAll("[\\[\\]]", "");
			this.vpnProperties.setProperty(PROPERTY_ENDPOINTS, endPointListCsv);
		}

	}

	private void setEndPointListFromCsv(String endPointListCsv) {
		setEndPointList(Arrays.asList(endPointListCsv.split(DELIM)));
	}

	public String getDefaultEndPoint() {
		return this.endPointList.get(0);
	}

}
