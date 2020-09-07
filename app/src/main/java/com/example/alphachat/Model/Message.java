package com.example.alphachat.Model;

public class Message {

    private String sender_id;
    private String receiver_id;
    private String message;
    private String timestamp;

    public Message(String sender_id, String receiver_id, String message, String timestamp) {
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Message(){}

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
