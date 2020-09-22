package com.example.alphachat.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alphachat.Activity.ChatActivity;
import com.example.alphachat.Adapter.FriendsAdapter;
import com.example.alphachat.Model.Friends;
import com.example.alphachat.R;
import com.example.alphachat.Util.PrefUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private ProgressBar progressBar;
    private ImageView no_friends_image;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setRecyclerView(view);
        ImageView fab = view.findViewById(R.id.add_friend_image);
        no_friends_image = view.findViewById(R.id.no_friends_bg);
        no_friends_image.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container_main,
                    new AddFriendsFragment()).addToBackStack(null).commit();
            }
        });

        ImageView profile_image = view.findViewById(R.id.profile_image);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile();
            }
        });

        progressBar = view.findViewById(R.id.main_activity_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("/users/" + PrefUtils.getUserId());
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getFriendList(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return view;
    }


    private void profile() {
        getFragmentManager().beginTransaction().replace(R.id.fragment_container_main,
                new ProfileFragment()).addToBackStack(null).commit();
    }

    private void getFriendList(DataSnapshot snapshot) {

        List<Friends> friendsList = getFriends(snapshot);
        friendsAdapter = new FriendsAdapter(friendsList);
        friendsRecyclerView.setAdapter(friendsAdapter);
        progressBar.setVisibility(View.GONE);
        friendsAdapter.setOnClickListener(new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(Friends friend) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND_ID, friend.getFriend_id());
                intent.putExtra(ChatActivity.FRIEND_NAME, friend.getFriend_name());
                intent.putExtra(ChatActivity.FRIEND_IMAGE, friend.getFriend_image());
                startActivity(intent);
            }
        });

        if(friendsList.size() == 0){
            no_friends_image.setVisibility(View.VISIBLE);
        }
        else{
            no_friends_image.setVisibility(View.GONE);
        }

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

    private void setRecyclerView(View view){
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsRecyclerView.setHasFixedSize(true);
    }
}
