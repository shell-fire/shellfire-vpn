package de.shellfire.vpn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;

import ch.qos.logback.classic.Logger;

public class VpnProperties extends Properties {

  private static Logger log = Util.getLogger(VpnProperties.class.getCanonicalName());
  private static VpnProperties instance;

  private VpnProperties() {
    InputStream is = null;
    try {
      log.debug("getProperties() - start - loading properties from file");
      String propertiesFilePath = getPropertiesFilePath();
      log.debug("loading from: " + propertiesFilePath);

      File propertiesFile = new File(propertiesFilePath);
      if (!propertiesFile.exists()) {
        propertiesFile.createNewFile();
      }

      is = new FileInputStream(propertiesFile);
      load(is);
      
      log.debug("getProperties() - finished");
    } catch (IOException e) {
      log.error("Exception occured during getPropertes()", e);
      Util.handleException(e);
    } finally {
      if  (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          log.error("Exception occured while closing inputstream", e);
          e.printStackTrace();
        }
      }
    }
  }

  public static VpnProperties getInstance() {
    if (instance == null) {
      instance = new VpnProperties();
    }

    return instance;
  }
  
  public void setBoolean(String key, boolean value) {
    setProperty(key, Boolean.toString(value));
  }
  
  public boolean getBoolean(String key) {
    return getBoolean(key, false);
  }
  
  public boolean getBoolean(String key, boolean defaultValue) {
    return Boolean.parseBoolean(getProperty(key, Boolean.toString(defaultValue)));
  }
  
  public void setInt(String key, int value) {
    setProperty(key, Integer.toString(value));
  }
  
  public int getInt(String key) {
    return getInt(key, 0);
  }
  
  public int getInt(String key, int defaultValue) {
    return Integer.parseInt(getProperty(key, Integer.toString(defaultValue)));
  }
  
  public Object setProperty(String key, String value) {
    Object res = super.setProperty(key, value);
    saveProperties();
    return res;
  }

  public static String getPropertiesFilePath() {
    return Util.getConfigDir() + "ShellfireVPN.properties";
  }
  
  private void saveProperties() {
    try {
      File f = new File(getPropertiesFilePath());
      OutputStream out = new FileOutputStream(f);
      store(out, String.format("Properties created on %s %s", Util.isoToday, Util.timeFormat.format(new Date())));
      } catch (IOException e) {
      log.error("Exception occured during getPropertes()", e);
      Util.handleException(e);
    }
    
  }

}
