package com.tamzid.android.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Service to play music. Implemented using http://www.vogella.com/tutorials/AndroidServices/article.html
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {
    public static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

    private MediaPlayer mMediaPlayer;
    public List<TrackBundle> mTrackList;
    public int mTrackNowPlaying;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: do something useful
        return START_STICKY;
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

    public void playMusic() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            releaseMediaPlayer(mMediaPlayer);
        }

        String songUrl = mTrackList.get(mTrackNowPlaying).preview_url;
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

    /** Goes to next track. Releases media player and plays new track. */
    private void nextTrack() {
        releaseMediaPlayer(mMediaPlayer);
        incrementTrack(true);
//        bindView();
    }

    /** Goes to previous track. Releases media player and plays new track */
    private void previousTrack() {
        releaseMediaPlayer(mMediaPlayer);
        incrementTrack(false);
//        bindView();
    }

    /**
     * Increment track count up or down, loop back if end is reached. Releases media player.
     *
     * @param incrementUp Enter <code>true</code> to increment up, <code>false</code> to increment
     *                    down.
     */
    private void incrementTrack(boolean incrementUp) {
        if (incrementUp) {
            mTrackNowPlaying = (mTrackNowPlaying + 1) % mTrackList.size();
        } else {
            mTrackNowPlaying = (mTrackNowPlaying - 1) < 0 ? mTrackList.size() - 1 : mTrackNowPlaying - 1;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        releaseMediaPlayer(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
