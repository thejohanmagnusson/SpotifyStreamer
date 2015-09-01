package se.johanmagnusson.android.spotifystreamer;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import java.util.ArrayList;
import java.util.List;

import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;

public class PlayerActivity extends AppCompatActivity {

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);

        //todo: add menu
    }
}
