import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA encryption and decryption.
 */
public class RSA {

    private static String PublicKeyBase64;
    private static String PrivateKeyBase64;

    /**
     * Generate an asymmetric RSA key pair
     */
    public static void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        // Get keys from keyPair
        PublicKey pub = kp.getPublic();
        PrivateKey prv = kp.getPrivate();

        // Convert keys to base64 strings
        PublicKeyBase64 = Base64.getEncoder().encodeToString(pub.getEncoded());
        PrivateKeyBase64 = Base64.getEncoder().encodeToString(prv.getEncoded());
    }

    /**
     * Retrieves the Public Key from the stored base64 string
     *
     * @return      The public key
     */
    public static PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(PublicKeyBase64.getBytes(StandardCharsets.UTF_8));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pub = keyFactory.generatePublic(spec);
        return pub;
    }

    /**
     * Retrieves the Private Key from the stored base64 string
     *
     * @return      The private key
     */
    public static PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(PrivateKeyBase64.getBytes(StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        return priv;
    }

    /**
     * Encrypt a plaintext message using the public or private key
     *
     * @param data  The plaintext message to be encrypted
     * @param key   The key to use for encryption (use the getPublicKey or getPrivateKey methods to get desired key)
     * @return      The ciphertext message
     */
    public static byte[] encrypt(String data, Key key) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes());
    }

    /**
     * Decrypt a ciphertext message using the public or private key
     *
     * @param data  The ciphertext message to decrypt
     * @param key   The key to use for encryption (use the getPublicKey or getPrivateKey methods to get desired key)
     * @return      The plaintext message
     */
    public static byte[] decrypt(byte[] data, Key key) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

}
