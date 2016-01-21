/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.lequer.vault.lib;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {

    /**
     * @return a new pseudorandom salt of the specified length
     */
    private static byte[] generateSalt(int length) {
        Random r = new SecureRandom();
        byte[] salt = new byte[length];
        r.nextBytes(salt);
        return salt;
    }

    private static SecretKey genrerateKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        /* Derive the key, given password and salt. */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    public static String encrypt(char[] password, String text) throws InvalidEncryptionException {
        try {
            byte[] salt = generateSalt(16);
            SecretKey key = genrerateKey(password, salt);
            /* Encrypt the message. */
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(text.getBytes("UTF-8"));

            byte[] finalCiphertext = new byte[ciphertext.length + 32];
            System.arraycopy(iv, 0, finalCiphertext, 0, 16);
            System.arraycopy(salt, 0, finalCiphertext, 16, 16);
            System.arraycopy(ciphertext, 0, finalCiphertext, 32, ciphertext.length);

            return new String(Base64.getEncoder().encode(finalCiphertext));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidParameterSpecException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new InvalidEncryptionException();
        }
    }

    public static String decrypt(char[] password, String encrypted) throws InvalidPasswordException {

        try {
            byte[] prevCypherText = Base64.getDecoder().decode(encrypted);
            byte[] ivb = Arrays.copyOfRange(prevCypherText, 0, 16);
            byte[] salt = Arrays.copyOfRange(prevCypherText, 16, 32);
            byte[] cypherText = Arrays.copyOfRange(prevCypherText, 32, prevCypherText.length);

            SecretKey key = genrerateKey(password, salt);
            IvParameterSpec iv = new IvParameterSpec(ivb);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            byte[] original = cipher.doFinal(cypherText);

            return new String(original);
            /**
             * TODO create and add relevant exceptions
             */
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new InvalidPasswordException();
        }

    }

    /**
     * Thrown if an attempt is made to decrypt a stream with an incorrect
     * password.
     */
    public static class InvalidPasswordException extends Exception {
    }

    public static class InvalidEncryptionException extends Exception {

        public InvalidEncryptionException() {
        }
    }
}
