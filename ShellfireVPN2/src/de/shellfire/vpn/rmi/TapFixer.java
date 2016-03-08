package de.shellfire.vpn.rmi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.shellfire.vpn.gui.IConsole;
import de.shellfire.vpn.gui.Util;

public class TapFixer {
  public static void main(String[] args) {
    restartAllTapDevices();
  }

  private static String getTapPath() {
    IConsole console = Console.getInstance();
    console.append("getTapPath() - start");
    Map<String, String> envs = System.getenv();
    String programFiles = envs.get("ProgramFiles");
    String programFiles86 = envs.get("ProgramFiles(x86)");
    String programW6432 = envs.get("ProgramW6432");
    console.append("programFiles: " + programFiles);
    console.append("programFiles86: " + programFiles86);
    
    String delTapAll = programFiles + "\\TAP-Windows\\bin\\deltapall.bat";
    String delTapAll86 = programFiles + "\\TAP-Windows\\bin\\deltapall.bat";
    
    String result = "";
    
    if (new File(delTapAll).exists()) {
      result = programFiles + "\\TAP-Windows";
    } else if (new File(delTapAll86).exists()) {
      result = programFiles86 + "\\TAP-Windows";
    } else {
      result = programW6432  + "\\TAP-Windows";
    }
    
    console.append("getTapPath() - returning " + result);
    return result;
  }
  
  public static void reinstallTapDriver() {
    IConsole console = Console.getInstance();
    console.append("reinstallTapDriver() - start");
    
    String delTapAll = getTapPath() + "\\bin\\deltapall.bat";
    String addTap = getTapPath() + "\\bin\\addtap.bat";

    if (!Util.isVistaOrLater()) {
      delTapAll = Util.getCmdExe() + " /C " + delTapAll;
      addTap = Util.getCmdExe() + " /C " + addTap;
    }
    
    console.append("delTapAll: " + delTapAll);
    String delResult = Util.runCommandAndReturnOutput(delTapAll);
    console.append(delResult);
    
    console.append("addTap: " + addTap);
    String addResult = Util.runCommandAndReturnOutput(addTap);
    console.append(addResult);
    
    console.append("reinstallTapDriver() - finished");
  }

  public static void restartAllTapDevices() {

    String disable = "";
    String enable = "";
    if (Util.isVistaOrLater()) {
      disable = getTapPath() + "\\bin\\tapinstall.exe disable tap0901";
      enable = getTapPath() + "\\bin\\tapinstall.exe enable tap0901";
    } else {
      disable = getTapPath() + "\\bin\\devcon.exe disable tap0901";
      enable = getTapPath() + "\\bin\\devcon.exe enable tap0901";
    }
    
    IConsole console = Console.getInstance();
    console.append("TapFixer disabling tap device");
    console.append(disable);
    String disableResult = Util.runCommandAndReturnOutput(disable);
    
    if (disableResult != null && disableResult.toLowerCase().contains("no matching devices found")) {
      TapFixer.reinstallTapDriver();
    }
    console.append(disableResult);
    console.append("TapFixer re-enabling tap device");
    console.append(enable);
    String enableResult = Util.runCommandAndReturnOutput(enable);
    console.append(enableResult);
  }

  static Iterable<MatchResult> findMatches(String pattern, CharSequence s) {
    List<MatchResult> results = new ArrayList<MatchResult>();

    for (Matcher m = Pattern.compile(pattern, Pattern.DOTALL | Pattern.MULTILINE).matcher(s); m.find();)
      results.add(m.toMatchResult());

    return results;
  }

}