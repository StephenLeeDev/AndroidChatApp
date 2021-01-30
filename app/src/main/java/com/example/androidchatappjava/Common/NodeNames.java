package com.example.androidchatappjava.Common;

public class NodeNames {

    public static NodeNames uniqueInstance = new NodeNames();

    public NodeNames() {

    }

    public static NodeNames getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new NodeNames();
        }
        return uniqueInstance;
    }

    public String USERS = "Users";
    public String FRIEND_REQUESTS = "FriendRequests";
    public String CHATS = "Chats";
    public String MESSAGES = "Messages";

    public String NAME = "Name";
    public String EMAIL = "Email";
    public String ONLINE = "Online";
    public String PHOTO = "Photo";

    public String REQUEST_TYPE = "RequestType";

    public String TIME_STAMP = "TimeStamp";

    public String MESSAGE = "Message";
    public String MESSAGE_ID = "MessageId";
    public String MESSAGE_TYPE = "MessageType";
    public String MESSAGE_TIME = "MessageTime";
    public String MESSAGE_FROM = "MessageFrom";
}
