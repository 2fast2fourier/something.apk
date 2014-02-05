package com.salvadordalvik.something;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by matthewshepard on 1/31/14.
 */
public class LoginActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        setTitle(R.string.login_activity_title);
    }
}
