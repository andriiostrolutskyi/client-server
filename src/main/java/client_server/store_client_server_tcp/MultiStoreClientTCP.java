package client_server.store_client_server_tcp;

import client_server.Client;
import client_server.Sender;

import java.io.IOException;
import java.net.InetAddress;

public class MultiStoreClientTCP {
    static final int MAX_THREADS = 5;

    public static void main(String[] args) throws IOException, InterruptedException {
        InetAddress addr = InetAddress.getByName(null);
        Sender sender = new Sender();
        int clientID = 1;
        int type = 1;
        int userID = 1;
        while (true) {
            if (StoreClientTCP.threadCount() < MAX_THREADS) {
                Client client = new Client(clientID++, type++, userID++, sender);
                new StoreClientTCP(addr, client);
                Thread.currentThread().sleep(100);
            } else break;
        }

        Thread.currentThread().sleep(1000);

    }
}

