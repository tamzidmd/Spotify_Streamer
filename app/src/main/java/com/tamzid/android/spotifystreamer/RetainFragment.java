package com.tamzid.android.spotifystreamer;

import android.app.Fragment;
import android.os.Bundle;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Tamzid on 15/06/2015.
 */
public class RetainFragment extends Fragment {

    // ArrayList to retain
    private List<Artist> mRetainedArtists;

    // This method is only called once for this fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain this fragment
    }

    public void setData(List<Artist> retainedArtists) {
        mRetainedArtists = retainedArtists;
    }

    public List<Artist> getData() {
        return mRetainedArtists;
    }
}
