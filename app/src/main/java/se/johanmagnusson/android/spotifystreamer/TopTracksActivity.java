package se.johanmagnusson.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import se.johanmagnusson.android.spotifystreamer.Models.ArtistItem;
import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;
import se.johanmagnusson.android.spotifystreamer.Service.PlayerService;


public class TopTracksActivity extends AppCompatActivity implements TopTracksFragment.Callback{

    private final String LOG_TAG = TopTracksActivity.class.getSimpleName();

    private MenuItem mReturnToPlayerMenuItem;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equalsIgnoreCase(PlayerService.ACTION_ON_PREPARING) || action.equalsIgnoreCase(PlayerService.ACTION_IS_PLAYING)) {
                setEnableReturnToPlayerMenuItem(true);
            }
            else if(action.equalsIgnoreCase(PlayerService.ACTION_ON_COMPLETED)) {
                setEnableReturnToPlayerMenuItem(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_top_tracks);

        if(savedInstanceState == null) {
            ArtistItem artist = getIntent().getParcelableExtra(TopTracksFragment.ARTIST_KEY);

            TopTracksFragment topTracksFragment = new TopTracksFragment();

            if(artist != null) {
                Bundle args = new Bundle();
                args.putParcelable(TopTracksFragment.ARTIST_KEY, artist);
                topTracksFragment.setArguments(args);
            }

            getSupportFragmentManager().beginTransaction().add(R.id.track_container, topTracksFragment).commit();
        }

        getSupportActionBar().setElevation(0f);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        mReturnToPlayerMenuItem = menu.findItem(R.id.action_return_to_player);
        setEnableReturnToPlayerMenuItem(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_return_to_player){
            Intent intent = new Intent(getApplication(), PlayerActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrackSelected(List<TrackItem> tracks, int position) {
        //no need to check device size since this activity will only exist on devices defined as not large.
        //start player
        Intent intent = new Intent(getApplication(), PlayerActivity.class);
        intent.putExtra(PlayerDialogFragment.PLAY_TRACK_POSITION_KEY, position);
        intent.putExtra(PlayerDialogFragment.TRACKS_KEY, (ArrayList<? extends Parcelable>) tracks);
        startActivity(intent);
    }

    private void setEnableReturnToPlayerMenuItem(boolean enabled){
        if(mReturnToPlayerMenuItem != null){
            mReturnToPlayerMenuItem.setEnabled(enabled);
            mReturnToPlayerMenuItem.setVisible(enabled);
        }
    }
}















