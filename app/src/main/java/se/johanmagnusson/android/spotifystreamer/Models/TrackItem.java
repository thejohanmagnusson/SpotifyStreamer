package se.johanmagnusson.android.spotifystreamer.Models;


import android.os.Parcel;
import android.os.Parcelable;

public class TrackItem implements Parcelable {

    public String name;
    public String album;
    public String imageUrlSmall;
    public String imageUrlLarge;
    public String previewUrl;

    public  TrackItem(String name, String album, String imageUrlSmall, String imageUrlLarge, String previewUrl){
        this.name = name;
        this.album = album;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrlLarge = imageUrlLarge;
        this.previewUrl = previewUrl;
    }

    protected TrackItem(Parcel in) {
        name = in.readString();
        album = in.readString();
        imageUrlSmall = in.readString();
        imageUrlLarge = in.readString();
        previewUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(album);
        dest.writeString(imageUrlSmall);
        dest.writeString(imageUrlLarge);
        dest.writeString(previewUrl);
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
