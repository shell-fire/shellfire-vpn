package de.shellfire.vpn.proxy;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

/**
 * <p>
 * A {@link ProxySelector} implementation that supports proxy auto-config (PAC) files, as summarized at
 * <a href="http://en.wikipedia.org./wiki/Proxy_auto-config" >http://en.wikipedia.org./wiki/Proxy_auto-config</a>. (Look for the links to
 * the archived Netscape documentation.)
 * </p>
 * 
 * @author Mark A. Ziesemer <a href="http://www.ziesemer.com.">&lt;www.ziesemer.com&gt;</a>
 */
public class PacProxySelector extends ProxySelector {

  public static final String PAC_LOCATION_PROPERTY = "proxy.autoConfig";

  protected static final Logger logGER = Logger.getLogger(PacProxySelector.class.getName());
  protected static final Pattern PAC_RESULT_PATTERN = Pattern.compile("(DIRECT|PROXY|SOCKS)(?:\\s+(\\S+):(\\d+))?(?:;|\\z)");

  protected Context context;
  protected Scriptable scriptable;

  /**
   * <p>
   * Convenience method that calls {@link ProxySelector#setDefault(ProxySelector)} with the result from {@link #configureFromProperties()}.
   * </p>
   */
  public static ProxySelector setDefaultFromProperties() throws Exception {
    ProxySelector ps = configureFromProperties();
    ProxySelector.setDefault(ps);
    return ps;
  }

  /**
   * <p>
   * Returns a new {@link ProxySelector} instance. Checks {@link System#getProperty(String)} then {@link System#getenv()} for a value named
   * {@link #PAC_LOCATION_PROPERTY}.
   * </p>
   * <p>
   * If found, a new {@link PacProxySelector} is returned using the value as the location of the PAC script. Values must be a valid input to
   * {@link URL#URL(String)}. Files from the local file system may be supported using <code>file://</code> URLs.
   * </p>
   * <p>
   * Otherwise, the current selector is returned from {@link ProxySelector#getDefault()}.
   * </p>
   */
  public static ProxySelector configureFromProperties() throws Exception {
    String urlStr = System.getProperty(PAC_LOCATION_PROPERTY);
    if (urlStr != null) {
      logGER.log(Level.CONFIG, "Found proxy.autoConfig system property: {0}", urlStr);
    } else {
      urlStr = System.getenv(PAC_LOCATION_PROPERTY);
      if (urlStr != null) {
        logGER.log(Level.CONFIG, "Found proxy.autoConfig environment variable: {0}", urlStr);
      }
    }
    if (urlStr != null) {
      URL url = new URL(urlStr);
      return new PacProxySelector(new InputStreamReader(url.openStream()));
    }
    logGER.info("No Proxy Auto-Configuration setting found.  Returning ProxySelector.getDefault()...");
    return ProxySelector.getDefault();
  }

  /**
   * @param pacReader
   *          A {@link Reader} to a PAC script.
   */
  public PacProxySelector(Reader pacReader) throws Exception {
    init(pacReader);
  }

  protected void init(Reader pacReader) throws Exception {
    try {
      Context c = this.context = new ContextFactory().enterContext();
      try {
        c.setClassShutter(new PacClassShutter());
        Scriptable s = this.scriptable = c.initStandardObjects();
        registerFunction("alert", PacFunctions.class.getMethod("alert", String.class), s);
        registerFunction("myIpAddress", PacFunctions.class.getMethod("myIpAddress"), s);
        registerFunction("dnsResolve", PacFunctions.class.getMethod("dnsResolve", String.class), s);

        InputStreamReader isrUtils = new InputStreamReader(getClass().getResourceAsStream("PacUtils.js"));
        try {
          c.evaluateReader(s, isrUtils, null, 1, null);
        } finally {
          isrUtils.close();
        }

        try {
          c.evaluateReader(s, pacReader, null, 1, null);
        } finally {
          pacReader.close();
        }
      } finally {
        Context.exit();
      }
    } finally {
      pacReader.close();
    }
  }

  protected void registerFunction(String name, Method m, Scriptable s) {
    FunctionObject fo = new FunctionObject(name, m, s);
    ScriptableObject.putProperty(s, name, fo);
  }

