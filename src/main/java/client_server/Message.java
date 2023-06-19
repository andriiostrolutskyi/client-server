package client_server;

public class Message {
    private int messageType;
    private int userID;
    private String usefulInfo;

    public Message(int messageType, int userID, String usefulInfo) {
        this.messageType = messageType;
        this.userID = userID;
        this.usefulInfo = usefulInfo;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsefulInfo() {
        return usefulInfo;
    }

    public void setUsefulInfo(String usefulInfo) {
        this.usefulInfo = usefulInfo;
    }

    @Override
    public String toString() {
        return  "Message type: " + this.getMessageType() + "\n" +
                "User ID: " + this.getUserID() + "\n" +
                "Message: " + this.getUsefulInfo() + "\n";
    }
}
