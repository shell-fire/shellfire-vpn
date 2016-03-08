package de.shellfire.vpn.proxy;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import de.shellfire.vpn.Connection;
import de.shellfire.vpn.gui.Util;
import de.shellfire.vpn.rmi.WinRegistry;

public class ProxyConfig {

  private static String host;
  private static int port;
  private static boolean proxyEnabled;

  public static void performWindows() {
	   System.setProperty("java.net.useSystemProxies", "true");
	    Proxy proxy = null;
	    proxy = getProxy();
	    if (proxy != null && proxy.type() == Proxy.Type.HTTP) {
	      InetSocketAddress addr = (InetSocketAddress) proxy.address();
	      if (addr != null) {
	        host = addr.getHostName();
	        port = addr.getPort();

	        System.setProperty("java.net.useSystemProxies", "false");
	        proxyEnabled = true;
	        
	        System.out.println("setting proxy to: " + host + ":" + port);
	        System.setProperty("http.proxyHost", host);
	        System.setProperty("http.proxyPort", ""+port);
	        System.setProperty("http.proxySet", "true");
	        
	      }

	    }

	    
	    System.setProperty("java.net.useSystemProxies", "false");
	  
	  
  }
  
  
  
  public static void perform() {
	  if (Util.isWindows()) {
		  performWindows();
	  }
   }

  public static String getHost() {
    return host;
  }

  public static int getPort() {
    return port;
  }
  
  public static String getOpenVpnConfigCommand() {
    if (host == null)
      return "";
    else 
      return "--http-proxy " + host + " " + port;
  }

  private static Proxy getProxy() {
    List<Proxy> l = null;
    try {
      ProxySelector selector = getSelector();
      
      l = selector.select(new URI("http://www.shellfire.de"));
      ProxySelector.setDefault(null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (l != null) {
      for (Iterator<Proxy> iter = l.iterator(); iter.hasNext();) {
        java.net.Proxy proxy = iter.next();
        return proxy;
      }
    }
    return null;
  }

    private static ProxySelector getSelector() throws RemoteException {
      if (proxyAutoConfigEnabled())
        return getPacProxySelector();
      else
        return ProxySelector.getDefault();
  }

    private static boolean proxyAutoConfigEnabled() throws RemoteException {
      return Connection.isAutoProxyConfigEnabled();
    }

    private static ProxySelector getPacProxySelector() throws RemoteException {
      System.setProperty(PacProxySelector.PAC_LOCATION_PROPERTY, Connection.getAutoProxyConfigPath());
      try {
        return PacProxySelector.configureFromProperties();
      } catch (Exception e) {
        Util.handleException(e);
      }
       
      
      return ProxySelector.getDefault();
    }

    public static boolean isProxyEnabled() {
        return proxyEnabled;
    }

}