package com.liamd.giggity_app;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
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
public class VenueUserBandFinderFragment extends Fragment
{
    // Declare visual components
    private SeekBar mDistanceSeekbar;
    private TextView mDistanceTextView;
    private MultiSelectSpinner mGenreSelectSpinner;
    private Button mSearchButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare general variables
    private int mDistanceSelected;
    private List<String> mGenreList;
    private com.google.android.gms.maps.model.LatLng mVenueLocation;
    private String mVenueId;
    private String mGigId;

    public VenueUserBandFinderFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.venue_user_fragment_band_finder, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        mDistanceSeekbar = (SeekBar) fragmentView.findViewById(R.id.distanceSeekBar);
        mDistanceTextView = (TextView) fragmentView.findViewById(R.id.distanceValueTextView);
        mGenreSelectSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);
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

        // Get passed data from previous fragment
        mGigId = getArguments().getString("GigId");

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This method gets the value from the database of the users set home location
                // and assigns its value to mHomeLocation
                GetVenueLocation(dataSnapshot);

                // This method populates the genre spinners with the genres the user
                // selected when setting up their account
                mGenreSelectSpinner.setSelection(PopulateUserGenreData(dataSnapshot));
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

    private void GetVenueLocation(DataSnapshot dataSnapshot)
    {
        mVenueId = dataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/venueID").getValue().toString();

        String userVenueLat = dataSnapshot.child("Venues/" + mVenueId + "/venueLocation/latitude").getValue().toString();
        String userVenueLng = dataSnapshot.child("Venues/" + mVenueId + "/venueLocation/longitude").getValue().toString();

        String latLng = userVenueLat + "," + userVenueLng;
        List<String> splitUserVenueLocation = Arrays.asList(latLng.split(","));

        double latitude = Double.parseDouble(splitUserVenueLocation.get(0));
        double longitude = Double.parseDouble(splitUserVenueLocation.get(1));

        mVenueLocation = new com.google.android.gms.maps.model.LatLng(latitude, longitude);
    }

    // This method takes a snapshot of the database as a parameter and returns a
    // list of trimmed strings to populate the list of genres that the user selected
    // when they created their account
    private ArrayList<String> PopulateUserGenreData(DataSnapshot dataSnapshot)
    {
        // This takes the list of genres from the database that the user has selected
        // and adds them to a string
        String userPulledGenres = dataSnapshot.child("Venues/" + mVenueId + "/genre").getValue().toString();

        // This then splits this string into an array of strings, each separated by
        // a comma
        List<String> splitUserPulledGenres = Arrays.asList(userPulledGenres.split(","));

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

    private void Search()
    {
        String mGenreListString = mGenreSelectSpinner.getSelectedItemsAsString();

        // This then stores the id of the selected gig in a bundle which is then
        // passed to the result fragment to display the gig details
        VenueUserBandResultsFragment fragment = new VenueUserBandResultsFragment();
        Bundle arguments = new Bundle();

        arguments.putString("GigId", mGigId);
        arguments.putString("Genres", mGenreListString);
        arguments.putDouble("VenueLocationLatitude", mVenueLocation.latitude);
        arguments.putDouble("VenueLocationLongitude", mVenueLocation.longitude);

        arguments.putInt("DistanceSelected", mDistanceSelected);
        fragment.setArguments(arguments);

        // Creates a new fragment transaction to display the details of the selected
        // preferences. Some custom animation has been added also.
        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
        fragmentTransaction.replace(R.id.frame, fragment, "VenueUserBandResultsFragment")
                .addToBackStack(null).commit();
    }
}
