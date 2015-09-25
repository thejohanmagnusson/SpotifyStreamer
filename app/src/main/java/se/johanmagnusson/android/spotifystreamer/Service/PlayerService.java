package se.johanmagnusson.android.spotifystreamer.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;

import se.johanmagnusson.android.spotifystreamer.MainActivity;
import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;
import se.johanmagnusson.android.spotifystreamer.PlayerActivity;
import se.johanmagnusson.android.spotifystreamer.R;

public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnErrorListener {

    private final String LOG_TAG = PlayerService.class.getSimpleName();
    private final String WIFI_TAG = "WIFI_TAG";
    private final int NOTIFICATION_ID = 1;

    //mediaplayer states
    private static final int MP_STATE_IDLE = 0;                //after created using new or after reset()
    private static final int MP_STATE_INITIALIZED  = 1;        //after setDataSource()
    private static final int MP_STATE_PREPARING = 2;           //after prepareAsync()
    private static final int MP_STATE_PREPARED = 3;            //after prepare() or after onPrepared() callback from prepareAsync()
    private static final int MP_STATE_STARTED = 4;             //after start()
    private static final int MP_STATE_PAUSED = 5;              //after pause()
    //private static final int MP_STATE_STOPPED = 6;             //after stop()
    private static final int MP_STATE_PLAYBACK_COMPLETED = 7;  //onCompletion() callback if looping mode is false
    //private static final int MP_STATE_ERROR = 8;               //onError callback
    private static final int MP_STATE_END = 9;                 //after release()

    public static final String ACTION_ON_PREPARING = "se.johanmagnusson.android.spotifystreamer.ACTION_ON_PREPARING";
    public static final String ACTION_ON_PLAY = "se.johanmagnusson.android.spotifystreamer.ACTION_ON_PLAY";
    public static final String ACTION_PAUSE = "se.johanmagnusson.android.spotifystreamer.ACTION_PAUSE";
    public static final String ACTION_RESUME = "se.johanmagnusson.android.spotifystreamer.ACTION_RESUME";
    public static final String ACTION_PROGRESS = "se.johanmagnusson.android.spotifystreamer.ACTION_PROGRESS";
    public static final String ACTION_ON_COMPLETED = "se.johanmagnusson.android.spotifystreamer.ACTION_ON_COMPLETED";
    public static final String ACTION_CHECK_IS_PLAYING = "se.johanmagnusson.android.spotifystreamer.ACTION_CHECK_IS_PLAYING";
    public static final String ACTION_IS_PLAYING = "se.johanmagnusson.android.spotifystreamer.ACTION_IS_PLAYING";

    public static final String EXTRA_TRACK = "track";
    public static final String EXTRA_DURATION = "duration";
    public static final String EXTRA_PROGRESS = "progress";

    public static final String ACTION_PREVIOUS = "se.johanmagnusson.android.spotifystreamer.ACTION_PREVIOUS";
    public static final String ACTION_PAUSE_RESUME = "se.johanmagnusson.android.spotifystreamer.ACTION_PAUSE_RESUME";
    public static final String ACTION_NEXT = "se.johanmagnusson.android.spotifystreamer.ACTION_NEXT";

    public static final String ACTION_SHOW_PLAYER_LARGE_DEVICE = "se.johanmagnusson.android.spotifystreamer.ACTION.SHOW_PLAYER_LARGE_DEVICE";
    public static final String ACTION_UPDATE_NOTIFICATION = "se.johanmagnusson.android.spotifystreamer.ACTION.UPDATE.NOTIFICATION";

    private final IBinder mBinder = new PlayerBinder();
    private MediaPlayer mPlayer;
    private int mState;
    private WifiLock mWifiLock;
    private final Handler mProgressHandler;
    private Target mLoadtarget;

    private List<TrackItem> mTracks;
    private int mIndexCurrentTrack;
    private TrackItem mCurrentTrack;
    private Bitmap mCurrentTrackIcon;

    private RemoteViews mNotificationView;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equalsIgnoreCase(ACTION_CHECK_IS_PLAYING)) {
                //respond with action
                sendBroadcast(new Intent().setAction(ACTION_IS_PLAYING).putExtra(EXTRA_TRACK, mCurrentTrack));
            }
            else if(action.equalsIgnoreCase(ACTION_PREVIOUS)) {
                previous();
            }
            else if(action.equalsIgnoreCase(ACTION_PAUSE_RESUME)) {
                pauseResume();
            }
            else if(action.equalsIgnoreCase(ACTION_NEXT)) {
                next();
            }
            else if(action.equalsIgnoreCase(ACTION_UPDATE_NOTIFICATION)) {
                notification();
            }
        }
    };

    //Binder
    public class PlayerBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public PlayerService() {
        mProgressHandler = new Handler();
        mPlayer = new MediaPlayer();
        mState = MP_STATE_IDLE;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationView = new RemoteViews(getPackageName(), R.layout.notification);

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_TAG);

        //register for intents
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CHECK_IS_PLAYING);
        intentFilter.addAction(ACTION_PREVIOUS);
        intentFilter.addAction(ACTION_PAUSE_RESUME);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_UPDATE_NOTIFICATION);
        registerReceiver(mBroadcastReceiver, intentFilter);

        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        releaseWifiLock();
        stopForeground(true);

        mProgressHandler.removeCallbacks(trackProgress);
        unregisterReceiver(mBroadcastReceiver);

        if (mPlayer != null) {
            if (mPlayer.isPlaying())
                mPlayer.stop();

            mPlayer.release();
            mPlayer = null;
            mState = MP_STATE_END;
        }

        super.onDestroy();
    }

    private void setWifiLock(){
        if (!mWifiLock.isHeld())
            mWifiLock.acquire();
    }

    private void releaseWifiLock(){
        if (mWifiLock.isHeld())
            mWifiLock.release();
    }

    //Player methods
    public void addTracks(List<TrackItem> tracks) {
        mTracks = tracks;
    }

    public boolean isPlaying(){
        return mState == MP_STATE_STARTED;
    }

    public boolean isPlayingOrPaused(){
        return mState == MP_STATE_STARTED || mState == MP_STATE_PAUSED;
    }

    public TrackItem getCurrentTrack(){
        return mCurrentTrack;
    }

    public int getTrackDuration(){
        if(isPlayingOrPaused())
            return mPlayer.getDuration() / 1000;
        else
            return 0;
    }

    public int getPlayProgress(){
        if(isPlayingOrPaused())
            return mPlayer.getCurrentPosition() / 1000;
        else
            return 0;
    }

    public void playTrack(int trackIndex)
    {
        if(mState != MP_STATE_IDLE) {
            mProgressHandler.removeCallbacks(trackProgress);
            mPlayer.reset();
            mState = MP_STATE_IDLE;
        }
        if(mTracks != null) {
            if(trackIndex < mTracks.size()) {
                mIndexCurrentTrack = trackIndex;

                try {
                    mCurrentTrack = mTracks.get(trackIndex);
                    mPlayer.setDataSource(mCurrentTrack.previewUrl);
                    mState = MP_STATE_INITIALIZED;

                    setWifiLock();
                    mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                    mPlayer.prepareAsync();
                    mState = MP_STATE_PREPARING;

                    Intent playIntent = new Intent(ACTION_ON_PREPARING).putExtra(EXTRA_TRACK, mCurrentTrack);
                    sendBroadcast(playIntent);

                    setNotificationImage(mCurrentTrack.imageUrlSmall);

                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //media player is done preparing the track
    @Override
    public void onPrepared(MediaPlayer mp) {
        mState = MP_STATE_PREPARED;

        mPlayer.start();
        mState = MP_STATE_STARTED;

        //set handler for track progress updates
        mProgressHandler.postDelayed(trackProgress, 1000);

        Intent playIntent = new Intent(ACTION_ON_PLAY).putExtra(EXTRA_DURATION, mPlayer.getDuration() / 1000);
        sendBroadcast(playIntent);
    }

    private Runnable trackProgress = new Runnable() {
        @Override
        public void run() {
            mProgressHandler.postDelayed(this, 1000);

            Intent playIntent = new Intent(ACTION_PROGRESS).putExtra(EXTRA_PROGRESS, mPlayer.getCurrentPosition() / 1000);
            sendBroadcast(playIntent);
        }
    };

    public void seekTo(int seekTo){
        if(isPlayingOrPaused())
            mPlayer.seekTo(seekTo * 1000);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Intent playIntent = new Intent(ACTION_PROGRESS).putExtra(EXTRA_PROGRESS, mPlayer.getCurrentPosition() / 1000);
        sendBroadcast(playIntent);
    }

    public void pauseResume() {
        if(mState == MP_STATE_STARTED)
            pause();
        else
            resume();
    }

    private void pause() {
        if(mState == MP_STATE_STARTED) {
            mPlayer.pause();
            mState = MP_STATE_PAUSED;

            releaseWifiLock();

            //stop broadcasting track progress
            mProgressHandler.removeCallbacks(trackProgress);

            notification();

            Intent playIntent = new Intent(ACTION_PAUSE);
            sendBroadcast(playIntent);
        }
    }

    private void resume() {
        if(mState == MP_STATE_PAUSED) {
            setWifiLock();
            mPlayer.start();
            mState = MP_STATE_STARTED;

            notification();

            Intent playIntent = new Intent(ACTION_RESUME);
            sendBroadcast(playIntent);

            //set handler for track progress updates
            mProgressHandler.postDelayed(trackProgress, 1);
        }
    }

    public  void previous() {
        if(mIndexCurrentTrack > 0)
            playTrack(--mIndexCurrentTrack);
        else
            playTrack(mIndexCurrentTrack);
    }

    public  void next() {
        if (mIndexCurrentTrack < mTracks.size() - 1)
            playTrack(++mIndexCurrentTrack);
    }

    //media player is done with current track(s)
    @Override
    public void onCompletion(MediaPlayer mp) {
        mState = MP_STATE_PLAYBACK_COMPLETED;

        if (mIndexCurrentTrack < mTracks.size() - 1)
            next();
        else{
            sendBroadcast(new Intent(ACTION_ON_COMPLETED));

            stopForeground(true);
            releaseWifiLock();
            stopSelf();
        }
    }

    //media player had an error, player needs to be reset
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        next();

        return false;
    }

    private void notification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    //notification
    private Notification createNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_headphones);
        builder.setShowWhen(false);

        if(mCurrentTrackIcon != null)
            mNotificationView.setImageViewBitmap(R.id.notification_image, mCurrentTrackIcon);
        else
            mNotificationView.setImageViewResource(R.id.notification_image, R.drawable.ic_music_circle_grey600_24dp);

        if(mCurrentTrack != null) {
            mNotificationView.setTextViewText(R.id.notification_title, mCurrentTrack.name);
            mNotificationView.setTextViewText(R.id.notification_subtitle, mCurrentTrack.artist);
        }
        else{
            mNotificationView.setTextViewText(R.id.notification_title, "");
            mNotificationView.setTextViewText(R.id.notification_subtitle, "");
        }

        //PI, open player activity
        PendingIntent playerPi;
        if(getResources().getBoolean(R.bool.is_large_device))
            playerPi = PendingIntent.getActivity(this, 100, new Intent(this, MainActivity.class).setAction(ACTION_SHOW_PLAYER_LARGE_DEVICE), 0);
        else
            playerPi = PendingIntent.getActivity(this, 100, new Intent(this, PlayerActivity.class), 0);

            builder.setContentIntent(playerPi);

        //get settings for notification controls
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        boolean useNotificationControls = prefs.getBoolean(getString(R.string.pref_notification_controls_key), true);
        Log.d(LOG_TAG, "Service pref notification: " + useNotificationControls);

        //default is private
        if(useNotificationControls) {
            mNotificationView.setViewVisibility(R.id.notification_pause_resume_action, View.VISIBLE);
            mNotificationView.setViewVisibility(R.id.notification_previous_action, View.VISIBLE);
            mNotificationView.setViewVisibility(R.id.notification_next_action, View.VISIBLE);

            if(mState == MP_STATE_PAUSED)
                mNotificationView.setImageViewResource(R.id.notification_pause_resume_action, R.drawable.ic_play_black_24dp);
            else
                mNotificationView.setImageViewResource(R.id.notification_pause_resume_action, R.drawable.ic_pause_black_24dp);

            mNotificationView.setImageViewResource(R.id.notification_previous_action, R.drawable.ic_skip_previous_black_24dp);
            mNotificationView.setImageViewResource(R.id.notification_next_action, R.drawable.ic_skip_next_black_24dp);

            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            Log.d(LOG_TAG, "Use controls");
            //PI Previouse
            PendingIntent piPrevious = PendingIntent.getBroadcast(getApplicationContext(), 101, new Intent().setAction(ACTION_PREVIOUS), PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationView.setOnClickPendingIntent(R.id.notification_previous_action, piPrevious);

            //PI Pause/Resume
            PendingIntent piPauseResume = PendingIntent.getBroadcast(getApplicationContext(), 102, new Intent().setAction(ACTION_PAUSE_RESUME), PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationView.setOnClickPendingIntent(R.id.notification_pause_resume_action, piPauseResume);

            //PI Next
            PendingIntent piNext = PendingIntent.getBroadcast(getApplicationContext(), 103, new Intent().setAction(ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationView.setOnClickPendingIntent(R.id.notification_next_action, piNext);
        }
        else {
            Log.d(LOG_TAG, "No controls");
            mNotificationView.setViewVisibility(R.id.notification_pause_resume_action, View.GONE);
            mNotificationView.setViewVisibility(R.id.notification_previous_action, View.GONE);
            mNotificationView.setViewVisibility(R.id.notification_next_action, View.GONE);
        }

        builder.setContent(mNotificationView);

        return builder.build();
    }

    public void setNotificationImage(String url) {

        if (mLoadtarget == null) mLoadtarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mCurrentTrackIcon = bitmap;
                    notification();
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    mCurrentTrackIcon = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.ic_music_circle_grey600_48dp);
                    notification();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    //do nothing
                }
            };

        Picasso.with(this).load(url).into(mLoadtarget);
    }
}
