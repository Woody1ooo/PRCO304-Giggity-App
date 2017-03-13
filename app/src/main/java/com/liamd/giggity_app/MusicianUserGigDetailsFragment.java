package com.liamd.giggity_app;


import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigDetailsFragment extends Fragment
{

    private TextView mGigIdTextView;
    private TextView mGigNameTextView;

    public MusicianUserGigDetailsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_gig_details, container, false);

        mGigIdTextView = (TextView) fragmentView.findViewById(R.id.gigIdTextView);
        mGigNameTextView = (TextView) fragmentView.findViewById(R.id.gigNameTextView);

        mGigIdTextView.setText(getArguments().getString("GigId"));
        mGigNameTextView.setText(getArguments().getString("GigName"));

        return fragmentView;
    }

}
