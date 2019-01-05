package com.example.nagyjahel.tcpchatclient.Fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.nagyjahel.tcpchatclient.Helpers.ConversationAdapter;
import com.example.nagyjahel.tcpchatclient.Helpers.MessageAdapter;
import com.example.nagyjahel.tcpchatclient.Models.Conversation;
import com.example.nagyjahel.tcpchatclient.Models.Message;
import com.example.nagyjahel.tcpchatclient.Models.User;
import com.example.nagyjahel.tcpchatclient.R;
import com.example.nagyjahel.tcpchatclient.TcpClient;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationListFragment extends Fragment {

    private FloatingActionButton createGroupButton;
    private ArrayList<User> users  = new ArrayList<>();
    private ArrayList<Conversation> conversations = new ArrayList<>();
    private TcpClient loggedClient;
    private ConversationAdapter conversationAdapter;
    private ListView conversationListView;
    private android.support.v7.app.ActionBar toolbar;
    private ArrayList<Message> messages = new ArrayList<>();
    private TextView headerText;
    private ArrayList<ConversationFragment> conversationFragments = new ArrayList<>();

    public ConversationListFragment() {
    }

    @SuppressLint("ValidFragment")

    public ConversationListFragment(TcpClient loggedClient, ArrayList<User> users, ArrayList<Conversation> conversation, android.support.v7.app.ActionBar toolbar, ArrayList<ConversationFragment> conversationFragments) {
        this.loggedClient=loggedClient;
        this.users = users;
        this.conversations = conversation;
        this.toolbar = toolbar;
        this.conversationFragments = conversationFragments;
        createMessageList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation_list, container, false);
        initView(view);
        return view;
    }

    public void initView(View view){
        toolbar.setTitle("Messages");
        headerText = view.findViewById(R.id.conversation_list_header);
        if(messages.size() == 0 ){
            headerText.setText(loggedClient.getmUser().getFirstName()+ " " + loggedClient.getmUser().getLastName()+ ", you don't have any messages yet!" );
        }
       else{
            headerText.setText(loggedClient.getmUser().getFirstName()+ " " + loggedClient.getmUser().getLastName()+ ", see your messages below!" );
        }
        conversationAdapter = new ConversationAdapter(getActivity(), messages, loggedClient.getmUser(), conversations);
        conversationListView = view.findViewById(R.id.conversation_list);
        conversationListView.setAdapter(conversationAdapter);
        createGroupButton = view.findViewById(R.id.create_conversation_button);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Conversation conversation = new Conversation();
                ArrayList<User> usersInConversation = new ArrayList<>();
                usersInConversation.add(loggedClient.getmUser());
                conversation.setUsers(usersInConversation);
                conversation.setCreator(loggedClient.getmUser());
                showFragment(new UserListForSelectionFragment(loggedClient, users, null, "newConversationRequest", usersInConversation));
            }
        });

        conversationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = messages.get(position);
                Conversation conversation = findConversation(message.getConversationId());
                ConversationFragment conversationFragment = findConversationFragment(conversation.getId());
                showFragment(conversationFragment);
            }
        });
    }

    private Conversation findConversation(int conversationId) {
        for(Conversation conversation: conversations){
            if(conversation.getId() == conversationId){
                return conversation;
            }
        }
        Log.d("ConvListFragment", "Find conversation: conversation not found");
        return null;
    }

    public void showFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.chat_fragment_placeholder, fragment);
        fragmentTransaction.commit();
    }

    private void createMessageList(){

        for(Conversation conversation:conversations){
            if(containsUser(conversation.getUsers(), loggedClient.getmUser()) != -1){
                if(conversation.getMessages().size() != 0){
                    messages.add(conversation.getMessages().get(conversation.getMessages().size()-1));
                }
            }
        }
    }

    private int containsUser(ArrayList<User> usersToDisplay, User user) {
        for(int i=0; i<usersToDisplay.size(); ++i){
            if (isTheSame(usersToDisplay.get(i),user)){
                return i;
            }
        }
        return -1;
    }

    private boolean isTheSame(User user1, User user2){
        if(user1.getId() == user2.getId()
                && user1.getUserName().equals(user2.getUserName())
                && user1.getFirstName().equals(user2.getFirstName())
                && user1.getLastName().equals(user2.getLastName())
                ){
            return true;
        }
        else {
            return false;
        }

    }

    private ConversationFragment findConversationFragment(int id){
        for(ConversationFragment conversationFragment:conversationFragments){
            if(conversationFragment.getConversation().getId() == id){
                return conversationFragment;
            }
        }
        return null;
    }


    public void notifyAdapter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conversationAdapter.notifyDataSetChanged();
            }
        });
    }


    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(ArrayList<Conversation> conversations) {
        this.conversations = conversations;
    }

    public TcpClient getLoggedClient() {
        return loggedClient;
    }

    public void setLoggedClient(TcpClient loggedClient) {
        this.loggedClient = loggedClient;
    }

    public ActionBar getToolbar() {
        return toolbar;
    }

    public void setToolbar(ActionBar toolbar) {
        this.toolbar = toolbar;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public ArrayList<ConversationFragment> getConversationFragments() {
        return conversationFragments;
    }

    public void setConversationFragments(ArrayList<ConversationFragment> conversationFragments) {
        this.conversationFragments = conversationFragments;
    }
}
