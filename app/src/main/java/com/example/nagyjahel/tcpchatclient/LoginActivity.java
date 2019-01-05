package com.example.nagyjahel.tcpchatclient;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private TextInputEditText firstName;
    private TextInputEditText lastName;
    private TextInputEditText userName;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate method called.");
        setContentView(R.layout.activity_login);
        initView();

    }

    private void initView() {
        Log.d(TAG, "initView method called.");
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        userName = findViewById(R.id.userName);
        connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                intent.putExtra("firstName", firstName.getText().toString());
                intent.putExtra("lastName", lastName.getText().toString());
                intent.putExtra("userName", userName.getText().toString());
                startActivity(intent);
                finish();
            }
        });

    }
}
