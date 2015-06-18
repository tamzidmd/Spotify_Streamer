package com.tamzid.android.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

/** Displays the top 10 tracks of a chosen artist */
public class TopTracksFragment extends Fragment {
    public static final String LOG_TAG = TopTracksFragment.class.getSimpleName();

    // Arguments
    private static final String ARG_ARTIST_NAME = "com.tamzid.android.spotifystreamer.artistName";
    private static final String ARG_ARTIST_ID = "com.tamzid.android.spotifystreamer.artistId";

    // Parameters
    private String mArtistName;
    private String mArtistId;
    public static final String COUNTRY_CODE = "US";

    // Views
    private ListView mListView;

    // Utilities
    private SpotifyWrapperTopTracksAdapter mTopTracksAdapter;
    private List<Track> mTracks = new ArrayList<>();

    private OnSongSelectedListener mListener;

    public interface OnSongSelectedListener {
        // TODO: Update argument type and name
        void onSongSelected(Uri uri);
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
        setRetainInstance(true);
        if (getArguments() != null) {
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
            mArtistId = getArguments().getString(ARG_ARTIST_ID);
            if (getActivity() != null) {
                searchTopTracks(mArtistId);
            }
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

        // Reload the track list if it exists
        if (!mTracks.isEmpty()) {
            showTracks(mTracks);
        }

        return v;
    }

    /** Creates an adapter for found tracks and attaches it to the ListView */
    private void showTracks(List<Track> tracks) {
        mTopTracksAdapter = new SpotifyWrapperTopTracksAdapter(getActivity(), R.layout.artist_item, tracks);
        mListView.setAdapter(mTopTracksAdapter);
    }

    /** Open the song player (placeholder for next project) */
    private void selectSong(Uri uri) {
        if (mListener != null) {
            mListener.onSongSelected(uri);
        }
    }

    /** Searches for the top 10 tracks for the artistId */
    private void searchTopTracks(String artistId) {
        Map<String, Object> options = new HashMap<>();
        options.put("country", COUNTRY_CODE);

        SpotifyApi api = new SpotifyApi();
        SpotifyService service = api.getService();
        service.getArtistTopTrack(artistId, options, new SpotifyCallback<Tracks>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                if (spotifyError.hasErrorDetails()) {
                    Log.v(LOG_TAG, spotifyError.getErrorDetails().message);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Error with internet connection.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void success(Tracks tracks, Response response) {
                Log.v(LOG_TAG, "Succeeded getting Tracks");
                mTracks = tracks.tracks;

                if (mTracks.isEmpty()) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "No top tracks for this artist!", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {

                    if (getActivity() != null) { // Prevent crash on quick exit
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showTracks(mTracks);
                            }
                        });
                    }

                }

            }
        });
    }

    private class SpotifyWrapperTopTracksAdapter extends ArrayAdapter<Track> {
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
                Picasso.with(getContext()).load(track.album.images.get(smallestImage).url).placeholder(R.drawable.loading_image).error(R.drawable.no_image_available).into(albumImage);
            } else {
                Picasso.with(getContext()).load(R.drawable.no_image_available).into(albumImage);
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
