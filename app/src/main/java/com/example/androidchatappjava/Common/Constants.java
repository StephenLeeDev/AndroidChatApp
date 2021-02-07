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
    public String REQUEST_STATUS_SENT = "sent";
    public String REQUEST_STATUS_RECEIVED = "received";
    public String REQUEST_STATUS_ACCEPTED = "accepted";

    public String MESSAGE_VIDEOS ="message_videos";
    public String MESSAGE_IMAGES ="message_images";

    public String MESSAGE_TYPE_TEXT = "text";
    public String MESSAGE_TYPE_IMAGE ="image" ;
    public String MESSAGE_TYPE_VIDEO ="video" ;

}
