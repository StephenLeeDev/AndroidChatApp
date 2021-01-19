package com.example.androidchatappjava.Common;

public class Constants {

    public static Constants uniqueInstance = new Constants();

    public Constants() {

    }

    public static Constants getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Constants();
        }
        return uniqueInstance;
    }

    public String IMAGE_FOLDER = "images";
}
