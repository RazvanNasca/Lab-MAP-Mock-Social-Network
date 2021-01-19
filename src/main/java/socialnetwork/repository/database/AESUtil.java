package socialnetwork.repository;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AESUtil
{
    public static String SALT = "852467391";
    public static String Algorithm = "AES/CBC/PKCS5Padding";
    public static SecretKey key;

    static {
        try {
            key = AESUtil.getKeyFromPassword("entity.getPassword()", AESUtil.SALT);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public AESUtil() throws InvalidKeySpecException, NoSuchAlgorithmException {
    }

    public static SecretKey getKeyFromPassword(String password, String salt)  throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;
    }

    /**
     * IV is a pseudo-random value and has the same size as the block that is encrypted
     * Using the SecureRandom class to generate random IV
     * @return a pseudo-random value
     */
    public static IvParameterSpec generateIv()
    {
        byte[] iv = new byte[]{5, 7, 3, -11, 42, 20, -14, 8, -19, 12, 2, 6, -30, 124, 127, 54};
        return new IvParameterSpec(iv);
    }

    /**
     *
     * @param algorithm - algorithm of the encryption
     * @param input - input to be encrypted
     * @param key - the secret key
     * @param iv - (pseudo-)random value
     * @return encrypted string
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */
    public static String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException
    {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     *
     * @param algorithm - decryption algorithm
     * @param cipherText - string to be decrypted
     * @param key - the secret key
     * @param iv - (pseudo-)random value
     * @return the decrypted string
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
    {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
}
