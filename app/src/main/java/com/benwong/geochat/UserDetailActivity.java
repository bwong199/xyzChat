package com.benwong.geochat;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.Firebase;

public class UserDetailActivity extends SingleFragmentActivity {
    Firebase ref;
    @Override
    protected Fragment createFragment() {
        return new UserDetailFragment();
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

        if(id == R.id.signOut){
            ref = new Firebase("https://originchat.firebaseio.com/");
            ref.unauth();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
