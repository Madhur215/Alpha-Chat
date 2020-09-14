package com.example.alphachat.Adapter;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alphachat.Model.Message;
import com.example.alphachat.R;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messageList;
    private String friend_id;
    private Context context;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_DATE_CHANGED_MESSAGE_SENT = 3;
    private static final int VIEW_TYPE_DATE_CHANGED_MESSAGE_RECEIVED = 4;
//    private String LAST_DATE;

    public ChatMessageAdapter(List<Message> messageList, String friend_id, Context context){
        this.friend_id = friend_id;
        this.context = context;
        this.messageList = messageList;
//        LAST_DATE = "99/99/9999";
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
//        if(!message.getDate().equals(LAST_DATE)){
//            LAST_DATE = message.getDate();
//            if(message.getSender_id().equals(friend_id)) {
//                return VIEW_TYPE_DATE_CHANGED_MESSAGE_RECEIVED;
//            }
//            return VIEW_TYPE_DATE_CHANGED_MESSAGE_SENT;
//        }
//        LAST_DATE = message.getDate();
        if(message.getSender_id().equals(friend_id)) {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
        return VIEW_TYPE_MESSAGE_SENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_MESSAGE_SENT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent,
                    parent, false);
            return new MessageViewHolder(view);
        }
        else if(viewType == VIEW_TYPE_MESSAGE_RECEIVED){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received,
                    parent, false);
            return new MessageViewHolder(view);
        }
        else if(viewType == VIEW_TYPE_DATE_CHANGED_MESSAGE_SENT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_changed_message_sent,
                    parent, false);
            return new DateViewHolder(view);
        }
        else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_changed_message_received,
                    parent, false);
            return new DateViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        switch (holder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_SENT:
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((MessageViewHolder)holder).setTexts(message);
                break;
            case VIEW_TYPE_DATE_CHANGED_MESSAGE_RECEIVED:
            case VIEW_TYPE_DATE_CHANGED_MESSAGE_SENT:
                ((DateViewHolder)holder).setTexts(message);
                break;
        }
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

    class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView message_text_view;
        private TextView time_text_view;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            message_text_view = itemView.findViewById(R.id.text_message_body);
            time_text_view = itemView.findViewById(R.id.text_message_time);
        }

        private void setTexts(Message message){
            message_text_view.setText(message.getMessage());
            time_text_view.setText(message.getTimestamp());
        }

    }

    class DateViewHolder extends RecyclerView.ViewHolder{

        private TextView message_text_view, time_text_view, date_text_view;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            message_text_view = itemView.findViewById(R.id.text_message_body);
            time_text_view = itemView.findViewById(R.id.text_message_time);
            date_text_view = itemView.findViewById(R.id.date_text_view);
        }

        private void setTexts(Message message){
            message_text_view.setText(message.getMessage());
            time_text_view.setText(message.getTimestamp());
            date_text_view.setText(message.getDate());
        }
    }

}
