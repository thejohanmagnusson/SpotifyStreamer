package se.johanmagnusson.android.spotifystreamer;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import se.johanmagnusson.android.spotifystreamer.Models.ArtistItem;
import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;


public class TopTracksFragment extends Fragment{

    private final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    private final String LAST_SCROLL_POSITION_KEY = "last_scroll_position";
    static String ARTIST_KEY = "artist";
    private final String TRACKS_KEY = "tracks";

    private View mCoordinatorLayoutView;

    private ListView mListView;
    private String mArtist;
    private List<TrackItem> mTopTracks;
    private ArrayAdapter<TrackItem> mTrackAdapter;
    private int mLastScrollPosition;

    //callback for activity
    public interface Callback {
        void onTrackSelected(List<TrackItem> tracks, int position);
    }

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            mTopTracks = new ArrayList<TrackItem>();

            Bundle args = getArguments();

            if(args != null && args.containsKey(ARTIST_KEY)) {
                ArtistItem artist = args.getParcelable(ARTIST_KEY);
                mArtist = artist.name;
                getTopTracks(artist.id);
            }

        }
        else {
            mArtist = savedInstanceState.getString(ARTIST_KEY);
            mTopTracks = savedInstanceState.getParcelableArrayList(TRACKS_KEY);
            mLastScrollPosition = savedInstanceState.getInt(LAST_SCROLL_POSITION_KEY);
        }

        setActionBarSubtitle(mArtist);
        mTrackAdapter = new TrackAdapter(getActivity(), mTopTracks);

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        mCoordinatorLayoutView = rootView.findViewById(R.id.top_tracks_coordinatorlayout);

        //set adapter for list and set listener for item click
        mListView = (ListView) rootView.findViewById(R.id.top_tracks_listview);
        mListView.setAdapter(mTrackAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mLastScrollPosition = position;
                TrackItem track = mTrackAdapter.getItem(position);

                //callback to main or top tracks activity depending if two pane unit
                ((Callback) getActivity()).onTrackSelected(mTopTracks, position);
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //restore scroll position
        if( mLastScrollPosition != mListView.INVALID_POSITION)
            mListView.smoothScrollToPosition(mLastScrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //artist and tracks
        outState.putString(ARTIST_KEY, mArtist);
        outState.putParcelableArrayList(TRACKS_KEY, (ArrayList<? extends Parcelable>) mTopTracks);

        //scroll position
        if( mLastScrollPosition != mListView.INVALID_POSITION)
            outState.putInt(LAST_SCROLL_POSITION_KEY, mLastScrollPosition);

        super.onSaveInstanceState(outState);
    }

    private void setActionBarSubtitle(String subTitle){
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        if(actionBar != null)
            actionBar.setSubtitle(subTitle);
    }

    public void getTopTracks(String artistId) {
        new GetTopTracksTask().execute(artistId);
    }

    public class GetTopTracksTask extends AsyncTask<String, Void, List<TrackItem>> {

        private static final String COUNTRY = "country";
        private static final String SHARE_URL = "spotify";

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
                            track.artists.size() > 0 ? track.artists.get(0).name : "",
                            track.album.name,
                            track.album.images.size() > 1 ? track.album.images.get(track.album.images.size() - 2).url : null,
                            track.album.images.size() > 0 ? track.album.images.get(0).url : null,
                            track.duration_ms,
                            track.preview_url,
                            track.external_urls.containsKey(SHARE_URL) ? track.external_urls.get(SHARE_URL) : "")
                    );
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

            mTrackAdapter.clear();

            if (result.size() > 0)
                mTrackAdapter.addAll(result);
            else
                Snackbar.make(mCoordinatorLayoutView, R.string.search_top_tracks_failed, Snackbar.LENGTH_LONG).show();

        }
    }
}
