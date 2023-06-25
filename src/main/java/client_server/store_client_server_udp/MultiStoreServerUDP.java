package client_server.store_client_server_udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiStoreServerUDP {
    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Максимальна кількість потоків
        DatagramSocket socket = new DatagramSocket(4445);

        try {
            while (true) {
                byte[] data = new byte[256];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);

                StoreServerUDP storeServer = new StoreServerUDP(packet);
                executorService.execute(storeServer);
            }
        } finally {
            socket.close();
            executorService.shutdown();
        }
    }
}
