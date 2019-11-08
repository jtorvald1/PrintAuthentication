import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PrinterServant extends UnicastRemoteObject implements PrinterRemote{
    public ArrayList<String> queue = new ArrayList<>() {{
        add("file1.docx");
        add("file2.docx");
        add("file3.docx");
    }};
    public ArrayList<String> printers = new ArrayList<>() {{
        add("Printer1");
        add("Printer2");
        add("Printer3");
    }};
    public String status = "";
    public HashMap<String,String> settings = new HashMap<>();
    private AESGCM aes = new AESGCM();
    private RSA rsa = new RSA();

    private static List<User> userList = new ArrayList<>() {{
        add(new User("Bob","bobbybob"));
        add(new User("Alice","password"));
        add(new User("York","qwerty"));
        add(new User("Zach","ytrewq"));
    }};

    public PrinterServant() throws RemoteException, NoSuchAlgorithmException {
        super();
        rsa.generateKeys();
    }

    public String echo(String input) {

        return "From server:" + input;
    }

    public String print(String filename, String printer) {
        for (String prnt : printers) {
            if(prnt.equals(printer)){
                queue.add(filename);
                return "Added print job for file: " + filename + " to printer: " + printer + " as job number: "
                        + (queue.indexOf(filename)+1);
            }
        }
        return "Printer: " + printer + " not found";
    }

    public String queue() {
        if (queue.isEmpty()){
            return "The queue is empty.";
        }
        String s = "";
        for (int i = 0; i<queue.size();i++) {
            s = s + "<" + (i+1) + ">" + " " + "<" + queue.get(i) + ">" +  "\n";

        }
        System.out.println(queue.size());
        return s.substring(0, s.length()-1);
    }

    public String topQueue(int jobnum) {
        if (jobnum < 1 || jobnum > queue.size() || queue.get(jobnum-1)==null) {
            return "Job number: " + jobnum + " not found.";
        }else{
            queue.add(0, queue.get(jobnum-1));
            queue.remove(jobnum);
            return "The print job: " + queue.get(0) + " has been moved to the top of the queue.";
        }

    }

    public String start() {
        status = "running";
        return "The print server has now started.";
    }

    public String stop() {
        status = "stopped";
        return "The print server is now stopped.";
    }

    public String restart() {
        System.out.println(stop());
        queue.clear();
        System.out.println(start());
        return "The print queue has been cleared, and the print server has restarted.";
    }

    public String status() {
        return status;
    }

    public String readConfig(String parameter) {
        String val = settings.get(parameter);
        return "The configuration parameter " + parameter + " is: " + val;
    }

    public String setConfig(String parameter, String value) {
        settings.put(parameter, value);
        return "The parameter " + parameter + " has been set to: " + value;
    }

    @Override
    public boolean authenticate(byte[] encUsername, byte[] encPassword) throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, BadPaddingException {
        String username = new String(rsa.decrypt(encUsername,rsa.getPrivateKey()));
        String password = new String(rsa.decrypt(encPassword,rsa.getPrivateKey()));
        for (User user:userList) {
            if(user.getUsername().equals(username)){
                return(checkPassword(password,user.getSalt(),user.getEncryptedPassword()));
            }
        }
        return false;
    }

    private boolean checkPassword(String password, byte[] salt, byte[] encryptedPassword) {
        byte[] checkPass = aes.encrypt(password,salt);
        return Arrays.equals(checkPass,encryptedPassword);
    }

    private boolean parseInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }

    @Override
    public String submitChoice(String choice, String arg1, String arg2) {
        String result = null;
        switch (choice) {
            case "print":
                if (arg1 != null && arg2 != null)
                    result = (print(arg1, arg2));
                else
                    result = "Arguments must not be null.";
                break;
            case "queue":
                result = queue();
                break;
            case "top queue":
                if (arg1 != null && parseInteger(arg1)) {
                    result = topQueue(Integer.parseInt(arg1));
                } else {
                    result = "Invalid argument.";
                }
                break;
            case "start":
                result = start();
                break;
            case "stop":
                result = stop();
                break;
            case "restart":
                result = restart();
                break;
            case "status":
                result = status();
                break;
            case "read configuration":
                if (arg1 != null)
                    result = readConfig(arg1);
                else
                    result = "Invalid argument.";
                break;
            case "set configuration":
                    if (arg1 != null && arg2!= null) {
                        result = (setConfig(arg1, arg2));
                    }else {
                        result = "Invalid argument.";
                    }
                break;
            default:
                result = "Invalid choice.";
                break;
        }
        return result;
    }

    @Override
    public PublicKey getPublicKey() throws RemoteException {
        try {
            return rsa.getPublicKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
