import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class Client {

    private static PrinterRemote remote;
    private static boolean loggedIn = false;
    private static boolean running = false;
    private static int loginAttempts = 0;
    private static RSA rsa = new RSA();
    private static PublicKey pk;

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        remote = (PrinterRemote) Naming.lookup("rmi://localhost:5099/printer");
        pk = remote.getPublicKey();

        running = true;
        while(running){
            while(!loggedIn){
                if(loginAttempts >= 4){
                    System.out.println("YOU HAVE EXCEEDED THE NUMBER OF ALLOWED LOGIN ATTEMPTS.");
                    System.out.println("SHUTTING DOWN.");
                    return;
                }
                loggedIn = authenticate();
                loginAttempts++;
            }
            if(loggedIn) {
                loginAttempts = 0;
                makeChoice();
            }
        }
        System.out.println("Goodbye.");
    }

    private static boolean authenticate() throws RemoteException, InterruptedException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException {
        String user;
        String password;
        byte[] encryptedUser;
        byte[] encryptedPass;
        Scanner input = new Scanner(System.in);

        System.out.print("Input username: ");
        user = input.nextLine();

        System.out.print("Input password: ");
        password = input.nextLine();

        encryptedUser = rsa.encrypt(user,pk);
        encryptedPass = rsa.encrypt(password,pk);

        if (remote.authenticate(encryptedUser, encryptedPass)) {
            System.out.println("You are now logged in to the print server.");
            return true;
        }
        else {
            System.out.println("Verifying credentials.");
            Thread.sleep(2000);
            System.out.println("USERNAME/PASSWORD MISMATCH. PLEASE TRY AGAIN.");
            return false;
        }
    }

    private static void makeChoice() throws RemoteException {
        Scanner input = new Scanner(System.in);
        String choice;
        String arg1;
        String arg2;

        System.out.println();
        System.out.println("Please enter one of the following commands:");
        System.out.println("Print\n" +
                "Queue\n" +
                "Top queue\n" +
                "Start\n" +
                "Stop\n" +
                "Restart\n" +
                "Status\n" +
                "Read configuration\n" +
                "Set configuration\n" +
                "Log out");

        choice = input.nextLine().toLowerCase();
        switch (choice) {
            case "print":
                System.out.println("Enter the name of the file to print: ");
                arg1 = input.nextLine();
                System.out.println("Enter the printer name: ");
                arg2 = input.nextLine();
                System.out.println(submitChoice(choice, arg1, arg2));
                break;
            case "queue":
            case "start":
            case "stop":
            case "restart":
            case "status":
                System.out.println(submitChoice(choice, null, null));
                break;
            case "top queue":
                System.out.println("Enter the job number: ");
                arg1 = input.nextLine();
                System.out.println(submitChoice(choice, arg1, null));
                break;
            case "read configuration":
                System.out.println("Enter the configuration parameter: ");
                arg1 = input.nextLine();
                System.out.println(submitChoice(choice, arg1, null));
                break;
            case "set configuration":
                System.out.println("Enter the configuration parameter: ");
                arg1 = input.nextLine();
                System.out.println("Enter the value: ");
                arg2 = input.nextLine();
                System.out.println(submitChoice(choice, arg1, arg2));
                break;
            case "log out":
                System.out.println("Logging out.");
                loggedIn = false;
                running = false;
                return;
            default:
                System.out.println("Unrecognized command, please try again.");
                break;
        }
        System.out.println("Press enter to continue");
        input.nextLine();
        loggedIn = false;
        return;
    }

    private static String submitChoice(String choice, String arg1, String arg2) throws RemoteException {
        return remote.submitChoice(choice, arg1, arg2);
    }


}
