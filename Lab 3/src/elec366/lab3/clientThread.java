package elec366.lab3;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class clientThread extends Thread {

    //the ClientServiceThread class extends the Thread class and has the following parameters
    public String clientName; //client name
    public Socket connectionSocket; //client connection socket
    ArrayList<clientThread> clients; //list of all clients connected to the server

    //constructor function
    public clientThread(String clientName, Socket connectionSocket, ArrayList<clientThread> clients) {

        this.clientName = clientName;
        this.connectionSocket = connectionSocket;
        this.clients = clients;

    }

    //thread's run function
    public void run() {

        try {

            //create a buffer reader and connect it to the client's connection socket
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));;
            String clientSentence;
            DataOutputStream outToClient;

            //always read messages from client
            while (true) {

                clientSentence = inFromClient.readLine();

                //check the start of the message
                if (clientSentence.startsWith("-RemUser")) { //Remove Client

                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).clientName.equals(clientName)) {
                            clients.remove(i); // remove from list of clients
                            i--; // decrement counter after removal to avoid skipping a "compare"
                        } else {
                            outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-RemUser;" + clientName + "\n"); //send to all other users to remove client from each drop down menu
                        }
                    }


                } else if (clientSentence.startsWith("-Message")) { // message needs to be sent

                    String []msg = clientSentence.split(";");
                    //System.out.println("Server: " + clientSentence);

                    for (int i = 0; i < clients.size(); i++) {
                        if (!clients.get(i).clientName.equals(clientName)) { //avoid sending message back to the sender
                            if (msg[1].equals(clients.get(i).clientName)) { //check if message should be private
                                outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                                outToClient.writeBytes("-Message;" + clientName + " (Privately): " + msg[2] + "\n"); //send private message with sender name
                            } else if (msg[1].equals("All Users")) { //check if message for all users
                                outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                                outToClient.writeBytes("-Message;" + clientName + ": " + msg[2] + "\n"); //send general message with sender name
                            }
                        }
                    }

                }
            }

        } catch(Exception ex) {

        }

    }

}


