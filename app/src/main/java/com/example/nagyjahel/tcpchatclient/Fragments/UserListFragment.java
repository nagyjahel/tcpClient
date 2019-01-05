package com.example.nagyjahel.tcpchatclient.Fragments;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.icu.util.LocaleData;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.nagyjahel.tcpchatclient.ChatActivity;
import com.example.nagyjahel.tcpchatclient.Helpers.UserAdapter;
import com.example.nagyjahel.tcpchatclient.Models.Conversation;
import com.example.nagyjahel.tcpchatclient.Models.Message;
import com.example.nagyjahel.tcpchatclient.Models.User;
import com.example.nagyjahel.tcpchatclient.R;
import com.example.nagyjahel.tcpchatclient.TcpClient;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserListFragment extends Fragment {

    private FragmentActivity activity;
    private UserAdapter adapter;
    private ArrayList<User> users;
    private ArrayList<User> exceptions = new ArrayList<>();
    private ListView userListView;
    private TcpClient loggedClient;
    private TextView onlyUserText;
    private ArrayList<Conversation> conversations = new ArrayList<>();
    private Conversation newConversation;
    private ArrayList<User> usersToDisplay = new ArrayList<>();
    private android.support.v7.app.ActionBar toolbar;
    private final static String TAG = "UserListFragment";
    public UserListFragment(){

    }

    @SuppressLint("ValidFragment")
    public UserListFragment(TcpClient client, ArrayList<User> users, ArrayList<Conversation> conversations, ArrayList<User> exceptions, android.support.v7.app.ActionBar toolbar) {
        this.loggedClient = client;
        this.users = users;
        this.conversations = conversations;
        this.exceptions = exceptions;
        this.toolbar = toolbar;
        createUserList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        initView(view);
        return view;
    }

    public void initView(View view){


        adapter = new UserAdapter(getContext(), usersToDisplay );
        userListView = view.findViewById(R.id.user_list);
        userListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        onlyUserText = view.findViewById(R.id.only_one_user);

        if(usersToDisplay.size() == 0){
            onlyUserText.setText( loggedClient.getmUser().getFirstName()+ " " + loggedClient.getmUser().getLastName() + ", it seems that you are the only one here!");
        }
        else{
            onlyUserText.setText(loggedClient.getmUser().getFirstName()+ " " + loggedClient.getmUser().getLastName() + ", see the user list below!");
        }

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                User user = usersToDisplay.get(position);
                Conversation conversation = conversationExists(loggedClient.getmUser(), user);

                ConversationFragment conversationFragment = new ConversationFragment();
                conversationFragment.setUsers(users);
                conversationFragment.setLoggedUser(loggedClient);
                conversationFragment.setToolbar(toolbar);

                if(conversation != null){
                    conversationFragment.setConversation(conversation);
                    showFragment(conversationFragment);

                }
                else{

                    newConversation = new Conversation();
                    newConversation.addUserToConversation(loggedClient.getmUser());
                    newConversation.addUserToConversation(user);
                    conversationFragment.setConversation(newConversation);
                    final String newConversationMessage = "3," + loggedClient.getmUser().getId()+"," +user.getId();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            loggedClient.sendMessage(newConversationMessage);
                        }
                    }).run();

                }


            }
        });
    }

    private Conversation conversationExists(User user, User userByName) {

        for(Conversation conversation: conversations){
            if(conversation.getUsers().size() == 2){
                if(containsUser(conversation.getUsers(),user) != -1 && containsUser(conversation.getUsers(),userByName) != -1)
                {
                    return conversation;
                }
            }
        }
        return null;
    }
    public void createUserList(){
        usersToDisplay = new ArrayList<>();
        for(User user:users){
            int index = containsUser(exceptions,user);
            if(index == -1){
                usersToDisplay.add(user);
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
    public void updateUserList(ArrayList<User> newUserList){

        for(User user:exceptions){
            int i = containsUser(newUserList,user);
            if(i!= -1){
                newUserList.remove(i);
            }
        }

        this.usersToDisplay = newUserList;
        if(usersToDisplay.size() != 0){
            onlyUserText.setText(loggedClient.getmUser().getFirstName()+ " " + loggedClient.getmUser().getLastName() + ", see the user list below!");
        }
        else{
            onlyUserText.setText(loggedClient.getmUser().getFirstName()+ " " + loggedClient.getmUser().getLastName() + ", it seems that you are the only one here!");
        }
        adapter.notifyDataSetChanged();

    }

    public void showFragment(Fragment fragment){
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.chat_fragment_placeholder, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }



    public void showConversation(Conversation conversation, ConversationFragment conversationFragment){

        conversationFragment.setLoggedUser(loggedClient);
        conversation.setId(conversation.getId());
        conversationFragment.setUsers(users);
        showFragment(conversationFragment);

    }

    public ArrayList<Conversation> getConversations(){
        return this.conversations;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FragmentActivity) context;
    }


    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<User> getExceptions() {
        return exceptions;
    }

    public void setExceptions(ArrayList<User> exceptions) {
        this.exceptions = exceptions;
    }

    public TcpClient getLoggedClient() {
        return loggedClient;
    }

    public void setLoggedClient(TcpClient loggedClient) {
        this.loggedClient = loggedClient;
    }

    public void setConversations(ArrayList<Conversation> conversations) {
        this.conversations = conversations;
    }

    public android.support.v7.app.ActionBar getToolbar() {
        return toolbar;
    }

    public void setToolbar(android.support.v7.app.ActionBar toolbar) {
        this.toolbar = toolbar;
    }
}
