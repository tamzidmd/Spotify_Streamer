package com.tamzid.android.spotifystreamer;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by Tamzid on 06/06/2015.
 */
public class Album extends ArtistModel {
    public String albumName;
    public Bitmap albumArt;
    public List<TrackModel> mAlbumTrackModels;
}
