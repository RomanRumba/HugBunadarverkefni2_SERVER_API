package project.services;

import java.security.SecureRandom;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.stereotype.Service;

@Service
public class CryptographyService {

	@Value("${cryptography.storage.password}")
	private String password;

	@Value("${cryptography.storage.salt}")
	private String salt;

	private static String spw;
	private static String ss;

	private static final String PASSWORD_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*()-_=+[{]}|;:,<.>?";

	private static final char[] PASSWORD_ALPHABET_CHARS = (new String(PASSWORD_ALPHABET)).toCharArray();

	/*
	 * After Spring has populated `password` and `salt` (and constructed this
	 * object), `init` is invoked and `spw` and `ss` are populated so this service
	 * can be access by regular Java classes (not Spring).
	 */
	@PostConstruct
	public void init() {
		spw = password;
		ss = salt;
	}

	/**
	 * Returns a random "strong" password of length <code>n</code>
	 * 
	 * @param n Length of password.
	 * @return Random "strong" password of length <code>n</code>
	 */
	public static String getStrongRandomPassword(int n) {
		final char[] alphabet = PASSWORD_ALPHABET_CHARS;
		String s = RandomStringUtils.random(n, 0, alphabet.length - 1, false, false, alphabet, new SecureRandom());
		return s;
	}

	/**
	 * Returns a random hexadecimal string of length <code>n</code>.
	 * 
	 * @param n Length of hexadecimal string.
	 * @return A random hexadecimal string of length <code>n</code>.
	 */
	public static String getRandomHexString(int n) {
		SecureRandom r = new SecureRandom();
		StringBuffer sb = new StringBuffer();
		while (sb.length() < n) {
			sb.append(Integer.toHexString(r.nextInt()));
		}
		return sb.toString().substring(0, n);
	}

	/**
	 * Encrypts plaintext and returns its ciphertext.
	 * 
	 * @param plaintext
	 * @return ciphertext
	 */
	public static String getCiphertext(String plaintext) {
		return Encryptors.text(spw, ss).encrypt(plaintext);
	}

	/**
	 * Decrypts ciphertext and returns in plaintext.
	 * 
	 * @param ciphertext
	 * @return plaintext
	 */
	public static String getPlaintext(String ciphertext) {
		return Encryptors.text(spw, ss).decrypt(ciphertext);
	}
}
