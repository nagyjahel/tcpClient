package com.example.nagyjahel.tcpchatclient.Models;

import android.util.Log;

import com.example.nagyjahel.tcpchatclient.TcpClient;

import java.util.ArrayList;

public class Conversation {

    private int Id;
    private ArrayList<User> users = new ArrayList<User>();
    private ArrayList<Message> messages  = new ArrayList<Message>();
    private User creator;

    public Conversation() {
    }

    public Conversation(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> usersList) {
        users = new ArrayList<>();
        this.users = usersList;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addUserToConversation(User user){

        this.users.add(user);
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getUsersAsString() {
        String string = "";
        for(int i=0; i<users.size(); ++i){
            string += users.get(i).getFirstName()+" " + users.get(i).getLastName();
            if(i!= users.size()-1){
                string+= ", ";
            }
        }
        return string;
    }

    public String getLastMessageAsString() {
        if(messages.size() > 0){
            return messages.get(messages.size()-1).getFrom().getFirstName() + " " + messages.get(messages.size()-1).getFrom().getFirstName()  + ": " + messages.get(messages.size()-1).getContent();
        }
       return  "";
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
