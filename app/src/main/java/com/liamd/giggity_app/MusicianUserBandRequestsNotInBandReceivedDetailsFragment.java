package com.liamd.giggity_app;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserBandRequestsNotInBandReceivedDetailsFragment extends Fragment
{
    public MusicianUserBandRequestsNotInBandReceivedDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_requests_not_in_band_received_details, container, false);




        return fragmentView;
    }

}
