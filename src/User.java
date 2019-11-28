import java.io.Serializable;

public class User implements Serializable{

    private String username = null;
    private byte[] encryptedPassword = null;
    private byte[] salt = null;
    private String role = null;

    User(String name, String pass, String userRole){

        AESGCM aes = new AESGCM();

        username = name;
        salt = aes.generateSalt();
        encryptedPassword = aes.encrypt(pass, salt);
        role = userRole;

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

    public String getRole() { return role; }
}
