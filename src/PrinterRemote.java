import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public interface PrinterRemote extends Remote {

    String echo(String input) throws RemoteException;

    String print(String filename, String printer) throws RemoteException;

    String queue() throws RemoteException;

    String topQueue(int job) throws RemoteException;

    String start() throws RemoteException;

    String stop() throws RemoteException;

    String restart() throws RemoteException;

    String status() throws RemoteException;

    String readConfig(String parameter) throws RemoteException;

    String setConfig(String parameter, String value) throws RemoteException;

    boolean authenticate(byte[] user, byte[] password) throws RemoteException, InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, BadPaddingException;

    String submitChoice(String choice, String arg1, String arg2, String username) throws RemoteException;

    PublicKey getPublicKey() throws RemoteException;
}
