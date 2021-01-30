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

    public String USER_KEY = "UserKey";
}
