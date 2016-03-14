package com.benwong.geochat;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by benwong on 2016-03-13.
 */
public class NewMessageWidget extends AppWidgetProvider {

    private Firebase nestedMessageListener;
    private List<Message> mMessageList;
    private ArrayList<Message> myMessages = new ArrayList<Message>();
    private String widgetMessage;
    private String username;
    private RemoteViews views;
    private RemoteViews linearView;
    String message;
    int count;
    Timer timer;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        views = new RemoteViews(context.getPackageName(), R.layout.message_widget);

        if (Constant.USERID != null) {
            nestedMessageListener = new Firebase("https://originchat.firebaseio.com/messages");
            Query nestedQueryRef = nestedMessageListener.orderByChild("recipientId").equalTo(Constant.USERID);
            mMessageList = new ArrayList<Message>();
            nestedQueryRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final Message facts = postSnapshot.getValue(Message.class);
                        //query for Sender's name based on ID
                        Firebase ref = new Firebase("https://originchat.firebaseio.com/users/" + facts.getSenderId());
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                System.out.println("widget " + facts.getMessage() + " from " + dataSnapshot.child("username").getValue());
//                                message.setWidgetMessage(facts.getMessage() + " from " + dataSnapshot.child("username").getValue());
                                message = facts.getMessage() + " from " + dataSnapshot.child("username").getValue();
                                System.out.println(message);
                                views.setTextViewText(R.id.messageWidgetView, message);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        views = new RemoteViews(context.getPackageName(), R.layout.message_widget);

        if (Constant.USERID != null) {
            nestedMessageListener = new Firebase("https://originchat.firebaseio.com/messages");
            Query nestedQueryRef = nestedMessageListener.orderByChild("recipientId").equalTo(Constant.USERID);
            mMessageList = new ArrayList<Message>();
            nestedQueryRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final Message facts = postSnapshot.getValue(Message.class);
                        //query for Sender's name based on ID
                        Firebase ref = new Firebase("https://originchat.firebaseio.com/users/" + facts.getSenderId());
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                System.out.println("widget " + facts.getMessage() + " from " + dataSnapshot.child("username").getValue());
//                                message.setWidgetMessage(facts.getMessage() + " from " + dataSnapshot.child("username").getValue());
                                message = facts.getMessage() + " from " + dataSnapshot.child("username").getValue();
                                System.out.println(message);
                                views.setTextViewText(R.id.messageWidgetView, message);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

        count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
    }


}