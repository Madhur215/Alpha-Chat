package com.example.alphachat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alphachat.Adapter.ChatMessageAdapter;
import com.example.alphachat.Model.Message;
import com.example.alphachat.R;
import com.example.alphachat.Util.PrefUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String FRIEND_ID = "friend";
    public static final String FRIEND_NAME = "friend name";
    public static final String FRIEND_IMAGE = "image";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private ChatMessageAdapter messageAdapter;
    private RecyclerView messageRecyclerView;
    private List<Message> messageList = new ArrayList<>();
    private String PUSH_ID;
    private EditText messageEditText;
    private ImageButton send_button;

    private FirebaseDatabase mFirebaseDatabase;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar chat_toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chat_toolbar);

        TextView friend_name_text_view = findViewById(R.id.chat_activity_friend_name);
        ImageView friend_image_view = findViewById(R.id.chat_activity_friend_image);
        friend_name_text_view.setText(getIntent().getStringExtra(FRIEND_NAME));
        send_button = findViewById(R.id.send_message_image);
        messageRecyclerView = findViewById(R.id.recycler_view_messages);
        messageEditText = findViewById(R.id.message_edit_text);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        init();
        setEditText();


        fetchMessages(this);

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void setEditText() {
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                messageRecyclerView.scrollToPosition(messageAdapter.getItemCount()-1);
                if(s.toString().trim().length() > 0){
                    send_button.setEnabled(true);
                } else {
                    send_button.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
    }

    // Initialize PUSH_ID and set up recycler view.
    private void init() {
        String uid1 = PrefUtils.getUserId();
        String uid2 = getIntent().getStringExtra(FRIEND_ID);
        if(uid1.compareTo(uid2) > 0) PUSH_ID = uid2 + uid1;
        else PUSH_ID = uid1 + uid2;

        mDatabaseReference = mFirebaseDatabase.getReference().child("messages").child(PUSH_ID);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setHasFixedSize(true);
        messageAdapter = new ChatMessageAdapter(messageList, getIntent().getStringExtra(FRIEND_ID), this);
        messageRecyclerView.setAdapter(messageAdapter);
    }

    private void fetchMessages(final Context context) {

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                messageRecyclerView.scrollToPosition(messageAdapter.getItemCount()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    private void sendMessage() {

        Message message = new Message(PrefUtils.getUserId(),
                                        getIntent().getStringExtra(FRIEND_ID),
                                        messageEditText.getText().toString().trim(),
                                        getTime());
        mDatabaseReference.push().setValue(message);
        messageEditText.setText("");
    }

    private String getTime(){
        Calendar calendar = Calendar.getInstance();
        String hour = Integer.toString(calendar.get(Calendar.HOUR));
        String minute = Integer.toString(calendar.get(Calendar.MINUTE));
        if(minute.length() == 1){
            minute = "0" + minute;
        }
        return hour + ":" + minute;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mChildEventListener != null){
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}