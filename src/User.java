import java.io.Serializable;

public class User implements Serializable{

    private String username = null;
    private byte[] encryptedPassword = null;
    private byte[] salt = null;

    User(String name, String pass){

        AESGCM aes = new AESGCM();

        username = name;
        salt = aes.generateSalt();
        encryptedPassword = aes.encrypt(pass, salt);

    }

    public String getUsername() {
        return username;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    public byte[] getSalt() {
        return salt;
    }
}
