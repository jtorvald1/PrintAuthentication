import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES encryption and decryption.
 * Uses AES in GCM mode with a 256 bit key
 */
public class AESGCM {

    private static final String key = "A]BnM8b<.)&kI0+I";

    private static final SecureRandom random = new SecureRandom();

    /**
     * Encrypt a plaintext message
     *
     * @param plaintext The plaintext to be encrypted
     * @param salt     The salt to use for encryption
     * @return The ciphertext bytes
     */
    public static byte[] encrypt(String plaintext, byte[] salt) {
        byte[] cipherBytes = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKey keySpec = new SecretKeySpec(key.getBytes(), "AES");

            // Create GCMParameterSpec
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, salt);

            // Initialize Cipher for ENCRYPT_MODE
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            cipherBytes = cipher.doFinal(plaintext.getBytes());
        } catch (Exception e) {
            System.out.println("Encryption error: " + e.toString());
        }
        return cipherBytes;
    }

    /**
     * Generate salt to use for encryption
     * @return Byte[] containing the salt
     */
    public static byte[] generateSalt() {
        byte [] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return saltBytes;
    }
}
