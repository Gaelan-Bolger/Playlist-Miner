package com.unknown.gaelan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;

public class MainActivity extends ActionBarActivity {

    private EditText etSearch;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpotifyAuthentication.openAuthWindow(PlaylistMiner.CLIENT_ID, "token", PlaylistMiner.REDIRECT_URI,
                new String[]{"user-read-private", "user-read-email", "playlist-read-private", "playlist-modify-public", "playlist-modify-private", "streaming"}, null, this);
    }

    @Override
    public void onSupportContentChanged() {
        super.onContentChanged();
        etSearch = (EditText) findViewById(R.id.et_search);
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = etSearch.getText().toString();
                if (searchQuery.length() == 0) {
                    etSearch.setError("Required");
                    return;
                }
                etSearch.setError(null);
                Intent intent = new Intent(MainActivity.this, SearchResultsActivity.class);
                intent.putExtra(SearchResultsActivity.EXTRA_SEARCH_QUERY, searchQuery);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
            String accessToken = response.getAccessToken();
            PrefsHelper.putToken(MainActivity.this, accessToken);
        }
    }


}