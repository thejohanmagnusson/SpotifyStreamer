package se.johanmagnusson.android.spotifystreamer;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import se.johanmagnusson.android.spotifystreamer.Models.ArtistItem;

public class ArtistAdapter extends ArrayAdapter<ArtistItem> {

    public ArtistAdapter(Activity context, List<ArtistItem> artists){
        super(context, 0, artists);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ArtistItem artist = (ArtistItem) getItem(position);

        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.artist_item, parent, false);
        ImageView artistIcon = (ImageView) rootView.findViewById(R.id.artist_item_image);

            Picasso.with(getContext())
                    .load(artist.imageUrl)
                    .error(R.drawable.default_list_icon)    //use default image on error
                    .fit()
                    .centerCrop()
                    .into(artistIcon);

        TextView artistName = (TextView) rootView.findViewById(R.id.artist_item_name);
        artistName.setText(artist.name);

        return rootView;
    }
}
