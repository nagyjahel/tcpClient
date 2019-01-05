package com.example.nagyjahel.tcpchatclient.Helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nagyjahel.tcpchatclient.Models.Conversation;
import com.example.nagyjahel.tcpchatclient.Models.Message;
import com.example.nagyjahel.tcpchatclient.Models.User;
import com.example.nagyjahel.tcpchatclient.R;

import java.util.ArrayList;


public class ConversationAdapter extends BaseAdapter {

    private ArrayList<Message> mMessages;
    private LayoutInflater mLayoutInflater;
    private ConversationViewHolder messageViewHolder;
    private User currentUser;
    private Context context;
    private ArrayList<Conversation> conversations = new ArrayList<>();

    public ConversationAdapter(Context context, ArrayList<Message> messages, User currentUser, ArrayList<Conversation> conversations) {
        this.mMessages = messages;
        this.mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.currentUser=currentUser;
        this.conversations = conversations;
    }

    private class ConversationViewHolder {
        TextView conversationUsers;
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

        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.conversation, null);
            messageViewHolder = new ConversationViewHolder();
            messageViewHolder.postingUser = convertView.findViewById(R.id.posting_user);
            messageViewHolder.time = convertView.findViewById(R.id.posted_time);
            messageViewHolder.content = convertView.findViewById(R.id.message_content);
            messageViewHolder.conversationUsers = convertView.findViewById(R.id.conversation_users_list);
            convertView.setTag(messageViewHolder);
        }
        Message currentMessage = mMessages.get(position);
        if( currentMessage!= null){
            messageViewHolder.postingUser.setText(currentMessage.getFrom().getFirstName() + " " + currentMessage.getFrom().getLastName());
            messageViewHolder.time.setText("("+ currentMessage.getPostedTime() + ")");
            messageViewHolder.content.setText( currentMessage.getContent());
            messageViewHolder.conversationUsers.setText(conversationUsersString(currentMessage.getConversationId()));

        }
        return convertView;
    }

    private String conversationUsersString(int conversationId){
        String users="";
        for(Conversation conversation: conversations){
            if(conversation.getId() == conversationId){
                for(int i=0; i<conversation.getUsers().size(); ++i){
                    users += conversation.getUsers().get(i).getFirstName()+" "+ conversation.getUsers().get(i).getLastName();
                    if(i != conversation.getUsers().size()-1){
                        users+= ", ";
                    }
                }
                return users;
            }
        }
        Log.d("ConversationAd", "ConversationUsersString is empty");
        return users;
    }
}
