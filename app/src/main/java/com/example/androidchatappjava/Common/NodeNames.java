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
    public String REQUEST_TYPE = "RequestType";
    public String NAME = "Name";
    public String EMAIL = "Email";
    public String ONLINE = "Online";
    public String PHOTO = "Photo";
}
