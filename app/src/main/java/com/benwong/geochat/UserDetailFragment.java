package com.benwong.geochat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class UserDetailFragment extends Fragment {

    private Firebase ref;
    Intent intent;
    private TextView usernameTV;
    private TextView descriptionTV;
    private ImageView profilePic;
    private EditText messageET;
    private Button messageBtn;
    private Firebase messageListener;


    public UserDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_detail, container, false);

        usernameTV = (TextView)view.findViewById(R.id.usernameTV);
        descriptionTV = (TextView)view.findViewById(R.id.descriptionTV);
        profilePic = (ImageView)view.findViewById(R.id.imageView2);

        messageET = (EditText)view.findViewById(R.id.messageET);
        messageBtn = (Button)view.findViewById(R.id.button);

        intent = getActivity().getIntent();

        ref = new Firebase("https://originchat.firebaseio.com/users/" + intent.getStringExtra("userId"));

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                System.out.println(dataSnapshot);
                byte[] imageAsBytes = Base64.decode(String.valueOf(dataSnapshot.child("profilePic").getValue()), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                usernameTV.setText(String.valueOf(dataSnapshot.child("username").getValue()));
                profilePic.setImageBitmap(bmp);
                descriptionTV.setText(String.valueOf(dataSnapshot.child("description").getValue()));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref = new Firebase("https://originchat.firebaseio.com/messages");

                Message newMessage = new Message();
                newMessage.setSenderId(Constant.USERID);
                newMessage.setRecipientId(intent.getStringExtra("userId"));
                newMessage.setMessage(messageET.getText().toString());
                newMessage.setDate(new Date());
                ref.push().setValue(newMessage);
            }
        });

        messageListener = new Firebase("https://originchat.firebaseio.com/messages/" );
        Query queryRef = messageListener.orderByChild("senderId").equalTo(Constant.USERID);

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return view;

    }


}
