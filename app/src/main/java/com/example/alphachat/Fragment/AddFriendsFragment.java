package com.example.alphachat.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class AddFriendsFragment extends Fragment {

    private DatabaseReference mDatabaseReference, dbRef;
    List<Friends> friendsList;
    private ProgressBar usersProgressbar;
    RecyclerView usersRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("/users");
        final SearchView searchView = view.findViewById(R.id.add_friend_search_view);
        usersProgressbar = view.findViewById(R.id.users_progressbar);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        usersRecyclerView = view.findViewById(R.id.users_recycler_view);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

        return view;
    }

    private void getUsersList(final String query) {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Friends> allUsersList = new ArrayList<>();
                for(DataSnapshot data : snapshot.getChildren()){
                    String name = data.child("name").getValue().toString();
                    String image = data.child("image").getValue().toString();
                    String uid = data.child("uid").getValue().toString();
                    String email = data.child("email").getValue().toString();
                    allUsersList.add(new Friends(name, image, null, uid, email, true));
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
                    user.getEmail().toLowerCase().contains(query)) &&
                    !user.getFriend_id().equals(PrefUtils.getUserId())){
                friendsList = new ArrayList<>();
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

    private void addUsersToFriendList(final Friends friend) {
        dbRef = FirebaseDatabase.getInstance().getReference("/users/" + PrefUtils.getUserId());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!alreadyFriends(friend, snapshot)) {
                    Friends fr = new Friends(
                            friend.getFriend_name(),
                            friend.getFriend_image(),
                            "No messages yet",
                            friend.getFriend_id(),
                            friend.getEmail(),
                            false
                    );
                    dbRef.child("friends").push().setValue(fr);
                    addToFriendList(friend);
                    Toast.makeText(getContext(), friend.getFriend_name() + " added as friend",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), "Already added as friend!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "An error occurred!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to add the logged in user to friend list of the user added as friend of the current user
    private void addToFriendList(Friends friend) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/users/" +
                friend.getFriend_id());
        Friends currentUser = new Friends(
                PrefUtils.getUserFullName(),
                PrefUtils.getUserImage(),
                "No messages yet",
                PrefUtils.getUserId(),
                PrefUtils.getUserEmail(),
                false
        );
        ref.child("friends").push().setValue(currentUser);
    }

    private boolean alreadyFriends(Friends friend, DataSnapshot snapshot) {
        if(!snapshot.hasChild("friends")){
            return false;
        }
        DataSnapshot friends = snapshot.child("friends");
        for(DataSnapshot fr : friends.getChildren()){
            if(fr.child("friend_id").getValue().toString().equals(friend.getFriend_id()))
                return true;
        }
        return false;
    }


}
