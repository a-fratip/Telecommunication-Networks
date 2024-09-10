package elec366.lab1;

import java.io.*;
import java.net.*;

public class Server {

    private static final int SERVER_PORT = 9876;

    public static void main(String[] args) throws IOException{

        try {
            // Create UDP socket
            DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);

            System.out.println("Server started...");

            while (true) {
                // Read received data, sent by client, through an array of bytes and a Datagram packet
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                // Process received data
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                String[] tokens = message.split(","); // Extract two numbers and operation from message
                double num1 = Double.parseDouble(tokens[0]);
                double num2 = Double.parseDouble(tokens[1]);
                String operation = tokens[2];
                double result = calculate(num1, num2, operation);

                // Get the IP address and Port number of the received packet
                InetAddress clientIPAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // Send data to client, through an array of bytes and a Datagram packet
                byte[] sendData = String.valueOf(result).getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, clientIPAddress, clientPort);
                serverSocket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static double calculate(double num1, double num2, String operation) {

        switch (operation) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "x":
                return num1 * num2;
            case "/":
                return num1 / num2;
            case "min":
                return Math.min(num1, num2);
            case "max":
                return Math.max(num1, num2);
            default:
                throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

}

