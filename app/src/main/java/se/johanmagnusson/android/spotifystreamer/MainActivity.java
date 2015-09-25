package se.johanmagnusson.android.spotifystreamer;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import se.johanmagnusson.android.spotifystreamer.Models.ArtistItem;
import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;
import se.johanmagnusson.android.spotifystreamer.service.PlayerService;


public class MainActivity extends AppCompatActivity implements ArtistFragment.Callback, TopTracksFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String TRACK_FRAGMENT_TAG = "TFTAG";
    private final String PLAYER_DIALOG_FRAGMENT_TAG = "PDFTAG";

    private boolean mTwoPane;
    private String mLastQuery = "";

    private MenuItem mReturnToPlayerMenuItem;
    private MenuItem mShareMenuItem;
    private ShareActionProvider mShareActionProvider;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equalsIgnoreCase(PlayerService.ACTION_ON_PREPARING) || action.equalsIgnoreCase(PlayerService.ACTION_IS_PLAYING)) {
                setEnableReturnToPlayerMenuItem(true);
                setShareIntent(Utility.createTrackShareIntent((TrackItem) intent.getParcelableExtra(PlayerService.EXTRA_TRACK)));
                setEnableShareMenuItem(true);
            }
            else if(action.equalsIgnoreCase(PlayerService.ACTION_ON_COMPLETED)) {
                setEnableReturnToPlayerMenuItem(false);
                setEnableShareMenuItem(false);

                if(getResources().getBoolean(R.bool.is_large_device)){
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(PLAYER_DIALOG_FRAGMENT_TAG);
                    if(fragment != null)
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //check if two pane
        if(findViewById(R.id.track_container) != null) {
            mTwoPane = true;

            //add track fragment if not already added
            if(savedInstanceState == null)
                getSupportFragmentManager().beginTransaction().replace(R.id.track_container, new TopTracksFragment(), TRACK_FRAGMENT_TAG).commit();
        }
        else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        //set default values to settings, only done on entering the app for the first time
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        Log.d(LOG_TAG, "Action: " + action);

        if(Intent.ACTION_SEARCH.equals(action)) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            if(!mLastQuery.equalsIgnoreCase(query)) {
                mLastQuery = query;
                //make search query in fragment
                ArtistFragment artistFragment = (ArtistFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_artists);

                if (artistFragment != null) {
                    //String query = intent.getStringExtra(SearchManager.QUERY);
                    artistFragment.searchArtist(query);
                }
            }
        }
        else if(action.equalsIgnoreCase(PlayerService.ACTION_SHOW_PLAYER_LARGE_DEVICE)) {
            showPlayerDialogFragment(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //register for intents
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_ON_PREPARING);
        intentFilter.addAction(PlayerService.ACTION_ON_COMPLETED);
        intentFilter.addAction(PlayerService.ACTION_IS_PLAYING);
        registerReceiver(mBroadcastReceiver, intentFilter);

        setEnableReturnToPlayerMenuItem(false);

        //send intent to check if service is playing a track
        sendBroadcast(new Intent().setAction(PlayerService.ACTION_CHECK_IS_PLAYING));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate and add items to the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mReturnToPlayerMenuItem = menu.findItem(R.id.action_return_to_player);
        setEnableReturnToPlayerMenuItem(false);

        mShareMenuItem = menu.findItem(R.id.action_share);
        //get shareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenuItem);
        setEnableShareMenuItem(false);

        //set searchable configuration to search view.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_return_to_player){
            if(mTwoPane){
                showPlayerDialogFragment(null);
            }
            else {
                Intent intent = new Intent(getApplication(), PlayerActivity.class);
                startActivity(intent);
            }
        }
        else if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //callback from artist fragment
    @Override
    public void onArtistSelected(ArtistItem artist) {

        if(mTwoPane) {
            //replace fragment and set data with argument
            Bundle args = new Bundle();
            args.putParcelable(TopTracksFragment.ARTIST_KEY, artist);

            TopTracksFragment topTracksFragment = new TopTracksFragment();
            topTracksFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.track_container, topTracksFragment, TRACK_FRAGMENT_TAG).commit();
        }
        else {
            //new activity and set data with extra
            Intent intent = new Intent(this, TopTracksActivity.class).putExtra(TopTracksFragment.ARTIST_KEY, artist);
            startActivity(intent);
        }
    }
    //callback from top tracks fragment
    @Override
    public void onTrackSelected(List<TrackItem> tracks, int position) {
        //no need to check device size since this method will only be called from devices that uses the two pane view
        //add tracks and index of selected track for auto play
        Bundle args = new Bundle();
        args.putInt(PlayerDialogFragment.PLAY_TRACK_POSITION_KEY, position);
        args.putParcelableArrayList(PlayerDialogFragment.TRACKS_KEY, (ArrayList<? extends Parcelable>) tracks);

        if(mTwoPane)
            showPlayerDialogFragment(args);
    }

    private void setEnableReturnToPlayerMenuItem(boolean enabled){
        if(mReturnToPlayerMenuItem != null){
            mReturnToPlayerMenuItem.setEnabled(enabled);
            mReturnToPlayerMenuItem.setVisible(enabled);
        }
    }

    private void setShareIntent(Intent shareIntent){
        if(mShareActionProvider != null)
            mShareActionProvider.setShareIntent(shareIntent);
    }

    private void setEnableShareMenuItem(boolean enabled){
        if(mShareMenuItem != null){
            mShareMenuItem.setEnabled(enabled);
            mShareMenuItem.setVisible(enabled);
        }
    }

    private void showPlayerDialogFragment(Bundle args){
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();
        playerDialogFragment.setArguments(args);
        playerDialogFragment.show(getSupportFragmentManager(), PLAYER_DIALOG_FRAGMENT_TAG);
    }
}






























