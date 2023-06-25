package client_server;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class Receiver {

    public String receiveMessage(byte[] encryptedByteMessagePackets) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        MessagePacket messagePacket = Decryptor.parsePacket(encryptedByteMessagePackets);
        Message m = messagePacket.getMessage();
        return Processor.process(m);
    }
}


