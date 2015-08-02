package com.tamzid.android.spotifystreamer;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

/** Manages fragments for navigating to the top 10 tracks of a user-chosen Spotify artist */
public class MainActivity extends AppCompatActivity
        implements SearchFragment.OnArtistSelectedListener, TopTracksFragment.OnSongSelectedListener,
        PlayerFragment.OnFragmentInteractionListener {

    private static final String TAG_SEARCH_FRAGMENT = "com.tamzid.android.spotifystreamer.searchFragment";
    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Try to retain last fragment used
        if (findViewById(R.id.fragmentContainer) != null) {
            if (savedInstanceState != null) {
                return;
            }

            if (getFragmentManager().findFragmentByTag(TAG_SEARCH_FRAGMENT) != null) {
                return;
            }

            SearchFragment searchFragment = new SearchFragment();

            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, searchFragment, TAG_SEARCH_FRAGMENT)
                    .commit();


        }
    }

    /** Replaces fragment in the container with passed fragment and maintains back-stack */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onArtistSelected(String artistName, String artistId) {
        TopTracksFragment topTracksFragment = TopTracksFragment.newInstance(artistName, artistId);
        replaceFragment(topTracksFragment);
    }

    @Override
    public void onSongSelected(List<TrackBundle> trackList, int selectedTrack) {
        PlayerDialogFragment playerDialogFragment = PlayerDialogFragment.newInstance(trackList, selectedTrack);
        replaceFragment(playerDialogFragment);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
        // When user presses back button on utility bar
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

}
