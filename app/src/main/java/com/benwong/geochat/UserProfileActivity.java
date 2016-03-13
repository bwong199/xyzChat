package com.benwong.geochat;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;

import com.firebase.client.Firebase;

public class UserProfileActivity extends SingleFragmentActivity {
    Firebase ref;


    protected Fragment createFragment() {
        return new UserProfileFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.userProfile) {
//            Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
//            startActivity(intent);
//            return true;
//        }
//
//        if(id == R.id.userListMenu){
//            Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
//            startActivity(intent);
//            return true;
//        }
//
//        if(id == R.id.myMessages){
//            Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
//            startActivity(intent);
//            return true;
//        }
//
//        if(id == R.id.signOut){
//            ref = new Firebase("https://originchat.firebaseio.com/");
//            ref.unauth();
//            LoginManager.getInstance().logOut();
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("Activity onActivityResult " + requestCode + " " +  resultCode  + " " + data);

    }
}
