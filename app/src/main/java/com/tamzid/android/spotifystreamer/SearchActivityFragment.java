package com.tamzid.android.spotifystreamer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchActivityFragment extends Fragment {
    private static final String LOG_TAG = SearchActivityFragment.class.getSimpleName();

    private static final int RETRIEVED_ARTISTS = 1;

    private ListView mListView;
    private SpotifyWrapperArtistAdapter mArtistAdapter;
    private List<Artist> mArtists = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case RETRIEVED_ARTISTS:
                    mArtistAdapter = new SpotifyWrapperArtistAdapter(getActivity(), R.layout.artist_item, (List<Artist>) msg.obj);
                    mListView.setAdapter(mArtistAdapter);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.debug_refresh_listview:

                return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        EditText artistSearchEditText = (EditText) v.findViewById(R.id.artist_search_edittext);
        artistSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchArtist(s.toString());
            }
        });

        mListView = (ListView) v.findViewById(R.id.artist_results_listview);
        //mListView.setOnItemClickListener();

        return v;
    }

    private void searchArtist(String artistQuery) {
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotifyService = api.getService();
        spotifyService.searchArtists(artistQuery, new SpotifyCallback<ArtistsPager>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.v(LOG_TAG, "Failure");
            }

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                Log.v(LOG_TAG, "Success");
                mArtists = artistsPager.artists.items;
                Message completeMessage = mHandler.obtainMessage(RETRIEVED_ARTISTS, mArtists);
                completeMessage.sendToTarget();
            }
        });
    }

    private AdapterView.OnItemClickListener mOpenTop10Listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String artistName = mArtistAdapter.getItem(position).id;

        }
    };


    private class SpotifyWrapperArtistAdapter extends ArrayAdapter<Artist> {
        public SpotifyWrapperArtistAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public SpotifyWrapperArtistAdapter(Context context, int resource, List<Artist> artists) {
            super(context, resource, artists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Artist artist = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist_item, parent, false);
            }

            TextView artistName = (TextView) convertView.findViewById(R.id.artist_name_textview);
            ImageView artistImage = (ImageView) convertView.findViewById(R.id.artist_image_imageview);

            artistName.setText(artist.name);

            if (artist.images.size() > 0) {
                // The last image in the array is always the smallest at 64x64 but it's too blurry,
                // get one before that to avoid making too many calculations searching for the perfect
                // size, but still better than downloading the largest images.
                int smallestImage = artist.images.size() - 2;
                Picasso.with(getContext()).load(artist.images.get(smallestImage).url).placeholder(R.drawable.boom).error(R.drawable.boom).into(artistImage);
            } else {
                Picasso.with(getContext()).load(R.drawable.boom).into(artistImage);
            }

            return convertView;
        }
    }

}
