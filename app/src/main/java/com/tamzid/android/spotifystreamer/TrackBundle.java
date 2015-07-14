package com.tamzid.android.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/** Parcelable object to store data needed by the track player */
public class TrackBundle implements Parcelable {
    public List<String> artists;
    public String album;
    public List<String> imageUrls;
    public String name;
    public long duration_ms;
    public String preview_url;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(artists);
        dest.writeString(album);
        dest.writeStringList(imageUrls);
        dest.writeString(name);
        dest.writeLong(duration_ms);
        dest.writeString(preview_url);
    }

    public static final Parcelable.Creator<TrackBundle> CREATOR = new Parcelable.Creator<TrackBundle>() {
        @Override
        public TrackBundle createFromParcel(Parcel source) {
            return new TrackBundle(source);
        }

        @Override
        public TrackBundle[] newArray(int size) {
            return new TrackBundle[size];
        }
    };

    private TrackBundle(Parcel in) {
        artists = new ArrayList<>();
        in.readStringList(artists);
        album = in.readString();
        imageUrls = new ArrayList<>();
        in.readStringList(imageUrls);
        name = in.readString();
        duration_ms = in.readLong();
        preview_url = in.readString();
    }

    public TrackBundle() {
        artists = new ArrayList<>();
        imageUrls = new ArrayList<>();
    }

}
