package client_server.store_client_server_tcp;

import client_server.Receiver;

import java.io.*;
import java.net.Socket;
import java.util.HexFormat;


public class StoreServerTCP extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public StoreServerTCP(Socket s) throws IOException {
        socket = s;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        start();
    }

    public void run() {
        try {
            String str = in.readLine();
            byte[] received = HexFormat.of().parseHex(str);
            Receiver receiver = new Receiver();
            String response = receiver.receiveMessage(received);
            out.println(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
