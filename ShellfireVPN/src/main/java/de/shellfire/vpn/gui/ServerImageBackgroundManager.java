package de.shellfire.vpn.gui;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;
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
	
	/*
	 * We want to initialize this class early for performance reasons
	 * */
	public static void init() {}
	
	private static void initFilenameMap() {
		// populate fileNameMap based on Properties
		String allKnownFileNameMappings = VpnProperties.getInstance().getProperty(Util.REG_SERVERBACKGROUNDIMAGEFILENAMEMAP);
		
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
			loadImageFromJarOrDownloadDir(filename);
		}
	}

	private static void loadImageFromJarOrDownloadDir(String filename) {
		Image image = loadImageFromJar(filename);
		if (image != null) {
			filenameImageMap.put(filename, image);
			// log.debug("loaded image for {} from jar file", filename);
		}
		if (image == null) {
			image = loadImageFromDownloadDir(filename);
		}
		if (image != null) {
			filenameImageMap.put(filename, image);
			// log.debug("loaded image for {} from in-jar or downloadDir", filename);
			
		} else {
			log.debug("did not yet find image in jar file or download store - doing nothing");
		}
	}
		
	private static Image loadImageFromDownloadDir(String filename) {

		String inDownloadDirPath = Util.getDownloadDir() + filename;
		File file = new File(inDownloadDirPath);
		inDownloadDirPath = file.toURI().toString();
	    
		Image image = null;
		try {
			// log.debug("try to load image from download dir {}", inDownloadDirPath);
			image = new Image(inDownloadDirPath);
			log.debug("image loaded");
		} catch (IllegalArgumentException e) {
			// log.error("could not load file within jar", e);
			image = null;
		}
		
		if (image != null && image.getException() != null) {
			// log.error("error while loading from download dir", image.getException());
			image = null;
		}
		
		return image;
	}
	
	private static Image loadImageFromJar(String filename) {
		String inJarPath = pathPrefix + filename;
		Image image = null;
		try {
			// log.debug("try to load image from in-jar {}", inJarPath);
			image = new Image(inJarPath);
			// log.debug("image loaded");
		} catch (IllegalArgumentException e) {
			// log.error("could not load file within download dir", e);
			image = null;
		}
		
		if (image != null && image.getException() != null) {
			log.error("error while loading from in-jar", image.getException());
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
			log.debug("image for filename {} not in filenameImageMap, loading it from webservice", filename);
			image = loadImageFromWebService(serverId, filename);
		}
		if (image == null) {
			throw new Exception("filename="+filename+" not found in filenameImageMap");
		}
		
		// log.debug("Image for serverId=" + serverId + " found in filename=" + filename + " and loaded, returning it");
		return image;
	}

	private static Image loadImageFromWebService(int serverId, String filename) {
		log.debug("loadImageFromWebService(serverId={}) - start", serverId);
		Image image = service.getServerBackgroundImage(serverId);
		
		if (image == null || image.getException() != null) {
			log.debug("Error - webservice did not deliver image for serverId {}", serverId);
			if (image.getException() != null) {
				log.error("Exception in image during loading from webservice", image.getException());
			}
		} else {
			filenameImageMap.put(filename, image);
			storeImageInDownloadDir(filename, image);
		}
		
		log.debug("loadImageFromWebService() - finished, returning: {}", filename);
		return image;
	}

	private static void storeImageInDownloadDir(String filename, Image image) {
	    File outputFile = new File(Util.getDownloadDir() + filename);
	    Util.storeImageInFile(image, outputFile);
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
		loadImageFromJarOrDownloadDir(filename);
		
		log.debug("loadFilenameFromWebService() - finished, returning: {}", filename);
		return filename;
	}

	private static void updateProperties() {
		String allKnownFileNameMappings = "";
		for (int serverId : serverFilenameMap.keySet()) {
			String filename = serverFilenameMap.get(serverId);
			allKnownFileNameMappings += serverId + "=" + filename + ",";
		}
		
		VpnProperties.getInstance().setProperty(Util.REG_SERVERBACKGROUNDIMAGEFILENAMEMAP, allKnownFileNameMappings); 
	}
}
