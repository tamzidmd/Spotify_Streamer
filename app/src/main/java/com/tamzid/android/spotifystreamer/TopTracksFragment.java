package com.tamzid.android.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.client.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnSongSelectedListener} interface
 * to handle interaction events.
 * Use the {@link TopTracksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopTracksFragment extends Fragment {
    public static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    public static final String COUNTRY_CODE = "US";
    public static final int RETRIEVED_TRACKS = 2;

    // Arguments
    private static final String ARG_ARTIST_NAME = "com.tamzid.android.spotifystreamer.artistName";
    private static final String ARG_ARTIST_ID = "com.tamzid.android.spotifystreamer.artistId";

    // Parameters
    private String mArtistName;
    private String mArtistId;

    private ListView mListView;
    private SpotifyWrapperTopTracksAdapter mTopTracksAdapter;
    private List<Track> mTracks = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RETRIEVED_TRACKS:
                    mTopTracksAdapter = new SpotifyWrapperTopTracksAdapter(getActivity(), R.layout.track_item, mTracks);
                    mListView.setAdapter(mTopTracksAdapter);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private OnSongSelectedListener mListener;

    public interface OnSongSelectedListener {
        // TODO: Update argument type and name
        public void onSongSelected(Uri uri);
    }

    public static TopTracksFragment newInstance(String artistName, String artistId) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_NAME, artistName);
        args.putString(ARG_ARTIST_ID, artistId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
            mArtistId = getArguments().getString(ARG_ARTIST_ID);
            searchTopTracks(mArtistId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.top_tracks_title);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(mArtistName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mListView = (ListView) v.findViewById(R.id.track_results_listview);

        return v;
    }

    private void selectSong(Uri uri) {
        if (mListener != null) {
            mListener.onSongSelected(uri);
        }
    }

    private void searchTopTracks(String artistId) {
        Map<String, Object> options = new HashMap<>();
        options.put("country", COUNTRY_CODE);

        SpotifyApi api = new SpotifyApi();
        SpotifyService service = api.getService();
        service.getArtistTopTrack(artistId, options, new SpotifyCallback<Tracks>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.v(LOG_TAG, "Failed to get Tracks");
            }

            @Override
            public void success(Tracks tracks, Response response) {
                Log.v(LOG_TAG, "Succeeded getting Tracks");
                mTracks = tracks.tracks;
                Message completeMessage = mHandler.obtainMessage(RETRIEVED_TRACKS, mTracks);
                completeMessage.sendToTarget();
            }
        });
    }

    private class SpotifyWrapperTopTracksAdapter extends ArrayAdapter<Track> {
        public SpotifyWrapperTopTracksAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public SpotifyWrapperTopTracksAdapter(Context context, int resource, List<Track> tracks) {
            super(context, resource, tracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Track track = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_item, parent, false);
            }

            TextView songName = (TextView) convertView.findViewById(R.id.song_name_textview);
            TextView albumName = (TextView) convertView.findViewById(R.id.album_name_textview);
            ImageView albumImage = (ImageView) convertView.findViewById(R.id.album_image_imageview);

            songName.setText(track.name);
            albumName.setText(track.album.name);

            if (track.album.images.size() > 0) {
                // The last image in the array is always the smallest at 64x64 but it's too blurry,
                // get one before that to avoid making too many calculations searching for the perfect
                // size, but still better than downloading the largest images.
                int smallestImage = track.album.images.size() - 2;
                Picasso.with(getContext()).load(track.album.images.get(smallestImage).url).placeholder(R.drawable.boom).error(R.drawable.boom).into(albumImage);
            } else {
                Picasso.with(getContext()).load(R.drawable.boom).into(albumImage);
            }

            return convertView;
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSongSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSongSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
