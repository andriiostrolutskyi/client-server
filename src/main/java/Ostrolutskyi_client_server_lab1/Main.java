package Ostrolutskyi_client_server_lab1;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;


public class Main {

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        //Build package:

        int messageType = 1;
        int userID = 1;
        String usefulInfo = "Anything";

        int clientID = 127;
        long messageID = 15545;

        Message message = new Message(messageType, userID, usefulInfo);
        MessagePacket messagePacket = MessagePacketBuilder.buildPacket(clientID, messageID, message);

        byte[] encryptedByteMessagePacket = messagePacket.encryptMessageBody(messagePacket.toByte());
        System.out.println("Packet with encrypted message body: " + Hex.encodeHexString(encryptedByteMessagePacket) + "\n");

        //Process package:

        System.out.println("Decrypted packet: ");
        MessagePacket mP = MessagePacketProcessor.parsePacket(encryptedByteMessagePacket);
        Message m = mP.getMessage();
        System.out.println("Client ID: " + mP.getClientID() + "\n" + "Message ID: " + mP.getMessageID() + "\n" +
                           "Packet length: " + mP.getPacketLength() + "\n" + m);

    }
}
