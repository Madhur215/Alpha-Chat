package com.example.alphachat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alphachat.Model.Friends;
import com.example.alphachat.R;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.friendsHolder> {

    private List<Friends> friendsList;
    private OnFriendClickListener mListener;

    public FriendsAdapter(){
    }

    public void setFriendsList(List<Friends> friendsList){
        this.friendsList = friendsList;
    }

    public interface OnFriendClickListener{
        void onFriendClick(Friends friend);
    }

    public void setOnClickListener(OnFriendClickListener listener){
        mListener = listener;
    }

    @NonNull
    @Override
    public FriendsAdapter.friendsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_card_view,
                parent, false);
        return new friendsHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.friendsHolder holder, int position) {
        Friends friend = friendsList.get(position);
        holder.friend_name.setText(friend.getFriend_name());
        holder.last_message.setText(friend.getLast_message());
    }

    @Override
    public int getItemCount() {
        try{
            return friendsList.size();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        return 0;
    }

    class friendsHolder extends RecyclerView.ViewHolder{

        TextView friend_name;
        TextView last_message;
        ImageView friend_image;

        public friendsHolder(@NonNull View itemView, final OnFriendClickListener listener) {
            super(itemView);
            friend_image = itemView.findViewById(R.id.friend_image);
            friend_name = itemView.findViewById(R.id.friend_name_text_view);
            last_message = itemView.findViewById(R.id.last_message_text_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onFriendClick(friendsList.get(getAdapterPosition()));
                        }
                    }
                }
            });
        }
    }

}
