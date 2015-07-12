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

        View rootView = convertView;

        //check if reuse of view
        if(rootView == null){
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.artist_item, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.artistIcon = (ImageView) rootView.findViewById(R.id.artist_item_image);
            viewHolder.artistName = (TextView) rootView.findViewById(R.id.artist_item_name);
            rootView.setTag(viewHolder);
        }

        //load data
        ViewHolder viewHolder  = (ViewHolder) rootView.getTag();

        Picasso.with(getContext())
                .load(artist.imageUrl)
                .error(R.drawable.default_list_icon)    //use default image on error
                .fit()
                .centerCrop()
                .into(viewHolder.artistIcon);

        viewHolder.artistName.setText(artist.name);

        return rootView;
    }


    private static class ViewHolder{
        public ImageView artistIcon;
        public TextView artistName;
    }
}
