package com.tamzid.android.spotifystreamer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Auto-play through a list of passed tracks.*/
public class PlayerDialogFragment extends DialogFragment implements MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = PlayerDialogFragment.class.getSimpleName();

    // Save instance state
    private static final String SAVESTATE_TRACK_NOW_PLAYING = "savestateTrackNowPlaying";
    public static final String SAVESTATE_TRACKLIST = "savestateTrackList";

    // Fragment initialization parameters
    private static final String ARG_TRACKLIST = "tracklist";
    private static final String ARG_TRACK_NOW_PLAYING = "trackNowPlaying";

    // Utilities
    private List<TrackBundle> mTrackList;
    private int mTrackNowPlaying;
    private static MediaPlayer mMediaPlayer;

    // UI
    private LinearLayout mBackgroundLinearLayout;
    private TextView mArtistNameTextView;
    private TextView mAlbumTitleTextView;
    private ImageView mAlbumArtImageView;
    private TextView mTrackTitleTextView;
    private SeekBar mSeekBar;
    private TextView mElapsedTimeTextView;
    private TextView mDurationTextView;
    private ImageButton mPlayPauseImageButton;

    private Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mAlbumArtImageView.setImageBitmap(bitmap);
            Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    int mutedDark = palette.getDarkMutedColor(getResources().getColor(R.color.background_material_dark));
                    int muted = palette.getMutedColor(getResources().getColor(R.color.background_material_dark));
                    int vibrant = palette.getVibrantColor(getResources().getColor(R.color.background_material_dark));
                    int vibrantDark = palette.getDarkVibrantColor(getResources().getColor(R.color.background_material_dark));

                    mBackgroundLinearLayout.setBackgroundColor(vibrantDark);
                    //mSeekBar.setBackgroundColor(vibrant);

                }
            });
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    // Handler for seekbar
    private Handler mHandler = new Handler();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param trackList A list of {@link TrackBundle}s containing relevant track information
     * @param trackNowPlaying Index of currently playing track from {@code trackList}
     * @return A new instance of fragment PlayerDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayerDialogFragment newInstance(List<TrackBundle> trackList, int trackNowPlaying) {
        PlayerDialogFragment fragment = new PlayerDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_TRACKLIST, (ArrayList<TrackBundle>) trackList);
        args.putInt(ARG_TRACK_NOW_PLAYING, trackNowPlaying);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            // If savedInstanceState has data, use it
            Debug.logD(LOG_TAG, "used saveInstanceState");
            mTrackList = savedInstanceState.getParcelableArrayList(SAVESTATE_TRACKLIST);
            mTrackNowPlaying = savedInstanceState.getInt(SAVESTATE_TRACK_NOW_PLAYING);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            // If savedInstanceState has data, use it
            Debug.logD(LOG_TAG, "used saveInstanceState");
            mTrackList = savedInstanceState.getParcelableArrayList(SAVESTATE_TRACKLIST);
            mTrackNowPlaying = savedInstanceState.getInt(SAVESTATE_TRACK_NOW_PLAYING);
        } else if (getArguments() != null) {
            // Otherwise, get it from the argument bundle
            mTrackList = getArguments().getParcelableArrayList(ARG_TRACKLIST);
            mTrackNowPlaying = getArguments().getInt(ARG_TRACK_NOW_PLAYING);
            Log.d(LOG_TAG, "Playing: " + mTrackNowPlaying);

            for (TrackBundle trackBundle : mTrackList) {
                Log.d(LOG_TAG, "Contains: " + trackBundle.name);
            }
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Request no titlebar when using as a dialog
        // http://stackoverflow.com/questions/15277460/how-to-create-a-dialogfragment-without-title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dialog_player, container, false);

        mBackgroundLinearLayout = (LinearLayout) v.findViewById(R.id.fragment_dialog_player_background);
        mArtistNameTextView = (TextView) v.findViewById(R.id.fragment_player_artist_textview);
        mAlbumTitleTextView = (TextView) v.findViewById(R.id.fragment_player_album_textview);
        mAlbumArtImageView = (ImageView) v.findViewById(R.id.fragment_player_albumart_imageview);
        mTrackTitleTextView = (TextView) v.findViewById(R.id.fragment_player_track_textview);
        mSeekBar = (SeekBar) v.findViewById(R.id.fragment_player_seekbar);
        mElapsedTimeTextView = (TextView) v.findViewById(R.id.fragment_player_elapsedtime_textview);
        mDurationTextView = (TextView) v.findViewById(R.id.fragment_player_duration_textview);

        bindView();



        ImageButton previousImageButton = (ImageButton) v.findViewById(R.id.fragment_player_previous_imagebutton);
        previousImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousTrack();
            }
        });

        mPlayPauseImageButton = (ImageButton) v.findViewById(R.id.fragment_player_playpause_imagebutton);
        mPlayPauseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseOrPlayCurrentTrack();

            }
        });

        ImageButton nextImageButton = (ImageButton) v.findViewById(R.id.fragment_player_next_imagebutton);
        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTrack();
            }
        });

        playCurrentTrack();

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVESTATE_TRACKLIST, (ArrayList<TrackBundle>) mTrackList);
        outState.putInt(SAVESTATE_TRACK_NOW_PLAYING, mTrackNowPlaying);
        Debug.logD(LOG_TAG, "saved state");
    }

    @Override
    public void onPause() {
        Picasso.with(getActivity()).cancelRequest(mTarget);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        releaseMediaPlayer(mMediaPlayer);
        super.onDestroy();
    }

    //region LOCAL METHODS=========================================================================

    /** Binds new data to the views */
    private void bindView() {
        TrackBundle trackPlaying = mTrackList.get(mTrackNowPlaying);

        mArtistNameTextView.setText(trackPlaying.artists.get(0));
        mAlbumTitleTextView.setText(trackPlaying.album);
        Picasso.with(getActivity()).load(trackPlaying.imageUrls.get(0)).into(mTarget);
        mTrackTitleTextView.setText(trackPlaying.name);

        mElapsedTimeTextView.setText(formatMillisToString(0));
        mDurationTextView.setText(formatMillisToString(0));
    }

    private void setPlayPauseIcon() {
        if (mMediaPlayer.isPlaying()) {
            mPlayPauseImageButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mPlayPauseImageButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void setMaxDuration(int millis) {
        mSeekBar.setMax(millis);
        mDurationTextView.setText(formatMillisToString(millis));
    }

    /** Set up media player, prepare, and start playing the song as soon as it's ready. */
    private void playCurrentTrack() {
        String songUrl = getSongUrl();
        mMediaPlayer = setupMediaPlayer(songUrl);
        prepareMediaPlayer(mMediaPlayer, mStartPlayerListener, mOnCompletionListener);
    }

    private void pauseOrPlayCurrentTrack() {
        if (mMediaPlayer == null) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }

        setPlayPauseIcon();
    }

    private MediaPlayer.OnPreparedListener mStartPlayerListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            setPlayPauseIcon();
            setMaxDuration(mp.getDuration());
            mHandler.postDelayed(mUpdateSeekBar, 100);

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mMediaPlayer.seekTo(seekBar.getProgress());
                }
            });
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            nextTrack();
        }
    };

    private Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                int currentPosition = mMediaPlayer.getCurrentPosition();
                mSeekBar.setProgress(currentPosition);
                mElapsedTimeTextView.setText(formatMillisToString(currentPosition));
            }
            mHandler.postDelayed(this, 100);
        }
    };

    /** Get Url of the song preview */
    private String getSongUrl() {
        TrackBundle trackPlaying = mTrackList.get(mTrackNowPlaying);
        return trackPlaying.preview_url;
    }

    /** Returns a media player with the data source set */
    private MediaPlayer setupMediaPlayer(String url) {
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "IOException: ", ioe);
        }

        return mediaPlayer;
    }

    /** Asynchronously prepares a media player and attaches a listener for when it is ready */
    private void prepareMediaPlayer(MediaPlayer mediaPlayer, MediaPlayer.OnPreparedListener onPreparedListener, MediaPlayer.OnCompletionListener onCompletionListener) {
        mediaPlayer.prepareAsync();
        // Buffering might cause this method to be slow, do not call on UI thread.
        // Here, prepareAsync() is used and a listener is set for when the data is ready.
        mediaPlayer.setOnPreparedListener(onPreparedListener);
        mediaPlayer.setOnCompletionListener(onCompletionListener);
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
        bindView();
    }

    /** Goes to previous track. Releases media player and plays new track */
    private void previousTrack() {
        releaseMediaPlayer(mMediaPlayer);
        incrementTrack(false);
        bindView();
    }

    /**
     * Increment track count up or down, loop back if end is reached. Releases media player.
     *
     * @param incrementUp Enter <code>true</code> to increment up, <code>false</code> to increment
     *                    down.
     */
    private void incrementTrack(boolean incrementUp) {
        int trackListLastIndex = mTrackList.size() - 1;

        if (incrementUp) {
            mTrackNowPlaying = (mTrackNowPlaying + 1) % mTrackList.size();
        } else {
            mTrackNowPlaying = (mTrackNowPlaying - 1) < 0 ? mTrackList.size() - 1 : mTrackNowPlaying - 1;
        }

        playCurrentTrack();
    }

    private String formatMillisToString(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }

    //endregion

    //region INTERFACE METHODS ===================================================================

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextTrack();
    }
}
