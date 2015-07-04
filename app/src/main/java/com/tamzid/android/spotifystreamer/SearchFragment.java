package com.tamzid.android.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.client.Response;


/** Searches for an artist on Spotify and returns a selected artist's name and id to host Activity */
public class SearchFragment extends Fragment {
    private static final String LOG_TAG = SearchFragment.class.getSimpleName();

    // Bundle args
    private static final String BUNDLE_SEARCH_TEXT = "com.tamzid.android.spotifystreamer.searchText";

    // Views
    private ListView mListView;
    private EditText mArtistSearchEditText;

    // Utilities
    private SpotifyWrapperArtistAdapter mArtistAdapter;
    private List<Artist> mArtists = new ArrayList<>();

    private OnArtistSelectedListener mListener;

    public interface OnArtistSelectedListener {
        void onArtistSelected(String artistName, String artistId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mArtistSearchEditText != null) {
            outState.putString(BUNDLE_SEARCH_TEXT, mArtistSearchEditText.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        mArtistSearchEditText = (EditText) v.findViewById(R.id.artist_search_edittext);
        mArtistSearchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mArtistSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event == null) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        searchArtist(v.getText().toString());
                        mArtistSearchEditText.clearFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    } else {
                        return false;
                    }
                }

                return true;
            }
        });

        mListView = (ListView) v.findViewById(R.id.artist_results_listview);
        mListView.setEmptyView(v.findViewById(R.id.empty_search_textview));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectArtist(mArtistAdapter.getItem(position).name, mArtistAdapter.getItem(position).id);
            }
        });

        if (savedInstanceState != null) {
            mArtistSearchEditText.setText(savedInstanceState.getString(BUNDLE_SEARCH_TEXT));
        }

        if (!mArtists.isEmpty()) {
            showArtists(mArtists);
        }

        return v;
    }

    private void searchArtist(String artistQuery) {
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotifyService = api.getService();
        spotifyService.searchArtists(artistQuery, new SpotifyCallback<ArtistsPager>() {
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
            public void success(ArtistsPager artistsPager, Response response) {
                Log.v(LOG_TAG, "Connection success");
                mArtists = artistsPager.artists.items;
                if (mArtists.isEmpty()) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "No results found, try refining the search terms.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showArtists(mArtists);
                        }
                    });


                }
            }
        });
    }

    private void showArtists(List<Artist> artists) {
        mArtistAdapter = new SpotifyWrapperArtistAdapter(getActivity(), R.layout.list_item_artist, artists);
        mListView.setAdapter(mArtistAdapter);
    }

    private void selectArtist(String artistName, String artistId) {
        if (mListener != null) {
            mListener.onArtistSelected(artistName, artistId);
        }
    }

    private class SpotifyWrapperArtistAdapter extends ArrayAdapter<Artist> {
        public SpotifyWrapperArtistAdapter(Context context, int resource, List<Artist> artists) {
            super(context, resource, artists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Artist artist = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
            }

            TextView artistName = (TextView) convertView.findViewById(R.id.artist_name_textview);
            ImageView artistImage = (ImageView) convertView.findViewById(R.id.artist_image_imageview);

            artistName.setText(artist.name);

            if (artist.images.size() > 0) {
                // The last image in the array is always the smallest at 64x64 but it's too blurry,
                // get one before that to avoid making too many calculations searching for the perfect
                // size, but still better than downloading the largest images.
                int smallestImage = artist.images.size() - 2;
                Picasso.with(getContext()).load(artist.images.get(smallestImage).url).placeholder(R.drawable.loading_image).error(R.drawable.no_image_available).into(artistImage);
            } else {
                Picasso.with(getContext()).load(R.drawable.no_image_available).into(artistImage);
            }

            return convertView;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnArtistSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArtistSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
