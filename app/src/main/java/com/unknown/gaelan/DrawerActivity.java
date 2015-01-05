package com.unknown.gaelan;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DrawerActivity extends ActionBarActivity {

    private static final String TAG = "DrawerActivity";
    private static final String[] SCOPES = new String[]{"user-read-private", "user-read-email", "playlist-read-private", "playlist-modify-public", "playlist-modify-private", "streaming"};
    private static final String CURRENT_FRAGMENT = "current";
    private FragmentManager mFm;
    private User mUser;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ArrayAdapter<String> mDrawerAdapter;
    private CircleImageView ivUserImage;
    private TextView tvUserDisplayName;
    private TextView tvUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFm = getSupportFragmentManager();
        setContentView(R.layout.activity_drawer);
        setSupportActionBar(mToolbar);
        selectFragment(0);
        SpotifyAuthentication.openAuthWindow(PlaylistMiner.CLIENT_ID, "token", PlaylistMiner.REDIRECT_URI, SCOPES, null, this);
    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.app_name);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name,
                R.string.app_name) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mToolbar.setTitle(R.string.app_name);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerAdapter = new ArrayAdapter<String>(this, R.layout.nav_section_item, new String[]{"Search", "Recent", "Playlists", "More"});
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectFragment(position)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }
            }
        });

        ivUserImage = (CircleImageView) findViewById(R.id.user_image);
        tvUserDisplayName = (TextView) findViewById(R.id.user_display_name);
        tvUserEmail = (TextView) findViewById(R.id.user_email);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
            String accessToken = response.getAccessToken();
            PrefsHelper.putToken(this, accessToken);
            getUser();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tips, menu);
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                SettingsActivity.start(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mFm.getBackStackEntryCount() > 0) {
            mFm.popBackStack();
            return;
        }
        if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
            return;
        }
        super.onBackPressed();
    }

    private void getUser() {
        PlaylistMiner.getSpotifyService(this).getMe(new Callback<User>() {
            @Override
            public void success(final User user, Response response) {
                mUser = user;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDrawerHeader();
                    }
                });

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void updateDrawerHeader() {
        tvUserDisplayName.setText(mUser.display_name);
        tvUserEmail.setText(mUser.email);
        List<Image> images = mUser.images;
        if (null != images && images.size() > 0) {
            Image image = images.get(0);
            Picasso.with(this).load(image.url).into(ivUserImage);
        }
    }

    private boolean selectFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = MainFragment.newInstance();
                break;
            case 2:
                fragment = UserPlaylistsFragment.newInstance(mUser);
                break;
            default:
                fragment = PlaceholderFragment.newInstance(position);
                break;

        }
        Fragment currentFragment = getCurrentFragment();
        if (null == currentFragment || !fragment.equals(currentFragment)) {
            mFm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mFm.beginTransaction().replace(R.id.container, fragment, CURRENT_FRAGMENT).commit();
            mDrawerList.setItemChecked(position, true);
            return true;
        }
        return false;
    }

    private Fragment getCurrentFragment() {
        return mFm.findFragmentByTag(CURRENT_FRAGMENT);
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dialog_save_ranked_tracks_options, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((DrawerActivity) activity).setTitle("Page" + ARG_SECTION_NUMBER);
        }
    }

}
