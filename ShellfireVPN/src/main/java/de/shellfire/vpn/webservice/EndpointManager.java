package de.shellfire.vpn.webservice;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.gui.CanContinueAfterBackEndAvailable;
import de.shellfire.vpn.gui.LoginForm;
import de.shellfire.vpn.gui.ProgressDialog;
import de.shellfire.vpn.i18n.VpnI18N;

public class EndpointManager {

  private static final String DELIM = ";";
  private final static String PROPERTY_ENDPOINTS = "webserviceEndPoints";
  private final static String PROPERTY_PREFERRED_ENDPOINT = "preferredWebserviceEndPoint";
  private final static String DEFAULT_PROPERTIES="www.shellfire.de:443;www.shellfire.net:443;www.shellfire.fr:443;server3.shellfire-vpn.com:443;server57.ownz.it:443;server12.m-4-t-r-i-x.de:443;server36.shellfire.fr:443;server38.sixer.de:443;server40.m-4-t-r-i-x.de:443;server44.shellfire-vpn.de:443;server48.vhorst.de:443;server51.anonymsurfen.de:443;server53.vhorst.de:443;server56.pow3r.de:443;server58.vhorst.de:443;server52.pow3r.de:38333;server33.shellfire-vpn.de:24719;server20.sixer.de:33303;server32.vhorst.de:45319;server4.ownz.it:44598;server8.shellfire-vpn.com:53920;server15.shellfire-vpn.com:63221;server18.shellfire.co.uk:43821;server47.shellfirevpn.de:58650;185.186.78.168:380;192.71.249.26:380;46.246.93.202:380;37.235.48.187:380;74.63.210.6:380;148.251.46.238:380;46.166.163.89:380;94.76.204.84:380;192.95.24.110:380;158.255.215.108:380;5.9.122.238:380;37.235.49.49:380;203.23.128.158:380;37.143.130.69:380;37.235.52.74:380;37.235.55.134:380;194.68.44.238:380;213.183.56.14:380;162.252.172.100:380;217.182.196.58:380;192.71.166.11:380;5.254.81.194:380;136.243.72.228:380;5.9.66.151:380;37.235.1.182:380;5.9.97.217:380;185.150.28.32:380;163.172.214.246:380;139.99.178.30:380;163.172.209.132:380;185.150.28.29:380;162.252.172.147:380;173.234.39.186:380;176.126.83.180:380;92.38.163.95:380;23.106.80.10:380;139.99.66.134:380;103.75.118.27:380;78.46.95.38:380;185.100.86.106:380;185.186.79.121:380;185.123.101.19:380;5.254.2.66:380;91.219.236.149:380;217.78.5.131:380;129.232.222.93:380;185.150.28.100:380;185.113.140.7:380;139.59.63.233:380;188.165.232.35:380;191.96.70.22:380;5.254.14.162:380;46.36.39.150:380;185.90.61.186:380;31.13.189.118:380;141.98.102.10:380;95.174.64.102:380;192.99.216.170:380;5.9.60.246:380;193.9.115.56:380;server10.sixer.de:380;server34.m-4-t-r-i-x.de:380;server50.shellfire.co.uk:380;server5.anonymsurfen.de:380;server6.m-4-t-r-i-x.de:380;server7.pow3r.de:380;server9.shellfire.co.uk:380;server11.anonymsurfen.de:380;server13.ownz.it:380;server14.pow3r.de:380;server16.shellfire-vpn.de:380;server19.shellfire.fr:380;server21.shellfirevpn.de:380;server22.sixer.de:380;server23.anonymsurfen.de:380;server24.m-4-t-r-i-x.de:380;server25.ownz.it:380;server26.pow3r.de:380;server27.shellfire-vpn.com:380;server28.shellfire-vpn.de:380;server29.shellfire.co.uk:380;server30.shellfire.fr:380;server31.shellfirevpn.de:380;server35.shellfire.co.uk:380;server37.shellfirevpn.de:380;server39.anonymsurfen.de:380;server41.ownz.it:380;server42.pow3r.de:380;server43.shellfire-vpn.com:380;server45.shellfire.co.uk:380;server46.shellfire.fr:380;server49.sixer.de:380;server54.m-4-t-r-i-x.de:380;server55.ownz.it:380;server59.shellfire-vpn.de:380;server60.shellfire.co.uk:380;server61.shellfire.fr:380;server62.shellfirevpn.de:380;server63.vhorst.de:380";
  private static I18n i18n = VpnI18N.getI18n();
  
