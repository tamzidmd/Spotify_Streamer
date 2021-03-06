package com.tamzid.android.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.client.Response;

/** Displays the top 10 tracks of a chosen artist */
public class TopTracksFragment extends Fragment {
    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();

    // Save instance state
    private static final String SAVESTATE_TOPTRACKS = "savestateTopTracks";
    private static final String SAVESTATE_TRACKLIST = "savestateTrackList";

    // Arguments
    private static final String ARG_ARTIST_NAME = "com.tamzid.android.spotifystreamer.artistName";
    private static final String ARG_ARTIST_ID = "com.tamzid.android.spotifystreamer.artistId";

    // Parameters
    private String mArtistName;
    private String mArtistId;
    private static final String COUNTRY_CODE = "US";

    // Views
    private ListView mListView;

    // Utilities
    private SpotifyWrapperTopTracksAdapter mTopTracksAdapter;
    private List<Track> mTracks = new ArrayList<>();
    private List<TrackBundle> mTrackBundle = new ArrayList<>();

    private OnSongSelectedListener mListener;

    public interface OnSongSelectedListener {
        void onSongSelected(List<TrackBundle> trackList, int selectedTrack);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mTrackBundle = savedInstanceState.getParcelableArrayList(SAVESTATE_TRACKLIST);
            showTracksBundle(mTrackBundle);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //setRetainInstance(true);
        if (getArguments() != null && savedInstanceState == null) {
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
            mArtistId = getArguments().getString(ARG_ARTIST_ID);
            if (getActivity() != null) {
                searchTopTracks(mArtistId);
            }
        } else if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            Debug.logD(LOG_TAG, "checked savedInstanceState in onCreateView");
            mTrackBundle = savedInstanceState.getParcelableArrayList(SAVESTATE_TRACKLIST);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Debug.logD(LOG_TAG, "saved state");
        outState.putParcelableArrayList(SAVESTATE_TRACKLIST, (ArrayList<TrackBundle>) mTrackBundle);
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectSong(mTrackBundle, position);
            }
        });

        // Reload the track list if it exists
        if (!mTrackBundle.isEmpty()) {
            showTracksBundle(mTrackBundle);
        }

        return v;
    }

    /** Converts list of {@link Track}s to {@link TrackBundle} and attaches it to the ListView */
    private void showTracks(List<Track> tracks) {
        mTrackBundle = packageTracksIntoParcelable(tracks);
        showTracksBundle(mTrackBundle);
    }

    /** Creates an adapter using the {@link TrackBundle} and sets the adapter */
    private void showTracksBundle(List<TrackBundle> trackBundle) {
        mTopTracksAdapter = new SpotifyWrapperTopTracksAdapter(getActivity(), R.layout.list_item_artist, trackBundle);
        mListView.setAdapter(mTopTracksAdapter);
    }

    /** Open the song player (placeholder for next project) */
    private void selectSong(List<TrackBundle> trackList, int selectedTrack) {
        if (mListener != null) {
            mListener.onSongSelected(trackList, selectedTrack);
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

    private List<TrackBundle> packageTracksIntoParcelable(List<Track> trackList) {
        List<TrackBundle> returnList = new ArrayList<>();

        for (Track track : trackList) {
            TrackBundle trackBundle = new TrackBundle();

            trackBundle.album = track.album.name;
            trackBundle.name = track.name;
            trackBundle.duration_ms = track.duration_ms;
            trackBundle.preview_url = track.preview_url;

            List<ArtistSimple> artists = track.artists;
            for (ArtistSimple artist : artists) {
                trackBundle.artists.add(artist.name);
            }

            List<Image> albumArt = track.album.images;
            for (Image image : albumArt) {
                trackBundle.imageUrls.add(image.url);
            }

            returnList.add(trackBundle);
        }

        return returnList;
    }

    private class SpotifyWrapperTopTracksAdapter extends ArrayAdapter<TrackBundle> {
        public SpotifyWrapperTopTracksAdapter(Context context, int resource, List<TrackBundle> tracks) {
            super(context, resource, tracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TrackBundle track = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
            }

            TextView songName = (TextView) convertView.findViewById(R.id.song_name_textview);
            TextView albumName = (TextView) convertView.findViewById(R.id.album_name_textview);
            ImageView albumImage = (ImageView) convertView.findViewById(R.id.album_image_imageview);

            songName.setText(track.name);
            albumName.setText(track.album);

            if (track.imageUrls.size() > 0) {
                // The last image in the array is always the smallest at 64x64 but it's too blurry,
                // get one before that to avoid making too many calculations searching for the perfect
                // size, but still better than downloading the largest images.
                int smallestImage = track.imageUrls.size() - 2;
                Picasso.with(getContext()).load(track.imageUrls.get(smallestImage)).placeholder(R.drawable.loading_image).error(R.drawable.no_image_available).into(albumImage);
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
