package client_server.store_client_server_tcp;

import client_server.Client;
import org.apache.commons.codec.binary.Hex;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


class StoreClientTCP extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static int counter = 0;
    private int id = counter++;
    private static int threadcount = 0;
    private final Client client;

    public StoreClientTCP(InetAddress addr, Client client) {
        this.client = client;
        threadcount++;
        System.out.println("Launching client with ID " + client.getClientID());
        try {
            socket = new Socket(addr, MultiStoreServerTCP.PORT);
        } catch (IOException e) {
            System.err.println("Failed to connect to server");
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);
            start();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e2) {
                System.err.println("Socket is not closed");
            }
        }
    }

    public void run() {
        /*
        Для тестування кожен клієнт збільшує кількість продуктів на 100
        */
        try {
            String request = Hex.encodeHexString(client.increaseNumberOfProducts(100));
            out.println(request);
            String str = in.readLine();
            System.out.println(str);
        } catch (Exception ignored) {}
    }

    public static int threadCount() {
        return threadcount;
    }
}