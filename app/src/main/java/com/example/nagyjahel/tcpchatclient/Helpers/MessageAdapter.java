package com.example.nagyjahel.tcpchatclient.Helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nagyjahel.tcpchatclient.Models.Message;
import com.example.nagyjahel.tcpchatclient.Models.User;
import com.example.nagyjahel.tcpchatclient.R;

import java.util.ArrayList;


public class MessageAdapter extends BaseAdapter {

    private ArrayList<Message> mMessages;
    private LayoutInflater mLayoutInflater;
    private MessageViewHolder messageViewHolder;
    private User currentUser;
    private Context context;

    public MessageAdapter(Context context, ArrayList<Message> messages, User currentUser) {
        this.mMessages = messages;
        this.mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.currentUser=currentUser;
    }

    private class MessageViewHolder {
        TextView postingUser;
        TextView time;
        TextView content;
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.message, null);
            messageViewHolder = new MessageViewHolder();
            messageViewHolder.postingUser = convertView.findViewById(R.id.posting_user);
            messageViewHolder.time = convertView.findViewById(R.id.posted_time);
            messageViewHolder.content = convertView.findViewById(R.id.message_content);
            convertView.setTag(messageViewHolder);
        }
        Message currentMessage = mMessages.get(position);
        if( currentMessage!= null){
            if(currentMessage.isTotal() == true){
                messageViewHolder.postingUser.setText(currentMessage.getFrom().getFirstName() + " " + currentMessage.getFrom().getLastName());
                messageViewHolder.time.setText("("+ currentMessage.getPostedTime() + ")");
                if(currentMessage.getMessageType() == 1){
                    messageViewHolder.content.setText( currentMessage.getContent());
                }
                else{
                    messageViewHolder.content.setText(currentMessage.getFileName());
                    messageViewHolder.content.setTypeface(null, Typeface.BOLD);
                    messageViewHolder.content.setClickable(true);

                }

                if(currentMessage.getFrom().getUserName().equals(currentUser.getUserName())){
                    messageViewHolder.postingUser.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                    messageViewHolder.time.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                }
                else{
                    messageViewHolder.postingUser.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    messageViewHolder.time.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                }
            }

        }
        return convertView;
    }
}
