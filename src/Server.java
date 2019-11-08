import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;

public class Server {

    public static void main(String[] args) throws RemoteException, NoSuchAlgorithmException {
        Registry registry = LocateRegistry.createRegistry(5099);
        registry.rebind("printer", new PrinterServant());
    }

}
