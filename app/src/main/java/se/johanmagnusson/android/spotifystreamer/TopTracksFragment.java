package se.johanmagnusson.android.spotifystreamer;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class TopTracksFragment extends Fragment {

    final String LOG_TAG = TopTracksFragment.class.getSimpleName();

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_top_tracks, container, false);
    }
}
