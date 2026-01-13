package de.shellfire.vpn.gui;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;

public class StaticMapImageCacheManager {
	private static final Logger log = Util.getLogger(StaticMapImageCacheManager.class.getCanonicalName());
	private static final String CACHE_PREFIX = "map_";
	private static final String CACHE_EXTENSION = ".png";
	private static final String CACHE_DIR_NAME = "servers";
	private static final int CONNECT_TIMEOUT_MS = 5000;
	private static final int READ_TIMEOUT_MS = 10000;

	public static String getCachedMapImageUrl(String remoteUrl) {
		if (remoteUrl == null || remoteUrl.isEmpty()) {
			return remoteUrl;
		}

		File cacheDir = getCacheDir();
		String filename = CACHE_PREFIX + hashUrl(remoteUrl) + CACHE_EXTENSION;
		File cachedFile = new File(cacheDir, filename);

		if (!cachedFile.exists() || cachedFile.length() == 0) {
			boolean downloaded = downloadToFile(remoteUrl, cachedFile);
			if (!downloaded) {
				return remoteUrl;
			}
		}

		String dataUrl = readFileAsDataUrl(cachedFile);
		if (dataUrl == null) {
			return remoteUrl;
		}
		return dataUrl;
	}

	private static File getCacheDir() {
		String downloadDir = Util.getDownloadDir();
		File cacheDir = new File(downloadDir, CACHE_DIR_NAME);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return cacheDir;
	}

	private static boolean downloadToFile(String remoteUrl, File cachedFile) {
		File tempFile = new File(cachedFile.getAbsolutePath() + ".tmp");
		try {
			URLConnection connection = new URL(remoteUrl).openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
			connection.setReadTimeout(READ_TIMEOUT_MS);

			try (InputStream inputStream = connection.getInputStream()) {
				Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			Files.move(tempFile.toPath(), cachedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (Exception e) {
			log.debug("Failed to download map image from {}", remoteUrl, e);
			if (tempFile.exists()) {
				tempFile.delete();
			}
			return false;
		}
	}

	private static String hashUrl(String remoteUrl) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(remoteUrl.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte value : hash) {
				String encoded = Integer.toHexString(0xff & value);
				if (encoded.length() == 1) {
					hex.append('0');
				}
				hex.append(encoded);
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm not available", e);
		}
	}

	private static String readFileAsDataUrl(File cachedFile) {
		try {
			byte[] bytes = Files.readAllBytes(cachedFile.toPath());
			String encoded = Base64.getEncoder().encodeToString(bytes);
			return "data:image/png;base64," + encoded;
		} catch (Exception e) {
			log.debug("Failed to read cached map image {}", cachedFile.getAbsolutePath(), e);
			return null;
		}
	}
}