  private static Logger log = Util.getLogger(EndpointManager.class.getCanonicalName());
  private static EndpointManager instance;
  private List<String> endPointList;
  private ProgressDialog initDialog;
  private String preferredEndPoint;
  private boolean currentlyUsingDefaultList = false;
  private VpnProperties vpnProperties;
  
  private EndpointManager() {
    loadFromProperties();
  }
  
  private void loadFromProperties() {
    vpnProperties = VpnProperties.getInstance();
    
    String endPointListCsv = vpnProperties.getProperty(PROPERTY_ENDPOINTS);
    
    if (endPointListCsv == null || endPointListCsv.length() < 5) {
      log.debug("No Endpoints in properties file, using hard coded default list (if possible will be replaced by new list later)");
      
      endPointListCsv = getDefaultListCsv();
      currentlyUsingDefaultList  = true;
    }
    
    setEndPointListFromCsv(endPointListCsv);
    setPreferredEndPoint(getPreferredEndPointFromProperties());
    
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
    log.debug("setPreferredEndPoint("+preferredEndPoint+")");
    this.preferredEndPoint = preferredEndPoint;
    this.vpnProperties.setProperty(PROPERTY_PREFERRED_ENDPOINT, preferredEndPoint);
  }

  public static EndpointManager getInstance() {
    if (instance == null) {
      instance = new EndpointManager();  
    }
    
    return instance;
  }
  

  public class FindEndpointTask extends SwingWorker<String, Object> {
    /*
     * Main task. Executed in background thread.
     */

    private CanContinueAfterBackEndAvailable continueForm;
    private boolean initDialogOrigin;

    public FindEndpointTask(CanContinueAfterBackEndAvailable form) {
      this.continueForm = form;
      initDialog = form.getDialog();
      if (initDialog == null) {
        initDialog = new ProgressDialog(null, true, "Update Check");
        
        initDialogOrigin =true;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            initDialog.setVisible(true);
          }
        });
      }
    }

    /*
     * Executed in event dispatch thread
     */
    public void done() {
      
      if (initDialogOrigin) {
        initDialog.setVisible(false);  
      }
      
      String result = null;;
      try {
        result = get();
      } catch (InterruptedException e) {
        log.error("InterruptedException while trying to get() result", e);
      } catch (ExecutionException e) {
        log.error("ExecutionException while trying to get() result", e);
      }
      
      if (result == null) {
        JOptionPane.showMessageDialog(null, i18n.tr("Could not connect to the Shellfire backend - Shellfire VPN is shutting down"));
        System.exit(0);
      }
      if (initDialogOrigin) {
        initDialog.dispose();
      }
      
      this.continueForm.continueAfterBackEndAvailabled();
    }
    
    private boolean testPreferredEndpoint() {
      log.debug("testPreferredEndpoint() - start");
      boolean result = false;
      if (preferredEndPoint == null) {
        log.debug("No preferred endPoint set yet, not testing");
      } else {
        log.debug("testing preferred endPoint {}", preferredEndPoint);
        initDialog.setText(i18n.tr("Testing endpoint that worked before..."));
        result = testEndpoint(preferredEndPoint);
      }

      log.debug("testPreferredEndpoint() - finished, returning {}", result);
      return result;
    }
    
    private boolean testEndPointList(List<String> endPointList) {
      log.debug("testEndPointList() - start");
      boolean result = false;
      
      for (int i = 0;i < endPointList.size() && result == false; i++) {
        initDialog.setText(i18n.tr("Searching for backend connection...") + String.format("%s / %s", (i+1),  endPointList.size()));
        String endPoint = endPointList.get(i);
        result = testEndpoint(endPoint);
      }
      
      log.debug("testEndPointList() - finished, returning {}", result);
      return result;
    }
    
    @Override
    protected String doInBackground() throws Exception {

      
      initDialog.setText(i18n.tr("Searching for backend connection..."));
      
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
  } 

  public void ensureShellfireBackendAvailable(CanContinueAfterBackEndAvailable form) {
    initDialog = LoginForm.initDialog;
    FindEndpointTask task = new FindEndpointTask(form);
    task.execute();
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

    log.debug("testEndpoint(String) - finsihed, returning {}", result);
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
