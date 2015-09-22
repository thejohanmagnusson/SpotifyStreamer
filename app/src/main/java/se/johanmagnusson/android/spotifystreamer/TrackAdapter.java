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

        View rootView = convertView;

        //check if reuse of view
        if(rootView == null){
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.track_item, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.trackImage = (ImageView) rootView.findViewById(R.id.track_item_image);
            viewHolder.trackName = (TextView) rootView.findViewById(R.id.track_item_name);
            viewHolder.albumName = (TextView) rootView.findViewById(R.id.track_item_album);
            rootView.setTag(viewHolder);
        }

        //load data
        ViewHolder viewHolder = (ViewHolder) rootView.getTag();

        Picasso.with(getContext())
                .load(track.imageUrlSmall)
                .placeholder(R.drawable.ic_music_circle_grey600_48dp)
                .error(R.drawable.ic_music_circle_grey600_48dp)    //use default image on error
                .fit()
                .centerCrop()
                .into(viewHolder.trackImage);

        viewHolder.trackName.setText(track.name);
        viewHolder.albumName.setText(track.album);

        return rootView;
    }

    private static class ViewHolder{
        public ImageView trackImage;
        public TextView trackName;
        public TextView albumName;
    }
}
