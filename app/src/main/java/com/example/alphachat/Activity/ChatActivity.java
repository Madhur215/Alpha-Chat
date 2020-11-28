package com.example.alphachat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
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

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    public static final String FRIEND_ID = "friend";
    public static final String FRIEND_NAME = "friend name";
    public static final String FRIEND_IMAGE = "image";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 500;
    private static final int RC_PHOTO_PICKER =  105;
    private String SECRET_STRING = "JITMEETALPHA";
    private String MeetCode;

    private AES aes;
    private ChatMessageAdapter messageAdapter;
    private RecyclerView messageRecyclerView;
    private List<Message> messageList = new ArrayList<>();
    private EditText messageEditText;
    private ImageView send_button;

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
        ImageView video_call_image = findViewById(R.id.video_call_image);
        video_call_image.setOnClickListener(view -> {
                MeetCode = getMeetingCode();
                sendMessage(true);
                VideoCall();
            }
        );
        send_button = findViewById(R.id.send_message_image);
        messageRecyclerView = findViewById(R.id.recycler_view_messages);
        messageEditText = findViewById(R.id.message_edit_text);
        ImageView select_image = findViewById(R.id.send_image_chat);

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

        select_image.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"),
                    RC_PHOTO_PICKER);
        });
        send_button.setOnClickListener(v -> sendMessage(false));
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
                    int check = isVideoCall(decryptedMessage, message.getSender_id());
                    if(check == 2)
                        showVideoCallDialog(message.getTimestamp());
                    else if(check == 0)
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

    // Checks whether a particular message is a video call request
    // If yes, then changes the MeetCode and return true
    // So that we can show a dialog to the user that he is
    // Receiving a video call.
    int isVideoCall(String message, String sender){

        if(message.length() < 23) return 0;
        int i;
        for(i = 0; i < 12; i++){
            if(message.charAt(i) != SECRET_STRING.charAt(i))
                return 0;
        }
        if(message.charAt(i) != '_') return 0;
        if(sender.equals(PrefUtils.getUserId())) return 1;
        StringBuilder code = new StringBuilder();
        i++;
        for(; i < message.length(); i++)
            code.append(message.charAt(i));
        MeetCode = code.toString();
        return 2;
    }

    void showVideoCallDialog(String time){
        String timeNow = dt.getTime();
        int diff = dt.TimeDifference(time, timeNow);
        if(diff > 1) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getIntent().getStringExtra(FRIEND_NAME) + "is requesting a video call")
                .setPositiveButton("Accept", (dialog, id) -> {
                    VideoCall();
                    dialog.dismiss();
                })
                .setNegativeButton("Decline", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void sendMessage(boolean isVideoCall) {

        if(messageEditText.getText().toString().trim().length() == 0 && !isVideoCall)
            return;

        String str;
        if(isVideoCall)
            str = SECRET_STRING + "_" + MeetCode;
        else
            str = messageEditText.getText().toString().trim();

        String encryptedMessage = aes.Encrypt(str, this);
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

    private void VideoCall() {
        URL serverURL;
        try {
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetUserInfo userInfo=new JitsiMeetUserInfo();
        userInfo.setDisplayName(PrefUtils.getUserFullName());
        userInfo.setEmail(PrefUtils.getUserEmail());

        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setUserInfo(userInfo)
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);
        JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setFeatureFlag("meeting-name.enabled",false)
                .setRoom(MeetCode)
                .build();
        JitsiMeetActivity.launch(this, options);
    }

    String getMeetingCode(){

        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
}