package se.johanmagnusson.android.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;
import se.johanmagnusson.android.spotifystreamer.Common.Parameter;
import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;


public class TopTracksFragment extends Fragment {

    private final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    private final String TRACKS_KEY = "tracks";

    private List<TrackItem> topTracks;
    private ArrayAdapter<TrackItem> trackAdapter;

    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check and restore data if available
        if(savedInstanceState == null || !savedInstanceState.containsKey(TRACKS_KEY))
            topTracks = new ArrayList<TrackItem>();
        else
            topTracks = savedInstanceState.getParcelableArrayList(TRACKS_KEY);

        trackAdapter = new TrackAdapter(getActivity(), topTracks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        Intent intent = getActivity().getIntent();

        if (intent != null) {
            if (intent.hasExtra(Parameter.ARTIST_ID)) {
                getTopTracks(intent.getStringExtra(Parameter.ARTIST_ID));
            }
            if(intent.hasExtra(Parameter.ARTIST_NAME)) {
                setActionBarSubtitle(intent.getStringExtra(Parameter.ARTIST_NAME));
            }
        }

        trackAdapter = new TrackAdapter(getActivity(), topTracks);

        //set adapter for list and set listener for item click
        ListView listView = (ListView) rootView.findViewById(R.id.top_tracks_listview);
        listView.setAdapter(trackAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TrackItem track = trackAdapter.getItem(position);

                //todo: Stage 2, add intent for playing track
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelableArrayList(TRACKS_KEY, (ArrayList<? extends Parcelable>) topTracks);

        super.onSaveInstanceState(outState);
    }

    private void setActionBarSubtitle(String subTitle){
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        if(actionBar != null)
            actionBar.setSubtitle(subTitle);
    }

    public void getTopTracks(String artistId) {
        new GetTopTracksTask().execute(artistId);
    }


    public class GetTopTracksTask extends AsyncTask<String, Void, List<TrackItem>> {

        private static final String COUNTRY = "country";

        private final String LOG_TAG = GetTopTracksTask.class.getSimpleName();

        @Override
        protected List<TrackItem> doInBackground(String... parameter) {

            if (parameter.length == 0)
                return null;

            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotify = spotifyApi.getService();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String countryCode = prefs.getString(getString(R.string.pref_country_key), getString(R.string.pref_country_default));

            final Map<String, Object> options = new HashMap<String, Object>();
            options.put(COUNTRY, countryCode);

            List<TrackItem> tracks = new ArrayList<TrackItem>();

            try {
                Tracks result = spotify.getArtistTopTrack(parameter[0], options);

                for (Track track : result.tracks) {

                    tracks.add(new TrackItem(
                            track.name,
                            track.album.name,
                            track.album.images.size() > 1 ? track.album.images.get(track.album.images.size() - 2).url : null,
                            track.album.images.size() > 0 ? track.album.images.get(0).url : null,
                            track.preview_url));
                }
            }
            catch (RetrofitError error){
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);

                if(spotifyError.hasErrorDetails())
                    Log.e(LOG_TAG, spotifyError.toString());
            }

            return tracks;
        }

        @Override
        protected void onPostExecute(List<TrackItem> result) {

            trackAdapter.clear();

            if (result.size() > 0)
                trackAdapter.addAll(result);
            else
                Toast.makeText(getActivity(), "No top tracks available", Toast.LENGTH_SHORT).show();

        }
    }
}
