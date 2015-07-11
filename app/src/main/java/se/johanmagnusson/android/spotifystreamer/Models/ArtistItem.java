package se.johanmagnusson.android.spotifystreamer.Models;


import android.os.Parcel;
import android.os.Parcelable;

public class ArtistItem implements Parcelable{

    public String id;
    public String name;
    public String imageUrl;

    public ArtistItem(String id, String namne, String imageUrl){

        this.id = id;
        this.name = namne;
        this.imageUrl = imageUrl;
    }

    protected ArtistItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ArtistItem> CREATOR = new Parcelable.Creator<ArtistItem>() {
        @Override
        public ArtistItem createFromParcel(Parcel in) {
            return new ArtistItem(in);
        }

        @Override
        public ArtistItem[] newArray(int size) {
            return new ArtistItem[size];
        }
    };
}


