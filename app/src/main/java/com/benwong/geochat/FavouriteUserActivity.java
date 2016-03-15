package com.benwong.geochat;

import android.support.v4.app.Fragment;

public class FavouriteUserActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FavouriteUserFragment();
    }


}
