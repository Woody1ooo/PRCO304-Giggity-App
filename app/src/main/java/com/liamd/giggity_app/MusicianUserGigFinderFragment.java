package com.liamd.giggity_app;


import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.*;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigFinderFragment extends Fragment
{
    // Declare visual components
    private SeekBar mDistanceSeekbar;
    private TextView mDistanceTextView;
    private RadioButton mCurrentLocationRadio;
    private MultiSelectSpinner mGenreSelectSpinner;
    private Button mSearchButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare general variables
    private int mDistanceSelected;
    private List<String> mGenreList;
    private LatLng mHomeLocation;
    private LatLng mCurrentLocation;
    private LocationManager mLocationManager;
    private Location location;

    final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public MusicianUserGigFinderFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_gig_finder, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        mDistanceSeekbar = (SeekBar) fragmentView.findViewById(R.id.distanceSeekBar);
        mDistanceTextView = (TextView) fragmentView.findViewById(R.id.distanceValueTextView);
        mCurrentLocationRadio = (RadioButton) fragmentView.findViewById(R.id.currentLocationRadio);
        mGenreSelectSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);
        mSearchButton = (Button) fragmentView.findViewById(R.id.searchButton);

        // Initialise various variables
        mLocationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);

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

        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This method populates the genre spinners with the genres the user
                // selected when setting up their account
                mGenreSelectSpinner.setSelection(PopulateUserGenreData(dataSnapshot));

                // This method gets the value from the database of the users set home location
                // and assigns its value to mHomeLocation
                GetUserHomeLocation(dataSnapshot);

                // This method gets the users current/last known location
                GetUserCurrentLocation();
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

    // This method calls the getLastKnownLocation method which returns a location object. This
    // is then split into a latitude and longitude to be passed to the maps fragment after search is
    // selected.
    private void GetUserCurrentLocation()
    {
        location = getLastKnownLocation();
        if(location == null)
        {
            Toast.makeText(getActivity(), "Are you sure you have location services enabled?" +
                    " We can't find you!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            mCurrentLocation = new LatLng(latitude, longitude);
        }
    }

    // This method checks to ensure the correct permissions have been given, then returns the
    // location as a Location variable
    private Location getLastKnownLocation()
    {
        mLocationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location closestLocation = null;
        for (String provider : providers)
        {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                // Request permission.
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
            else
            {
                Location location = mLocationManager.getLastKnownLocation(provider);
                if (location == null)
                {
                }
                if (closestLocation == null || location.getAccuracy() < closestLocation.getAccuracy())
                {
                    // Found best last known location: %s", l);
                    closestLocation = location;
                }
            }
        }
        return closestLocation;
    }

    // This method should process the result of the permission selected, though
    // at the moment doesn't get called for some reason

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        {
            if (permissions.length == 1 &&
                    permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                GetUserCurrentLocation();
            }
            else
            {
                // Permission was denied. Display an error message.
            }
        }
    }

    private void GetUserHomeLocation(DataSnapshot dataSnapshot)
    {
        String userHomeLat = dataSnapshot.child(mAuth.getCurrentUser().getUid()
                + "/homeLocation/latitude").getValue().toString();
        String userHomeLng = dataSnapshot.child(mAuth.getCurrentUser().getUid()
                + "/homeLocation/longitude").getValue().toString();

        String latLng = userHomeLat + "," + userHomeLng;
        List<String> splitUserHomeLocation = Arrays.asList(latLng.split(","));

        double latitude = Double.parseDouble(splitUserHomeLocation.get(0));
        double longitude = Double.parseDouble(splitUserHomeLocation.get(1));

        mHomeLocation = new LatLng(latitude, longitude);
    }

    // This method takes a snapshot of the database as a parameter and returns a
    // list of trimmed strings to populate the list of genres that the user selected
    // when they created their account
    private ArrayList<String> PopulateUserGenreData(DataSnapshot dataSnapshot)
    {
        // This takes the list of genres from the database that the user has selected
        // and adds them to a string
        String userPulledGenres = dataSnapshot.child(mAuth.getCurrentUser().getUid()
                + "/genres").getValue().toString();

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
        MusicianUserGigResultsFragment fragment = new MusicianUserGigResultsFragment();
        Bundle arguments = new Bundle();

        arguments.putString("Genres", mGenreListString);

        if(mCurrentLocationRadio.isChecked())
        {
            arguments.putDouble("CurrentLocationLatitude", mCurrentLocation.latitude);
            arguments.putDouble("CurrentLocationLongitude", mCurrentLocation.longitude);
            arguments.putBoolean("CurrentLocation", true);
        }

        else
        {
            arguments.putDouble("HomeLocationLatitude", mHomeLocation.latitude);
            arguments.putDouble("HomeLocationLongitude", mHomeLocation.longitude);
            arguments.putBoolean("CurrentLocation", false);
        }

        arguments.putInt("DistanceSelected", mDistanceSelected);
        fragment.setArguments(arguments);

        // Creates a new fragment transaction to display the details of the selected
        // preferences. Some custom animation has been added also.
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        fragmentTransaction.replace(R.id.frame, fragment, "GigResultsMapFragment")
                .addToBackStack(null).commit();
    }
}
