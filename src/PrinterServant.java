import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class PrinterServant extends UnicastRemoteObject implements PrinterRemote{
    private ArrayList<String> queue = new ArrayList<>() {{
        add("file1.docx");
        add("file2.docx");
        add("file3.docx");
    }};
    private ArrayList<String> printers = new ArrayList<>() {{
        add("Printer1");
        add("Printer2");
        add("Printer3");
    }};
    private String status = "";
    private HashMap<String,String> settings = new HashMap<>();
    private AESGCM aes = new AESGCM();
    private RSA rsa = new RSA();
    private String usersFilepath = "Resources/users.txt";
    private String ACLFilepath = "Resources/access_control_list.txt";
    private String RBACFilepath = "Resources/rbac.txt";
    private static HashMap<String, List<String>> roles = new HashMap<>();
    private int ACMODE = 0;

    private static List<User> tempUserList = new ArrayList<>() {{
        add(new User("Alice","password", "administrator"));
        //add(new User("Bob","bobbybob", "technician"));
        add(new User("Cecilia","1111", "powerUser"));
        add(new User("David","2222", "user"));
        add(new User("Erica","3333", "user"));
        add(new User("Fred","4444", "user"));
        add(new User("George","5555", "technician"));
        add(new User("Henry","6666", "user"));
        add(new User("Ida","7777", "powerUser"));
    }};
    private static List<User> userList = new ArrayList<>();

    public PrinterServant() throws IOException, NoSuchAlgorithmException {
        super();
        rsa.generateKeys();
        writeUsersToFile(tempUserList);
        readUsersFromFile(usersFilepath);

        Scanner input = new Scanner(System.in);
        String choice = "";
        boolean choosing = true;

        while(ACMODE == 0) {
            System.out.println();
            System.out.println("Please choose the desired access control mode from the following options:");
            System.out.println("Type 1 to start in Access Control List mode.");
            System.out.println("Type 2 to start in Role Based Access Control mode.");

            choice = input.nextLine();
            System.out.println(choice);

            if(choice.equals("1")) {
                System.out.println("Starting in Access Control List mode.");
                readAccessControlFile(new File(ACLFilepath));
                ACMODE=1;
            } else if(choice.equals("2")) {
                System.out.println("Starting in Role Based Access Control mode.");
                readAccessControlFile(new File(RBACFilepath));
                ACMODE=2;
            } else {
                System.out.println("Choice not recognized, please try again.");
                System.out.println("Press enter to continue");
                input.nextLine();
            }
        }



        System.out.println(roles.get("powerUser"));
    }

    private void writeUsersToFile(List<User> users) {
        try {
            //clear contents
            new FileOutputStream(usersFilepath).close();

            FileOutputStream fileOut = new FileOutputStream(usersFilepath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            for (Object o : users) {
                try {
                    objectOut.writeObject(o);
                    //System.out.println("saved");
                } catch (NotSerializableException e) {
                    //System.out.println("An object was not serializable, it has not been saved.");
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                objectOut.writeObject(null);
                //System.out.println("saved");
            } catch (NotSerializableException e) {
                //System.out.println("An object was not serializable, it has not been saved.");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            objectOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void readUsersFromFile(String filepath) {
        try {
            FileInputStream fileIn = new FileInputStream(filepath);
            boolean cont = true;
            try {
                ObjectInputStream input = new ObjectInputStream(fileIn);
                while (cont) {
                    Object obj = input.readObject();
                    if (obj != null)
                        userList.add((User) obj);
                    else
                        cont = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void readAccessControlFile(File file) throws IOException {
        String key = null;
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));

        line = reader.readLine();
        String[] parts = line.split(":", 2);
        key = parts[0];
        boolean reading = true;
        while(reading){
            List<String> value = new ArrayList<String>();
            while(key.equals(parts[0])){
                value.add(parts[1]);
                line = reader.readLine();
                if(line == null){
                    reading = false;
                    parts[0]="";
                } else {
                    parts = line.split(":", 2);
                }
            }
            roles.put(key, value);
            key = parts[0];
        }
        reader.close();
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
        return "Printer: " + printer + " not found.";
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
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    @Override
    public String submitChoice(String choice, String arg1, String arg2, String username) {
        String role = null;
        for(User user: userList){
            if(user.getUsername().equals(username)){
                role = user.getRole();
            }
        }
        String result = null;
        switch (choice) {
            case "print":
                if(ACMODE == 1) {
                    if (roles.get("print").contains(username)) {
                        if (arg1 != null && arg2 != null)
                            result = (print(arg1, arg2));
                        else
                            result = "Arguments must not be null.";
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                } else {
                    if(roles.get(role).contains("print")){
                        if (arg1 != null && arg2 != null)
                            result = (print(arg1, arg2));
                        else
                            result = "Arguments must not be null.";
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                }
            case "queue":
                if(ACMODE == 1) {
                    if (roles.get("queue").contains(username)) {
                        result = queue();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                } else {
                    if (roles.get(role).contains("queue")) {
                        result = queue();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                }
            case "top queue":
                if(ACMODE == 1) {
                    if (roles.get("topQueue").contains(username)) {
                        if (arg1 != null && parseInteger(arg1)) {
                            result = topQueue(Integer.parseInt(arg1));
                        } else {
                            result = "Invalid argument.";
                        }
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                } else {
                    if (roles.get(role).contains("topQueue")) {
                        if (arg1 != null && parseInteger(arg1)) {
                            result = topQueue(Integer.parseInt(arg1));
                        } else {
                            result = "Invalid argument.";
                        }
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                }
            case "start":
                if(ACMODE == 1) {
                    if (roles.get("start").contains(username)) {
                        result = start();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                } else {
                    if (roles.get(role).contains("start")) {
                        result = start();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                }
            case "stop":
                if(ACMODE == 1) {
                    if (roles.get("stop").contains(username)) {
                        result = stop();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                } else {
                    if (roles.get(role).contains("stop")) {
                        result = stop();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                }
            case "restart":
                if(ACMODE == 1) {
                    if (roles.get("restart").contains(username)) {
                        result = restart();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                } else {
                    if (roles.get(role).contains("restart")) {
                        result = restart();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                }
            case "status":
                if(ACMODE == 1) {
                    if (roles.get("status").contains(username)) {
                        result = status();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                } else {
                    if (roles.get(role).contains("status")) {
                        result = status();
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                }
            case "read configuration":
                if(ACMODE == 1) {
                    if (roles.get("readConfig").contains(username)) {
                        if (arg1 != null)
                            result = readConfig(arg1);
                        else
                            result = "Invalid argument.";
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                } else {
                    if (roles.get(role).contains("readConfig")) {
                        if (arg1 != null)
                            result = readConfig(arg1);
                        else
                            result = "Invalid argument.";
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                }
            case "set configuration":
                if(ACMODE == 1) {
                    if (roles.get("setConfig").contains(username)) {
                        if (arg1 != null && arg2 != null) {
                            result = (setConfig(arg1, arg2));
                        } else {
                            result = "Invalid argument.";
                        }
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                } else {
                    if (roles.get(role).contains("setConfig")) {
                        if (arg1 != null && arg2 != null) {
                            result = (setConfig(arg1, arg2));
                        } else {
                            result = "Invalid argument.";
                        }
                        break;
                    } else {
                        result = "You are not allowed to perform this operation.";
                        break;
                    }
                }
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
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
