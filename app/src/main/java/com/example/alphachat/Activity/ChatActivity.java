package com.example.alphachat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
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
import com.example.alphachat.Util.AES;
import com.example.alphachat.Util.DateAndTime;
import com.example.alphachat.Util.PrefUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    public static final String FRIEND_ID = "friend";
    public static final String FRIEND_NAME = "friend name";
    public static final String FRIEND_IMAGE = "image";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 500;
    private static final int RC_PHOTO_PICKER =  105;

    private AES aes;
    private ChatMessageAdapter messageAdapter;
    private RecyclerView messageRecyclerView;
    private List<Message> messageList = new ArrayList<>();
    private EditText messageEditText;
    private ImageView send_button;
    private ImageView select_image;

    private FirebaseDatabase mFirebaseDatabase;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mDatabaseReference, onlineRef;
    private StorageReference mStorageReference;
    private DateAndTime dt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_chat);

        TextView friend_name_text_view = findViewById(R.id.chat_activity_friend_name);
        ImageView friend_image_view = findViewById(R.id.chat_activity_friend_image);
        final TextView lastSeenOrOnlineText = findViewById(R.id.last_seen_text_view);
        friend_image_view.setImageURI(Uri.parse(getIntent().getStringExtra(FRIEND_IMAGE)));
        friend_name_text_view.setText(getIntent().getStringExtra(FRIEND_NAME));
        send_button = findViewById(R.id.send_message_image);
        messageRecyclerView = findViewById(R.id.recycler_view_messages);
        messageEditText = findViewById(R.id.message_edit_text);
        select_image = findViewById(R.id.send_image_chat);

        dt = new DateAndTime();
        aes = new AES(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        onlineRef = FirebaseDatabase.getInstance().getReference("users/" + PrefUtils.getUserId());
        mStorageReference = FirebaseStorage.getInstance().getReference().child("photos");
        init();
        setEditText();
        fetchMessages(this);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/users/" +
                getIntent().getStringExtra(FRIEND_ID));
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String status = String.valueOf(snapshot.child("isOnline").getValue());
                    lastSeenOrOnlineText.setText(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"),
                        RC_PHOTO_PICKER);
            }
        });
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
                messageRecyclerView.scrollToPosition(messageAdapter.getItemCount()-1);
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
        String PUSH_ID;
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
                if(message.getPhotoUrl() == null) {
                    String decryptedMessage = aes.Decrypt(message.getMessage(), context);
                    messageList.add(new Message(message.getSender_id(), message.getReceiver_id(), decryptedMessage,
                            message.getTimestamp(), message.getDate(), message.getPhotoUrl()));
                }
                else{
                    messageList.add(message);
                }
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

        if(messageEditText.getText().toString().trim().length() == 0)
            return;

        String encryptedMessage = aes.Encrypt(messageEditText.getText().toString().trim(), this);
        Message message = new Message(PrefUtils.getUserId(),
                                        getIntent().getStringExtra(FRIEND_ID),
                                        encryptedMessage,
                                        dt.getTime(), dt.getDATE(), null);
        mDatabaseReference.push().setValue(message);
        messageEditText.setText("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mChildEventListener != null){
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == RC_PHOTO_PICKER){
            Uri selectedImage = data.getData();
            final StorageReference ref = mStorageReference.child(selectedImage.getLastPathSegment());
            ref.putFile(selectedImage).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Message message = new Message(PrefUtils.getUserId(),
                                                        getIntent().getStringExtra(FRIEND_ID),
                                                        null,
                                                        dt.getTime(), dt.getDATE(), downloadUri.toString());
                        mDatabaseReference.push().setValue(message);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("Online");
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        setStatus("false");
//    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("Last seen at " + dt.getTime());
    }

    private void setStatus(String status){
        Map<String, Object> mp = new HashMap<>();
        mp.put("isOnline", status);
        onlineRef.updateChildren(mp);
    }
}