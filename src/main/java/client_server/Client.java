package client_server;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static java.lang.String.valueOf;

public class Client {
    private final Sender sender;
    private final int clientID;
    private int type;
    private int userID;

    public Client(int clientID, int type, int userID, Sender sender) {
        this.sender = sender;
        this.clientID = clientID;
        this.type = type;
        this.userID = userID;
    }

    public byte[] showNumberOfProducts() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return sender.buildPacket(this, "1/0");
    }

    public byte[] decreaseNumberOfProducts(int number) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String info = valueOf(number);
        return sender.buildPacket(this, "2/" + info);
    }

    public byte[] increaseNumberOfProducts(int number) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String info = valueOf(number);
        return sender.buildPacket(this, "3/" + info);
    }

    public byte[] addProductGroup(String name) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return sender.buildPacket(this, "4/" + name);
    }

    public byte[] setPrice(int price) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return sender.buildPacket(this, "5/" + price);
    }
    public int getClientID() {
        return clientID;
    }

    public int getType() {
        return type;
    }

    public int getUserID() {
        return userID;
    }

}
