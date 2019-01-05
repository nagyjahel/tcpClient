package com.example.nagyjahel.tcpchatclient.Models;

import com.example.nagyjahel.tcpchatclient.TcpClient;

public class User {

    private int id;
    private String lastName;
    private String firstName;
    private String userName;
    private TcpClient client;

    public User() {
    }

    public User(String lastName, String firstName, String userName) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.userName = userName;

    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public TcpClient getClient() {
        return client;
    }

    public void setClient(TcpClient client) {
        this.client = client;
    }

    public void startTcpClient(){
        this.client.run();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
