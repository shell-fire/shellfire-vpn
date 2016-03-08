package de.shellfire.vpn;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class CryptFactory {

	private static SecretKey getKey() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyPhrase = CryptFactory.getKeyPhrase();

		DESKeySpec keySpec = new DESKeySpec(keyPhrase);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(keySpec);
		
		return key;
	}
	private static byte[] getKeyPhrase() {
		byte[] keyPhrase = new byte[8];
		byte[] mac = CryptFactory.getMacAddress();

		for (int i = 0; i < 8; i++) {
			keyPhrase[i] = mac[i % 6];
		}

		return keyPhrase;
	}

	private static byte[] getMacAddress() {
		try {
			InetAddress address = InetAddress.getLocalHost();
			NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			byte[] mac = ni.getHardwareAddress();
			return mac;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "fallback".getBytes();
	}

	public static String encrypt(String toEncrypt) {

		Cipher cipher;
		try {
			SecretKey key = CryptFactory.getKey();
			byte[] cleartext = toEncrypt.getBytes("UTF8");
			cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			BASE64Encoder base64encoder = new BASE64Encoder();
			String encrypedPwd = base64encoder.encode(cipher.doFinal(cleartext));
			return encrypedPwd;

		} catch (Exception e) {}

		return null;
	}

	public static String decrypt(String encrypted) {
		try {
			BASE64Decoder base64decoder = new BASE64Decoder();
			byte[] encrypedPwdBytes = base64decoder.decodeBuffer(encrypted);

			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			SecretKey key = CryptFactory.getKey();
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
			return new String(plainTextPwdBytes);
		} catch (Exception e) {}

		return null;
	}

}
