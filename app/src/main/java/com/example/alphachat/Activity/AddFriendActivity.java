package com.example.alphachat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.example.alphachat.Adapter.FriendsAdapter;
import com.example.alphachat.Model.Friends;
import com.example.alphachat.R;
import com.example.alphachat.Util.PrefUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;
    List<Friends> friendsList = new ArrayList<>();
    private ProgressBar usersProgressbar;
    RecyclerView usersRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("/users");
        final SearchView searchView = findViewById(R.id.add_friend_search_view);
        usersProgressbar = findViewById(R.id.users_progressbar);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setHasFixedSize(true);
        usersProgressbar.setVisibility(View.GONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                usersProgressbar.setVisibility(View.VISIBLE);
                getUsersList(searchView.getQuery().toString());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void getUsersList(final String query) {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Friends> allUsersList = new ArrayList<>();
                for(DataSnapshot data : snapshot.getChildren()){
                    String name = data.child("name").getValue().toString();
                    Log.e("USER NAME: ", name);
                    String image = data.child("image").getValue().toString();
                    String uid = data.child("uid").getValue().toString();
                    String email = data.child("email").getValue().toString();
                    allUsersList.add(new Friends(name, image, email, uid));
                }
                filterUsers(allUsersList, query);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filterUsers(List<Friends> allUsersList, String query) {
        for(Friends user : allUsersList){
            if((user.getFriend_name().toLowerCase().contains(query) ||
                    user.getLast_message().toLowerCase().contains(query)) &&
                    !user.getFriend_id().equals(PrefUtils.getUserId())){
                friendsList.add(user);
                FriendsAdapter mAdapter = new FriendsAdapter(friendsList);
                usersRecyclerView.setAdapter(mAdapter);
                mAdapter.setOnClickListener(new FriendsAdapter.OnFriendClickListener() {
                    @Override
                    public void onFriendClick(Friends friend) {
                        addUsersToFriendList(friend);
                    }
                });
            }
        }
        usersProgressbar.setVisibility(View.GONE);
    }

    private void addUsersToFriendList(Friends friend) {


    }

}