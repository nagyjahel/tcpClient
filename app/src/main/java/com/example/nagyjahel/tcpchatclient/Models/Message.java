package com.example.nagyjahel.tcpchatclient.Models;

import android.util.Log;

public class Message {

    private User from;
    private String content;
    private String postedTime;
    private int conversationId;
    private int messageType;
    private String fileName;
    private boolean isTotal;
    //private byte[] fileContent;
    private String fileContent;

    public Message() {
    }

    public Message(String content, User user, int conversationId, String postedTime, int messageType) {

        this.content = content;
        this.from= user;
        this.conversationId = conversationId;
        this.postedTime=postedTime;
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public String getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(String postedTime) {
        this.postedTime = postedTime;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public boolean isTotal() {
        return isTotal;
    }

    public void setTotal(boolean total) {
        isTotal = total;
    }

    /*
    public void initFileContent(byte[] bytes){
        this.fileContent = bytes;
    }

    public void addToContent(byte[] newBytes){
        byte[] c = new byte[fileContent.length + newBytes.length];
        System.arraycopy(fileContent, 0, c, 0, fileContent.length);
        System.arraycopy(newBytes, 0, c, fileContent.length, newBytes.length);
        this.fileContent = c;
        Log.d("AddToContent", "New size: " + this.fileContent.length);

    }
*/
    public void initFileContent(String firstLine){
        this.fileContent = firstLine;
    }


    public void addToContent(String newMessage){
       this.fileContent += newMessage;

    }
    /*
    public byte[] getFileContent() {
        return fileContent;
    }*/
    public String getFileContent(){
        return fileContent;
    }
}
