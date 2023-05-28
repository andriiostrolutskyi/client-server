package client_server;


import java.nio.ByteBuffer;

class MessagePacketBuilder {

    static MessagePacket buildPacket(int clientID, long messageID, Message message) {

        byte[] usefulInfo = message.getUsefulInfo().getBytes();

        int encryptedMessageLength = 8 + usefulInfo.length;
        byte[] encryptedMessage = new byte[encryptedMessageLength];

        int intMessageType = message.getMessageType();
        ByteBuffer a = ByteBuffer.allocate(4);
        a.putInt(intMessageType);
        byte[] messageType = a.array();

        int intUserID = message.getUserID();
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(intUserID);
        byte[] userID = b.array();

        System.arraycopy(messageType, 0, encryptedMessage, 0, messageType.length);
        System.arraycopy(userID, 0, encryptedMessage, messageType.length, userID.length);
        System.arraycopy(usefulInfo, 0, encryptedMessage, messageType.length + userID.length, usefulInfo.length);

        int packetLength = encryptedMessage.length + 18;

        return new MessagePacket(clientID, messageID, packetLength, encryptedMessage);

    }
}
