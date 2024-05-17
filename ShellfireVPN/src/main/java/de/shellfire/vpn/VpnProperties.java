package de.shellfire.vpn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ch.qos.logback.classic.Logger;

public class VpnProperties extends Properties {

    private static Logger log = Util.getLogger(VpnProperties.class.getCanonicalName());
    private static VpnProperties instance;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private VpnProperties() {
        InputStream is = null;
        try {
            log.debug("getProperties() - start - loading properties from file");
            String propertiesFilePath = getPropertiesFilePath();
            log.debug("loading from: " + propertiesFilePath);

            File propertiesFile = new File(propertiesFilePath);
            if (!propertiesFile.exists()) {
                propertiesFile.getParentFile().mkdirs();
                propertiesFile.createNewFile();
            }

            is = new FileInputStream(propertiesFile);
            lock.writeLock().lock();
            try {
                load(is);
            } finally {
                lock.writeLock().unlock();
            }

            log.debug("getProperties() - finished loading properties");
            for (String key : stringPropertyNames()) {
                log.debug(key + "=" + getProperty(key));
            }
        } catch (IOException e) {
            log.error("Exception occurred during getProperties()", e);
            Util.handleException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("Exception occurred while closing inputstream", e);
                    e.printStackTrace();
                }
            }
        }
    }

    public void printProperties() {
        lock.readLock().lock();
        try {
            for (String key : stringPropertyNames()) {
                log.info(key + "=" + getProperty(key));
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder("{");
            for (String key : stringPropertyNames()) {
                sb.append(key).append("=").append(getProperty(key)).append(", ");
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("}");
            return sb.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    public static VpnProperties getInstance() {
        if (instance == null) {
            synchronized (VpnProperties.class) {
                if (instance == null) {
                    instance = new VpnProperties();
                }
            }
        }
        return instance;
    }

    public void setBoolean(String key, boolean value) {
        log.debug("VpnProperties.setBoolean(key=" + key + " to value=" + value + ")");
        setProperty(key, Boolean.toString(value));
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        lock.readLock().lock();
        try {
            boolean result = Boolean.parseBoolean(getProperty(key, Boolean.toString(defaultValue)));
            log.debug("VpnProperties.getBoolean(key=" + key + ") - returning result: " + (result ? "true" : "false"));
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setInt(String key, int value) {
        setProperty(key, Integer.toString(value));
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        lock.readLock().lock();
        try {
            return Integer.parseInt(getProperty(key, Integer.toString(defaultValue)));
        } finally {
            lock.readLock().unlock();
        }
    }

    public Object setProperty(String key, String value) {
        lock.writeLock().lock();
        try {
            Object res;
            if (value == null) {
                res = super.remove(key);
            } else {
                res = super.setProperty(key, value);
            }
            saveProperties();
            return res;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void saveProperties() {
        lock.writeLock().lock();
        try {
            File f = new File(getPropertiesFilePath());
            OutputStream out = new FileOutputStream(f);
            store(out, String.format("Properties created on %s %s", Util.isoToday, Util.timeFormat.format(new Date())));
        } catch (IOException e) {
            log.error("Exception occurred during saveProperties()", e);
            Util.handleException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static String getPropertiesFilePath() {
        return Util.getConfigDir() + "ShellfireVPN.properties";
    }
}
