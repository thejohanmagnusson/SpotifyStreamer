package se.johanmagnusson.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import se.johanmagnusson.android.spotifystreamer.Models.ArtistItem;


public class ArtistFragment extends Fragment {

    private final String LOG_TAG = ArtistFragment.class.getSimpleName();
    private ArrayAdapter<ArtistItem> artistAdapter;

    public ArtistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //enable menu from this fragment
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artist,container, false);

        List<ArtistItem> artists = new ArrayList<ArtistItem>();

        artistAdapter = new ArtistAdapter(getActivity(), artists);

        //set adapter for list and set listener for item click
        ListView listView = (ListView) rootView.findViewById(R.id.artist_listview);
        listView.setAdapter(artistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ArtistItem artist = artistAdapter.getItem(position);

                Intent topTracksIntent = new Intent(getActivity(), TopTracksActivity.class);
                topTracksIntent.putExtra(Intent.EXTRA_TEXT, artist.id);
                startActivity(topTracksIntent);
            }
        });

        return rootView;
    }

    public void searchArtist(String artist){

        new SearchArtistTask().execute(artist);
    }

    public class SearchArtistTask extends AsyncTask<String, Void, List<ArtistItem>>{

        private final String LOG_TAG = SearchArtistTask.class.getSimpleName();

        @Override
        protected List<ArtistItem> doInBackground(String... parameter) {

            if (parameter.length == 0)
                return null;

            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotify = spotifyApi.getService();

            ArtistsPager result = spotify.searchArtists(parameter[0]);

            List<ArtistItem> artists = new ArrayList<ArtistItem>();

            for (Artist artist : result.artists.items) {

                artists.add(new ArtistItem(artist.id, artist.name, artist.images.size() > 0 ? artist.images.get(artist.images.size()-1).url : null));
            }

            return artists;
        }

        @Override
        protected void onPostExecute(List<ArtistItem> result){

            artistAdapter.clear();

            if(result.size() > 0)
                artistAdapter.addAll(result);
            else
                Toast.makeText(getActivity(), "No artist found.", Toast.LENGTH_SHORT).show();

        }
    }
}
