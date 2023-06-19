package client_server;


import java.nio.ByteBuffer;

public class Enryptor {

    //Builds a MessagePacket with clientID (int), messageID (long), packetLength (int), and unencrypted message (byte)
    public static MessagePacket buildPacket(int clientID, long messageID, Message message) {

        byte[] byteUsefulInfo = message.getUsefulInfo().getBytes();

        int messageLength = 8 + byteUsefulInfo.length;
        byte[] byteMessage = new byte[messageLength];

        int intMessageType = message.getMessageType();
        ByteBuffer a = ByteBuffer.allocate(4);
        a.putInt(intMessageType);
        byte[] byteMessageType = a.array();

        int intUserID = message.getUserID();
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(intUserID);
        byte[] byteUserID = b.array();

        System.arraycopy(byteMessageType, 0, byteMessage, 0, byteMessageType.length);
        System.arraycopy(byteUserID, 0, byteMessage, byteMessageType.length, byteUserID.length);
        System.arraycopy(byteUsefulInfo, 0, byteMessage, byteMessageType.length + byteUserID.length, byteUsefulInfo.length);

        int packetLength = byteMessage.length + 18;

        return new MessagePacket(clientID, messageID, packetLength, byteMessage);

    }
}