  @Override
  public List<Proxy> select(URI uri) {
    if (uri == null) {
      throw new IllegalArgumentException("uri must not be null.");
    }
    String pacResult = findProxyForUrl(uri);
    List<Proxy> result = convert(pacResult);
    logGER.log(Level.FINE, "Returning {0} for {1}.", new Object[] { result, uri });
    return result;
  }

  protected String findProxyForUrl(URI uri) {
    Context c = new ContextFactory().enterContext(this.context);
    try {
      Scriptable s = this.scriptable;
      // Considered caching, but would prevent a possibility where the function could rewrite itself.
      Object fObj = ScriptableObject.getProperty(s, "FindProxyForURL");
      if (!(fObj instanceof Callable)) {
        logGER.log(Level.WARNING, "No FindProxyForURL function found: {0}", fObj);
        return null;
      }
      Callable f = (Callable) fObj;
      Object scriptResultObj = f.call(c, s, s, new Object[] { uri.toString(), uri.getHost() });
      if (scriptResultObj == null) {
        logGER.log(Level.WARNING, "Null result from FindProxyForURL: {0}", uri);
        return null;
      }
      return scriptResultObj.toString();
    } finally {
      Context.exit();
    }
  }

  protected List<Proxy> convert(String pacResult) {
    List<Proxy> result = new LinkedList<Proxy>();
    if (pacResult != null) {
      convert(pacResult, result);
    }
    if (result.isEmpty()) {
      // Mozilla Firefox, as visible in nsPluginHostImpl.cpp, defaults to "DIRECT" on any unexpected returns.
      logGER.warning("Empty or invalid result from FindProxyForURL.  Returning default of DIRECT...");
      result.add(Proxy.NO_PROXY);
    }
    return result;
  }

  protected void convert(String pacResult, List<Proxy> result) {
    Matcher m = PAC_RESULT_PATTERN.matcher(pacResult);
    while (m.find()) {
      String scriptProxyType = m.group(1);
      if ("DIRECT".equals(scriptProxyType)) {
        result.add(Proxy.NO_PROXY);
      } else {
        Type proxyType;
        if ("PROXY".equals(scriptProxyType)) {
          proxyType = Type.HTTP;
        } else if ("SOCKS".equals(scriptProxyType)) {
          proxyType = Type.SOCKS;
        } else {
          // Should never happen, already filtered by Pattern.
          throw new RuntimeException("Unrecognized proxy type.");
        }
        result.add(new Proxy(proxyType, new InetSocketAddress(m.group(2), Integer.parseInt(m.group(3)))));
      }
    }
  }

  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    logGER.log(Level.WARNING, "connectFailed: " + uri + ", " + sa, ioe);
    Context c = new ContextFactory().enterContext(this.context);
    try {
      Scriptable s = this.scriptable;
      Object fObj = ScriptableObject.getProperty(s, "connectFailed");
      if (!(fObj instanceof Callable)) {
        logGER.log(Level.FINE, "No connectFailed function found: {0}", fObj);
        return;
      }
      ((Callable) fObj).call(c, s, s, new Object[] { uri.toString(), sa.toString(), ioe.toString() });
    } finally {
      Context.exit();
    }
  }

  protected static class PacFunctions {

    public static void alert(String s) {
      logGER.log(Level.INFO, "PAC-alert: {0}", s);
    }

    public static String myIpAddress() {
      if (logGER.isLoggable(Level.FINE)) {
        logGER.fine("myIpAddress called.");
      }
      try {
        return InetAddress.getLocalHost().getHostAddress();
      } catch (Exception ex) {
        throw new WrappedException(ex);
      }
    }

    public static String dnsResolve(String name) {
      if (logGER.isLoggable(Level.FINE)) {
        logGER.log(Level.FINE, "dnsResolve called: {0}", name);
      }
      try {
        return InetAddress.getByName(name).getHostAddress();
      } catch (UnknownHostException uhe) {
        logGER.log(Level.WARNING, "dnsResolve returning null for: {0}", name);
        return null;
      }
    }
  }

  /**
   * <p>
   * Hack for
   * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6782031" >http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6782031</a>
   * and <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=468385" >https://bugzilla.mozilla.org/show_bug.cgi?id=468385</a>.
   * </p>
   */
  protected static class PacClassShutter implements ClassShutter {
    public boolean visibleToScripts(String fullClassName) {
      logGER.log(Level.WARNING, "visibleToScripts returning false for: {0}", fullClassName);
      return false;
    }
  }
}
