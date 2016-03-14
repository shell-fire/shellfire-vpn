package de.shellfire.vpn.service.win;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ice.jni.registry.NoSuchValueException;
import com.ice.jni.registry.RegDWordValue;
import com.ice.jni.registry.RegStringValue;
import com.ice.jni.registry.Registry;
import com.ice.jni.registry.RegistryKey;
import com.ice.jni.registry.RegistryValue;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForm;
import de.shellfire.vpn.proxy.InternetOptions;
import de.shellfire.vpn.service.IVpnRegistry;

public class WinRegistry implements IVpnRegistry {
  private static final String ICE_JNI_REGISTRY_DLL = "ICE_JNIRegistry";

  private static final String LIB_ICE_JNI_REGISTRY_AMD64 = "lib/ICE_JNIRegistry_amd64.dll";

  private static final String LIB_ICE_JNI_REGISTRY_X86 = "lib/ICE_JNIRegistry_x86.dll";

  private static Logger log = LoggerFactory.getLogger(WinRegistry.class.getCanonicalName());
  
  private static final String INSTDIR = LoginForm.getInstDir();
  private static final String SHELLFIRE_VPN2_EXE = "ShellfireVPN2.exe";
  private static final String SHELLFIRE_VPN = "ShellfireVPN";
  private static final String REGKEY_CURRENTVERSION_RUN = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run\\";

  private static final String REGKEY_INTERNETSETTINGS = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\";
  private static RegistryKey key;

  public WinRegistry() {
  }

  public static void setStringValue(RegistryKey hkey, String path, String name, String value) {
    log.debug("setStringValue(RegistryKey "+hkey+", String "+path+", String "+name+", String "+value+")");
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

  public void enableAutoStart() {
    String fileName = SHELLFIRE_VPN2_EXE;
    String params = "minimize";

    WinRegistry.addAutoStart(SHELLFIRE_VPN, INSTDIR, fileName, params);
  }

  public void disableAutoStart() {
    WinRegistry.removeAutoStart(SHELLFIRE_VPN);
  }

  public boolean autoStartEnabled() {
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

  public boolean autoProxyConfigEnabled() {
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
      log.debug("LOADING ICE JNI");
      String jvmArch = System.getProperty("sun.arch.data.model");
      String lib = LIB_ICE_JNI_REGISTRY_X86;
      if (jvmArch.equals("32")) {
        lib = LIB_ICE_JNI_REGISTRY_X86;
        
      } else if (jvmArch.equals("64")) {
        lib = LIB_ICE_JNI_REGISTRY_AMD64;
      } else {
        log.warn("Could not determin architecture of jvm - trying to load 32 bit ICE JNI");
      }
      Path libPath = FileSystems.getDefault().getPath(lib);
      Path libPathDest = FileSystems.getDefault().getPath(ICE_JNI_REGISTRY_DLL+".DLL");
      try {
        Files.copy(libPath, libPathDest, REPLACE_EXISTING);
      } catch (IOException e) {
        // Util.handleException(e);
      }
      

      log.debug("Loading: {}", ICE_JNI_REGISTRY_DLL);
      System.loadLibrary(ICE_JNI_REGISTRY_DLL);  
      
      log.debug("DONE LOADING ICE JNI");
    } else {
      log.debug("NOT WINDOWS - NOT LOADING ICE JNI");
    }
  }


}
