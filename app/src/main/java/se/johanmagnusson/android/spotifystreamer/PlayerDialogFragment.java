package se.johanmagnusson.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
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

import java.util.ArrayList;
import java.util.List;

import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;
import se.johanmagnusson.android.spotifystreamer.service.PlayerService;

public class PlayerDialogFragment extends DialogFragment {

    private final String LOG_TAG = PlayerDialogFragment.class.getSimpleName();
    public static String TRACKS_KEY = "tracks";
    public static String PLAY_TRACK_POSITION_KEY = "position";

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

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action == PlayerService.SERVICE_STATE_INTENT) {

                    switch (intent.getStringExtra(PlayerService.STATE)){
                        case PlayerService.STATE_PLAY:
                            updateTrackView((TrackItem) intent.getParcelableExtra(PlayerService.EXTRA_CURRENT_TRACK), intent.getIntExtra(PlayerService.EXTRA_TRACK_DURATION, 0));
                            setPlayOrPauseButton(true);
                            break;
                        case PlayerService.STATE_PAUSE:
                            setPlayOrPauseButton(false);
                            break;
                        case PlayerService.STATE_RESUME:
                            setPlayOrPauseButton(true);
                            break;
                        case PlayerService.STATE_TRACK_PROGRESS:
                            if(!mScrubing) {
                                updateTrackProgress(intent.getIntExtra(PlayerService.EXTRA_TRACK_PROGRESS, 0));
                            }
                            break;
                    }
                }
            }
        };

        //register for intents from player
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(PlayerService.SERVICE_STATE_INTENT));
    }

    private void setPlayOrPauseButton(boolean isPlaying) {
        if(isPlaying)
            playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
        else
            playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(savedInstanceState == null || !savedInstanceState.containsKey(TRACKS_KEY)) {
            Log.d(LOG_TAG, "No saved data available");

            Bundle args = getArguments();

            if(args != null && args.containsKey(TRACKS_KEY)) {
                Log.d(LOG_TAG, "Bundled data available");

                mTracks = args.getParcelableArrayList(TRACKS_KEY);
                mAutoPlayPosition = args.getInt(PLAY_TRACK_POSITION_KEY);
            }
        }
        else {
            Log.d(LOG_TAG, "Saved data available");
            mTracks = savedInstanceState.getParcelableArrayList(TRACKS_KEY);
            mAutoPlayPosition = -1;
        }

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

        setPlayOrPauseButton(true);

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mBound)
//                    mPlayerService.previous();
                if(mBound) {
                    Intent intent = new Intent(PlayerService.SERVICE_CONTROL_INTENT).putExtra(PlayerService.CONTROL, PlayerService.CONTROL_PREVIOUS);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                }
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mBound) {
//                    if (mPlayerService.isPlaying()) {
//                        Log.d(LOG_TAG, "Pause");
//                        mPlayerService.pause();
//                    } else {
//                        Log.d(LOG_TAG, "Resume");
//                        mPlayerService.resume();
//                    }
//                }
                if(mBound) {
                    Intent intent = new Intent(PlayerService.SERVICE_CONTROL_INTENT).putExtra(PlayerService.CONTROL, PlayerService.CONTROL_PAUSE_RESUME);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mBound)
//                    mPlayerService.next();
                if(mBound) {
                    Intent intent = new Intent(PlayerService.SERVICE_CONTROL_INTENT).putExtra(PlayerService.CONTROL, PlayerService.CONTROL_NEXT);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                }
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

                if (mBound) {
                    Log.d(LOG_TAG, "Seek to: " + seekBar.getProgress());

                    Intent intent = new Intent(PlayerService.SERVICE_CONTROL_INTENT).putExtra(PlayerService.CONTROL, PlayerService.CONTROL_SEEK_TO);
                    intent.putExtra(PlayerService.EXTRA_SEEK_TO, seekBar.getProgress());
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

                    //mPlayerService.seekTo(seekBar.getProgress());
                }
                mScrubing = false;
            }
        });

        return rootView;
    }

    private void updateTrackView(TrackItem track, int trackDuration) {
        artistName.setText(track.artist);
        albumName.setText(track.album);

        //todo: add empty on loading image so controls don´t jump up and down or change so controls are aligned to bottom of the view
        Picasso.with(getActivity())
                .load(track.imageUrlLarge)
                .error(R.drawable.default_list_icon)    //use default image on error
                .into(trackArtwork);

        trackName.setText(track.name);
        durationPlayed.setText(formatTrackTime(0));
        seekBar.setMax(trackDuration);
        duration.setText(formatTrackTime(trackDuration));
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

    @Override
    public void onStart() {
        super.onStart();

        Log.d(LOG_TAG, "initialize connection");

        //todo: shift start and bind?
        Intent intent = new Intent(getActivity(), PlayerService.class);
        getActivity().startService(intent);

        //try to bind to service to see if it is running, mConnection sets mBound in onServiceConnected
        if(!mBound) {
            getActivity().bindService (intent, mConnection, getActivity().BIND_AUTO_CREATE);
            Log.d(LOG_TAG, "started/bound to service");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop listening for intents from player
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);

        if(mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TRACKS_KEY, (ArrayList<? extends Parcelable>) mTracks);

        super.onSaveInstanceState(outState);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            mPlayerService = binder.getService();
            mBound = true;
            Log.d(LOG_TAG, "Service connected.");

            if( mAutoPlayPosition != -1) {
//                mPlayerService.addTracks(mTracks);
//                mPlayerService.playTrack(mAutoPlayPosition);

                Intent intent = new Intent(PlayerService.SERVICE_CONTROL_INTENT).putExtra(PlayerService.CONTROL, PlayerService.CONTROL_PLAY);
                intent.putParcelableArrayListExtra(PlayerService.EXTRA_TRACKS, (ArrayList<? extends Parcelable>) mTracks);
                intent.putExtra(PlayerService.EXTRA_SELECTED_TRACK, mAutoPlayPosition);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.d(LOG_TAG, "Service disconnected.");
        }
    };
}

