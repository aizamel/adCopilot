/**
 * Encrypter class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lps.cvp.exception.CustomException;

/**
 * This encrypter encrypts and decrypts values that are compatible with openssl.
 * This code uses SHA-256 to produce a salted key and the AES-256-CBC algorithm
 * to encrypt the data. In openssl, use the parameters: -aes-256-cbc and -md
 * sha256. e.g. <code>
 * echo -n "text" | openssl enc -aes-256-cbc -md sha256 -pass pass:password123 -a
 * </code>
 */
public class Encrypter {

	private Logger logger = LoggerFactory.getLogger(Encrypter.class);

	// Used for salt generation
	SecureRandom srand = new SecureRandom();

	// The bytes of the supplied password
	byte[] password;

	/**
	 * The supplied password will be used to encrypt/decrypt text. The algorithm
	 * uses SHA-256 to generate a key and AES-256-CBC to encrypt the data.
	 *
	 * @param password A non-null password string of any size. Ideally it should be
	 *                 at least 32 characters.
	 */
	public Encrypter(String password) {
		this.password = password.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Encrypts the supplied text.
	 *
	 * @param clearText
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public String encrypt(String clearText)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		// Generate salt
		byte[] salt = new byte[8];
		srand.nextBytes(salt);

		// Derive key
		byte[] passAndSalt = concat(password, salt);
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] key = md.digest(passAndSalt);
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

		// Derive iv
		md.reset();
		byte[] iv = Arrays.copyOfRange(md.digest(concat(key, passAndSalt)), 0, 16);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write("Salted__".getBytes(StandardCharsets.US_ASCII));
		bos.write(salt);
		bos.write(cipher.doFinal(clearText.getBytes(StandardCharsets.UTF_8)));
		return Base64.getEncoder().encodeToString(bos.toByteArray());
	}

	public String decrypt(String cipherText) {
		// Parse cipher text
		String origValue = "";
		try {
			byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
			byte[] salt = Arrays.copyOfRange(cipherBytes, 8, 16);
			cipherBytes = Arrays.copyOfRange(cipherBytes, 16, cipherBytes.length);

			// Derive key
			byte[] passAndSalt = concat(password, salt);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] key = md.digest(passAndSalt);
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

			// Derive IV
			md.reset();
			byte[] iv = Arrays.copyOfRange(md.digest(concat(key, passAndSalt)), 0, 16);

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
			origValue = new String(cipher.doFinal(cipherBytes));
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger.error("Wrong X-3scale-proxy-secret-token value.");
			throw new CustomException(400, "TOKEN");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
				| InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IllegalArgumentException ex) {
			logger.error("Wrong encryted token list value or wrong token key value.");
			throw new CustomException(400, "TOKEN2");
		}
		return origValue;
	}

	/**
	 * Returns a new byte array concatenating the contents of a and b.
	 *
	 * @param a A non-null byte array.
	 * @param b A non-null byte array.
	 * @return A non-null byte array with the contents of a and b.
	 */
	private static byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
}
