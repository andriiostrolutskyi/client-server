package client_server;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static client_server.CRC16.calculateCRC16;


class MessagePacketProcessor {
    private static final byte MAGIC_BYTE = 0x13;

    static boolean isValidPacket(byte[] packet) {

        if (packet.length < 18 || packet[0] != MAGIC_BYTE) {
            return false;
        }

        int packetLength = ByteBuffer.wrap(packet, 10, 4).getInt();
        if (packetLength != packet.length) {
            return false;
        }

        //Check if the expected CRC16 of the first 14 bytes matches the calculated CRC16
        int expectedHeaderCRC16 = ByteBuffer.wrap(packet, 14, 2).getShort(); //& 0xFFFF;
        byte[] headerSubset = Arrays.copyOfRange(packet, 0, 13);
        int calculatedHeaderCRC16 = calculateCRC16(headerSubset);
        if (expectedHeaderCRC16 != calculatedHeaderCRC16) {
            return false;
        }

        //Check if the expected CRC16 of the message matches the calculated CRC16
        int expectedMessageCRC16 = ByteBuffer.wrap(packet, packetLength - 2, 2).getShort(); //& 0xFFFF;
        byte[] messageSubset = Arrays.copyOfRange(packet, 16, packetLength - 2);
        int calculatedMessageCRC16 = calculateCRC16(messageSubset);
        if (expectedMessageCRC16 != calculatedMessageCRC16) {
            return false;
        }

        return true;
    }

    static MessagePacket parsePacket(byte[] encryptedPacket) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        byte[] packet = decryptMessageBody(encryptedPacket);

        if (!isValidPacket(packet)) {
            throw new IllegalArgumentException("Invalid packet");
        }

        int clientID = packet[1];
        long messageID = ByteBuffer.wrap(packet, 2, 8).getLong();
        int packetLength = ByteBuffer.wrap(packet, 10, 4).getInt();

        byte[] encryptedMessage = Arrays.copyOfRange(packet, 16, packetLength - 2);

        return new MessagePacket(clientID, messageID, packetLength, encryptedMessage);
    }

    static byte[] decryptMessageBody(byte[] encryptedByteMessagePacket) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] keyBytes = new byte[16];
        byte keyByte = 0x10;
        for (int i = 0; i < 16; i++) {
            keyBytes[i] = keyByte++;
        }
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] encryptedMessageBody = Arrays.copyOfRange(encryptedByteMessagePacket, 16, encryptedByteMessagePacket.length - 2);

        byte[] firstSubset = Arrays.copyOfRange(encryptedByteMessagePacket, 0, 16);
        byte[] decryptedMessageBody = cipher.doFinal(encryptedMessageBody);
        byte[] lastSubset = Arrays.copyOfRange(encryptedByteMessagePacket, 16 + encryptedMessageBody.length, encryptedByteMessagePacket.length);

        byte[] finalPacket = new byte[decryptedMessageBody.length + 18];

        System.arraycopy(firstSubset, 0, finalPacket, 0, 16);
        System.arraycopy(decryptedMessageBody, 0, finalPacket, 16, decryptedMessageBody.length);
        System.arraycopy(lastSubset, 0, finalPacket, finalPacket.length - 2, 2);

        return finalPacket;
    }
}
