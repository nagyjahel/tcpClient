package com.example.nagyjahel.tcpchatclient.Fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.nagyjahel.tcpchatclient.Helpers.MessageAdapter;
import com.example.nagyjahel.tcpchatclient.Models.Conversation;
import com.example.nagyjahel.tcpchatclient.Models.Message;
import com.example.nagyjahel.tcpchatclient.Models.User;
import com.example.nagyjahel.tcpchatclient.R;
import com.example.nagyjahel.tcpchatclient.TcpClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationFragment extends Fragment {

    private static final String TAG = "ConversationFragment";
    private TextInputEditText messageToSend;
    private ImageView sendButton, addUserToConversationButton, sendFileButton;
    private TextView usersInGroup, filePath;
    private ListView messageListView;
    private View view;

    private MessageAdapter messageAdapter;
    private Conversation conversation;
    private String usersList = "";
    private TcpClient loggedClient;
    private ArrayList<User> users = new ArrayList<>();
    private byte[] bytesToSend;

    private android.support.v7.app.ActionBar toolbar;
    private Uri fileUriToSend;

    private static final int SELECT_FILE_REQUEST_CODE = 1;
    private static final int REQUEST_CODE = 1;
    public ConversationFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversation, container, false);
        initView(view);
        return view;
    }

    public void initView(View view){

        displayUsers();
        final ConversationFragment conversationFragment = this;
        toolbar.setTitle("View conversation");
        messageAdapter = new MessageAdapter(getContext(), conversation.getMessages(), loggedClient.getmUser());
        messageListView = view.findViewById(R.id.conversation_message_list);
        messageListView.setAdapter(messageAdapter);
        messageToSend = view.findViewById(R.id.messageToSend);
        messageToSend.setText("");

        filePath = view.findViewById(R.id.file_path);
        filePath.setText("");

        usersInGroup = view.findViewById(R.id.conversation_users);
        usersInGroup.setText("");
        usersInGroup.setText(usersList);


        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message currentMessage = conversation.getMessages().get(position);
                if(currentMessage.getMessageType() == 2){
                    Toast.makeText(getActivity(),"Your file has been downloaded!", Toast.LENGTH_LONG).show();
                    downloadFile(currentMessage.getFileName(), currentMessage.getFileContent());
                }
            }
        });

        sendButton = view.findViewById(R.id.send_message_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if(!messageToSend.getText().toString().equals("")){
                    Log.d("ConvFragment", messageToSend.getText().toString());
                    loggedClient.sendMessage("5," + conversation.getId()+"," + loggedClient.getmUser().getId() + ",1," + messageToSend.getText().toString());
                    messageToSend.setText("");
                }


                if(!filePath.getText().equals("")){
                    String fileInputStream = "";
                    try{
                        fileInputStream = new String(getBytes(getActivity().getContentResolver().openInputStream(fileUriToSend)));
                    }
                    catch (FileNotFoundException e){

                    }
                    if(!fileInputStream.equals("")){
                        sendFileContentInParts(fileInputStream);
                        filePath.setText("");
                    }

                }
            }
        });


        addUserToConversationButton = view.findViewById(R.id.add_users_to_conversation_button);
        addUserToConversationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new UserListForSelectionFragment(loggedClient, users, conversationFragment, "addClientToConversation", conversation.getUsers()));
            }
        });

        sendFileButton = view.findViewById(R.id.send_file_button);
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void downloadFile(String fileName, String fileContent) {

        Log.d("Photo", "BYTES: " + new String( fileContent));
        Log.d("ConvFragment", "Bytes length: " + new String( fileContent).length());

        verifyPermissions();
        File myDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/jahelMessenger");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        File file = new File(myDir + "/" + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d("ConvFragment", "file: " + file.getAbsolutePath());
        byte[] decodedBytes = Base64.decodeBase64(fileContent.getBytes());
        String content = new String(decodedBytes);


        Log.d("ConvFragment", "decoded length: " + content.length());
        InputStream inputStream = new ByteArrayInputStream( decodedBytes );
        Log.d("Content", content);
        try (PrintWriter out = new PrintWriter(myDir + "/" + fileName)) {
            out.println(content);
        }
            //FileUtils.writeByteArrayToFile(file, decodedBytes);
            /*
            OutputStream outputStream = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[decodedBytes.length];
            Log.d("FileCreate", "bytes length: " + decodedBytes.length);
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            outputStream.flush();
            outputStream.close();*/
        //}
        catch (FileNotFoundException e){
            Log.e("ConvFragment", "fileNotFoundException");
        }
        catch (IOException e){
            Log.e("ConvFragment", "IOException");
        }
    }


    private void startSearch(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, SELECT_FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            if(data != null){
                Uri uri = data.getData();
                fileUriToSend = uri;
                filePath.setText(getFileName(uri));

            }
        }
    }

    private void verifyPermissions(){
        Log.d(TAG, "verifyPermissions method called.");
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED){
            Log.d("ConvFragment", "Permission is granted");
        }
        else{
            ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_CODE);
            Log.d("ConvFragment", "Permission is revoked");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermissions();
    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    @Nullable
    private String readTextFromUri(Uri uri){
        try{

            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        }
       catch (IOException exeption){
            Log.e(TAG,  "Exception: readTextFromUri");
            return null;
       }
    }

    public byte[] getBytes(InputStream inputStream)  {

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        try{


            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

        }
        catch (IOException e){

        }

        return byteBuffer.toByteArray();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendFileContentInParts(String string){
        Log.d("ConvFragment", "string length: " + string.length());
        byte[] encodedBytes = Base64.encodeBase64(string.getBytes());
        String encoded = new String (encodedBytes, Charset.defaultCharset());
        byte [] decodedBytes = Base64.decodeBase64(encodedBytes);
        String decoded = new String(decodedBytes, Charset.defaultCharset());

        Log.d("ConvFragment", "encoded length: " + encoded.length());
        Log.d("ConvFragment", "decoded2 length: " + decoded.length());
        List<String> parts = getParts(encoded, 900);
        for(int i=0; i<parts.size(); ++i){
            if(i ==0 ){
                loggedClient.sendMessage("5," + conversation.getId()+"," + loggedClient.getmUser().getId() + ",2," + getFileName(fileUriToSend)+ ",1," + parts.get(i).length() + "," + parts.get(i));
            }
            else if(i != parts.size()-1){
                loggedClient.sendMessage("5," + conversation.getId()+"," + loggedClient.getmUser().getId() + ",2," + getFileName(fileUriToSend)+ ",2," + parts.get(i).length() + "," +  parts.get(i));
            }
            else{
                loggedClient.sendMessage("5," + conversation.getId()+"," + loggedClient.getmUser().getId() + ",2," + getFileName(fileUriToSend)+ ",3," + parts.get(i).length() + "," +  parts.get(i));
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static List<String> getParts(String string, int partitionSize) {
        List<String> parts = new ArrayList<String>();
        int len = string.length();
        for (int i=0; i<len; i+=partitionSize)
        {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }


    public void displayUsers() {

        usersList = "";
        for(int i=0; i < conversation.getUsers().size(); ++i){
            Log.d("Conversation fragment: " , "Users size:" +conversation.getUsers().size());
            String user = conversation.getUsers().get(i).getFirstName() + " " + conversation.getUsers().get(i).getLastName();
            if(i != conversation.getUsers().size() -1){
                user += ", ";
            }
            usersList += user;
        }
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setLoggedUser(TcpClient loggedClient) {
        this.loggedClient=loggedClient;
    }

    public void addMessageToFragment(Message message) {

        conversation.getMessages().add(message);
        if(this.isResumed()){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    public void showFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.chat_fragment_placeholder, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void setUsers(ArrayList<User> users){
        this.users = users;
    }

    public ActionBar getToolbar() {
        return toolbar;
    }

    public void setToolbar(ActionBar toolbar) {
        this.toolbar = toolbar;
    }

    public void notifyAdapter(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
            }
        });

    }



    public void fillWithData(){

    }

    private void executeSendTask(){


    }

}
