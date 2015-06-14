package com.tamzid.android.spotifystreamer;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class SearchActivity extends AppCompatActivity
        implements SearchActivityFragment.OnArtistSelectedListener, TopTracksFragment.OnSongSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (findViewById(R.id.fragmentContainer) != null) {
            if (savedInstanceState != null) {
                return;
            }

            SearchActivityFragment searchActivityFragment = new SearchActivityFragment();

            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, searchActivityFragment)
                    .commit();

        }

    }

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
    public void onSongSelected(Uri uri) {

    }

    @Override
    public void onBackPressed() {
        // When user presses back button on utility bar\
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

}
