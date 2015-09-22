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

    //views
    private TextView mArtistName;
    private TextView mAlbumName;
    private ImageView mTrackArtwork;
    private TextView mTrackName;
    private SeekBar mSeekBar;
    private TextView mDurationPlayed;
    private TextView mDuration;
    private ImageButton mPreviousBtn;
    private ImageButton mPlayPauseBtn;
    private ImageButton mNextBtn;

    //service
    private PlayerService mPlayerService;
    private boolean mBound = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equalsIgnoreCase(PlayerService.ACTION_ON_PREPARING)) {
                setTrackArtAndText((TrackItem)intent.getParcelableExtra(PlayerService.EXTRA_TRACK));
                setTrackDurationSeekBar(0);
                updateTrackProgress(0);
            }
            else if(action.equalsIgnoreCase(PlayerService.ACTION_ON_PLAY)) {
                setTrackDurationSeekBar(intent.getIntExtra(PlayerService.EXTRA_DURATION, 0));
            }
            else if(action.equalsIgnoreCase(PlayerService.ACTION_PROGRESS)) {
                if(!mScrubing)
                    updateTrackProgress(intent.getIntExtra(PlayerService.EXTRA_PROGRESS, 0));
            }
            else if(action.equalsIgnoreCase(PlayerService.ACTION_PAUSE)) {
                setButtonsPlayingState(false);
            }
            else if(action.equalsIgnoreCase(PlayerService.ACTION_RESUME)) {
                setButtonsPlayingState(true);
            }
            else if(action.equalsIgnoreCase(PlayerService.ACTION_ON_COMPLETED)) {
                getActivity().finish();
            }
        }
    };

    //view buddies :)
    private List<TrackItem> mTracks = null;
    private int mPlayTrackPosition = -1;
    private boolean mScrubing;

    public PlayerDialogFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        mArtistName = (TextView) rootView.findViewById(R.id.player_artist_name);
        mAlbumName = (TextView) rootView.findViewById(R.id.player_album_name);
        mTrackArtwork = (ImageView) rootView.findViewById(R.id.player_track_artwork);
        mTrackName = (TextView) rootView.findViewById(R.id.player_track_name);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.player_seekbar);
        mDurationPlayed = (TextView) rootView.findViewById(R.id.player_duration_played);
        mDuration = (TextView) rootView.findViewById(R.id.player_duration_left);
        mPreviousBtn = (ImageButton) rootView.findViewById(R.id.player_action_previous);
        mPlayPauseBtn = (ImageButton) rootView.findViewById(R.id.player_action_play_pause);
        mNextBtn = (ImageButton) rootView.findViewById(R.id.player_action_next);

        if(savedInstanceState == null) {
            Bundle args = getArguments();

            if(args != null && args.containsKey(TRACKS_KEY) && args.containsKey(PLAY_TRACK_POSITION_KEY)) {
                mPlayTrackPosition = args.getInt(PLAY_TRACK_POSITION_KEY);
                mTracks = args.getParcelableArrayList(TRACKS_KEY);
            }
        }
        else {
            mPlayTrackPosition = savedInstanceState.getInt(PLAY_TRACK_POSITION_KEY);

            if(savedInstanceState.containsKey(TRACKS_KEY))
                mTracks = savedInstanceState.getParcelableArrayList(TRACKS_KEY);
        }

        setUiListeners();

        //set default button states
        setButtonsPlayingState(true);

        return rootView;
    }

    //sets buttons and seekbar listeners
    private void setUiListeners(){
        mPreviousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound)
                    mPlayerService.previous();
            }
        });

        mPlayPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound)
                    mPlayerService.pauseResume();
            }
        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound)
                    mPlayerService.next();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()

        {
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

    @Override
    public void onResume() {
        super.onResume();
        bindStartRegisterService();
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindUnregisterService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(PLAY_TRACK_POSITION_KEY, mPlayTrackPosition);

        //only save tracks if selected track has not been started yet
        if(mPlayTrackPosition != -1) {
            outState.putParcelableArrayList(TRACKS_KEY, (ArrayList<? extends Parcelable>) mTracks);
        }

        super.onSaveInstanceState(outState);
    }

    private void bindStartRegisterService(){
        //bind/create service, mConnection sets mBound in onServiceConnected
        if(!mBound) {
            //register for intents
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PlayerService.ACTION_ON_PREPARING);
            intentFilter.addAction(PlayerService.ACTION_ON_PLAY);
            intentFilter.addAction(PlayerService.ACTION_PROGRESS);
            intentFilter.addAction(PlayerService.ACTION_PAUSE);
            intentFilter.addAction(PlayerService.ACTION_RESUME);
            intentFilter.addAction(PlayerService.ACTION_ON_COMPLETED);
            getActivity().registerReceiver(mBroadcastReceiver, intentFilter);

            Intent intent = new Intent(getActivity(), PlayerService.class);
            getActivity().bindService(intent, mConnection, getActivity().BIND_AUTO_CREATE);
        }
    }

    private void unbindUnregisterService(){
        if(mBound) {
            getActivity().unregisterReceiver(mBroadcastReceiver);

            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    //Handle connection to service and setup listener
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            //get service and set bound
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            mPlayerService = binder.getService();
            mBound = true;

            //use start so service doesn´t stop when binding is released
            Intent intent = new Intent(getActivity(), PlayerService.class);
            getActivity().startService(intent);

            //handle auto playing of selected track
            if( mPlayTrackPosition != -1) {
                mPlayerService.addTracks(mTracks);
                mPlayerService.playTrack(mPlayTrackPosition);
                mPlayTrackPosition = -1;
            }
            //get track info if track is already playing
            else {
                if(mPlayerService.isPlayingOrPaused()){
                    setTrackArtAndText(mPlayerService.getCurrentTrack());
                    setTrackDurationSeekBar(mPlayerService.getTrackDuration());
                    updateTrackProgress(mPlayerService.getPlayProgress());
                    setButtonsPlayingState(mPlayerService.isPlaying());
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPlayerService = null;
            getActivity().finish();
        }
    };

    private void setButtonsPlayingState(boolean isPlaying) {
        if(isPlaying)
            mPlayPauseBtn.setImageResource(R.drawable.ic_pause_black_48dp);
        else
            mPlayPauseBtn.setImageResource(R.drawable.ic_play_black_48dp);
    }

    private void setTrackArtAndText(TrackItem track) {
        mArtistName.setText(track.artist);
        mAlbumName.setText(track.album);

        Picasso.with(getActivity())
                .load(track.imageUrlLarge)
                .placeholder(R.drawable.default_list_icon)
                .error(R.drawable.default_list_icon)
                .into(mTrackArtwork);

        mTrackName.setText(track.name);
    }

    private void setTrackDurationSeekBar(int duration){
        mSeekBar.setMax(duration);
        mDuration.setText(Utility.formatTrackTime(duration));
    }

    private void updateTrackProgress(int progress) {
        mDurationPlayed.setText(Utility.formatTrackTime(progress));
        mSeekBar.setProgress(progress);
    }
}

