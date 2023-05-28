package client_server;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static client_server.CRC16.calculateCRC16;


class MessagePacket {
    private final int clientID;
    private final long messageID;
    private final int packetLength;
    private final byte[] message;

    MessagePacket(int clientID, long messageID, int packetLength, byte[] message) {
        this.clientID = clientID;
        this.messageID = messageID;
        this.packetLength = packetLength;
        this.message = message;
    }

    int getClientID() {
        return clientID;
    }

    long getMessageID() {
        return messageID;
    }

    int getPacketLength() {
        return packetLength;
    }

    Message getMessage() {

        int messageType = ByteBuffer.wrap(message, 0, 4).getInt();
        int userID = ByteBuffer.wrap(message, 4, 4).getInt();
        String usefulInfo = new String(Arrays.copyOfRange(message, 8, message.length));

        return new Message(messageType, userID, usefulInfo);
    }

    byte[] toByte() {
        byte[] byteMessagePacket = new byte[18 + message.length];

        byteMessagePacket[0] = 0x13;

        byteMessagePacket[1] = (byte) clientID;

        byte[] byteMessageID = ByteBuffer.wrap(new byte[8]).putLong(messageID).array();
        System.arraycopy(byteMessageID, 0, byteMessagePacket, 2, byteMessageID.length);

        ByteBuffer a = ByteBuffer.allocate(4);
        a.putInt(packetLength);
        byte[] bytePacketLength = a.array();
        System.arraycopy(bytePacketLength, 0, byteMessagePacket, 10, bytePacketLength.length);

        byte[] headerSubset = Arrays.copyOfRange(byteMessagePacket, 0, 13);
        int calculatedHeaderCRC16 = calculateCRC16(headerSubset);

        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(calculatedHeaderCRC16);
        byte[] byteCalculatedHeaderCRC16 = b.array();
        System.arraycopy(byteCalculatedHeaderCRC16, 2, byteMessagePacket, 14, 2);

        System.arraycopy(message, 0, byteMessagePacket, 16, message.length);

        byte[] messageSubset = Arrays.copyOfRange(byteMessagePacket, 16, packetLength - 2);
        int calculatedMessageCRC16 = calculateCRC16(messageSubset);

        ByteBuffer c = ByteBuffer.allocate(4);
        c.putInt(calculatedMessageCRC16);
        byte[] byteCalculatedMessageCRC16 = c.array();
        System.arraycopy(byteCalculatedMessageCRC16, 2, byteMessagePacket, packetLength - 2, 2);
        return byteMessagePacket;
    }

    byte[] encryptMessageBody(byte[] packet) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] keyBytes = new byte[16];
        byte keyByte = 0x10;
        for (int i = 0; i < 16; i++) {
            keyBytes[i] = keyByte++;
        }
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] unencryptedMessageBody = Arrays.copyOfRange(packet, 16, packet.length - 2);

        byte[] firstSubset = Arrays.copyOfRange(packet, 0, 16);
        byte[] encryptedMessage = cipher.doFinal(unencryptedMessageBody);
        byte[] lastSubset = Arrays.copyOfRange(packet, 16 + unencryptedMessageBody.length, packet.length);

        byte[] finalPacket = new byte[encryptedMessage.length + 18];
        System.arraycopy(firstSubset, 0, finalPacket, 0, 16);
        System.arraycopy(encryptedMessage, 0, finalPacket, 16, encryptedMessage.length);
        System.arraycopy(lastSubset, 0, finalPacket, finalPacket.length - 2, 2);

        return finalPacket;
    }
}