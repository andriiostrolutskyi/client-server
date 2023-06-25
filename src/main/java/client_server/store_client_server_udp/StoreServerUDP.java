package client_server.store_client_server_udp;

import client_server.Receiver;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class StoreServerUDP implements Runnable {
    private DatagramPacket packet;

    public StoreServerUDP(DatagramPacket packet) {
        this.packet = packet;
    }

    public void run() {
        try {
            // Process the received data
            byte[] receivedData = packet.getData();
            byte[] received = removeTrailingZeros(receivedData);

            Receiver receiver = new Receiver();
            String response = receiver.receiveMessage(received);

            // Send the response to the client
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            byte[] responseData = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
            socket.send(responsePacket);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] removeTrailingZeros(byte[] inputArray) {
        int endIndex = inputArray.length - 1;
        while (endIndex >= 0 && inputArray[endIndex] == 0) {
            endIndex--;
        }
        return Arrays.copyOfRange(inputArray, 0, endIndex + 1);
    }
}