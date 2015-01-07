package com.unknown.gaelan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;

import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TextUtils.isEmpty(PrefsHelper.getAccessToken(this))) {
            if (!PrefsHelper.isAccessTokenExpired(this)) {
                setUser();
            } else {
                onLoginUser();
            }
        } else {
            setContentView(R.layout.activity_login);
        }
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (null != uri) {
            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
            String accessToken = response.getAccessToken();
            if (!TextUtils.isEmpty(accessToken)) {
                int expiresIn = response.getExpiresIn();
                PrefsHelper.putAccessToken(this, accessToken, expiresIn);
                setUser();
            } else {
                Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                onLoginUser();
                break;
        }
    }

    private void onLoginUser() {
        SpotifyAuthentication.openAuthWindow(PlaylistMiner.CLIENT_ID, "token", PlaylistMiner.REDIRECT_URI, PlaylistMiner.SCOPES, null, this);
    }

    private void setUser() {
        PlaylistMiner.getSpotifyService(this).getMe(new Callback<User>() {
            @Override
            public void success(final User user, Response response) {
                PlaylistMiner.setUser(user);
                startActivity(new Intent(LoginActivity.this, DrawerActivity.class));
                finish();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}
