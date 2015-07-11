package se.johanmagnusson.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;
import se.johanmagnusson.android.spotifystreamer.Common.Parameter;
import se.johanmagnusson.android.spotifystreamer.Models.ArtistItem;


public class ArtistFragment extends Fragment {

    private final String LOG_TAG = ArtistFragment.class.getSimpleName();
    private final String ARTIST_KEY = "artist";

    private List<ArtistItem> artists;
    private ArrayAdapter<ArtistItem> artistAdapter;

    public ArtistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check and restore data if available
        if(savedInstanceState == null || !savedInstanceState.containsKey(ARTIST_KEY))
            artists = new ArrayList<ArtistItem>();
        else
            artists = savedInstanceState.getParcelableArrayList(ARTIST_KEY);

        artistAdapter = new ArtistAdapter(getActivity(), artists);

        //enable menu from this fragment
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artist,container, false);

        //set adapter for list and set listener for item click
        ListView listView = (ListView) rootView.findViewById(R.id.artist_listview);
        listView.setAdapter(artistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ArtistItem artist = artistAdapter.getItem(position);

                //make intent for top tracks
                Intent topTracksIntent = new Intent(getActivity(), TopTracksActivity.class);
                topTracksIntent.putExtra(Parameter.ARTIST_ID, artist.id);
                topTracksIntent.putExtra(Parameter.ARTIST_NAME, artist.name);
                startActivity(topTracksIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelableArrayList(ARTIST_KEY, (ArrayList<? extends Parcelable>) artists);

        super.onSaveInstanceState(outState);
    }

    public void searchArtist(String artist){

        new SearchArtistTask().execute(artist.trim());
    }


    public class SearchArtistTask extends AsyncTask<String, Void, List<ArtistItem>>{

        private final String LOG_TAG = SearchArtistTask.class.getSimpleName();

        @Override
        protected List<ArtistItem> doInBackground(String... parameter) {

            if (parameter.length == 0)
                return null;

            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotify = spotifyApi.getService();

            List<ArtistItem> artists = new ArrayList<ArtistItem>();

            try {
                ArtistsPager result = spotify.searchArtists(parameter[0]);

                for (Artist artist : result.artists.items) {

                    artists.add(new ArtistItem(artist.id, artist.name, artist.images.size() > 1 ? artist.images.get(artist.images.size() - 2).url : null));
                }
            }
            catch (RetrofitError error){
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);

                if(spotifyError.hasErrorDetails())
                    Log.e(LOG_TAG, spotifyError.toString());
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
