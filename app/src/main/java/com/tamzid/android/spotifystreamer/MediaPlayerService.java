package com.tamzid.android.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

/**
 * Service to play one song. Implemented using help of http://www.vogella.com/tutorials/AndroidServices/article.html
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {
    public static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

    public static final String INTENT_MEDIA_PLAYER_SERVICE_BROADCAST = "com.tamzid.android.spotifystreamer.MediaPlayerService.broadcast";
    public static final String INTENT_MEDIA_PLAYER_SERVICE_IS_PREPARED = "com.tamzid.android.spotifystreamer.MediaPlayerService.isPrepared";

    private MediaPlayer mMediaPlayer;
    public boolean mIsPaused;
    public boolean mIsPrepared;

    private LocalBroadcastManager mBroadcaster;

    private onPlayButtonActiveListener mListener;

    public interface onPlayButtonActiveListener {
        void onPlayButtonActive();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public class MediaPlayerBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private final IBinder mBinder = new MediaPlayerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        releaseMediaPlayer(mMediaPlayer);
        return false;
    }

    @Override
    public void onDestroy() {
        releaseMediaPlayer(mMediaPlayer);
        super.onDestroy();
    }

    public void pauseOrPlayCurrentTrack() {
        if (mMediaPlayer == null) {
            return;
        }

        if (!mIsPrepared) {

        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    public boolean mIsMusicPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    /** Public method for the player UI to play music from the service, restarts MediaPlayer if song is switched */
    public void playMusic(String songUrl) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
            releaseMediaPlayer(mMediaPlayer);
        }

        mMediaPlayer = setupMediaPlayer(songUrl);
    }

    /** Returns an asynchronously prepared media player with the data source and listeners set */
    private MediaPlayer setupMediaPlayer(String songUrl) {
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(songUrl);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "IOException: ", ioe);
        }

        mediaPlayer.prepareAsync();
        // Buffering might cause this method to be slow, do not call on UI thread.
        // Here, prepareAsync() is used and a listener is set for when the data is ready.
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

        return mediaPlayer;
    }

    private void releaseMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public int getMaxDuration() {
        return (mMediaPlayer == null || !mIsPrepared) ? -1 : mMediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return (mMediaPlayer == null || !mIsPrepared) ? -1 : mMediaPlayer.getCurrentPosition();
    }

    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    private void sendPreparedBroadcast() {
        Intent intent = new Intent(INTENT_MEDIA_PLAYER_SERVICE_BROADCAST);
        intent.putExtra(INTENT_MEDIA_PLAYER_SERVICE_IS_PREPARED, true);
        mBroadcaster.sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        releaseMediaPlayer(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(LOG_TAG, "MediaPlayer returned error");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsPrepared = true;
        sendPreparedBroadcast();
        if (!mIsPaused) {
            mp.start();
        }
    }

}
