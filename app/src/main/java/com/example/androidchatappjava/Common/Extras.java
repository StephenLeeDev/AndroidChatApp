package com.example.androidchatappjava.Common;

public class Extras {

    public static Extras uniqueInstance = new Extras();

    public Extras() {

    }

    public static Extras getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Extras();
        }
        return uniqueInstance;
    }

    public static final String USER_KEY ="user_key";
    public static final String USER_NAME ="user_name";
    public static final String PHOTO_NAME ="photo_name";

    public static final String MESSAGE = "message";
    public static final String MESSAGE_ID = "message_id";
    public static final String MESSAGE_TYPE = "message_type";
}
