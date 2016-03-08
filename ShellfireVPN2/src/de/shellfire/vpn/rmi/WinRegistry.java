package de.shellfire.vpn.rmi;

import com.ice.jni.registry.NoSuchValueException;
import com.ice.jni.registry.RegDWordValue;
import com.ice.jni.registry.RegStringValue;
import com.ice.jni.registry.Registry;
import com.ice.jni.registry.RegistryKey;
import com.ice.jni.registry.RegistryValue;

import de.shellfire.vpn.gui.LoginForm;
import de.shellfire.vpn.gui.Util;
import de.shellfire.vpn.proxy.InternetOptions;

public class WinRegistry implements IVpnRegistry {
  private static final String INSTDIR = LoginForm.getInstDir();
  private static final String SHELLFIRE_VPN2_EXE = "ShellfireVPN2.exe";
  private static final String SHELLFIRE_VPN = "ShellfireVPN";
  private static final String REGKEY_CURRENTVERSION_RUN = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run\\";

  private static final String REGKEY_INTERNETSETTINGS = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\";
  private static RegistryKey key;

  public WinRegistry() {
  }

  public static void setStringValue(RegistryKey hkey, String path, String name, String value) {
    System.out.println("setStringValue(RegistryKey "+hkey+", String "+path+", String "+name+", String "+value+")");
    try {
      RegistryKey key = hkey.openSubKey(path, RegistryKey.ACCESS_WRITE);
      RegStringValue rv = new RegStringValue(key, name, value);

      key.setValue(rv);
      key.closeKey();
    } catch (Exception e) {
      Util.handleException(e);
    }
  }

  private static void setDWordValue(RegistryKey hkey, String path, String name, int value) {
    try {
      RegistryKey key = hkey.openSubKey(path, RegistryKey.ACCESS_WRITE);
      RegDWordValue rv = new RegDWordValue(key, name, RegistryValue.REG_DWORD, value);
      key.setValue(rv);
      key.closeKey();
    } catch (Exception e) {
      Util.handleException(e);
    }
  }

  private static void deleteValue(RegistryKey hkey, String keyName, String name) {
    try {
      key = Registry.openSubkey(hkey, keyName, RegistryKey.ACCESS_WRITE);
      key.deleteValue(name);
      key.closeKey();
    } catch (NoSuchValueException e) {
      // no problem, as value had not existed in the first place
      return;
    } catch (Exception e) {
      Util.handleException(e);
    }
  }

  public static String readStringValue(RegistryKey hkey, String path, String name) {
    try {
      RegistryKey key = hkey.openSubKey(path, RegistryKey.ACCESS_READ);
      RegistryValue val = key.getValue(name);
      String str = val.toString();
      key.closeKey();
      return str;

    } catch (NoSuchValueException e) {
      return null;
    } catch (Exception e) {
      Util.handleException(e);
    }

    return null;
  }

  public static int readDWordValue(RegistryKey hkey, String path, String name) {
    try {
      RegistryKey key = hkey.openSubKey(path, RegistryKey.ACCESS_READ);
      RegistryValue val = key.getValue(name);

      if (val.getType() == RegistryValue.REG_DWORD) {
        RegDWordValue dw = (RegDWordValue) val;
        int result = dw.getData();

        return result;
      }
    } catch (Exception e) {
      Util.handleException(e);
    }

    return -1000;
  }

  public static void addAutoStart(String name, String dir, String fileName, String params) {
    String value = "\"" + dir + fileName + "\" "  + params;
    
    setStringValue(Registry.HKEY_CURRENT_USER, REGKEY_CURRENTVERSION_RUN, name, value);
  }

  public static void removeAutoStart(String name) {
    deleteValue(Registry.HKEY_CURRENT_USER, REGKEY_CURRENTVERSION_RUN, name);
  }

  public void addVpnToAutoStart() {
    String fileName = SHELLFIRE_VPN2_EXE;
    String params = "minimize";

    WinRegistry.addAutoStart(SHELLFIRE_VPN, INSTDIR, fileName, params);
  }

  public void removeVpnFromAutoStart() {
    WinRegistry.removeAutoStart(SHELLFIRE_VPN);
  }

  public boolean vpnAutoStartEnabled() {
    return WinRegistry.autoStartEnabled(SHELLFIRE_VPN);
  }

  public static boolean autoStartEnabled(String name) {
    try {
      String enabled = readStringValue(Registry.HKEY_CURRENT_USER, REGKEY_CURRENTVERSION_RUN, name);
      return enabled != null;
    } catch (Exception e) {
      Util.handleException(e);
    }

    return false;
  }

  public String getAutoProxyConfigPath() {
    try {
      return readStringValue(Registry.HKEY_CURRENT_USER, REGKEY_INTERNETSETTINGS, "AutoConfigURL");
    } catch (Exception e) {
      Util.handleException(e);
    }

    return null;
  }

  public boolean isAutoProxyConfigEnabled() {
    try {
      return getAutoProxyConfigPath() != null;
    } catch (Exception e) {
      Util.handleException(e);
    }

    return false;
  }

  public static void setSystemProxy(int active) {
    try {
      WinRegistry.setDWordValue(Registry.HKEY_CURRENT_USER, REGKEY_INTERNETSETTINGS, "ProxyEnable", active);

    } catch (Exception e) {
      Util.handleException(e);
    }

  }

  public void disableSystemProxy() {
    setSystemProxy(0);
    InternetOptions.refreshSystemProxySettings();
  }


  public void enableSystemProxy() {
    setSystemProxy(1);
    InternetOptions.refreshSystemProxySettings();

  }

  
  static {
    if (Util.isWindows()) {
      System.out.println("LOADING ICE JNI");
      System.loadLibrary("ICE_JNIRegistry");
      System.out.println("DONE LOADING ICE JNI");
    } else {
      System.out.println("NOT WINDOWS - NOT LOADING ICE JNI");
    }
  }


}
