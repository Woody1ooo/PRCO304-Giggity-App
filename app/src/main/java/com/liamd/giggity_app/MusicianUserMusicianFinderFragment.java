package com.liamd.giggity_app;


import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserMusicianFinderFragment extends Fragment
{
    // Declare visual components
    private SeekBar mDistanceSeekbar;
    private TextView mDistanceTextView;
    private MultiSelectSpinner mGenreSelectSpinner;
    private MultiSelectSpinner mRoleSelectSpinner;
    private Button mSearchButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare general variables
    private int mDistanceSelected;
    private List<String> mGenreList;
    private List<String> mInstrumentList;
    private com.google.android.gms.maps.model.LatLng mBandLocation;
    private String mBandId;
    private String mBandPosition;
    private String mPositionInstruments;
    private String mBandGenres;

    public MusicianUserMusicianFinderFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_musician_finder, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        mDistanceSeekbar = (SeekBar) fragmentView.findViewById(R.id.distanceSeekBar);
        mDistanceTextView = (TextView) fragmentView.findViewById(R.id.distanceValueTextView);
        mGenreSelectSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);
        mRoleSelectSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.roleSpinner);
        mSearchButton = (Button) fragmentView.findViewById(R.id.searchButton);

        // Add items to the genre list, and set the spinner to use these
        mGenreList = new ArrayList<>();
        mGenreList.add("Classic Rock");
        mGenreList.add("Alternative Rock");
        mGenreList.add("Blues");
        mGenreList.add("Indie");
        mGenreList.add("Metal");
        mGenreList.add("Pop");
        mGenreList.add("Classical");
        mGenreList.add("Jazz");
        mGenreList.add("Acoustic");

        mGenreSelectSpinner.setItems(mGenreList);

        // Add items to the instrument list, and set the spinner to use these
        mInstrumentList = new ArrayList<>();
        mInstrumentList.add("Lead Vocals");
        mInstrumentList.add("Backing Vocals");
        mInstrumentList.add("Lead Guitar");
        mInstrumentList.add("Rhythm Guitar");
        mInstrumentList.add("Acoustic Guitar");
        mInstrumentList.add("Bass Guitar");
        mInstrumentList.add("Drums");
        mInstrumentList.add("Keyboards");
        mInstrumentList.add("Piano");

        mRoleSelectSpinner.setItems(mInstrumentList);

        // Get passed data from previous fragment
        mBandId = getArguments().getString("BandId");
        mBandPosition = getArguments().getString("BandPosition");
        mPositionInstruments = getArguments().getString("PositionInstruments");
        mBandGenres = getArguments().getString("BandGenres");

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This method gets the value from the database of the users set home location
                // and assigns its value to mHomeLocation
                GetBandLocation(dataSnapshot);

                // This method populates the genre spinners with the genres the user
                // selected when setting up their account
                mGenreSelectSpinner.setSelection(PopulateBandGenreData());
                mRoleSelectSpinner.setSelection(PopulateUserInstrumentData());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // This listener gets the selected distance from the seekbar and displays it
        mDistanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                mDistanceSelected = i;

                mDistanceTextView.setText("Distance (km): " + mDistanceSelected);

                if (mDistanceSelected == 100)
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

        mSearchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Search();
            }
        });

        return fragmentView;
    }

    private void GetBandLocation(DataSnapshot dataSnapshot)
    {
        String bandLat = dataSnapshot.child("Bands/" + mBandId + "/baseLocation/latitude").getValue().toString();
        String bandLng = dataSnapshot.child("Bands/" + mBandId + "/baseLocation/longitude").getValue().toString();

        String latLng = bandLat + "," + bandLng;
        List<String> splitBandLocation = Arrays.asList(latLng.split(","));

        double latitude = Double.parseDouble(splitBandLocation.get(0));
        double longitude = Double.parseDouble(splitBandLocation.get(1));

        mBandLocation = new com.google.android.gms.maps.model.LatLng(latitude, longitude);
    }

    // This method takes a snapshot of the database as a parameter and returns a
    // list of trimmed strings to populate the list of genres that the user selected
    // when they created their account
    private ArrayList<String> PopulateBandGenreData()
    {
        // This then splits this string into an array of strings, each separated by
        // a comma
        List<String> splitUserPulledGenres = Arrays.asList(mBandGenres.split(","));

        // For the select list to understand this, they need any leading or trailing
        // spaces to be removed
        ArrayList<String> splitUserPulledGenresFormatted = new ArrayList<>();

        // The string array is then iterated through and added to a separate string
        // array and passed to the spinner.
        for(int i = 0; i < splitUserPulledGenres.size(); i++)
        {
            String formattedGenreStringToAdd;

            formattedGenreStringToAdd = splitUserPulledGenres.get(i).trim();

            splitUserPulledGenresFormatted.add(formattedGenreStringToAdd);
        }

        return splitUserPulledGenresFormatted;
    }

    // This method takes a snapshot of the database as a parameter and returns a
    // list of trimmed strings to populate the list of instruments that the user selected
    // when they created their account
    private ArrayList<String> PopulateUserInstrumentData()
    {
        // This then splits this string into an array of strings, each separated by
        // a comma
        List<String> splitUserPulledInstruments = Arrays.asList(mPositionInstruments.split(","));

        // For the select list to understand this, they need any leading or trailing
        // spaces to be removed
        ArrayList<String> splitUserPulledInstrumentsFormatted = new ArrayList<>();

        // The string array is then iterated through and added to a separate string
        // array and passed to the spinner.
        for (int i = 0; i < splitUserPulledInstruments.size(); i++)
        {
            String formattedGenreStringToAdd;

            formattedGenreStringToAdd = splitUserPulledInstruments.get(i).trim();

            splitUserPulledInstrumentsFormatted.add(formattedGenreStringToAdd);
        }

        return splitUserPulledInstrumentsFormatted;
    }

    private void Search()
    {
        String mGenreListString = mGenreSelectSpinner.getSelectedItemsAsString();

        // This then stores the id of the selected gig in a bundle which is then
        // passed to the result fragment to display the gig details
        MusicianUserMusicianResultsFragment fragment = new MusicianUserMusicianResultsFragment();
        Bundle arguments = new Bundle();

        arguments.putString("BandId", mBandId);
        arguments.putString("Genres", mGenreListString);
        arguments.putString("Instruments", mPositionInstruments);
        arguments.putString("BandPosition", mBandPosition);
        arguments.putDouble("BandLocationLatitude", mBandLocation.latitude);
        arguments.putDouble("BandLocationLongitude", mBandLocation.longitude);

        arguments.putInt("DistanceSelected", mDistanceSelected);
        fragment.setArguments(arguments);

        // Creates a new fragment transaction to display the details of the selected
        // preferences. Some custom animation has been added also.
        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
        fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserMusicianResultsFragment")
                .addToBackStack(null).commit();
    }
}
