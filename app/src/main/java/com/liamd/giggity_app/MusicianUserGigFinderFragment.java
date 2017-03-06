package com.liamd.giggity_app;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigFinderFragment extends Fragment
{
    // Declare visual components
    private SeekBar mDistanceSeekbar;
    private TextView mDistanceTextView;


    public MusicianUserGigFinderFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView =  inflater.inflate(R.layout.musician_user_fragment_gig_finder, container, false);

        // Initialise visual components
        mDistanceSeekbar = (SeekBar) fragmentView.findViewById(R.id.distanceSeekBar);
        mDistanceTextView = (TextView) fragmentView.findViewById(R.id.distanceValueTextView);

        mDistanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                mDistanceTextView.setText("Distance (km): " + i);

                if(i == 100)
                {
                    mDistanceTextView.setText("Distance (km): National");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });


        return  fragmentView;
    }

}
