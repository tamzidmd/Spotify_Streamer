package com.tamzid.android.spotifystreamer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

/** Auto-play through a list of passed tracks.*/
public class PlayerDialogFragment extends DialogFragment {
    private static final String LOG_TAG = PlayerDialogFragment.class.getSimpleName();

    // Save instance state
    private static final String SAVESTATE_TRACK_NOW_PLAYING = "savestateTrackNowPlaying";
    public static final String SAVESTATE_TRACKLIST = "savestateTrackList";
    public static final String SAVESTATE_IS_MEDIA_PLAYER_SERVICE_BOUND = "saveStateIsMediaPlayerServiceBound";

    // Fragment initialization parameters
    private static final String ARG_TRACKLIST = "tracklist";
    private static final String ARG_TRACK_NOW_PLAYING = "trackNowPlaying";

    // Utilities
    private List<TrackBundle> mTrackList;
    private int mTrackNowPlaying;
    private MediaPlayerService mMediaPlayerService;
    private Intent mPlayMusicIntent;
    private boolean mIsMediaPlayerServiceBound = false;

    private ServiceConnection mMediaPlayerServiceConnection;

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
                    int vibrantDark = palette.getDarkVibrantColor(getResources().getColor(R.color.background_material_dark));
                    mBackgroundLinearLayout.setBackgroundColor(vibrantDark);
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
            mIsMediaPlayerServiceBound = savedInstanceState.getBoolean(SAVESTATE_IS_MEDIA_PLAYER_SERVICE_BOUND);
        } else if (getArguments() != null) {
            // Otherwise, get it from the argument bundle
            mTrackList = getArguments().getParcelableArrayList(ARG_TRACKLIST);
            mTrackNowPlaying = getArguments().getInt(ARG_TRACK_NOW_PLAYING);
            Log.d(LOG_TAG, "Playing: " + mTrackNowPlaying);

            for (TrackBundle trackBundle : mTrackList) {
                Log.d(LOG_TAG, "Contains: " + trackBundle.name);
            }
        }

        if (mPlayMusicIntent == null && !mIsMediaPlayerServiceBound) {
            Log.d(LOG_TAG, "mPlayMusicIntent re-initialized");
            mPlayMusicIntent = new Intent(getActivity().getApplicationContext(), MediaPlayerService.class);

            mMediaPlayerServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                    mMediaPlayerService = binder.getService();
                    mMediaPlayerService.mTrackList = mTrackList;
                    mMediaPlayerService.mTrackNowPlaying = mTrackNowPlaying;
                    mIsMediaPlayerServiceBound = true;
                    mMediaPlayerService.playMusic();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mIsMediaPlayerServiceBound = false;
                }
            };

            getActivity().getApplicationContext().bindService(mPlayMusicIntent, mMediaPlayerServiceConnection, Context.BIND_AUTO_CREATE);
            getActivity().getApplicationContext().startService(mPlayMusicIntent);
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

            }
        });

        mPlayPauseImageButton = (ImageButton) v.findViewById(R.id.fragment_player_playpause_imagebutton);
        mPlayPauseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        ImageButton nextImageButton = (ImageButton) v.findViewById(R.id.fragment_player_next_imagebutton);
        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVESTATE_TRACKLIST, (ArrayList<TrackBundle>) mTrackList);
        outState.putInt(SAVESTATE_TRACK_NOW_PLAYING, mTrackNowPlaying);
        outState.putBoolean(SAVESTATE_IS_MEDIA_PLAYER_SERVICE_BOUND, mIsMediaPlayerServiceBound);
        Debug.logD(LOG_TAG, "saved state");
    }

    @Override
    public void onPause() {
        Picasso.with(getActivity()).cancelRequest(mTarget);
        super.onPause();
    }

    @Override
    public void onDestroy() {
//        getActivity().stopService(mPlayMusicIntent);
//        mMediaPlayerService = null;
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

        mElapsedTimeTextView.setText(MediaPlayerUtilities.formatMillisToString(0));
        mDurationTextView.setText(MediaPlayerUtilities.formatMillisToString(0));
    }

    private void displayToast(String displayText) {
        Toast.makeText(getActivity(), displayText, Toast.LENGTH_SHORT).show();
    }

//    private void setPlayPauseIcon() {
//        if (mMediaPlayer.isPlaying()) {
//            mPlayPauseImageButton.setImageResource(android.R.drawable.ic_media_pause);
//        } else {
//            mPlayPauseImageButton.setImageResource(android.R.drawable.ic_media_play);
//        }
//    }
//
//    private void setMaxDuration(int millis) {
//        mSeekBar.setMax(millis);
//        mDurationTextView.setText(MediaPlayerUtilities.formatMillisToString(millis));
//    }
//
//    /** Set up media player, prepare, and start playing the song as soon as it's ready. */
//    private void playCurrentTrack() {
//        String songUrl = getSongUrl();
//        mMediaPlayer = setupMediaPlayer(songUrl);
//        prepareMediaPlayer(mMediaPlayer, mStartPlayerListener, mOnCompletionListener);
//    }
//
//    private void pauseOrPlayCurrentTrack() {
//        if (mMediaPlayer == null) {
//            return;
//        }
//
//        if (mMediaPlayer.isPlaying()) {
//            mMediaPlayer.pause();
//        } else {
//            mMediaPlayer.start();
//        }
//
//        setPlayPauseIcon();
//    }
//
//    private MediaPlayer.OnPreparedListener mStartPlayerListener = new MediaPlayer.OnPreparedListener() {
//        @Override
//        public void onPrepared(MediaPlayer mp) {
//            mp.start();
//            setPlayPauseIcon();
//            setMaxDuration(mp.getDuration());
//            mHandler.postDelayed(mUpdateSeekBar, 100);
//
//            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                @Override
//                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//                }
//
//                @Override
//                public void onStartTrackingTouch(SeekBar seekBar) {
//
//                }
//
//                @Override
//                public void onStopTrackingTouch(SeekBar seekBar) {
//                    mMediaPlayer.seekTo(seekBar.getProgress());
//                }
//            });
//        }
//    };
//
//    private Runnable mUpdateSeekBar = new Runnable() {
//        @Override
//        public void run() {
//            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
//                int currentPosition = mMediaPlayer.getCurrentPosition();
//                mSeekBar.setProgress(currentPosition);
//                mElapsedTimeTextView.setText(MediaPlayerUtilities.formatMillisToString(currentPosition));
//            }
//            mHandler.postDelayed(this, 100);
//        }
//    };

    //endregion

}
