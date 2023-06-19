package client_server;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import client_server.pseudo_client.Client;
import client_server.pseudo_client.Sender;
import client_server.pseudo_server.Receiver;
import client_server.pseudo_server.Storage;


public class Main {

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ExecutionException, InterruptedException {

        Sender sender = new Sender();
        Receiver receiver = new Receiver();

        Client client1 = new Client(127, 1, 1, sender);
        Client client2 = new Client(128, 1, 1, sender);


        ArrayList<byte[]> requests = new ArrayList<>();
        requests.add(client1.decreaseNumberOfProducts(100));
        requests.add(client1.increaseNumberOfProducts(150));
        requests.add(client2.decreaseNumberOfProducts(20));
        requests.add(client2.decreaseNumberOfProducts(20));
        requests.add(client1.decreaseNumberOfProducts(10));
        requests.add(client1.increaseNumberOfProducts(1000));
        requests.add(client1.addProductGroup("Dairy"));
        requests.add(client1.setPrice(123));

        sender.sendMessage(requests, receiver);

        System.out.println("The actual number of products in Storage: " + Storage.getNumberOfProducts());

    }
}
