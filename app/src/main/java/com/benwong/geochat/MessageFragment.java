package com.benwong.geochat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MessageFragment extends Fragment {

    private Firebase messageListener;
    private List<Message> mMessageList;
    private RecyclerView mMessageRecyclerView;

    public MessageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_message_master, container, false);

        mMessageRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_myMessage_recycler_view);
        mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Retrieve all messages where the user is the sender and recipient
        messageListener = new Firebase("https://originchat.firebaseio.com/messages");
        mMessageList = new ArrayList<Message>();

        new FetchMessages().execute();

        return view;
    }

    private class FetchMessages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //Retrieve messages where the "recipientId" equals the current user's ID
            messageListener = new Firebase("https://originchat.firebaseio.com/messages");
            Query messageQueryRef = messageListener.orderByChild("recipientId").equalTo(Constant.USERID);
            messageQueryRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Message facts = postSnapshot.getValue(Message.class);

                        System.out.println(facts.getRecipientId() + " - " + facts.getMessage());
                        Message message = new Message();
                        message.setDate(facts.getDate());
                        message.setMessage(facts.getMessage());
                        message.setRecipientId(facts.getRecipientId());
                        message.setSenderId(facts.getSenderId());
                        mMessageList.add(message);
                        System.out.println(mMessageList);

                    }

                    //Create holder to put in adapter
                    class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                        private Message mMessage;
                        TextView mSenderTV;
                        TextView mMessageTV;
                        TextView mDateTV;

                        public MessageHolder(View itemView) {
                            super(itemView);
                            itemView.setOnClickListener(this);
                            mSenderTV = (TextView) itemView.findViewById(R.id.senderTV);
                            mMessageTV = (TextView) itemView.findViewById(R.id.messageTV);
                            mDateTV = (TextView) itemView.findViewById(R.id.dateTV);
                        }

                        public void bindMessageItem(Message message) {
                            mMessage = message;


                            //query for Sender's name based on ID
                            Firebase ref = new Firebase("https://originchat.firebaseio.com/users/" + message.getSenderId());
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    System.out.println("chat sender name" + dataSnapshot.child("username").getValue());
                                    mSenderTV.setText(String.valueOf(dataSnapshot.child("username").getValue()));
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });

                            mMessageTV.setText(message.getMessage());

                            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MMM, h:m a");

                            mDateTV.setText(dt1.format(message.getDate()));
                        }

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                            intent.putExtra("userId", mMessage.getSenderId());
                            startActivity(intent);
                        }
                    }

                    class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {

                        private List<Message> mMessageItems;

                        public MessageAdapter(List<Message> messageItems) {
                            mMessageItems = messageItems;
                        }

                        @Override
                        public MessageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//                                TextView textView = new TextView(getActivity());
                            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                            View view = layoutInflater.inflate(R.layout.list_item_message_master, viewGroup, false);
                            return new MessageHolder(view);
                        }

                        @Override
                        public void onBindViewHolder(MessageHolder messageHolder, int position) {
                            Message messageItem = mMessageItems.get(position);
                            messageHolder.bindMessageItem(messageItem);
                        }

                        @Override
                        public int getItemCount() {
                            return mMessageItems.size();
                        }
                    }

//                        for(Message x : mMessageList){
//                            System.out.println(x.getSenderId() + " sent " + x.getMessage() + " to " + x.getRecipientId());
//                        }
//
//                        System.out.println("before setting up adapter");
                    if (isAdded()) {

                        Collections.sort(mMessageList);
                        mMessageRecyclerView.setAdapter(new MessageAdapter(mMessageList));
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
                //need to delete when done
//                           mListview.setAdapter(arrayAdapter);
            });
            return null;
        }
    }
}