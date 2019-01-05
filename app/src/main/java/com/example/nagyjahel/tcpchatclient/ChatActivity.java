package com.example.nagyjahel.tcpchatclient;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nagyjahel.tcpchatclient.Fragments.ConversationFragment;
import com.example.nagyjahel.tcpchatclient.Fragments.ConversationListFragment;
import com.example.nagyjahel.tcpchatclient.Fragments.UserListFragment;
import com.example.nagyjahel.tcpchatclient.Models.Conversation;
import com.example.nagyjahel.tcpchatclient.Models.Message;
import com.example.nagyjahel.tcpchatclient.Models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Conversation> conversations = new ArrayList<>();
    private ArrayList<User> userListForAdapter;
    private User loggedUser;
    private TcpClient loggedClient;
    private ArrayList<User> exceptions = new ArrayList<>();
    private UserListFragment userListFragment = null;
    private ArrayList<ConversationFragment> conversationFragments = new ArrayList<>();
    private BottomNavigationView navigation;
    private android.support.v7.app.ActionBar mToolbar;
    private ConversationListFragment conversationListFragment;
    private boolean userListFragmentHasToBeLoaded = true;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.users:
                    Log.d("Conversation", "length in activity: " + conversations.size());
                    mToolbar.setTitle("Available users");
                    userListFragment = new UserListFragment(loggedClient, users, conversations, exceptions, mToolbar);
                    showFragment(userListFragment);
                    return true;
                case R.id.conversations:
                    mToolbar.setTitle("Messages");
                    conversationListFragment = new ConversationListFragment(loggedClient, users, conversations, mToolbar, conversationFragments);
                    showFragment(conversationListFragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        manageNewData(getIntent());
        initView();
        userListFragment = new UserListFragment();
        userListFragment.setToolbar(mToolbar);
        conversationListFragment = new ConversationListFragment();
        conversationListFragment.setToolbar(mToolbar);

        new ConnectTask().execute("");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        loggedClient.sendMessage("9," + loggedClient.getmUser().getId());
    }


    public void initView() {
        mToolbar = getSupportActionBar();
        mToolbar.setTitle("Available users");
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }

    public void manageNewData(Intent intent) {
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String userName = intent.getStringExtra("userName");
        addUser(new User(lastName, firstName, userName));
        Toast.makeText(this, "Welcome " + userName + "!", Toast.LENGTH_SHORT).show();

    }

    public void createUserList(String userListFromServer) {

        users = new ArrayList<>();
        userListForAdapter = new ArrayList<>();

        List<String> usersData = Arrays.asList(userListFromServer.split(","));
        for (int i = 1; i < usersData.size() - 2; i = i + 4) {
            int id = Integer.parseInt(usersData.get(i));
            String userName = usersData.get(i + 1);
            String firstName = usersData.get(i + 2);
            String lastName = usersData.get(i + 3);
            User user = new User(lastName, firstName, userName);
            user.setId(id);

            if (user.getUserName().equals(loggedClient.getmUser().getUserName())) {
                loggedUser.setId(id);
                exceptions.add(loggedUser);
            } else {
                userListForAdapter.add(user);
            }
            users.add(user);
            Log.d("ChatActivity", "Adding user: " + user.getUserName());
        }

        exceptions.add(loggedUser);

        showUserList();
    }

    private void showUserList() {

        userListFragment = new UserListFragment(loggedClient, users, conversations, exceptions, mToolbar);
        showFragment(userListFragment);

    }

    public void addUser(User user) {
        loggedUser = user;
    }

    private User findUser(int id) {
        Log.d("Chat activity", "Find user: User id:" + id);
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        Log.d("Chat activity", "User not found!");
        return null;
    }

    public void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragment.isResumed()) {
            fragmentTransaction.detach(fragment);
            fragmentTransaction.attach(fragment);
        } else {
            fragmentTransaction.replace(R.id.chat_fragment_placeholder, fragment);
        }

        fragmentTransaction.commit();
    }

    private ConversationFragment findConversationFragment(int id) {
        for (ConversationFragment conversationFragment : conversationFragments) {
            if (conversationFragment.getConversation().getId() == id) {
                return conversationFragment;
            }
        }
        return null;
    }

    private void updateConversationUserList(String message) {
        String[] splitted = message.split(",");
        int conversationId = Integer.parseInt(splitted[1]);
        ConversationFragment conversationFragment = findConversationFragment(conversationId);
        Conversation conversation = null;

        if (conversationFragment == null) {
            conversationFragment = new ConversationFragment();
            conversation = new Conversation();
            conversation.setId(conversationId);
            conversations.add(conversation);
            conversationFragments.add(conversationFragment);
            userListFragment.getConversations().add(conversation);
        } else {
            conversation = conversationFragment.getConversation();
        }
        conversation.setId(conversationId);
        ArrayList<User> newUsers = new ArrayList<>();

        for (int i = 2; i < splitted.length; ++i) {
            User user = findUser(Integer.parseInt(splitted[i]));

            if (user != null) {
                newUsers.add(user);
            } else {
                Log.e("UpdateConvUserList", "User with id " + splitted[i] + "not found!");
            }
        }

        conversation.setUsers(newUsers);
        conversationFragment.setConversation(conversation);
        conversationFragment.setUsers(users);
        conversationFragment.setLoggedUser(loggedClient);
        conversationFragment.setToolbar(mToolbar);

        if (conversationFragment.isResumed()) {
            showFragment(conversationFragment);

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "One of your groups have been updated!", Toast.LENGTH_SHORT).show();
                }
            });
            conversationFragment.notifyAdapter();
        }


    }

    private void updateConversation(String messageContent) {
        String[] splitted = messageContent.split(",");
        String newMessage;
        String fileName;

        int conversationId = Integer.parseInt(splitted[1]);
        int userId = Integer.parseInt(splitted[2]);
        int messageType = Integer.parseInt(splitted[3]);
        ConversationFragment conversationFragment = findConversationFragment(conversationId);

        if (messageType == 1) {
            newMessage = splitted[4];
            Message textMessage = new Message();
            textMessage.setConversationId(conversationId);
            textMessage.setFrom(findUser(userId));
            textMessage.setPostedTime(getCurrentTime());
            textMessage.setMessageType(messageType);
            textMessage.setContent(newMessage);
            textMessage.setTotal(true);
            conversationFragment.addMessageToFragment(textMessage);
        }

        else {
            fileName = splitted[4];
            int messageContentType = Integer.parseInt(splitted[5]);
            int messageLength = Integer.parseInt(splitted[6]);
            newMessage = splitted[7];


            if (messageContentType == 1) { // first sequence of the message
                Message fileMessage = new Message();
                fileMessage.setConversationId(conversationId);
                fileMessage.setFrom(findUser(userId));
                fileMessage.setPostedTime(getCurrentTime());
                fileMessage.setMessageType(messageType);
                fileMessage.initFileContent(newMessage);
                fileMessage.setTotal(false);
                fileMessage.setFileName(fileName);
                conversationFragment.addMessageToFragment(fileMessage);
            }
            else {
                Message existingFileMessage = findMessage(conversationFragment, userId, fileName);
                if(existingFileMessage != null){
                    existingFileMessage.addToContent(newMessage);
                    if (messageContentType == 3) { // last sequence, it can be shown
                        existingFileMessage.setTotal(true);
                        Log.d("Photo", "BYTES: " + new String( existingFileMessage.getFileContent()));
                        //conversationFragment.notifyAdapter();
                    }
                }

            }

        }


    }

    private Message findMessage(ConversationFragment conversationFragment, int userId, String fileName) {
        for (Message message : conversationFragment.getConversation().getMessages()) {
            if (message.getFrom().getId() == userId && message.getFileName().equals(fileName)) {
                return message;
            }
        }
        Log.d("ChatActivity", "findMessage: message wasn't found");
        return null;
    }

    private void createConversation(String message) {

        String[] splitted = message.split(",");

        Conversation conversation = new Conversation();
        conversation.setId(Integer.parseInt(splitted[1]));
        conversation.setCreator(findUser(Integer.parseInt(splitted[2])));
        conversation.addUserToConversation(conversation.getCreator());

        ConversationFragment conversationFragment = new ConversationFragment();

        conversationFragment.setToolbar(mToolbar);
        conversationFragment.setLoggedUser(loggedClient);

        for (int i = 3; i < splitted.length; ++i) {
            conversation.addUserToConversation(findUser(Integer.parseInt(splitted[i])));
        }
        conversationFragment.setConversation(conversation);
        conversations.add(conversation);
        conversationFragments.add(conversationFragment);

        if (isTheSame(conversation.getCreator(), loggedUser)) {
            userListFragment.showConversation(conversation, conversationFragment);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "You've been added to a new group", Toast.LENGTH_SHORT).show();
                }
            });

        }

        conversationListFragment = new ConversationListFragment(loggedClient, users, conversations, mToolbar, conversationFragments);
    }

    private boolean isTheSame(User user1, User user2) {
        if (user1.getId() == user2.getId()
                && user1.getUserName().equals(user2.getUserName())
                && user1.getFirstName().equals(user2.getFirstName())
                && user1.getLastName().equals(user2.getLastName())
                ) {
            return true;
        } else {
            return false;
        }

    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String formattedDate = format.format(calendar.getTime());
        return formattedDate;
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... strings) {

            loggedClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                public void messageReceived(String message) {

                    Log.d("ConnectTask", "Message received");
                    if (message.substring(0, 1).equals("0")) {
                        createUserList(message);
                    }
                    if (message.substring(0, 1).equals("4")) {

                        createConversation(message);
                    }

                    if (message.substring(0, 1).equals("6")) {
                        updateConversation(message);

                    }

                    if (message.substring(0, 1).equals("8")) {
                        updateConversationUserList(message);
                    }
                    publishProgress(message);
                }
            });
            loggedClient.setmUser(loggedUser);
            loggedClient.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

    }

}
