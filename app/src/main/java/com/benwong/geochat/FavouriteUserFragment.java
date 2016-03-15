package com.benwong.geochat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class FavouriteUserFragment extends Fragment {

    SQLiteDatabase favouriteDatabase;
    private Firebase ref;

    ArrayList<String> favouriteUserList;
    ArrayList<String> favouriteUserNames;
    ListView favouriteUserListView;
    ArrayAdapter<String> arrayAdapter;


    public FavouriteUserFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favourite_user, container, false);

        favouriteUserList = new ArrayList<String>();
        favouriteUserNames= new ArrayList<String>();
        favouriteUserListView = (ListView) view.findViewById(R.id.listView);


        boolean inEmulator = "generic".equals(Build.BRAND.toLowerCase());
        //check to see if it's running on Emulator or device. Set favouriteDatabase to different path based on if it's running on emulator or device

        if (inEmulator) {
            favouriteDatabase = SQLiteDatabase.openDatabase("/data/data/com.benwong.geochat/databases/OriginUsers", null, SQLiteDatabase.OPEN_READONLY);

        } else {
            favouriteDatabase = SQLiteDatabase.openDatabase("/data/user/0/com.benwong.geochat/databases/OriginUsers", null, SQLiteDatabase.OPEN_READONLY);
//
        }

        Log.i("dbpath", favouriteDatabase.getPath());


        Cursor c = favouriteDatabase.rawQuery("SELECT * FROM favouriteUsers", null);

        int idIndex = c.getColumnIndex("userId");


        if (c != null && c.moveToFirst()) {
            do {

                String userId = c.getString(idIndex);

                System.out.println("adding favourite user " + c.getString(idIndex));

                favouriteUserList.add(userId);

            } while (c.moveToNext());
        }

        for (String eachUser : favouriteUserList) {
            System.out.println("each user in favouritelist " + eachUser);

            ref = new Firebase("https://originchat.firebaseio.com/users/" + eachUser);

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("added " + dataSnapshot.child("username").getValue());

                    if(dataSnapshot.child("username").getValue() != null){
                        favouriteUserNames.add(String.valueOf(dataSnapshot.child("username").getValue()));
                    }

                    arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, favouriteUserNames);

                    favouriteUserListView.setAdapter(arrayAdapter);


                    favouriteUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(getContext(), favouriteUserNames.get(position), Toast.LENGTH_LONG).show();
                        }
                    });

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });


        }




    return view;
}

}
