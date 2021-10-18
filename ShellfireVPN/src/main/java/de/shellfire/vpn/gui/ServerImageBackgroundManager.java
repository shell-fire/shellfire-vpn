package de.shellfire.vpn.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
import de.shellfire.vpn.gui.controller.LoginController;
import de.shellfire.vpn.webservice.WebService;
import javafx.scene.image.Image;

public class ServerImageBackgroundManager {
	
	private static Logger log = Util.getLogger(ServerImageBackgroundManager.class.getCanonicalName());
	private static Map<Integer, String> serverFilenameMap = new HashMap<Integer, String>();
	private static Set<String> filenameSet = new HashSet<String>();
	private static Map<String, Image> filenameImageMap = new HashMap<String, Image>();
	private static final String pathPrefix = "/servers/";
	private static WebService service = WebService.getInstance();
	
	static {
		initFilenameMap();
		initImageMap();
	}
	
	private static void initFilenameMap() {
		// populate fileNameMap based on Properties
		String allKnownFileNameMappings = VpnProperties.getInstance().getProperty(LoginController.REG_SERVERBACKGROUNDIMAGEFILENAMEMAP);
		
		if (allKnownFileNameMappings != null) {
			serverFilenameMap = new HashMap<Integer, String>();
			filenameSet = new HashSet<String>();
			for (String currentMapping : allKnownFileNameMappings.split(",")) {
				String[] parts = currentMapping.split("=");
				int serverId = Integer.valueOf(parts[0]);
				String filename = parts[1];
				serverFilenameMap.put(serverId, filename);
				filenameSet.add(filename);
			}
		}
	}	
	
	private static void initImageMap() {
		filenameImageMap = new HashMap<String, Image>();
		
		for (String filename : filenameSet) {
			loadImageFromJarOrDownloadFolder(filename);
		}
	}

	private static void loadImageFromJarOrDownloadFolder(String filename) {
		Image image = loadImageFromJar(filename);
		if (image != null) {
			filenameImageMap.put(filename, image);
			log.debug("loaded image for {} from jar file");
		}
		if (image == null) {
			image = loadImageFromDownloadFolder(filename);
		}
		if (image != null) {
			filenameImageMap.put(filename, image);
			log.debug("loaded image for {} from jar download store");
			
		} else {
			log.debug("did not yet find image in jar file or download store - doing nothing");
		}
	}
		
	private static Image loadImageFromDownloadFolder(String filename) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static Image loadImageFromJar(String filename) {
		String inJarPath = pathPrefix + filename;
		Image image = null;
		try {
			log.debug("try to load image from {}", inJarPath);
			image = new Image(inJarPath);
			log.debug("image loaded");
		} catch (IllegalArgumentException e) {
			log.error("could not load file within jar", e);
		}
		
		return image;
	}

	public static Image getImage(int serverId) throws Exception {
		String filename = serverFilenameMap.get(serverId);
		if (filename == null) {
			log.debug("serverId {} not found in serverFilenameMap - loading from webservice");
			filename = loadFilenameFromWebService(serverId);
		}

		if (filename == null) {
			throw new Exception("filename for serverId="+serverId+" could not be found in properties nor loaded from webservice");
		}
		
		Image image = filenameImageMap.get(filename);
		if (image == null) {
			// TODO: Need to implement download function
			image = loadImageFromWebService(serverId, filename);
		}
		if (image == null) {
			throw new Exception("filename="+filename+" not found in filenameImageMap");
		}
		
		log.debug("Image for serverId=" + serverId + " found in filename=" + filename + " and loaded, returning it");
		return image;
	}

	private static Image loadImageFromWebService(int serverId, String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String loadFilenameFromWebService(int serverId) {
		log.debug("loadFilenameFromWebService(serverId={}) - start", serverId);
		String filename = service.getServerBackgroundImageFilename(serverId);
		
		if (filename == null) {
			log.debug("Error - webservice did not deliver filename for serverId {}", serverId);
		} else {
			serverFilenameMap.put(serverId, filename);
			updateProperties();
		}
		
		// check if myabe we already have a file with this filename available...
		loadImageFromJarOrDownloadFolder(filename);
		
		log.debug("loadFilenameFromWebService() - finished, returning: {}", filename);
		return filename;
	}

	private static void updateProperties() {
		String allKnownFileNameMappings = "";
		for (int serverId : serverFilenameMap.keySet()) {
			String filename = serverFilenameMap.get(serverId);
			allKnownFileNameMappings += serverId + "=" + filename + ",";
		}
		
		VpnProperties.getInstance().setProperty(LoginController.REG_SERVERBACKGROUNDIMAGEFILENAMEMAP, allKnownFileNameMappings); 
	}
}
