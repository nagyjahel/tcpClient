package com.example.nagyjahel.tcpchatclient.Helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nagyjahel.tcpchatclient.Models.User;
import com.example.nagyjahel.tcpchatclient.R;

import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {

    private ArrayList<User> mUsers;
    private LayoutInflater mLayoutInflater;
    private UserViewHolder userViewHolder;

    public UserAdapter(Context context, ArrayList<User> users) {
        this.mUsers = users;
        this.mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    private class UserViewHolder {
        TextView firstName;
        TextView lastName;
        TextView userName;
    }
    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.user, null);
            userViewHolder = new UserViewHolder();
            userViewHolder.firstName = convertView.findViewById(R.id.user_first_name);
            userViewHolder.lastName = convertView.findViewById(R.id.user_last_name);
            userViewHolder.userName = convertView.findViewById(R.id.user_user_name);
            convertView.setTag(userViewHolder);
        }
        User currentUser = mUsers.get(position);
        if( currentUser!= null){
                userViewHolder.firstName.setText(currentUser.getFirstName());
                userViewHolder.lastName.setText(currentUser.getLastName());
                userViewHolder.userName.setText( "(" + currentUser.getUserName() + ")");

        }
        return convertView;
    }
}
