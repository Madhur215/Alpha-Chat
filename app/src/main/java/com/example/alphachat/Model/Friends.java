package com.example.alphachat.Model;

public class Friends {

    private String friend_name;
    private String friend_image;
    private String last_message;
    private String friend_id;

    public Friends(String friend_name, String friend_image, String last_message, String friend_id) {
        this.friend_name = friend_name;
        this.friend_image = friend_image;
        this.last_message = last_message;
        this.friend_id = friend_id;
    }

    public String getFriend_name() {
        return friend_name;
    }

    public String getFriend_image() {
        return friend_image;
    }

    public String getLast_message() {
        return last_message;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_name(String friend_name) {
        this.friend_name = friend_name;
    }

    public void setFriend_image(String friend_image) {
        this.friend_image = friend_image;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }
}
