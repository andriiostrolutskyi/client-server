package client_server.store_client_server_udp;

import client_server.Client;
import client_server.Sender;

import java.util.concurrent.Semaphore;


public class MultiStoreClientUDP {
    static final int MAX_THREADS = 5;
    static Semaphore semaphore = new Semaphore(MAX_THREADS);

    public static void main(String[] args) throws InterruptedException {
        Sender sender = new Sender();
        int clientID = 1;
        int type = 1;
        int userID = 1;
        while (true) {
            // Запит на дозвіл у семафора
            semaphore.acquire();
            Client client = new Client(clientID++, type++, userID++, sender);
            new StoreClientUDP(client);
            Thread.sleep(100);
        }
    }
}

