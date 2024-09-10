package elec366.lab2;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Server {

    private static final int SERVER_PORT = 6789;

    public static void main(String[] args) {
        try {
            // Create server socket
            ServerSocket welcomeSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server listening on port 6789...");

            while (true) {
                // Keep listening for clients connection request, accept the connection when available
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("Client connected...");

                // Read received data, sent by client, through a buffer reader that is connected to the connection socket's input stream
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                String clientMessage = inFromClient.readLine();
                System.out.println("Received message from client...");

                // Extract numbers from message
                double[] numbers = Arrays.stream(clientMessage.split(",")).mapToDouble(Double::parseDouble).toArray();

                String result = computeResults(numbers); // get results

                // Send data to client, through a buffer writer connected to the connection socket's output stream
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                outToClient.writeBytes(result + '\n');
                System.out.println("Sent results back to client...");

                // Close the socket
                connectionSocket.close();
                System.out.println("Client disconnected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String computeResults(double[] numbers) {
        // Compute the sum, average, max, and min
        double sum = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (double number : numbers) {
            sum += number;
            if (number > max) max = number;
            if (number < min) min = number;
        }
        double average = sum / numbers.length;

        // Prepare the results as a String to send back to client
        String s1 = String.format("Sum = %.2f", sum);
        String s2 = String.format("Average = %.2f", average);
        String s3 = String.format("Max = %.2f", max);
        String s4 = String.format("Min = %.2f", min);
        String result = s1 + "," + s2 + "," + s3 + "," + s4;

        return result;
    }
}

