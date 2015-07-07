package se.johanmagnusson.android.spotifystreamer.Models;


public class TrackItem {

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
}
