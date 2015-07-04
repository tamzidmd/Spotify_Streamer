package com.tamzid.android.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * This player UI should display the following information:
 * artist name
 * album name
 * album artwork
 * track name
 * track duration
 */
public class TrackBundle implements Parcelable {
    public List<String> artists;
    public String album;
    public List<String> imageUrls;
    public String name;
    public long duration_ms;

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
        in.readStringList(artists);
        album = in.readString();
        in.readStringList(imageUrls);
        name = in.readString();
        duration_ms = in.readLong();
    }

    public TrackBundle() {
        artists = new ArrayList<>();
        imageUrls = new ArrayList<>();
    }

}
