package com.liamd.giggity_app;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigResultsFragment extends Fragment implements OnMapReadyCallback
{
    // Declare Map/Location specific
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Double mLatitude;
    private Double mLongitude;
    private LatLng mLocation;
    private Boolean mLocationType;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DataSnapshot mGigDataSnapshot;
    private DataSnapshot mVenueDataSnapshot;

    // Declare general variables
    private ArrayList<Gig> mListOfGigs = new ArrayList<>();
    private ArrayList<Venue> mListOfVenues = new ArrayList<>();

    public MusicianUserGigResultsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_gig_results, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise variables
        mLocationType = getArguments().getBoolean("CurrentLocation");

        // Determine the type of search the user has carried out based on the boolean above
        if(mLocationType == true)
        {
            mLatitude = getArguments().getDouble("CurrentLocationLatitude");
            mLongitude = getArguments().getDouble("CurrentLocationLongitude");
        }

        else
        {
            mLatitude = getArguments().getDouble("HomeLocationLatitude");
            mLongitude = getArguments().getDouble("HomeLocationLongitude");
        }

        // This creates a location for the current user
        mLocation = new LatLng(mLatitude, mLongitude);

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.googleMap);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        return fragmentView;
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        mGoogleMap = map;
        SetupMap();
    }

    private void SetupMap()
    {
        mDatabase.child("Gigs").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mGigDataSnapshot = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mDatabase.child("Venues").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mVenueDataSnapshot = dataSnapshot;

                AddGigMarkers(mGigDataSnapshot, mVenueDataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        float zoomLevel = 12;
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, zoomLevel));
        mGoogleMap.addMarker(new MarkerOptions().position(mLocation).title("You are here!"));
    }

    private void AddGigMarkers(DataSnapshot gigsSnapshot, DataSnapshot venuesSnapshot)
    {
        // Each gig is then iterated through and added to an
        // array list of gigs (mListOfVenueGigs)
        Iterable<DataSnapshot> gigChildren = gigsSnapshot.getChildren();
        for (DataSnapshot child : gigChildren)
        {
            Gig gig;
            gig = child.getValue(Gig.class);
            gig.setGigId(child.getKey());
            mListOfGigs.add(gig);
        }

        Iterable<DataSnapshot> venueChildren = gigsSnapshot.getChildren();
        for (DataSnapshot child : venueChildren)
        {
            Venue venue;
            venue = child.getValue(Venue.class);
            mListOfVenues.add(venue);
        }
    }
}
