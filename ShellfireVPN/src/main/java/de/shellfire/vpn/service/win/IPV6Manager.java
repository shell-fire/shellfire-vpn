package de.shellfire.vpn.service.win;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;

public class IPV6Manager {
  private static Logger log = Util.getLogger(IPV6Manager.class.getCanonicalName());
  private final static String GET_ADAPTER_LIST = Util.getWmicExe() + " nic get NetConnectionID";
  private final static String IPV6_MANAGE = "%s /%s \"%s\" ms_tcpip6";
  private static String nvspBindLocation;
  private static LinkedList<String> disabledAdapterList;
  
  private final static String SUCCESS = "finished (0)";

  public void enableIPV6OnPreviouslyDisabledDevices() {
    log.debug("enableIPV6OnPreviouslyDisabledDevices() - start");

    String nvspbind = getNvspBindLocation();
    if (nvspbind == null) {
      log.warn("nvspbind not found - did not enable ipv6 on any devices");
    } else {
      if (disabledAdapterList == null) {
          log.warn("no adapters have been disabled yet. doing nothing.");  
      } else {
        for (String adapter : disabledAdapterList) {
          String enableCommand = String.format(IPV6_MANAGE, nvspBindLocation, "e", adapter);
          String result = Util.runCommandAndReturnOutput(enableCommand);
          if (result.contains(SUCCESS)) {
            log.debug("succesfully enabled ipv6 on {}", adapter);
          }
        }
      }
      
    }

    log.debug("enableIPV6OnPreviouslyDisabledDevices() - finish");  }
  
  public void enableIPV6OnAllDevices() {
    log.debug("enableIPV6() - start");

    List<String> adapterList = getAdapterList();

    String nvspbind = getNvspBindLocation();
    if (nvspbind != null) {
      for (String adapter : adapterList) {
        String enableCommand = String.format(IPV6_MANAGE, nvspBindLocation, "e", adapter);
        String result = Util.runCommandAndReturnOutput(enableCommand);
        if (result.contains(SUCCESS)) {
          log.debug("succesfully enabled ipv6 on {}", adapter);
        }
      }
    } else {
      log.warn("nvspbind not found - did not enable ipv6 on any devices");
    }

    log.debug("enableIPV6() - finish");
  }

  public void disableIPV6OnAllDevices() {
    log.debug("disableIPV6() - start");

    String nvspbind = getNvspBindLocation();
    if (nvspbind != null) {
      List<String> adapterList = getAdapterList();
      disabledAdapterList = new LinkedList<String>();
      for (String adapter : adapterList) {
        String enableCommand = String.format(IPV6_MANAGE, nvspBindLocation, "d", adapter);
        String result = Util.runCommandAndReturnOutput(enableCommand);
        if (result.contains(SUCCESS)) {
          log.debug("succesfully disabled ipv6 on {} - adding to list", adapter);
          disabledAdapterList.add(adapter);
        }

      }
    } else {
      log.warn("nvspbind not found - did not disable ipv6 on any devices");
    }

    log.debug("disableIPV6() - finish");
  }

  private List<String> getAdapterList() {
    log.debug("getAdapterList() - start");
    String output = Util.runCommandAndReturnOutput(GET_ADAPTER_LIST);
    
    List<String> result = new LinkedList<String>();
    String[] lines = output.split("\\n");
    for (String line : lines) {
      line = line.trim();

      if (line.length() > 0) {
        result.add(line);
      }
    }

    log.debug("getAdapterList() - returning: {}", result);
    return result;
  }

  private String getNvspBindLocation() {
    if (nvspBindLocation == null) {
      Map<String, String> envs = System.getenv();
      String programFiles = envs.get("ProgramFiles");
      String programFiles86 = envs.get("ProgramFiles(x86)");
      List<String> possibleLocations = Arrays.asList("nvspbind\\nvspbind.exe", "..\\nvspbind\\nvspbind.exe",
          programFiles + "\\ShellfireVPN\\nvspbind\\nvspbind.exe", programFiles86 + "\\ShellfireVPN\\nvspbind\\nvspbind.exe",
          programFiles + "\\nvspbind\\nvspbind.exe", programFiles86 + "\\nvspbind\\nvspbind.exe",
          programFiles + "\\ShellfireVPN\\bin\\nvspbind.exe", programFiles86 + "\\ShellfireVPN\\bin\\nvspbind.exe");

      for (String location : possibleLocations) {
        if (new File(location).exists()) {
          nvspBindLocation = location;
          return location;
        }
      }
      log.error("Did not find nvspbind. Looked in these places unsuccesfully: {}", possibleLocations);
    }

    return nvspBindLocation;
  }

}
