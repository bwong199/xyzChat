package com.benwong.geochat;

import android.support.v4.app.Fragment;
import android.view.Menu;

public class UserListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new UserListFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
