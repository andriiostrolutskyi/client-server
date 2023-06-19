package client_server.pseudo_client;

import client_server.Message;
import client_server.MessagePacket;
import client_server.Enryptor;
import client_server.pseudo_server.Receiver;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Sender {
    public byte[] buildPacket(Client client, String info) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Message message = new Message(client.getType(), client.getUserID(), info);
        int cID = client.getClientID();
        long messageID = 15545; //Поки що захардкодив
        MessagePacket messagePacket = Enryptor.buildPacket(cID, messageID, message);
        byte[] encryptedByteMessagePacket = messagePacket.encryptMessageBody(messagePacket.toByte());
        return encryptedByteMessagePacket;
    }

    public void sendMessage(ArrayList<byte[]> encryptedByteMessagePackets, Receiver to) throws ExecutionException, InterruptedException {
        to.receiveMessage(encryptedByteMessagePackets);
    }
}
