package Ostrolutskyi_client_server_lab1;

class Message {
    private int messageType;
    private int userID;
    private String usefulInfo;

    Message(int messageType, int userID, String usefulInfo) {
        this.messageType = messageType;
        this.userID = userID;
        this.usefulInfo = usefulInfo;
    }

    int getMessageType() {
        return messageType;
    }

    void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    int getUserID() {
        return userID;
    }

    void setUserID(int userID) {
        this.userID = userID;
    }

    String getUsefulInfo() {
        return usefulInfo;
    }

    void setUsefulInfo(String usefulInfo) {
        this.usefulInfo = usefulInfo;
    }

    @Override
    public String toString() {
        return  "Message type: " + this.getMessageType() + "\n" +
                "User ID: " + this.getUserID() + "\n" +
                "Message: " + this.getUsefulInfo() + "\n";
    }
}
