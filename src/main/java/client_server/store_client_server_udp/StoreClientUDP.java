package client_server.store_client_server_udp;

import client_server.Client;


import javax.swing.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class StoreClientUDP extends Thread {
    private Client client;

    public StoreClientUDP(Client client) {
        this.client = client;
        start();
    }

    public void run() {

        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        int serverPort = 4445;

        try {
            // prepare request
            byte[] request = client.increaseNumberOfProducts(100);

            // get a datagram socket
            DatagramSocket socket = new DatagramSocket();

            // send request
            DatagramPacket requestPacket = new DatagramPacket(request, request.length, serverAddress, serverPort);
            socket.send(requestPacket);

            // receive the response from the server
            byte[] responseData = new byte[256];
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);
            socket.receive(responsePacket);

            // Process the response data
            String responseMessage = new String(responsePacket.getData(), 0, responsePacket.getLength());

            // при передачі по UDP пакети можуть бути втрачені тому ваш власний протокл обміну даними
            // це має обслуговувати і робити переповтор в разі втрати даних
            long startTime = System.currentTimeMillis();
            long timeout = 10000; // 10000 = 10 seconds)
            boolean timedOut = false;
            while (!responseMessage.equals("OK")) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    timedOut = true;
                    break;
                }
                socket.send(requestPacket);
                responseData = new byte[256];
                responsePacket = new DatagramPacket(responseData, responseData.length);
                socket.receive(responsePacket);
                responseMessage = new String(responsePacket.getData(), 0, responsePacket.getLength());
            }
            if (timedOut) {
                System.err.println("Server cannot process the request");
            } else {
                System.out.println("Response from server: " + responseMessage);
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}