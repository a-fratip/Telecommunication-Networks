package elec366.lab1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JTextField;

public class Client {

    private static final int SERVER_PORT = 9876; // create port number
    private static DatagramSocket clientSocket;

    // UI elements
    protected static JButton addButton, subtractButton, multiplyButton, divideButton, minButton, maxButton; // Buttons
    protected static JTextField textField1, textField2; // Text fields
    protected static JLabel label1, label2, label3, resultLabel; // Labels
    protected static JFrame frame; // Frame

    public static void main(String[] args) throws IOException{

        setupGUI();

        // Create UDP socket
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Setup action listeners for buttons
        addButton.addActionListener(e-> sendRequest(textField1.getText(), textField2.getText(), "+", resultLabel));
        subtractButton.addActionListener(e-> sendRequest(textField1.getText(), textField2.getText(), "-", resultLabel));
        multiplyButton.addActionListener(e-> sendRequest(textField1.getText(), textField2.getText(), "x", resultLabel));
        divideButton.addActionListener(e-> sendRequest(textField1.getText(), textField2.getText(), "/", resultLabel));
        minButton.addActionListener(e-> sendRequest(textField1.getText(), textField2.getText(), "min", resultLabel));
        maxButton.addActionListener(e-> sendRequest(textField1.getText(), textField2.getText(), "max", resultLabel));

    }

    private static void sendRequest(String num1, String num2, String operation, JLabel resultLabel) {
        try {
            // Create message to send
            String message = num1 + "," + num2 + "," + operation;
            byte[] sendData = message.getBytes();

            // Send data to server through an array of bytes and a datagram packet
            InetAddress serverIPAddress = InetAddress.getByName("localhost"); // create an IP address
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, SERVER_PORT);
            clientSocket.send(sendPacket);

            // Receive data sent by the server through an array of bytes and a Datagram packet
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            // Display result
            String result = new String(receivePacket.getData(), 0, receivePacket.getLength());
            resultLabel.setText(result);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setupGUI() {
        // Frame
        frame = new JFrame();
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Calculator Assignment" );
        frame.getContentPane().setLayout(null);
        frame.setVisible(true);

        // Labels
        label1 = new JLabel("First Number:");
        label1.setBounds(20, 20, 150, 20);
        label1.setFont(new Font("Times", Font.BOLD, 14));
        label1.setHorizontalAlignment(SwingConstants.LEFT);
        label1.setVerticalAlignment(SwingConstants.CENTER);
        frame.getContentPane().add(label1);

        label2 = new JLabel("Second Number:");
        label2.setBounds(20, 60, 150, 20);
        label2.setFont(new Font("Times", Font.BOLD, 14));
        label2.setHorizontalAlignment(SwingConstants.LEFT);
        label2.setVerticalAlignment(SwingConstants.CENTER);
        frame.getContentPane().add(label2);

        label3 = new JLabel("Answer:");
        label3.setBounds(20, 140, 150, 20);
        label3.setFont(new Font("Times", Font.BOLD, 14));
        label3.setHorizontalAlignment(SwingConstants.LEFT);
        label3.setVerticalAlignment(SwingConstants.CENTER);
        frame.getContentPane().add(label3);

        resultLabel = new JLabel("");
        resultLabel.setBounds(160, 140, 150, 20);
        resultLabel.setFont(new Font("Times", Font.BOLD, 14));
        resultLabel.setHorizontalAlignment(SwingConstants.LEFT);
        resultLabel.setVerticalAlignment(SwingConstants.CENTER);
        frame.getContentPane().add(resultLabel);

        // Text fields
        textField1= new JTextField("");
        textField1.setFont(new Font("Times", Font.BOLD, 14));
        textField1.setBounds(160, 20, 150, 20);
        frame.getContentPane().add(textField1);

        textField2= new JTextField("");
        textField2.setFont(new Font("Times", Font.BOLD, 14));
        textField2.setBounds(160, 60, 150, 20);
        frame.getContentPane().add(textField2);

        // Buttons
        addButton = new JButton("+");
        addButton.setBounds(20, 100, 50, 25);
        frame.getContentPane().add(addButton);

        subtractButton = new JButton("-");
        subtractButton.setBounds(80, 100, 50, 25);
        frame.getContentPane().add(subtractButton);

        multiplyButton = new JButton("x");
        multiplyButton.setBounds(140, 100, 50, 25);
        frame.getContentPane().add(multiplyButton);

        divideButton = new JButton("/");
        divideButton.setBounds(200, 100, 50, 25);
        frame.getContentPane().add(divideButton);

        maxButton = new JButton("Max");
        maxButton.setBounds(260, 100, 70, 25);
        frame.getContentPane().add(maxButton);

        minButton = new JButton("Min");
        minButton.setBounds(340, 100, 70, 25);
        frame.getContentPane().add(minButton);
    }

}

