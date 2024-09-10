package elec366.lab3;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;

public class Client {

    private static final int SERVER_PORT = 6789;

    //class variables to be accessed in Thread
    static Socket clientSocket;
    static JTextArea mainTextArea;
    static JComboBox<String> recipient;
    static JButton sendButton;
    static JTextArea messageTextArea;
    static JTextField nameTextField;
    static String clientName;

    // other UI elements
    protected static JFrame frame;
    protected static JLabel clientNameLabel;
    protected static JButton connectButton;
    protected static JScrollPane mainTextAreaScroll;
    protected static JLabel sendToLabel;


    public static void main(String[] args) throws Exception {

        setupGUI();

        onClickConnectButton();

        onClickSendButton();

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

    private static void onClickSendButton() {
        //Action listener when send button is pressed
        sendButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    //create an output stream
                    DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());

                    if (!messageTextArea.getText().equals("")) { //make text area contains a message

                        // Send message with intended recipient
                        String sendingSentence = "-Message;" + recipient.getSelectedItem() + ";" + messageTextArea.getText() + "\n"; // using ; for field seperation
                        outToServer.writeBytes(sendingSentence);
//                        System.out.println("Sending:" + sendingSentence);

                        //update sender's main text area
                        if (recipient.getSelectedItem().equals("All Users")) { // global message
                            mainTextArea.setText(mainTextArea.getText() + "\n" + "You: " + messageTextArea.getText()); //append message to existing text
                        } else { // private message
                            mainTextArea.setText(mainTextArea.getText() + "\n" + "You to " + recipient.getSelectedItem() + ": " + messageTextArea.getText());
                        }
                        messageTextArea.setText(""); //clear message text area for next messages

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
                                mainTextArea.setText("Connection rejected: The name " + clientName + " is used by another client");

                            } else if (strings[0].equals("Valid")) { //connected

                                StartThread(); //this Thread checks for input messages from server

                                // create combo box to list "send to" recipients
                                String[] choices = {"All Users"}; //default recipients
                                recipient = new JComboBox<String>(choices); //create drop down menu
                                recipient.setBounds(110, 380, 150, 30);
                                recipient.setVisible(true);
                                frame.add(recipient);

                                //add existing users to recipient list
                                for (int i = 1; i < strings.length; i++) {
                                    recipient.addItem(strings[i]);
                                }

                                //make the GUI components visible, so the client can send messages
                                sendButton.setVisible(true);
                                sendToLabel.setVisible(true);
                                messageTextArea.setVisible(true);

                                mainTextArea.setText("You are Connected"); //clears all previous messages (new session)

                                //change the Connect button text to Disconnect
                                connectButton.setText("Disconnect");
                                nameTextField.setEnabled(false);
                            }
                        }

                    } else { //if pressed to Disconnect

                        //create an output stream and send a RemUser message to disconnect from the server
                        DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());
                        outToServer.writeBytes("-RemUser" + "\n");

                        //close the client's socket
                        clientSocket.close();

                        //make the GUI components for sending message invisible
                        sendButton.setVisible(false);
                        sendToLabel.setVisible(false);
                        messageTextArea.setVisible(false);
                        recipient.setVisible(false);

                        mainTextArea.setText(mainTextArea.getText() + "\n" + "You Disconnected");

                        //change the Disconnect button text to Connect
                        connectButton.setText("Connect");
                        nameTextField.setEnabled(true);

                    }

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }});

    }

    private static void setupGUI() {
        //Create the GUI frame and components
        frame = new JFrame ("Chatting Client");
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

        mainTextArea = new JTextArea(); // text area for client and server messages
        mainTextArea.setBounds(20, 60, 440, 300);
        mainTextArea.setEditable(false);
        frame.getContentPane().add(mainTextArea);

        // text area scroll for scrolling messages in main text area
        mainTextAreaScroll = new JScrollPane(mainTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainTextAreaScroll.setBounds(20, 60, 440, 300);
        frame.getContentPane().add(mainTextAreaScroll);

        sendToLabel = new JLabel("Send to");
        sendToLabel.setBounds(20, 380, 100, 30);
        frame.getContentPane().add(sendToLabel);
        sendToLabel.setVisible(false);

        messageTextArea = new JTextArea(); // text area to enter message to be sent
        messageTextArea.setBounds(20, 430, 300, 70);
        frame.getContentPane().add(messageTextArea);
        messageTextArea.setVisible(false);

        sendButton = new JButton("Send"); //button to send message
        sendButton.setBounds(340, 460, 80, 30);
        frame.getContentPane().add(sendButton);
        sendButton.setVisible(false);
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

                    if (receivedSentence.startsWith("-Message")) { //received a message from another user

                        String []strings = receivedSentence.split(";");
                        mainTextArea.setText(mainTextArea.getText() + "\n" + strings[1]); //second index is the message with sender information

                    } else if (receivedSentence.startsWith("-AddUser")) { //new user to add to recipient list

                        String []strings = receivedSentence.split(";");
                        mainTextArea.setText(mainTextArea.getText() + "\n" + strings[1] + " is Connected"); //second index is the name of the user

                        //add user to drop down menu for sending message
                        recipient.addItem(strings[1]);
                        recipient.setEnabled(true);

                        //enable button and text area in case they were previously disabled if there was only one client connected
                        sendButton.setEnabled(true);
                        messageTextArea.setEnabled(true);
                        if (messageTextArea.getText().equals("No Other Users Connected")) {
                            messageTextArea.setText(""); //message erased that specifies that only one client is connected
                        }

                    } else if (receivedSentence.startsWith("-RemUser")) { //user removed from recipient list

                        String []strings = receivedSentence.split(";");
                        mainTextArea.setText(mainTextArea.getText() + "\n" + strings[1] +  " Disconnected"); //second index is the name of the user

                        for (int i = 0; i < recipient.getItemCount(); i++) {
                            if (recipient.getItemAt(i).equals(strings[1])) { //find and remove user from drop down menu
                                recipient.removeItemAt(i);
                                break;
                            }
                        }

                    } else if (receivedSentence.startsWith("-OnlyUser")) { //user is the only one connected to server
                        recipient.setEnabled(false); //disable send message related features
                        sendButton.setEnabled(false);
                        messageTextArea.setText("No Other Users Connected"); //set message in text area to tell user why they cannot send a message
                        messageTextArea.setEnabled(false);
                    }
                }

            }
            catch(Exception ex) {

            }

        }}).start();

    }

}
