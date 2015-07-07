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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;


public class TopTracksFragment extends Fragment {

    private final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    private ArrayAdapter<TrackItem> trackAdapter;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String artistId = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (artistId != null)
                getTopTracks(artistId);
        }

        List<TrackItem> topTracks = new ArrayList<TrackItem>();

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

    public void getTopTracks(String artistId) {
        new GetTopTracksTask().execute(artistId);
    }

    public class GetTopTracksTask extends AsyncTask<String, Void, List<TrackItem>> {

        public static final String COUNTRY = "country";

        private final String LOG_TAG = GetTopTracksTask.class.getSimpleName();

        @Override
        protected List<TrackItem> doInBackground(String... parameter) {

            if (parameter.length == 0)
                return null;

            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotify = spotifyApi.getService();

            final Map<String, Object> options = new HashMap<String, Object>();
            options.put(COUNTRY, "SE");

            Tracks result = spotify.getArtistTopTrack(parameter[0], options);

            List<TrackItem> tracks = new ArrayList<TrackItem>();

            for (Track track : result.tracks) {

                tracks.add(new TrackItem(
                        track.name,
                        track.album.name,
                        track.album.images.size() > 0 ? track.album.images.get(track.album.images.size() - 1).url : null,
                        track.album.images.size() > 0 ? track.album.images.get(0).url : null,
                        track.preview_url));

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
