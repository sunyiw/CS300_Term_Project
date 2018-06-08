//Yiwen Sun
//CS300

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class Client {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Scanner scanner;

    public static void main(String args[]) {

        new Client().start();
    }

    public void start() {

        try {

            socket = new Socket("localhost", 8080);//localhost:8080

            dis = new DataInputStream(this.socket.getInputStream());
            dos = new DataOutputStream(this.socket.getOutputStream());
            scanner = new Scanner(System.in);

            new Thread(new SendThread()).start();
            new Thread(new RecThread()).start();
            
            //dos.writeUTF("I am connected");

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    class SendThread implements Runnable {

        private String str;
        private Boolean iConnect = false;


        public void run() {
            iConnect = true;
            try {
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. View Users List");
                System.out.println("4. View History");
                System.out.println("0. Logout");
                System.out.println("The default mode is public chat, If you want to send private message, please use following format:");
                System.out.println("Tom:Hi");

                while (iConnect) {
                    str = scanner.nextLine();
                    if (null == str || "".equals(str.trim()) || str.isEmpty() || str.length() == 0 || str.startsWith("***")) {
                        continue;
                    } else {
                        if(str.equals("1")){
                            System.out.print("User name: ");
                            String n = scanner.nextLine();
                            System.out.print("Password: ");
                            String m = scanner.nextLine();
                            dos.writeUTF("***register-"+n+"-"+m);
                        }
                        else if(str.equals("2")){
                            System.out.print("User name: ");
                            String n = scanner.nextLine();
                            System.out.print("Password: ");
                            String m = scanner.nextLine();
                            dos.writeUTF("***login-"+n+"-"+m);
                        }
                        else if(str.equals("3")){
                            dos.writeUTF("***usersList");
                        }
                        else if(str.equals("4")){
                            dos.writeUTF("***history");
                        }
                        else if(str.equals("0")){
                            dos.writeUTF("***logout");
                        }
                        else{
                            dos.writeUTF(str);
                            //System.out.println("I: " + str);
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class RecThread implements Runnable {

        private String str;
        private Boolean iConnect = false;

        public void run() {
            iConnect = true;
            try {
                while (iConnect) {
                    str = dis.readUTF();
                    System.out.println(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
