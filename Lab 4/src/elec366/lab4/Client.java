package elec366.lab4;

import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;

public class Client {

    private static final int SERVER_PORT = 6789;

    //class variables to be accessed in Thread
    static Socket clientSocket;
    static JComboBox<String> availablePlayers; // drop down menu to choose players to play with
    static JButton playButton;
    static JTextField nameTextField;
    static String clientName;
    static JButton rockButton;
    static JButton paperButton;
    static JButton scissorsButton;
    static String opponent;

    // other UI elements
    protected static JFrame frame;
    protected static JLabel clientNameLabel;
    protected static JButton connectButton;
    protected static JLabel playWithLabel;
    protected static JLabel messageLabel; // label to display messages about winners and/or players stopping/disconnecting


    public static void main(String[] args) throws Exception {

        setupGUI();

        onClickConnectButton();

        onClickPlayButton();

        disconnectOnClose();

        frame.setVisible(true);

    }

    private static void disconnectOnClose() {
        //Disconnect on window close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                try {

                    //create an output stream and send a RemUser message to disconnect from the server
                    DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());
                    outToServer.writeBytes("-RemUser" + "\n");

                    //close the client's socket
                    clientSocket.close();

                    System.exit(0); //exit code

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }

            }
        });
    }

    private static void onClickPlayButton() {
        //Action listener when play button is pressed
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    if (playButton.getText().equals("Play")) { // button pressed to start a game

                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

                        if (availablePlayers.getSelectedItem() == null) {
                            messageLabel.setForeground(Color.red);
                            messageLabel.setText("You have not selected an opponent!");
                        }
                        else {
                            String request = "-Request;" + availablePlayers.getSelectedItem() + "\n";
                            outToServer.writeBytes(request);

                            opponent = (String) availablePlayers.getSelectedItem();

                            playButton.setText("Stop");
                            playButton.setEnabled(true);
                            availablePlayers.setEnabled(false);

                            messageLabel.setText(""); // clear message label
                        }

                    } else if (playButton.getText().equals("Stop")) { // button pressed to stop a game
                        // remove game components
                        rockButton.setVisible(false);
                        paperButton.setVisible(false);
                        scissorsButton.setVisible(false);

                        availablePlayers.setEnabled(true); // allow player to choose new opponent

                        // let opponent know that game has ended
                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        outToServer.writeBytes("-EndGame;\n");

                        playButton.setText("Play"); // allow player to start a new game
                        playButton.setEnabled(true);

                        messageLabel.setForeground(Color.red);
                        messageLabel.setText("The game has ended");

                    }
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }

            }});

    }

    private static void onClickConnectButton() {
        //Action listener when connect/disconnect button is pressed
        connectButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try {

                    if (connectButton.getText().equals("Connect")) { //if pressed to Connect

                        if (!nameTextField.getText().isEmpty()) { //check if field contains name
                            clientName = nameTextField.getText();

                            //create a new socket to connect with the server application
                            clientSocket = new Socket ("localhost", SERVER_PORT);

                            //send the name
                            DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());
                            outToServer.writeBytes("-AddUser;" + clientName + "\n"); //send name to server for validation

                            //receive the reply (rejected or accepted)
                            BufferedReader inFromServer = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
                            String receivedReply = inFromServer.readLine(); // packet starting with Valid/Invalid
                            String []strings = receivedReply.split(";");

                            if (strings[0].equals("Invalid")) { //reject
                                // display a message rejected
                                messageLabel.setText("Connection rejected: The name " + clientName + " is used by another client");
                                messageLabel.setForeground(Color.RED);
                                messageLabel.setVisible(true);

                            } else if (strings[0].equals("Valid")) { //connected

                                StartThread(); //this Thread checks for input messages from server

                                // add available players to player list
                                for (int i = 1; i < strings.length; i++) {
                                    availablePlayers.addItem(strings[i]);
                                }

                                availablePlayers.setEnabled(true);
                                playButton.setEnabled(true);

                                //change the Connect button text to Disconnect
                                connectButton.setText("Disconnect");
                                nameTextField.setEnabled(false);

                                opponent = "";

                                if (availablePlayers.getItemCount() == 0) { // player is the first client connected
                                    availablePlayers.setEnabled(false);
                                    playButton.setEnabled(false);

                                    messageLabel.setForeground(Color.red);
                                    messageLabel.setText("There is no one for you to play with right now...");
                                }
                            }
                        }

                    } else { //if pressed to Disconnect
                        // TODO: disconnect not working as expected when a game is running

                        //create an output stream and send a RemUser message to disconnect from the server
                        DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());
                        outToServer.writeBytes("-RemUser" + "\n");

                        //close the client's socket
                        clientSocket.close();

                        //change the Disconnect button text to Connect
                        connectButton.setText("Connect");
                        nameTextField.setEnabled(true);

                        // remove playing buttons if visible
                        rockButton.setVisible(false);
                        paperButton.setVisible(false);
                        scissorsButton.setVisible(false);

                        playButton.setEnabled(false);
                        playButton.setText("Play");
                        availablePlayers.setEnabled(false);

                    }

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }});

    }

    private static void setupGUI() {
        //Create the GUI frame and components
        frame = new JFrame ("RPS Game Client");
        frame.setLayout(null);
        frame.setBounds(100, 100, 500, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        clientNameLabel = new JLabel("Client Name");
        clientNameLabel.setBounds(20, 20, 80, 30);
        frame.getContentPane().add(clientNameLabel);

        nameTextField = new JTextField(); // text field for client name
        nameTextField.setBounds(110, 20, 150, 30);
        frame.getContentPane().add(nameTextField);

        connectButton = new JButton("Connect"); // button to connect/disconnect from server
        connectButton.setBounds(290, 18, 100, 30);
        frame.getContentPane().add(connectButton);

        playWithLabel = new JLabel("Play with:");
        playWithLabel.setBounds(20, 60, 80, 30);
        frame.getContentPane().add(playWithLabel);

        availablePlayers = new JComboBox<String>(); //create drop down menu
        availablePlayers.setBounds(110, 60, 150, 30);
        frame.add(availablePlayers);

        playButton = new JButton("Play"); //button to send message
        playButton.setBounds(290, 58, 100, 30);
        frame.getContentPane().add(playButton);

        rockButton = new JButton("ROCK");
        rockButton.setBounds(210,100,80,80);
        rockButton.setVisible(false);
        frame.getContentPane().add(rockButton);

        paperButton = new JButton("PAPER");
        paperButton.setBounds(210,190,80,80);
        paperButton.setVisible(false);
        frame.getContentPane().add(paperButton);

        scissorsButton = new JButton("SCISSORS");
        scissorsButton.setBounds(210,280,80,80);
        scissorsButton.setVisible(false);
        frame.getContentPane().add(scissorsButton);

        messageLabel = new JLabel();
        messageLabel.setBounds(20, 400, 500, 50);
//        messageLabel.setVisible(false);
        messageLabel.setText("");
        frame.getContentPane().add(messageLabel);
    }

    //Thread to always read messages from the server and print them in the textArea
    private static void StartThread() {

        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                //create a buffer reader and connect it to the socket's input stream
                BufferedReader inFromServer = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));

                String receivedSentence;

                //always read received messages and append them to the textArea
                while (true) {

                    receivedSentence = inFromServer.readLine();
                    //System.out.println(receivedSentence);

                    if (receivedSentence.startsWith("-AddUser")) { //new user to add to player list

                        String []strings = receivedSentence.split(";");

                        //add user to drop down menu for playing
                        availablePlayers.addItem(strings[1]); // second index of strings array is name of player

                        if (availablePlayers.getItemCount() == 1 && opponent.isEmpty()) {
                            messageLabel.setText("");
                            availablePlayers.setEnabled(true);
                            playButton.setEnabled(true);
                        }

                    } else if (receivedSentence.startsWith("-Request")) {
                        String[] strings = receivedSentence.split(";");
                        opponent = strings[1];

                        availablePlayers.setEnabled(false);
                        playButton.setText("Stop");
                        playButton.setEnabled(true);

                        // tell opponent that game is starting
                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        outToServer.writeBytes("-StartGame;" + strings[1] + "\n");

                    } else if (receivedSentence.startsWith("-StartGame")) {
                        availablePlayers.setEnabled(false); // can no longer choose an opponent

                        // show and enable game buttons
                        rockButton.setVisible(true);
                        paperButton.setVisible(true);
                        scissorsButton.setVisible(true);

                        rockButton.setEnabled(true);
                        paperButton.setEnabled(true);
                        scissorsButton.setEnabled(true);

                        messageLabel.setForeground(Color.BLUE);
                        messageLabel.setText("Game started against " + opponent + "!");
                        messageLabel.setVisible(true);

                        // action listener for rock button
                        rockButton.addActionListener( new ActionListener() { //rock is chosen
                            public void actionPerformed(ActionEvent e) {

                                DataOutputStream outToServer;
                                try {

                                    //disable all buttons until new game starts
                                    rockButton.setEnabled(false);
                                    paperButton.setEnabled(false);
                                    scissorsButton.setEnabled(false);

                                    outToServer = new DataOutputStream (clientSocket.getOutputStream());
                                    outToServer.writeBytes("-Choice;Rock\n"); //send choice to server

                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });

                        // action listener for paper button
                        paperButton.addActionListener( new ActionListener() { //paper is chosen
                            public void actionPerformed(ActionEvent e) {

                                DataOutputStream outToServer;
                                try {

                                    //disable all buttons until new game starts
                                    rockButton.setEnabled(false);
                                    paperButton.setEnabled(false);
                                    scissorsButton.setEnabled(false);

                                    outToServer = new DataOutputStream (clientSocket.getOutputStream());
                                    outToServer.writeBytes("-Choice;Paper\n"); //send choice to server

                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });

                        // action listener for scissors button
                        scissorsButton.addActionListener( new ActionListener() { //scissors is chosen
                            public void actionPerformed(ActionEvent e) {

                                DataOutputStream outToServer;
                                try {

                                    //disable all buttons until new game starts
                                    rockButton.setEnabled(false);
                                    paperButton.setEnabled(false);
                                    scissorsButton.setEnabled(false);

                                    outToServer = new DataOutputStream (clientSocket.getOutputStream());
                                    outToServer.writeBytes("-Choice;Scissors\n"); //send choice to server

                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });


                    } else if (receivedSentence.startsWith("-EndGame")) {
                        //hide all buttons that are not needed anymore for communication with opponent
                        rockButton.setVisible(false);
                        paperButton.setVisible(false);
                        scissorsButton.setVisible(false);

                        rockButton.setEnabled(true);
                        paperButton.setEnabled(true);
                        scissorsButton.setEnabled(true);

                        playButton.setText("Play");
                        playButton.setEnabled(true);
                        availablePlayers.setEnabled(true);

                        messageLabel.setForeground(Color.red); //tell player opponent ended the game
                        messageLabel.setText("You can not play with " + opponent + ", Please choose another player");

                        opponent = "";

                    } else if (receivedSentence.startsWith("-RemUser")) {

                        String []strings = receivedSentence.split(";");

                        for (int i = 0; i < availablePlayers.getItemCount(); i++) {
                            if (availablePlayers.getItemAt(i).equals(strings[2])) { //find and remove player from drop down menu
                                availablePlayers.removeItemAt(i);
                                break;
                            }
                        }

                        if (strings[2].equals(opponent) && strings[1].equals("true")) { // opponent disconnected
                            // reset client gui
                            playButton.setText("Play");
                            playButton.setEnabled(true);
                            availablePlayers.setEnabled(true);

                            rockButton.setVisible(false);
                            paperButton.setVisible(false);
                            scissorsButton.setVisible(false);

                            rockButton.setEnabled(true);
                            paperButton.setEnabled(true);
                            scissorsButton.setEnabled(true);

                            messageLabel.setForeground(Color.RED);
                            messageLabel.setText("You can not play with " + opponent + " , Please choose another player");

                            opponent = "";
                        }

                        if (availablePlayers.getItemCount() == 0 && opponent.isEmpty()) { // player is the only client connected
                            // do not allow a game to start
                            availablePlayers.setEnabled(false);
                            playButton.setEnabled(false);

                            messageLabel.setForeground(Color.RED);
                            messageLabel.setText("There is no one for you to play with right now...");
                        }

                    }
                    else if (receivedSentence.startsWith("-Result")) {
                        String [] strings = receivedSentence.split(";");
                        messageLabel.setForeground(Color.BLUE);
                        messageLabel.setText(strings[1]);

                        // enable game buttons again
                        rockButton.setEnabled(true);
                        paperButton.setEnabled(true);
                        scissorsButton.setEnabled(true);
                    } else if (receivedSentence.startsWith("-OnlyUser")) { // only one player connected
                        // do not allow a game to start
                        availablePlayers.setEnabled(false);
                        playButton.setEnabled(false);

                        messageLabel.setForeground(Color.RED);
                        messageLabel.setText("There is no one for you to play with right now...");
                    }
                }

            }
            catch(Exception ex) {

            }

        }}).start();

    }

}
