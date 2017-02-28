package com.liamd.giggity_app;


import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserViewGigDetailsFragment extends Fragment
{
    // Declare visual components
    private EditText mGigNameEditText;
    private Button mSelectStartDateButton;
    private Button mSelectFinishDateButton;
    private TextView mStartDateSelectedTextView;
    private TextView mFinishDateSelectedTextView;
    private Button mSelectStartTimeButton;
    private Button mSelectFinishTimeButton;
    private TextView mStartTimeSelectedTextView;
    private TextView mFinishTimeSelectedTextView;
    private Button mCreateGigButton;

    // Declare general variables
    private String mGigID;
    private String mStartDate;
    private String mFinishDate;
    private Date mParsedStartDate;
    private Date mParsedFinishDate;

    public VenueUserViewGigDetailsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView =  inflater.inflate(R.layout.venue_user_fragment_view_gig_details, container, false);

        // Initialise visual components
        mGigNameEditText = (EditText) fragmentView.findViewById(R.id.gigNameEditText);

        mSelectStartDateButton = (Button) fragmentView.findViewById(R.id.LaunchStartDatePickerButton);
        mSelectFinishDateButton = (Button) fragmentView.findViewById(R.id.LaunchFinishDatePickerButton);

        mStartDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartDateSelectedLabel);
        mFinishDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishDateSelectedLabel);

        mSelectStartTimeButton = (Button) fragmentView.findViewById(R.id.LaunchStartTimePickerButton);
        mSelectFinishTimeButton = (Button) fragmentView.findViewById(R.id.LaunchFinishTimePickerButton);

        mStartTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.StartTimeSelectedLabel);
        mFinishTimeSelectedTextView = (TextView) fragmentView.findViewById(R.id.FinishTimeSelectedLabel);

        mCreateGigButton = (Button) fragmentView.findViewById(R.id.createGigButton);


        // Populate components and variables with the details of the chosen gig
        mGigID = getArguments().getString("GigID");
        mStartDate = getArguments().getString("GigStartDate");
        mFinishDate = getArguments().getString("GigEndDate");

        // This parses the dates from a String back into a Date object
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
        try
        {
            mParsedStartDate = format.parse(mStartDate);
            mParsedFinishDate = format.parse(mFinishDate);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        mGigNameEditText.setText(getArguments().getString("GigTitle"));
        mStartDateSelectedTextView.setText(getArguments().getString("GigStartDate"));
        mFinishDateSelectedTextView.setText(getArguments().getString("GigEndDate"));

        return fragmentView;
    }
}
