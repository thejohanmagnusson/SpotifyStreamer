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

import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;


public class TrackAdapter extends ArrayAdapter<TrackItem> {

    public TrackAdapter(Activity context, List<TrackItem> tracks){
        super(context, 0, tracks);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TrackItem track = (TrackItem) getItem(position);

        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.track_item, parent, false);
        ImageView trackImage = (ImageView) rootView.findViewById(R.id.track_item_image);

        Picasso.with(getContext())
                .load(track.imageUrlSmall)
                .error(R.drawable.default_list_icon)    //use default image on error
                .fit()
                .centerCrop()
                .into(trackImage);

        TextView trackName = (TextView) rootView.findViewById(R.id.track_item_name);
        trackName.setText(track.name);

        TextView albumName = (TextView) rootView.findViewById(R.id.track_item_album);
        albumName.setText(track.album);

        return rootView;
    }
}
