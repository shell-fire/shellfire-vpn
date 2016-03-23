package de.shellfire.vpn.service.win;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.slf4j.Logger;

import de.shellfire.vpn.Util;

public class IPV6Manager {
  
  private static final String LIB_PCAP_REGISTRY_DLL = "jnetpcap";
  private static final String LIB_PCAP_JNI_REGISTRY_AMD64 = "lib/jnetpcap_amd64.dll";
  private static final String LIB_PCAP_JNI_REGISTRY_X86 = "lib/jnetpcap_x68.dll";
  Pattern p = Pattern.compile(".*\\{(.*)\\}");

  private static Logger log = Util.getLogger(IPV6Manager.class.getCanonicalName());
  private final static String IPV6_MANAGE = "%s /%s \"%s\" ms_tcpip6";
  private static String nvspBindLocation;
  private static LinkedList<String> disabledAdapterList;
  
  private final static String SUCCESS = "finished (0)";

  static {
    if (Util.isWindows()) {
      log.debug("LOADING LIBPCAP JNI");
      log.debug("Path is: {}", System.getProperty("java.library.path"));
      String jvmArch = System.getProperty("sun.arch.data.model");
      String lib = LIB_PCAP_REGISTRY_DLL;
      if (jvmArch.equals("32")) {
        lib = LIB_PCAP_JNI_REGISTRY_X86;
        
      } else if (jvmArch.equals("64")) {
        lib = LIB_PCAP_JNI_REGISTRY_AMD64;
      } else {
        log.warn("Could not determin architecture of jvm - trying to load 32 bit LIBPCAP JNI");
      }
      Path libPath = FileSystems.getDefault().getPath(lib);
      Path libPathDest = FileSystems.getDefault().getPath(LIB_PCAP_REGISTRY_DLL+".DLL");
      try {
        Files.copy(libPath, libPathDest, REPLACE_EXISTING);
      } catch (IOException e) {
        // Util.handleException(e);
      }
      

      log.debug("Loading: {}", LIB_PCAP_REGISTRY_DLL);
      System.loadLibrary(LIB_PCAP_REGISTRY_DLL);  
      
      log.debug("DONE LOADING LIBPCAP JNI");
    } else {
      log.debug("NOT WINDOWS - NOT LOADING LIBPCAP JNI");
    }
  }

  
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
          String[] enableCommand = String.format(IPV6_MANAGE, nvspBindLocation, "e", adapter).split(" ");
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
        String[] enableCommand = String.format(IPV6_MANAGE, nvspBindLocation, "e", adapter).split(" ");
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
        String[] enableCommand = String.format(IPV6_MANAGE, nvspBindLocation, "d", adapter).split(" ");
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
    
    List<PcapIf> alldevs = new ArrayList<PcapIf>(); // Will be filled with NICs  
    StringBuilder errbuf = new StringBuilder(); // For any error msgs  

    int r = Pcap.findAllDevs(alldevs, errbuf);  
    if (r == Pcap.NOT_OK || alldevs.isEmpty()) {  
      log.error("Can't read list of devices, error is {}", errbuf.toString());  
     
    }
    
    List<String> result = new LinkedList<String>();
    for (PcapIf device : alldevs) {
      String name = device.getName();
      String guid = "{" + this.extractGUID(name) + "}";
      
      result.add(guid);
    }
      

    log.debug("getAdapterList() - returning: {}", result);
    return result;
  }
  
  private String extractGUID(String name) {
    Matcher m = p.matcher(name);

    String result = null;
    if (m.find()) {
       result = m.group(1);
    }
    
    log.debug("extractGUID({}} - returning {}", name, result); 
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
