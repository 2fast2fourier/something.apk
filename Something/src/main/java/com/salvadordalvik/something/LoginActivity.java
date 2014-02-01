package com.salvadordalvik.something;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by matthewshepard on 1/31/14.
 */
public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        setTitle(R.string.login_activity_title);
    }
}
