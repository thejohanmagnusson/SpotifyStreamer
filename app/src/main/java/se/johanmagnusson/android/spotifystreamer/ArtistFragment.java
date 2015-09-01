package se.johanmagnusson.android.spotifystreamer;

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
import se.johanmagnusson.android.spotifystreamer.Models.ArtistItem;


public class ArtistFragment extends Fragment {

    private final String LOG_TAG = ArtistFragment.class.getSimpleName();
    private final String LAST_SCROLL_POSITION_KEY = "last_scroll_position";
    private final String ARTIST_KEY = "artist";

    private ListView mListView;
    private List<ArtistItem> mArtists;
    private ArrayAdapter<ArtistItem> mArtistAdapter;
    private int mLastScrollPosition;

    //callback interface to communicate with activities
    public interface Callback {
        public void onArtistSelected(ArtistItem artist);
    }

    public ArtistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check and restore saved data if available
        if(savedInstanceState == null || !savedInstanceState.containsKey(ARTIST_KEY))
            mArtists = new ArrayList<ArtistItem>();
        else
            mArtists = savedInstanceState.getParcelableArrayList(ARTIST_KEY);

        mArtistAdapter = new ArtistAdapter(getActivity(), mArtists);

        //enable menu from this fragment
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artist,container, false);

        //set adapter for list and set listener for item click
        mListView = (ListView) rootView.findViewById(R.id.artist_listview);
        mListView.setAdapter(mArtistAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mLastScrollPosition = position;
                ArtistItem artist = mArtistAdapter.getItem(position);

                //callback to main activity method
                ((Callback) getActivity()).onArtistSelected(artist);
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null && savedInstanceState.containsKey(LAST_SCROLL_POSITION_KEY)) {
            mLastScrollPosition = savedInstanceState.getInt(LAST_SCROLL_POSITION_KEY);
            mListView.smoothScrollToPosition(mLastScrollPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save list of artists
        outState.putParcelableArrayList(ARTIST_KEY, (ArrayList<? extends Parcelable>) mArtists);

        if( mLastScrollPosition != mListView.INVALID_POSITION)
            outState.putInt(LAST_SCROLL_POSITION_KEY, mLastScrollPosition);

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

            mArtistAdapter.clear();

            if(result.size() > 0)
                mArtistAdapter.addAll(result);
            else
                Toast.makeText(getActivity(), "No artist found.", Toast.LENGTH_SHORT).show();
            //todo: change to snackbar
        }
    }
}
