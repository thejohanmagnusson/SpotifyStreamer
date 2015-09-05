package se.johanmagnusson.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;
import se.johanmagnusson.android.spotifystreamer.service.PlayerService;

public class PlayerDialogFragment extends DialogFragment {

    private final String LOG_TAG = PlayerDialogFragment.class.getSimpleName();
    public static String TRACKS_KEY = "tracks";
    public static String PLAY_TRACK_POSITION_KEY = "position";
    public static String SEEKBAR_VALUE_KEY = "seekbar-value";

    private List<TrackItem> mTracks = null;
    private int mAutoPlayPosition = -1;
    private boolean mScrubing;

    private PlayerService mPlayerService;
    private boolean mBound = false;
    private BroadcastReceiver mBroadcastReceiver;

    TextView artistName;
    TextView albumName;
    ImageView trackArtwork;
    TextView trackName;
    SeekBar seekBar;
    TextView durationPlayed;
    TextView duration;
    ImageButton previousBtn;
    ImageButton playPauseBtn;
    ImageButton nextBtn;

    public PlayerDialogFragment(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set receiver for service intents
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action == PlayerService.SERVICE_STATE_INTENT) {

                    switch (intent.getStringExtra(PlayerService.STATE)){
                        case PlayerService.STATE_PLAY:
                            if(mBound){
                                updateTrackView();
                                setPlayOrPauseButton(true);
                            }
                            break;
                        case PlayerService.STATE_PAUSE_RESUME:
                            if(mBound)
                                setPlayOrPauseButton(mPlayerService.isPlaying());
                            break;
                        case PlayerService.STATE_TRACK_PROGRESS:
                            if(!mScrubing)
                                updateTrackProgress(intent.getIntExtra(PlayerService.EXTRA_TRACK_PROGRESS, 0));
                            break;
                    }
                }
            }
        };

        //registering for intents when binding/creating service
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        artistName = (TextView) rootView.findViewById(R.id.player_artist_name);
        albumName = (TextView) rootView.findViewById(R.id.player_album_name);
        trackArtwork = (ImageView) rootView.findViewById(R.id.player_track_artwork);
        trackName = (TextView) rootView.findViewById(R.id.player_track_name);
        seekBar = (SeekBar) rootView.findViewById(R.id.player_seekbar);
        durationPlayed = (TextView) rootView.findViewById(R.id.player_duration_played);
        duration = (TextView) rootView.findViewById(R.id.player_duration_left);
        previousBtn = (ImageButton) rootView.findViewById(R.id.player_action_previous);
        playPauseBtn = (ImageButton) rootView.findViewById(R.id.player_action_play_pause);
        nextBtn = (ImageButton) rootView.findViewById(R.id.player_action_next);

        if(savedInstanceState == null) {

            Bundle args = getArguments();

            if(args != null && args.containsKey(TRACKS_KEY)) {

                mTracks = args.getParcelableArrayList(TRACKS_KEY);
                mAutoPlayPosition = args.getInt(PLAY_TRACK_POSITION_KEY);
            }
        }
        else {
            Log.d(LOG_TAG, "Saved data available");
            seekBar.setProgress(savedInstanceState.getInt(SEEKBAR_VALUE_KEY));
        }

        setPlayOrPauseButton(true);
        setUiListeners();

        return rootView;
    }

    private void setUiListeners(){
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound)
                    mPlayerService.previous();
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound)
                    mPlayerService.pauseResume();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound)
                    mPlayerService.next();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mScrubing)
                    updateTrackProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mScrubing = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (mBound)
                    mPlayerService.seekTo(seekBar.getProgress());

                mScrubing = false;
            }
        });
    }

    private void setPlayOrPauseButton(boolean isPlaying) {
        if(isPlaying)
            playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
        else
            playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
    }

    private void updateTrackView() {
        TrackItem track = mPlayerService.getCurrentTrack();

        artistName.setText(track.artist);
        albumName.setText(track.album);

        Picasso.with(getActivity())
                .load(track.imageUrlLarge)
                .placeholder(R.drawable.default_list_icon)
                .error(R.drawable.default_list_icon)
                .into(trackArtwork);

        trackName.setText(track.name);
        seekBar.setMax((int) track.duration);
        durationPlayed.setText(formatTrackTime(mPlayerService.getCurrentTrackProgress()));
        duration.setText(formatTrackTime((int)track.duration));
    }

    private void updateTrackProgress(int progress) {
        durationPlayed.setText(formatTrackTime(progress));
        seekBar.setProgress(progress);
    }

    private String formatTrackTime(int timeSec) {
        int sec = timeSec % 60;
        int min = (timeSec / 60) % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void bindStartRegisterService(){
        //bind/create service, mConnection sets mBound in onServiceConnected
        if(!mBound) {
            Intent intent = new Intent(getActivity(), PlayerService.class);
            getActivity().bindService(intent, mConnection, getActivity().BIND_AUTO_CREATE);

            //register for intents from service
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(PlayerService.SERVICE_STATE_INTENT));
        }
    }

    private void unbindUnregisterService(){
        if(mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;

            //stop listening for intents from service
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        bindStartRegisterService();
    }

    @Override
    public void onPause() {
        super.onPause();

        unbindUnregisterService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SEEKBAR_VALUE_KEY, seekBar.getProgress());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        bindStartRegisterService();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            //get binder to call methods from service
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            mPlayerService = binder.getService();
            mBound = true;

            //use start so service dosn´t stop when binding is released
            Intent intent = new Intent(getActivity(), PlayerService.class);
            getActivity().startService(intent);

            //update ui with current track
            if(mPlayerService.isPlayingOrPaused())
                updateTrackView();

            //handle auto playing of selected track
            if( mAutoPlayPosition != -1) {
                mPlayerService.addTracks(mTracks);
                mPlayerService.playTrack(mAutoPlayPosition);
                mAutoPlayPosition = -1;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            unbindUnregisterService();
        }
    };
}

