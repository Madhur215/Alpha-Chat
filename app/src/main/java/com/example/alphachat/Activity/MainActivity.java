package com.example.alphachat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.alphachat.Adapter.FriendsAdapter;
import com.example.alphachat.Model.Friends;
import com.example.alphachat.R;
import com.example.alphachat.Util.PrefUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRecyclerView();
        FloatingActionButton fab = findViewById(R.id.add_friends_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddFriendActivity.class));
            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("/users/" + PrefUtils.getUserId());
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("SUCCESSFUL", "FRIENDS EXISTS");
                getFriendList(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAILED", "NO FRIEND ADDED");
                // TODO SHOW NO FRIENDS IMAGE
            }
        });
    }

    private void getFriendList(DataSnapshot snapshot) {

        friendsAdapter = new FriendsAdapter(getFriends(snapshot));
        friendsRecyclerView.setAdapter(friendsAdapter);
        friendsAdapter.setOnClickListener(new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(Friends friend) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND_ID, friend.getFriend_id());
                intent.putExtra(ChatActivity.FRIEND_NAME, friend.getFriend_name());
                intent.putExtra(ChatActivity.FRIEND_IMAGE, friend.getFriend_image());
                startActivity(intent);
            }
        });
    }

    private List<Friends> getFriends(DataSnapshot snapshot) {
        List<Friends> list = new ArrayList<>();
        DataSnapshot friends = snapshot.child("friends");
        for(DataSnapshot data : friends.getChildren()){
            String name = data.child("friend_name").getValue().toString();
            String image = data.child("friend_image").getValue().toString();
            String last_message = data.child("last_message").getValue().toString();
            String friend_id = data.child("friend_id").getValue().toString();
            String email = data.child("email").getValue().toString();
            Friends friend = new Friends(name, image, last_message, friend_id, email, false);
            list.add(friend);
        }
        return list;
    }

    private void setRecyclerView(){
        friendsRecyclerView = findViewById(R.id.friends_recycler_view);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setHasFixedSize(true);
    }

}