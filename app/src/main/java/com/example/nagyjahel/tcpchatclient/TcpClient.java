package com.example.nagyjahel.tcpchatclient;

import android.util.Log;

import com.example.nagyjahel.tcpchatclient.Models.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpClient {

    public static final String SERVER_IP = "192.168.0.101"; //your computer IP address
    public static final int SERVER_PORT = 13000 ;
    private String mServerMessage;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    private User mUser;

    public TcpClient(OnMessageReceived listener) {
        Log.d("TcpClient:", "constructor");
        mMessageListener = listener;
    }

    public void sendMessage(final String message) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null && !mBufferOut.checkError()) {
                    Log.d("TcpClient", "Sending message: " + message);
                    mBufferOut.println(message);
                }
            }
        }).start();


    }

    public void run(){

        Log.d("Tcp client" , "run()");
        mRun = true;
        try{

            final Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    waitForMessage();
                }
            }).start();

        } catch (UnknownHostException e) {
            Log.d("TcpClient:", "run() - catch1");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("TcpClient:", "run() - catch2");
            e.printStackTrace();
        }
    }

    private void waitForMessage() {
        Log.d("TcpClient","waitForMessage");
        sendMessage("1," + mUser.getUserName()+","+ mUser.getFirstName()+"," + mUser.getLastName());
        while(mRun){
            try {
                Log.d("TcpClient", "try");
                mServerMessage = mBufferIn.readLine();
                Log.d("TcpClient", "Message: " + mServerMessage);
                if(mServerMessage == null){
                    Log.e("TcpClient", "Message received from the server is null");
                    stopClient();
                }
                if (mServerMessage != null && mMessageListener != null) {
                    Log.d("WaitForMessage","Message:" + mServerMessage);
                    mMessageListener.messageReceived(mServerMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }
    public interface OnMessageReceived {
        void messageReceived(String message);
    }

    public User getmUser() {
        return mUser;
    }

    public void setmUser(User mUser) {
        this.mUser = mUser;
    }
}
