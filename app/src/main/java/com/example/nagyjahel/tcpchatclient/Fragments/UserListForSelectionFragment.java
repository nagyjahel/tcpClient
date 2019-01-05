package com.example.nagyjahel.tcpchatclient.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.nagyjahel.tcpchatclient.Helpers.UserAdapter;
import com.example.nagyjahel.tcpchatclient.Models.Conversation;
import com.example.nagyjahel.tcpchatclient.Models.User;
import com.example.nagyjahel.tcpchatclient.R;
import com.example.nagyjahel.tcpchatclient.TcpClient;

import java.util.ArrayList;


public class UserListForSelectionFragment extends Fragment {

    private UserAdapter adapter;
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<User> newUsers = new ArrayList<>();
    private ListView userListView;
    private TextView selectedUsers;
    private ArrayList<User> usersToDisplay = new ArrayList<>();
    private ArrayList<User> exceptions = new ArrayList<>();
    private TcpClient loggedClient;
    private Button createButton;
    private String instruction;
    private ConversationFragment conversationFragment;
    private static final String TAG = "UserListSelectFr";
    public UserListForSelectionFragment(){

    }

    @SuppressLint("ValidFragment")
    public UserListForSelectionFragment(TcpClient client, ArrayList<User> users, ConversationFragment conversationFragment, String instruction, ArrayList<User> exceptions) {
        this.loggedClient = client;
        this.users= users;
        this.conversationFragment = conversationFragment;
        this.instruction = instruction;
        this.exceptions = exceptions;
        createUserList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list_select, container, false);
        userListView = view.findViewById(R.id.user_list);
        adapter = new UserAdapter(this.getContext(),usersToDisplay);
        userListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        selectedUsers = view.findViewById(R.id.selected_users);
        createButton = view.findViewById(R.id.create_group_button);
        createButton.setVisibility(View.INVISIBLE);

        if(instruction.equals("newConversationRequest")){
            createButton.setText("Create group");
        }
        if(instruction.endsWith("addClientToConversation")){
            createButton.setText("Add users");
        }
        populateSelectedUserList();

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = usersToDisplay.get(position);

                if(selectedUsers.getText().equals("")){
                    selectedUsers.setText(user.getFirstName()+ " " + user.getLastName());
                }
                else {
                    selectedUsers.setText(selectedUsers.getText() + ", " + user.getFirstName()+ " " + user.getLastName());
                }
                newUsers.add(user);
                removeSelectedUsersFromList(user);
                createButton.setVisibility(View.VISIBLE);
            }

        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(instruction.equals("addClientToConversation")){
                    loggedClient.sendMessage(addUsersToConversation());
                }
                if(instruction.equals("newConversationRequest")){
                    loggedClient.sendMessage(newGroupRequest());
                }
                showFragment(conversationFragment);

            }
        });

        return view;
    }

    private String addUsersToConversation(){
        String message = "7," + conversationFragment.getConversation().getId()+",";
        for(int i=0; i<newUsers.size(); ++i){
            message+= newUsers.get(i).getId();
            if(i!= newUsers.size()-1){
                message+=",";
            }
        }
        return message;
    }


    private void populateSelectedUserList() {
        if(conversationFragment != null){
            for(User user:conversationFragment.getConversation().getUsers()){
                if(selectedUsers.getText().equals("")){
                    selectedUsers.setText(user.getFirstName()+" "+user.getLastName());
                }
                else{
                    selectedUsers.setText(selectedUsers.getText().toString() + ", " +user.getFirstName()+" "+user.getLastName() );
                }

            }
        }
    }

    private void removeSelectedUsersFromList(User user) {
        int index = containsUser(usersToDisplay,user);
        if(index != -1){
            usersToDisplay.remove(index);
        }
        adapter.notifyDataSetChanged();

    }

    public String newGroupRequest(){
        Log.d("Selected users", selectedUsers.getText().toString());
        String userList = selectedUsers.getText().toString();
        String[] userNameList = userList.split(",");
        boolean isLoggedClientInList = false;

        String request = "3,";
        for(int i=0; i<userNameList.length; ++i){
            if(i!=0){
                userNameList[i] = userNameList[i].substring(1,userNameList[i].length());
            }
            String[] userName = userNameList[i].split(" ");
            User user = findUserByNames(userName[0], userName[1]);
            if(user == loggedClient.getmUser()){
                isLoggedClientInList = true;
            }
            request += user.getId();
            if(i != userNameList.length -1){
                request+= ",";
            }
        }

        if(!isLoggedClientInList){
            request+= ",";
            request+= loggedClient.getmUser().getId();
        }
        return request;
    }

    public User findUserByNames(String firstName, String lastName){
        Log.d("FindUserByName: " , firstName + " "+ lastName);
        for(User user:users){
            Log.d("FindUserByName: " , user.getFirstName() + " "+ user.getLastName());
            if(user.getFirstName().equals(firstName) && user.getLastName().equals(lastName)){
                Log.d("FindUserByName: " , "foundUser");
                return user;
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

    public void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragment.isResumed()) {
            fragmentTransaction.detach(fragment);
            fragmentTransaction.attach(fragment);
        } else {
            fragmentTransaction.replace(R.id.chat_fragment_placeholder, fragment);
        }

        fragmentTransaction.commit();
    }

}
