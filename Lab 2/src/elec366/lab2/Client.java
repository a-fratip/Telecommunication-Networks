package elec366.lab2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client {
    private static final int SERVER_PORT = 6789;
    private static Socket clientSocket;
    private static BufferedReader inFromServer;
    private static DataOutputStream outToServer;
    private static int count = 0;
    private static String message;

    // UI elements
    protected static JFrame frame; // Frame
    protected static JLabel connectionStatusLabel, numberCountLabel, addNumberLabel, messageLabel; // Labels
    protected static JButton connectionButton, setButton, addButton, sendButton; // Buttons
    protected static JTextField countTextField, addNumberTextField, messageTextField; // TextFields
    protected static JTextArea resultsTextArea; // TextArea

    public static void main(String[] args) {
        setupGUI();

        // Set up action listeners for buttons
        onClickConnectionButton();
        onClickSetButton();
        onClickAddButton();
        onClickSendButton();
    }

    private static void onClickSendButton() {
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendToServer(message); // send message to server

                try {
                    // Get the server's results message
                    String resultFromServer = inFromServer.readLine(); // read received data from server
                    System.out.println("Received data from server...");
                    String results = resultFromServer.replace(",","\n");

                    // Display results in text area
                    frame.getContentPane().add(resultsTextArea); // add text area to GUI
                    resultsTextArea.setEditable(false);
                    resultsTextArea.setText(results);

                    // Close the socket
                    clientSocket.close();
                    System.out.println("Connection terminated");
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

            }
        });

    }

    private static void sendToServer(String message) {
        // Send String data to server
        try {
            outToServer.writeBytes(message + '\n');
            System.out.println("Sent message to server...");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void onClickAddButton() {
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // New UI elements appear when "Add" button is clicked
                frame.getContentPane().add(messageLabel);
                frame.getContentPane().add(messageTextField);
                messageTextField.setEditable(false); // disable editing

                addNumberToMessage();

                // check if count is reached
                if (count == 0) {
                    // remove "Add" UI elements from GUI
                    frame.getContentPane().remove(addNumberLabel);
                    frame.getContentPane().remove(addNumberTextField);
                    frame.getContentPane().remove(addButton);

                    // refresh pane
                    frame.getContentPane().revalidate();
                    frame.getContentPane().repaint();

                    // "Send" Button appears
                    frame.getContentPane().add(sendButton);

                }

            }
        });

    }

    private static void onClickSetButton() {
        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCount();

                if (count != 0) {
                    // New UI elements appear when "Set" button clicked and user enters valid count
                    frame.getContentPane().add(addNumberLabel);
                    frame.getContentPane().add(addNumberTextField);
                    frame.getContentPane().add(addButton);
                }
            }
        });
    }

    private static void onClickConnectionButton() {
        connectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // First, new UI elements appear when "connect" button clicked
                frame.getContentPane().add(numberCountLabel);
                frame.getContentPane().add(countTextField);
                frame.getContentPane().add(setButton);

                // Next, connect to server
                try {
                    connectToServer();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

    }

    private static void setupGUI() {
        // Frame
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Client TCP");
        frame.getContentPane().setLayout(null);
        frame.setVisible(true);

        // Connection status label
        connectionStatusLabel = new JLabel("Connection Status: Not connected");
        connectionStatusLabel.setForeground(Color.red);
        connectionStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        connectionStatusLabel.setVerticalAlignment(SwingConstants.CENTER);
        connectionStatusLabel.setBounds(20,20,250,20);
        frame.getContentPane().add(connectionStatusLabel);

        // Connection button
        connectionButton = new JButton("Connect");
        connectionButton.setBounds(300,20,100,20);
        frame.getContentPane().add(connectionButton);

        // Number count label
        numberCountLabel = new JLabel("Count of Numbers:");
        numberCountLabel.setHorizontalAlignment(SwingConstants.LEFT);
        numberCountLabel.setVerticalAlignment(SwingConstants.CENTER);
        numberCountLabel.setBounds(20,60,150,20);

        // Count text field
        countTextField = new JTextField();
        countTextField.setEditable(true);
        countTextField.setBounds(200,60,50,20);

        // Set button
        setButton = new JButton("Set");
        setButton.setBounds(300,60,100,20);

        // Add number label
        addNumberLabel = new JLabel("Add Number to Message:");
        addNumberLabel.setHorizontalAlignment(SwingConstants.LEFT);
        addNumberLabel.setVerticalAlignment(SwingConstants.CENTER);
        addNumberLabel.setBounds(20,100,175,20);

        // Add number text field
        addNumberTextField = new JTextField();
        addNumberTextField.setBounds(200,100,75,20);

        // Add button
        addButton = new JButton("Add");
        addButton.setBounds(300,100,100,20);

        // Numbers in message label
        messageLabel = new JLabel("Numbers in Message:");
        messageLabel.setHorizontalAlignment(SwingConstants.LEFT);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        messageLabel.setBounds(20,140,160,20);

        // Message text field
        messageTextField = new JTextField();
        messageTextField.setBounds(20,170,380,20);

        // Send button
        sendButton = new JButton("Send");
        sendButton.setBounds(180,200,100,20);

        // Results text area
        resultsTextArea = new JTextArea(4,1);
        resultsTextArea.setBounds(20,280,380,150);

    }

    private static void addNumberToMessage() {
        // add number to the message (with input validation)
        try {
            double inputNumber = Double.parseDouble(addNumberTextField.getText());
            if (message != null) {
                // append to message
                message += inputNumber + ",";
            }
            else {
                // first assign number to message
                message = String.valueOf(inputNumber);
                // then add delimiter
                message += ",";
            }
            messageTextField.setText(message);
            count--;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid number to add to the message", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void setCount() {
        // set the count of numbers in the message (with input validation)
        try {
            int inputCount = Integer.parseInt(countTextField.getText());
            if (inputCount > 0) {
                count = inputCount;
                numberCountLabel.setEnabled(false);
                countTextField.setEnabled(false);
//                countTextField.setEditable(false); // disable count text field
                setButton.setEnabled(false); // disable set button
            }
            else {
                JOptionPane.showMessageDialog(frame, "Please enter a positive integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void connectToServer() throws IOException {
        try {
            // create client socket
            clientSocket = new Socket("localhost",SERVER_PORT);

            // set up a buffer writer, connected to the connection socket's output stream, to send data to the server
            outToServer = new DataOutputStream(clientSocket.getOutputStream());

            // set up a buffer reader, connected to the connection socket's input stream, to read data sent by server
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Change "Connect" button to "Disconnect" and update corresponding label
            connectionStatusLabel.setText("Connection Status: Connected");
            connectionStatusLabel.setForeground(Color.BLUE);
            connectionButton.setText("Disconnect");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

