package de.shellfire.vpn.service;

import com.sun.jna.platform.win32.Crypt32Util;
import org.apache.commons.codec.binary.Base64;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("restriction")
public class CryptFactory {

    public static String encrypt(String toEncrypt) {
        try {
            byte[] cleartext = toEncrypt.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedData = Crypt32Util.cryptProtectData(cleartext);
            return new String(Base64.encodeBase64(encryptedData));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            byte[] encryptedData = Base64.decodeBase64(encrypted);
            byte[] decryptedData = Crypt32Util.cryptUnprotectData(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
