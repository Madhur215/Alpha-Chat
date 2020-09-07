package com.example.alphachat.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alphachat.Model.Message;
import com.example.alphachat.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList){
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatAdapter.ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received,
                parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ChatHolder holder, int position) {
        Message message = messageList.get(position);
        Log.e("ADAPTER : ", message.getMessage());
        holder.message_text_view.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        try{
            return messageList.size();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        return 0;
    }

    class ChatHolder extends RecyclerView.ViewHolder{

        TextView message_text_view, time_text_view;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            message_text_view = itemView.findViewById(R.id.text_message_body);
            time_text_view = itemView.findViewById(R.id.text_message_time);
        }
    }

}
