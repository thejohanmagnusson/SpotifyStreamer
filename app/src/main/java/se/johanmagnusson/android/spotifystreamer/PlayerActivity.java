package se.johanmagnusson.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;
import se.johanmagnusson.android.spotifystreamer.service.PlayerService;

public class PlayerActivity extends AppCompatActivity {

    private final String LOG_TAG = PlayerActivity.class.getSimpleName();

    private MenuItem mShareMenuItem;
    private ShareActionProvider mShareActionProvider;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equalsIgnoreCase(PlayerService.ACTION_ON_PREPARING)) {
                setShareIntent(Utility.createTrackShareIntent((TrackItem) intent.getParcelableExtra(PlayerService.EXTRA_TRACK)));
                setEnableShareMenuItem(true);
            }
            else if(action.equalsIgnoreCase(PlayerService.ACTION_ON_COMPLETED)) {
                setEnableShareMenuItem(false);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);

        if(savedInstanceState == null) {
            int position = getIntent().getIntExtra(PlayerDialogFragment.PLAY_TRACK_POSITION_KEY, -1);
            List<TrackItem> tracks = getIntent().getParcelableArrayListExtra(PlayerDialogFragment.TRACKS_KEY);

            PlayerDialogFragment playerFragment = new PlayerDialogFragment();

            if( position != -1 && tracks != null) {

                Bundle args = new Bundle();
                args.putInt(PlayerDialogFragment.PLAY_TRACK_POSITION_KEY, position);
                args.putParcelableArrayList(PlayerDialogFragment.TRACKS_KEY, (ArrayList<? extends Parcelable>) tracks);
                playerFragment.setArguments(args);
            }

            getSupportFragmentManager().beginTransaction().add(R.id.player_container, playerFragment).commit();
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
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate and add items to the menu
        getMenuInflater().inflate(R.menu.menu_player, menu);
        mShareMenuItem = menu.findItem(R.id.action_share);
        //get shareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenuItem);

        return super.onCreateOptionsMenu(menu);
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
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mBroadcastReceiver);
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
 }
