package com.benwong.geochat;

import android.support.v4.app.Fragment;
import android.view.Menu;

public class RegisterActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return new RegisterFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
