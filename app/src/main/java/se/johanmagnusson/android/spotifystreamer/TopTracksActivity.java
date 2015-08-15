package se.johanmagnusson.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;


public class TopTracksActivity extends AppCompatActivity implements TopTracksFragment.Callback{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_top_tracks);

        //add fragment
        if(savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.track_container, new TopTracksFragment()).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
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
    public void onTrackSelected(TrackItem track) {
        //no need to check device size since this activity will only exist on devices defined as not large.
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerDialogFragment.TRACK_KEY, track);
        startActivity(intent);
    }
}
