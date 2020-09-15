package com.example.alphachat.Model;

public class Message {

    private String sender_id;
    private String receiver_id;
    private String message;
    private String timestamp;
    private String date;
    private String photoUrl;

    public Message(String sender_id, String receiver_id, String message, String timestamp,
                   String date, String photoUrl) {
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.message = message;
        this.timestamp = timestamp;
        this.date = date;
        this.photoUrl = photoUrl;
    }

    public Message(){}

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender_id() {
        return sender_id;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
