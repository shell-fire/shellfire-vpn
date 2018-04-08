package de.shellfire.vpn.proxy;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

public class InternetOptions {

  public interface WinInet extends StdCallLibrary {
    final static Map<String, Object> WIN32API_OPTIONS = new HashMap<String, Object>() {
      private static final long serialVersionUID = 1L;
      {
        put(Library.OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
        put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
      }
    };

    WinInet INSTANCE = (WinInet) Native.loadLibrary("wininet", WinInet.class, WIN32API_OPTIONS);

    public boolean InternetSetOption(Pointer hInternet, int dwOption, Pointer lpBuffer, int dwBufferLength);
  }

  public static void refreshSystemProxySettings() {
    int INTERNET_OPTION_SETTINGS_CHANGED = 39;
    int INTERNET_OPTION_REFRESH = 37;
    WinInet.INSTANCE.InternetSetOption(Pointer.NULL, INTERNET_OPTION_SETTINGS_CHANGED, Pointer.NULL, 0);
    WinInet.INSTANCE.InternetSetOption(Pointer.NULL, INTERNET_OPTION_REFRESH, Pointer.NULL, 0);
  }

}
