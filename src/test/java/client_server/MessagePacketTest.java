package client_server;

import junit.framework.TestCase;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static client_server.Decryptor.isValidPacket;


public class MessagePacketTest extends TestCase
{
    public void testMessagePacketEncryption() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        int messageType = 2;
        int userID = 2;
        String usefulInfo = "Something";
        int clientID = 127;
        long messageID = 15545;
        Message message = new Message(messageType, userID, usefulInfo);
        message.setMessageType(1);
        message.setUserID(1);
        message.setUsefulInfo("Some information");
        MessagePacket messagePacket = Enryptor.buildPacket(clientID, messageID, message);
        byte[] encryptedByteMessagePacket = messagePacket.encryptMessageBody(messagePacket.toByte());
        String expectedOutput = "137f0000000000003cb90000002a611b4d8b359c4f9e1197a6c04161fb83a18a3e46ccd2244b25fd46016c8be139c667f67b";
        Assert.assertEquals(expectedOutput, Hex.encodeHexString(encryptedByteMessagePacket));
    }

    public void testMessagePacketDecryption() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, DecoderException {
        String encryptedPacket = "137f0000000000003cb90000002a611b4d8b359c4f9e1197a6c04161fb83a18a3e46ccd2244b25fd46016c8be139c667f67b";
        byte[] encryptedByteMessagePacket = Hex.decodeHex(encryptedPacket.toCharArray());
        MessagePacket mP = Decryptor.parsePacket(encryptedByteMessagePacket);
        Message m = mP.getMessage();
        int expectedClientId = 127;
        long expectedMessageId = 15545;
        int expectedPacketLength = 42;
        Assert.assertEquals(expectedClientId, mP.getClientID());
        Assert.assertEquals(expectedMessageId, mP.getMessageID());
        Assert.assertEquals(expectedPacketLength, mP.getPacketLength());
        Assert.assertEquals(1, m.getMessageType());
        Assert.assertEquals(1, m.getUserID());
        Assert.assertEquals("Some information", m.getUsefulInfo());
    }

    public void testInvalidPacketException() {
        //Invalid packet:
        byte[] invalidPacket = new byte[] { 0x00, 0x01, 0x02 };

        try {

            if (!isValidPacket(invalidPacket)) {
                throw new IllegalArgumentException("Invalid packet");
            }
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid packet", e.getMessage());
        }
    }
}
