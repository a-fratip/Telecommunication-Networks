package elec366.lab4;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class clientThread extends Thread {

    //the ClientServiceThread class extends the Thread class and has the following parameters
    public String clientName;
    public String opponent;
    public String choice;
    public Socket connectionSocket; //client connection socket
    ArrayList<clientThread> clients; //list of all clients connected to the server

    //constructor function
    public clientThread(String clientName, Socket connectionSocket, ArrayList<clientThread> clients) {

        this.clientName = clientName;
        this.connectionSocket = connectionSocket;
        this.clients = clients;

        this.opponent = "";
        this.choice = "";

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
                            if (clients.get(i).opponent.equals(clientName)) { // player's opponent is the client that needs to be removed
                                clients.get(i).opponent = ""; // clear opponent
                                clients.get(i).opponent = ""; // clear choice

                                String availableClient = clients.get(i).clientName; // save available client's name in a string

                                for (int j = 0; j < clients.size(); j++) { // add available player to player list of appropriate clients
                                    if (!(clients.get(j).clientName.equals(availableClient) || clients.get(j).clientName.equals(clientName))) {
                                        outToClient = new DataOutputStream(clients.get(j).connectionSocket.getOutputStream());
                                        outToClient.writeBytes("-AddUser;" + availableClient + "\n");
                                    }
                                }

                            }
                            outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-RemUser;true" + clientName + "\n"); //send to all other users to remove client from each drop down menu
                        }
                    }


                } else if (clientSentence.startsWith("-Request")) { //request from clientName to opponent
                    String [] msg = clientSentence.split(";");

                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).clientName.equals(msg[1])) {
                            if(clients.get(i).opponent.isEmpty()) { //check that opponent is not already in a game
                                opponent = msg[1]; //set client opponent
                                clients.get(i).opponent = clientName; //set opponent for other client

                                outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                                outToClient.writeBytes("-Request;" + clientName + "\n"); //send game request to opponent

                                for (int j = 0; j < clients.size(); j++) { //remove unavailable clients from all lists

                                    outToClient = new DataOutputStream(clients.get(j).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-RemUser;false;" + clientName + "\n");
                                    outToClient.writeBytes("-RemUser;false;" + opponent + "\n");

                                }

                            } else { // in case client already has an opponent
                                for (int j = 0; j < clients.size(); j++) {
                                    if (clients.get(j).clientName.equals(clientName)) { //end the game
                                        outToClient = new DataOutputStream(clients.get(j).connectionSocket.getOutputStream());
                                        outToClient.writeBytes("-EndGame;" + msg[1] + "\n");
                                    }
                                }

                            }

                            break;
                        }

                    }

                } else if (clientSentence.startsWith("-StartGame")) {

                    String[] msg = clientSentence.split(";");

                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).clientName.equals(msg[1]) || clients.get(i).clientName.equals((clientName))) {
                            outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-StartGame\n");
                        }
                    }

                } else if (clientSentence.startsWith("-Choice")) { // player made a game choice
                    String[] msg = clientSentence.split(";");
                    choice = msg[1];

                    // check for opponent's choice
                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).clientName.equals(opponent)) {
                            if (!clients.get(i).choice.isEmpty()) {
                                String choices = choice + " x " + clients.get(i).choice; // store both choice in a string for comparison
                                String results;

                                // determine winner via switch case
                                switch (choices) {
                                    case "Rock x Paper":
                                        results = "lose;win";
                                        break;
                                    case "Paper x Rock":
                                        results = "win;lose";
                                        break;
                                    case "Rock x Scissors":
                                        results = "win;lose";
                                        break;
                                    case "Scissors x Rock":
                                        results = "lose;win";
                                        break;
                                    case "Scissors x Paper":
                                        results = "win;lose";
                                        break;
                                    case "Paper x Scissors":
                                        results = "lose;win";
                                        break;
                                    case "Rock x Rock":
                                        results = "draw;draw";
                                        break;
                                    case "Paper x Paper":
                                        results = "draw;draw";
                                        break;
                                    case "Scissors x Scissors":
                                        results = "draw;draw";
                                        break;
                                    default:
                                        results = "error;error";

                                }

                                // send message to players informing them of the results
                                if (results.equals("win;lose")) {
                                    outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-Results;" + choices + "... " + clientName + " wins\n");

                                    for (int j = 0; j < clients.size(); j++) {
                                        if (clients.get(j).clientName.equals(clientName)) {
                                            outToClient = new DataOutputStream(clients.get(j).connectionSocket.getOutputStream());
                                            outToClient.writeBytes("-Results;" + choices + "... " + "You win\n");
                                        }
                                    }
                                } else if (results.equals("lose;win")) {
                                    outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-Results;" + choices + "... " + "You win\n");

                                    for (int j = 0; j < clients.size(); j++) {
                                        if (clients.get(j).clientName.equals(clientName)) {
                                            outToClient = new DataOutputStream(clients.get(j).connectionSocket.getOutputStream());
                                            outToClient.writeBytes("-Results;" + choices + "... " + opponent + " wins\n");
                                        }
                                    }

                                } else if (results.equals("draw;draw")) {
                                    outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-Results;" + choices + "... draw, Play again!\n");

                                    for (int j = 0; j < clients.size(); j++) {
                                        if (clients.get(j).clientName.equals(clientName)) {
                                            outToClient = new DataOutputStream(clients.get(j).connectionSocket.getOutputStream());
                                            outToClient.writeBytes("-Results;" + choices + "... draw, Play again!\n");
                                        }
                                    }

                                } else { // in case an error occurs
                                    outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-Results;" + "There was an error, try again\n");

                                    for (int j = 0; j < clients.size(); j++) {
                                        if (clients.get(j).clientName.equals(clientName)) {
                                            outToClient = new DataOutputStream(clients.get(j).connectionSocket.getOutputStream());
                                            outToClient.writeBytes("-Results;" + "There was an error, try again!\n");
                                        }
                                    }

                                }

                                // clear both players choices for new game
                                choice = "";
                                clients.get(i).choice = "";

                            }
                        }
                    }


                } else if (clientSentence.startsWith("-EndGame") && !opponent.isEmpty()) { // a client has stopped and need to tell his opponent
                    // add available players to all lists again
                    for (int i = 0; i < clients.size(); i++) {
                        outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                        if (clients.get(i).clientName.equals(clientName)) {
                            outToClient.writeBytes("-AddUser;" + opponent + "\n");
                        } else if (clients.get(i).clientName.equals(opponent)) {
                            outToClient.writeBytes("-AddUser;" + clientName + "\n");
                        }
                        else {
                            outToClient.writeBytes("-AddUser;" + clientName + "\n");
                            outToClient.writeBytes("-AddUser;" + opponent + "\n");
                        }
                    }

                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).clientName.equals(opponent)) {
                            outToClient = new DataOutputStream(clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-EndGame\n"); // let opponent know that game ended

                            // clear opponents and choice for new game
                            clients.get(i).opponent = "";
                            clients.get(i).choice = "";
                            opponent = "";
                            choice = "";
                            break;
                        }
                    }

                }
            }

        } catch(Exception ex) {

        }

    }

}


