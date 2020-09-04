package com.example.alphachat.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.alphachat.R;

public class ChatActivity extends AppCompatActivity {

    public static final String FRIEND_ID = "friend";
    public static final String FRIEND_NAME = "friend name";
    public static final String FRIEND_IMAGE = "image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }
}