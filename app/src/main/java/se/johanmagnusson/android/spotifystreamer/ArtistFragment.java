package se.johanmagnusson.android.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class ArtistFragment extends Fragment {

    private final String LOG_TAG = ArtistFragment.class.getSimpleName();
    private ArrayAdapter<String> artistAdapter;

    public ArtistFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artist,container, false);

        //todo: replace dummy data with data from API call, change dummyArtists in adapter to new ArrayList<String>()
        ArrayList<String> dummyArtists = new ArrayList<String>();

        dummyArtists.add("Artist 0");
        dummyArtists.add("Artist 1");
        dummyArtists.add("Artist 2");
        dummyArtists.add("Artist 3");
        dummyArtists.add("Artist 4");
        dummyArtists.add("Artist 5");
        dummyArtists.add("Artist 6");
        dummyArtists.add("Artist 7");
        dummyArtists.add("Artist 8");
        dummyArtists.add("Artist 9");

        //set views for the adapter
        artistAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_artist, R.id.list_item_artist_name, dummyArtists);

        //set adapter for list and set listener for item click
        ListView listView = (ListView) rootView.findViewById(R.id.artist_listview);
        listView.setAdapter(artistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String artist = artistAdapter.getItem(position);

                //todo: remove before release
                Log.d(LOG_TAG, "Artist item " + String.valueOf(position) + "clicked.");

                //todo add .putExtra() with artist data to intent
                Intent topTracksIntent = new Intent(getActivity(), TopTracksActivity.class);
                startActivity(topTracksIntent);
            }
        });

        return rootView;
    }
}
