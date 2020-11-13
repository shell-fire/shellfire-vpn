package de.shellfire.vpn.service.win;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;

public class TapFixer {

	private static Logger log = Util.getLogger(TapFixer.class.getCanonicalName());

	private static String getTapPath() {
		log.debug("getTapPath() - start");
		Map<String, String> envs = System.getenv();
		String programFiles = envs.get("ProgramFiles");
		String programFiles86 = envs.get("ProgramFiles(x86)");
		String programW6432 = envs.get("ProgramW6432");
		log.debug("programFiles: " + programFiles);
		log.debug("programFiles86: " + programFiles86);

		String delTapAll = programFiles + "\\TAP-Windows\\bin\\deltapall.bat";
		String delTapAll86 = programFiles + "\\TAP-Windows\\bin\\deltapall.bat";

		String result = "";

		if (new File(delTapAll).exists()) {
			result = programFiles + "\\TAP-Windows";
		} else if (new File(delTapAll86).exists()) {
			result = programFiles86 + "\\TAP-Windows";
		} else {
			result = programW6432 + "\\TAP-Windows";
		}

		log.debug("getTapPath() - returning " + result);
		return result;
	}

	public static void reinstallTapDriver() {
		log.debug("reinstallTapDriver() - start");

		List<String> delTapAll = new LinkedList<String>();
		delTapAll.add(getTapPath());
		delTapAll.add(getTapPath() + "\\bin\\deltapall.bat");

		List<String> addTap = new LinkedList<String>();
		addTap.add(getTapPath());
		addTap.add(getTapPath() + "\\bin\\addtap.bat");

		if (!Util.isVistaOrLater()) {
			delTapAll.add(0, Util.getCmdExe());
			delTapAll.add(1, "/C");

			addTap.add(0, Util.getCmdExe());
			addTap.add(1, "/C");
		}

		log.debug("delTapAll: " + delTapAll);
		String delResult = Util.runCommandAndReturnOutput(delTapAll);
		log.debug(delResult);

		log.debug("addTap: " + addTap);
		String addResult = Util.runCommandAndReturnOutput(addTap);
		log.debug(addResult);

		log.debug("reinstallTapDriver() - finished");
	}

	public static void restartAllTapDevices() {

		String[] disable;
		String[] enable;
		if (Util.isVistaOrLater()) {
			disable = new String[] { getTapPath() + "\\bin\\tapinstall.exe", "disable", "tap0901" };
			enable = new String[] { getTapPath() + "\\bin\\tapinstall.exe", "enable", "tap0901" };
		} else {
			disable = new String[] { getTapPath() + "\\bin\\devcon.exe", "disable", "tap0901" };
			enable = new String[] { getTapPath() + "\\bin\\devcon.exe", "enable", "tap0901" };
		}

		log.debug("TapFixer disabling tap device");
		log.debug("{}", disable);
		String disableResult = Util.runCommandAndReturnOutput(disable);

		if (disableResult != null && disableResult.toLowerCase().contains("no matching devices found")) {
			TapFixer.reinstallTapDriver();
		}
		log.debug(disableResult);
		log.debug("TapFixer re-enabling tap device");
		log.debug("{}", enable);
		String enableResult = Util.runCommandAndReturnOutput(enable);
		log.debug(enableResult);
	}

	static Iterable<MatchResult> findMatches(String pattern, CharSequence s) {
		List<MatchResult> results = new ArrayList<MatchResult>();

		for (Matcher m = Pattern.compile(pattern, Pattern.DOTALL | Pattern.MULTILINE).matcher(s); m.find();)
			results.add(m.toMatchResult());

		return results;
	}

}