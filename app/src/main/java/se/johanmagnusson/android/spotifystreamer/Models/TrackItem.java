package se.johanmagnusson.android.spotifystreamer.Models;


import android.os.Parcel;
import android.os.Parcelable;

public class TrackItem implements Parcelable {

    public String name;
    public String artist;
    public String album;
    public String imageUrlSmall;
    public String imageUrlLarge;
    public long duration;
    public String previewUrl;
    public String shareUrl;

    public  TrackItem(String name, String artist, String album, String imageUrlSmall, String imageUrlLarge, long duration, String previewUrl, String shareUrl){
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrlLarge = imageUrlLarge;
        this.duration = duration;
        this.previewUrl = previewUrl;
        this.shareUrl = shareUrl;
    }

    protected TrackItem(Parcel in) {
        name = in.readString();
        artist = in.readString();
        album = in.readString();
        imageUrlSmall = in.readString();
        imageUrlLarge = in.readString();
        duration = in.readLong();
        previewUrl = in.readString();
        shareUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(imageUrlSmall);
        dest.writeString(imageUrlLarge);
        dest.writeLong(duration);
        dest.writeString(previewUrl);
        dest.writeString(shareUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TrackItem> CREATOR = new Parcelable.Creator<TrackItem>() {
        @Override
        public TrackItem createFromParcel(Parcel in) {
            return new TrackItem(in);
        }

        @Override
        public TrackItem[] newArray(int size) {
            return new TrackItem[size];
        }
    };
}
