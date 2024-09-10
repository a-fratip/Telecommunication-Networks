package elec366.lab4;

import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Server {

    //Array of type ClientServiceThread, for all connected clients
    public static ArrayList<clientThread> clients = new ArrayList<clientThread>();

    private static final int SERVER_PORT = 6789;
    static ServerSocket welcomeSocket;

    // UI elements
    protected static JFrame frame;
    protected static JLabel connectionStatusLabel;

    public static void main(String[] args) throws Exception {

        setupGUI();

        //create the welcoming server's socket (should not be closed)
        welcomeSocket = new ServerSocket(SERVER_PORT);

        listenThread();

        updateThread();

        frame.setVisible(true);

    }

    private static void updateThread() {
        //thread to always get the count of connected clients and update the label and send to clients if they are alone online
        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                DataOutputStream outToClient;

                while (true) {

                    if (clients.size() > 0) //if there are one or more clients print their number
                    {
                        if (clients.size() == 1) {

                            connectionStatusLabel.setText("1 Client Connected");

                            //tell client if they are the only one connected
                            outToClient = new DataOutputStream(clients.get(0).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-OnlyUser" + "\n");

                        } else {

                            connectionStatusLabel.setText(clients.size() + " Clients Connected");

                        }

                        connectionStatusLabel.setForeground(Color.blue);
                    }
                    else { //if there are no clients connected, print "No Clients Connected"

                        connectionStatusLabel.setText("No Clients Connected");
                        connectionStatusLabel.setForeground(Color.red);

                    }

                    Thread.sleep(1000);

                }

            } catch (Exception ex) {

            }

        }}).start();

    }

    private static void listenThread() {
        //thread to always listen for new connections from clients
        new Thread (new Runnable(){ @Override
        public void run() {

            Socket connectionSocket;
            DataOutputStream outToClient;

            while (!welcomeSocket.isClosed()) { // socket is open

                try {

                    //when a new client connect, accept this connection and assign it to a new connection socket
                    connectionSocket = welcomeSocket.accept();

                    //receive the connection request with name
                    BufferedReader inFromClient = new BufferedReader (new InputStreamReader(connectionSocket.getInputStream()));
                    String receivedRequest = inFromClient.readLine();

                    String check = "Valid"; // request should be valid by default

                    if (receivedRequest.startsWith("-AddUser")) {
                        String []strings = receivedRequest.split(";");

                        String clientName = strings[1];
                        String users = "";

                        // check if name is used by another client
                        for (int i = 0; i < clients.size(); i++) {
                            // store list of all available players in a string
                            if (clients.get(i).opponent.isEmpty()) { // no opponent
                                users = users + ";" + clients.get(i).clientName;
                            }

                            if (clients.get(i).clientName.equals(clientName)) {
                                check = "Invalid"; // name is already in use
                                break;
                            }
                        }

                        if (check.equals("Valid")) { // name is not used

                            // send list of existing users to client to be added to player drop down menu
                            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                            outToClient.writeBytes(check + users + "\n");

                            // send client name to all other clients so new name can be added to their recipient list
                            for (int i = 0; i < clients.size(); i++) {
                                outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                                outToClient.writeBytes("-AddUser;" + clientName + "\n");
                            }

                            //add the new client to the client's array
                            clients.add(new clientThread(clientName, connectionSocket, clients));

                            //start the new client's thread
                            clients.get(clients.size() - 1).start();

                        } else if (check.equals("Invalid")) {
                            // send back invalid message
                            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                            outToClient.writeBytes(check + "\n");
                        }

                    }

                }
                catch (Exception ex) {

                }

            }

        }}).start();

    }

    private static void setupGUI() {
        //Create the GUI frame and components
        frame = new JFrame ("RPS Game Server");
        frame.setLayout(null);
        frame.setBounds(100, 100, 300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        connectionStatusLabel = new JLabel("No Clients Connected");
        connectionStatusLabel.setBounds(80, 30, 200, 30);
        connectionStatusLabel.setForeground(Color.red);
        frame.getContentPane().add(connectionStatusLabel);

    }

}



