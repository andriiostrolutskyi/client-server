package client_server.store_client_server_tcp;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiStoreServerTCP {

    static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket s = new ServerSocket(PORT);
        System.out.println("Server is launched");

        try {
            while (true) {
                Socket socket = s.accept();
                try {
                    new StoreServerTCP(socket);
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            s.close();
        }
    }
}
