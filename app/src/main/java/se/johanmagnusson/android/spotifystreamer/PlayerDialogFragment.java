package se.johanmagnusson.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;


public class PlayerDialogFragment extends DialogFragment {

    static String TRACK_KEY = "track";

    public PlayerDialogFragment(){
        //empty constructor required for DialogFragment
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        TrackItem track;

        //todo: args/intent handling not good. Try remove intent and just use args.
        Bundle args = getArguments();

        if(args != null) {
            track = args.getParcelable(TRACK_KEY);
        }
        else {
            Intent intent = getActivity().getIntent();

            if (intent != null) {
                if(savedInstanceState == null) {
                    track = intent.getParcelableExtra(TRACK_KEY);
                    if(track != null)
                        ;
                }

            }
        }

        View view = inflater.inflate(R.layout.fragment_player, container, false);

        //todo: get view items

        return view;
    }
}
