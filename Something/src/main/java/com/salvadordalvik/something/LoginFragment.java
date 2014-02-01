package com.salvadordalvik.something;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.alert.FastAlert;
import com.salvadordalvik.something.request.LoginRequest;

/**
 * Created by matthewshepard on 1/31/14.
 */
public class LoginFragment extends FastFragment implements TextWatcher, View.OnClickListener {
    private EditText username, password;
    private Button loginButton;

    public LoginFragment() {
        super(R.layout.login_fragment);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        username = (EditText) frag.findViewById(R.id.login_username);
        password = (EditText) frag.findViewById(R.id.login_password);
        loginButton = (Button) frag.findViewById(R.id.login_submit);

        loginButton.setEnabled(hasLogin());
        loginButton.setOnClickListener(this);
        username.addTextChangedListener(this);
        password.addTextChangedListener(this);
    }

    private void attemptLogin(){
        if(hasLogin()){
            queueRequest(new LoginRequest(username.getText().toString(), password.getText().toString(), new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean response) {
                    FastAlert.notice(getActivity(), getView(), getString(R.string.login_success));
                    Activity act = getActivity();
                    if(act != null){
                        act.startActivity(new Intent(getActivity(), MainActivity.class));
                        act.finish();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    FastAlert.error(getActivity(), getView(), getString(R.string.login_failed));
                }
            }));
        }
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {}

    private boolean hasLogin(){
        return username != null && username.length() > 0 && password.length() > 0;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        loginButton.setEnabled(hasLogin());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_submit:
                attemptLogin();
                break;
        }
    }
}
