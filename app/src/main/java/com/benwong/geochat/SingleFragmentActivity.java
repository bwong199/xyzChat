package com.benwong.geochat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;

/**
 * Created by benwong on 2016-03-04.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

    private Firebase ref;

    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Firebase.setAndroidContext(getApplicationContext());
//        ref = new Firebase("https://originchat.firebaseio.com");
//        AuthData authData = ref.getAuth();
//        if (authData != null) {
//            // user authenticated
//            System.out.println("authentication data" + authData);
//        } else {
//            // no user authenticated
//
//            System.out.println("authentication data" + authData);
//        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.userProfile) {
            Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.userListMenu){
            Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.signOut){
            ref = new Firebase("https://originchat.firebaseio.com/");
            ref.unauth();
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        if(id == R.id.myMessages){
            Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
