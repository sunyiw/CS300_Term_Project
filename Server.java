//Yiwen Sun
//CS300

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.time.LocalDateTime;


public class Server {

    ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
    ArrayList<User> users = new ArrayList<User>();


    public static void main(String args []) {

        new Server().start();
    }

    public void start() {

        try {

            boolean iConnect = false;
            ServerSocket serverSocket = new ServerSocket(8080);
            iConnect = true;

            while (iConnect) {

                Socket socket = serverSocket.accept();

                ClientThread currentClient = new ClientThread(socket);
                clients.add(currentClient);
                new Thread(currentClient).start();
                //currentClient.sendMsg("Client" + "Connected");
            }

        } catch (IOException e) {
            
            e.printStackTrace();

        }
    }

    class ClientThread implements Runnable{

        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private String str;
        private Boolean iConnect = false;
        private Boolean iLogin = false;
        private String name;

        ClientThread(Socket socket) {
            this.socket = socket;
            iConnect = true;
        }

        public void run() {
            try {
                while (iConnect) {

                    dis = new DataInputStream(socket.getInputStream());
                    str = dis.readUTF();
                    if(str.startsWith("***register")){//***register-Name-Pass
                        if(this.iLogin){
                            this.sendMsg("<Error> You have logined.");
                        }else{
                            int firstDash = str.indexOf("-");
                            String n = str.substring(12, str.indexOf("-", firstDash+1));//name
                            String p = str.substring(str.indexOf("-", firstDash+1)+1);//Password
                            //this.sendMsg(n+p);
                            Boolean userExist = false;
                            for (int i = 0; i < users.size(); i++) {
                                User user = users.get(i);
                                if(user.userName.equals(n))
                                    userExist = true;
                            }
                            if(userExist){
                                this.sendMsg("<Error> User exist.");
                            }else{
                                this.sendMsg("<Succeed> You are register as " +n+", password: "+p);
                                this.iLogin = true;
                                User currentUser = new User(n, p);
                                users.add(currentUser);
                                
                                this.name = n;
                                for (int i = 0; i < clients.size(); i++) {
                                    ClientThread clientThread = clients.get(i);
                                    clientThread.sendMsg(n + " is connected");
                                }
                            }
                        }
                    }
                    else if(str.startsWith("***login")){//***login-Name-Pass
                        if(this.iLogin){
                            this.sendMsg("<Error> You have logined.");
                        }else{
                            int firstDash = str.indexOf("-");
                            String n = str.substring(9, str.indexOf("-", firstDash+1));//name
                            String p = str.substring(str.indexOf("-", firstDash+1)+1);//Password
                            //this.sendMsg(n+p);
                            for (int i = 0; i < users.size(); i++) {
                                User user = users.get(i);
                                if(user.userName.equals(n)){
                                    if(user.userPass.equals(p)){
                                        this.sendMsg("<Succeed> You are login as " + n);
                                        this.iLogin = true;
                                        this.name = n;
                                        user.online = true;
                                        for (int j = 0; j < clients.size(); j++) {
                                            ClientThread clientThread = clients.get(j);
                                            clientThread.sendMsg(n + " is connected");
                                        }
                                    }
                                }
                            }
                            if(!this.iLogin){
                                this.sendMsg("<Error> Can't match your username or password" + n);
                            }
                        }
                    }
                    else if(!this.iLogin){
                        this.sendMsg("<Error> You have to login first.");
                    }
                    else if(str.startsWith("***usersList")){
                        this.sendMsg("Name\tStatus");
                        for (int i = 0; i < users.size(); i++) {
                            User user = users.get(i);
                            if(user.online)
                                this.sendMsg(user.userName + "\ton line");
                            else
                                this.sendMsg(user.userName + "\toff line");
                        }
                    }
                    else if(str.startsWith("***logout")){
                        this.sendMsg("You are disconnected");
                        this.iLogin = false;
                        for (int i = 0; i < users.size(); i++) {
                            User user = users.get(i);
                            if(user.userName.equals(this.name)){
                                user.online = false;
                            }
                        }
                        for (int i = 0; i < clients.size(); i++) {
                            ClientThread clientThread = clients.get(i);
                            if (!clientThread.equals(this))
                                clientThread.sendMsg(this.name + " is disconnected");
                        }
                        this.iConnect = false;
                    }
                    else if(str.startsWith("***history")){
                        for (int i = 0; i < users.size(); i++) {
                            User user = users.get(i);
                            if(this.name.equals(user.userName)){
                                for (int j = 0; j < user.historys.size(); j++) {
                                    this.sendMsg(user.historys.get(j));
                                }
                            }
                        }
                    }
                    else if(str.contains(":")){//Tom:Hellow
                        String n;//name
                        String m;//message
                        LocalDateTime currentTime = LocalDateTime.now();
                        n = str.substring(0, str.indexOf(":"));
                        m = str.substring(str.indexOf(":")+1);
                        Boolean userExist = false;
                        for (int i = 0; i < clients.size(); i++) {
                            ClientThread clientThread = clients.get(i);
                            if(clientThread.name.equals(n)){
                                clientThread.sendMsg("<Private message> "+currentTime+" "+this.name+": "+m);
                                userExist = true;
                            }
                        }
                        if(userExist){
                            this.sendMsg("<Private message> "+currentTime+" To "+n+" "+m);
                            for (int i = 0; i < users.size(); i++) {
                                User user = users.get(i);
                                if(user.userName.equals(n))
                                    user.historys.add("<Private message> "+currentTime+" "+this.name+": "+m);
                                if(user.userName.equals(this.name))
                                    user.historys.add("<Private message> "+currentTime+" To "+n+" "+m);
                            }
                        }
                        else{
                            this.sendMsg("<Error> User not exist");
                        }
                    }
                    else{//Public conversation
                        LocalDateTime currentTime = LocalDateTime.now();
                        for (int i = 0; i < clients.size(); i++) {
                            ClientThread clientThread = clients.get(i);
                                //clientThread.sendMsg("Client-" + currentIndex + ": " + str);
                                clientThread.sendMsg(currentTime+" "+this.name + ": " + str);
                        }
                        for (int i = 0; i < users.size(); i++) {
                            User user = users.get(i);
                            user.historys.add(currentTime+" "+this.name + ": " + str);
                        }

                    }
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        public void sendMsg(String str) {

            try {

                dos = new DataOutputStream(this.socket.getOutputStream());
                dos.writeUTF(str);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }

    }
    class User {
        private String userName;
        private String userPass;
        private Boolean online;
        private ArrayList<String> historys;
        
        User(String n, String p) {
            this.userName = n;
            this.userPass = p;
            this.online = true;
            historys = new ArrayList<String>();
        }
    }
}
