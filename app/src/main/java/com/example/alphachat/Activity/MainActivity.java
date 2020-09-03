package com.example.alphachat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.alphachat.Adapter.FriendsAdapter;
import com.example.alphachat.R;
import com.example.alphachat.Util.PrefUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private PrefUtils pr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAdapter();
        FloatingActionButton fab = findViewById(R.id.add_friends_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddFriendActivity.class));
            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("/users/" + PrefUtils.getUserId());
//        Log.e("UID", PrefUtils.getUserId());
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("friends")){
                    Log.e("SUCCESSFUL", "FRIENDS EXISTS");
                    // TODO ADD FRIENDS TO ADAPTER
                }
                else{
                    Log.e("FAILED", "NO FRIEND ADDED");
                    // TODO SHOW NO FRIENDS IMAGE
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void setAdapter(){
        friendsRecyclerView = findViewById(R.id.friends_recycler_view);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setHasFixedSize(true);
        friendsAdapter = new FriendsAdapter();
        friendsRecyclerView.setAdapter(friendsAdapter);
    }


}