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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class UserDetailFragment extends Fragment {

    private Firebase ref;
    private Firebase nestedMessageListener;
    Intent intent;
    private TextView usernameTV;
    private TextView descriptionTV;
    private ImageView profilePic;
    private EditText messageET;
    private Button messageBtn;
    private Firebase messageListener;
    private List<Message> mMessageList;
    private ListView mListview;
    ArrayAdapter<Message> arrayAdapter;

    public UserDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_user_detail, container, false);

        usernameTV = (TextView) view.findViewById(R.id.usernameTV);
        descriptionTV = (TextView) view.findViewById(R.id.descriptionTV);
        profilePic = (ImageView) view.findViewById(R.id.imageView2);

        messageET = (EditText) view.findViewById(R.id.messageET);
        messageBtn = (Button) view.findViewById(R.id.button);
        final ArrayAdapter<Message> arrayAdapter;
        mMessageList = new ArrayList<Message>();
        mListview = (ListView) view.findViewById(R.id.listView);

        arrayAdapter = new ArrayAdapter<Message>(getActivity(), android.R.layout.simple_list_item_1, mMessageList);

        intent = getActivity().getIntent();

        ref = new Firebase("https://originchat.firebaseio.com/users/" + intent.getStringExtra("userId"));

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                System.out.println(dataSnapshot);
                byte[] imageAsBytes = Base64.decode(String.valueOf(dataSnapshot.child("profilePic").getValue()), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);


                usernameTV.setText(String.valueOf(dataSnapshot.child("username").getValue()));

                if (bmp != null) {
                    profilePic.setImageBitmap(bmp);
                } else {
                    Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),
                            R.drawable.unknownuser);
                    profilePic.setImageBitmap(icon);
                }

                if (dataSnapshot.child("description").getValue().equals("null")) {
                    descriptionTV.setText("I haven't written anything about myself yet.");
                } else {
                    descriptionTV.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageET.setText("");
                ref = new Firebase("https://originchat.firebaseio.com/messages");

                Message newMessage = new Message();
                newMessage.setSenderId(Constant.USERID);
                newMessage.setRecipientId(intent.getStringExtra("userId"));
                newMessage.setMessage(messageET.getText().toString());
                newMessage.setDate(new Date());
                ref.push().setValue(newMessage);


            }
        });

        messageListener = new Firebase("https://originchat.firebaseio.com/messages");
        Query queryRef = messageListener.orderByChild("senderId").equalTo(Constant.USERID);

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);

                System.out.println("There are " + dataSnapshot.getChildrenCount() + " messages");
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Message facts = postSnapshot.getValue(Message.class);

                    if (facts.getRecipientId().toString().equals(intent.getStringExtra("userId"))) {

                        Message message = new Message();
                        message.setDate(facts.getDate());
                        message.setMessage(facts.getMessage());
                        message.setRecipientId(facts.getRecipientId());
                        message.setSenderId(facts.getSenderId());
                        mMessageList.add(message);
                    }

                }


                System.out.println(dataSnapshot.getKey() + " " + dataSnapshot.child("senderId").getValue() + " " + dataSnapshot.child("recipientId").getValue());


                nestedMessageListener = new Firebase("https://originchat.firebaseio.com/messages");
                Query nestedQueryRef = nestedMessageListener.orderByChild("recipientId").equalTo(Constant.USERID);
                nestedQueryRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Message facts = postSnapshot.getValue(Message.class);

                            if (facts.getSenderId().toString().equals(intent.getStringExtra("userId")) ) {
                                System.out.println(facts.getRecipientId() + " - " + facts.getMessage());
                                Message message = new Message();
                                message.setDate(facts.getDate());
                                message.setMessage(facts.getMessage());
                                message.setRecipientId(facts.getRecipientId());
                                message.setSenderId(facts.getSenderId());
                                mMessageList.add(message);
                            }

                        }

                        System.out.println("final message list" + mMessageList);

                        mListview.setAdapter(arrayAdapter);

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return view;

    }


}
