package se.johanmagnusson.android.spotifystreamer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import se.johanmagnusson.android.spotifystreamer.Models.ArtistItem;
import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;


public class MainActivity extends AppCompatActivity implements ArtistFragment.Callback, TopTracksFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String TRACK_FRAGMENT_TAG = "TFTAG";
    private final String PLAYER_DIALOG_FRAGMENT_TAG = "PDFTAG";

    private boolean mTwoPane;

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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {

            //make search query in fragment
            ArtistFragment artistFragment = (ArtistFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_artists);

            if(artistFragment != null){
                String query = intent.getStringExtra(SearchManager.QUERY);
                artistFragment.searchArtist(query);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate and add items to the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        //set searchable configuration to search view.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onArtistSelected(ArtistItem artist) {

        if(mTwoPane) {
            //replace fragment and set data with bundle
            Bundle args = new Bundle();
            args.putParcelable(TopTracksFragment.ARTIST_KEY, artist);

            TopTracksFragment topTracksFragment = new TopTracksFragment();
            topTracksFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.track_container, topTracksFragment, TRACK_FRAGMENT_TAG).commit();
        }
        else {
            //new activity and set data with bundle
            Intent intent = new Intent(this, TopTracksActivity.class).putExtra(TopTracksFragment.ARTIST_KEY, artist);
            startActivity(intent);
        }
    }

    @Override
    public void onTrackSelected(TrackItem track) {
        //no need to check device size since this method will only be called from devices that uses the two pane view
        Bundle args = new Bundle();
        args.putParcelable(PlayerDialogFragment.TRACK_KEY, args);

        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();

        playerDialogFragment.setArguments(args);
        playerDialogFragment.show(getSupportFragmentManager(), PLAYER_DIALOG_FRAGMENT_TAG);
    }
}






























