package de.shellfire.vpn.proxy;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.Client;

public class ProxyConfig {
  private static Logger log = Util.getLogger(ProxyConfig.class.getCanonicalName());
  private static String host;
  private static int port;
  private static boolean proxyEnabled;

  public static void performWindows() {
    log.debug("ProxyConfig - In performWindows method");
    System.setProperty("java.net.useSystemProxies", "true");
    // Proxy proxy = null;
    // proxy = getProxy();
    Proxy proxy = getProxy();
    if (proxy != null && proxy.type() == Proxy.Type.HTTP) {
      InetSocketAddress addr = (InetSocketAddress) proxy.address();
      if (addr != null) {
        host = addr.getHostName();
        port = addr.getPort();

        System.setProperty("java.net.useSystemProxies", "false");
        proxyEnabled = true;

        log.debug("setting proxy to: " + host + ":" + port);
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", "" + port);
        System.setProperty("http.proxySet", "true");

      }

    }

    System.setProperty("java.net.useSystemProxies", "false");

  }

  public static void perform() {
    if (Util.isWindows()) {
      performWindows();
      log.debug("In the ProxyConfig class");
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
      log.debug("ProxyConfig: selector is " + selector.toString());
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

  private static ProxySelector getSelector() {
    if (proxyAutoConfigEnabled())
      return getPacProxySelector();
    else
      log.debug("Returning default Proxy selector");
    return ProxySelector.getDefault();
  }

  private static boolean proxyAutoConfigEnabled() {
    return Client.isAutoProxyConfigEnabled();
  }

  private static ProxySelector getPacProxySelector() {
    System.setProperty(PacProxySelector.PAC_LOCATION_PROPERTY, Client.getAutoProxyConfigPath());
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