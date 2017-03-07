package com.liamd.giggity_app;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigResultsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener
{
    // Declare Map/Location specific
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Double mLatitude;
    private Double mLongitude;
    private LatLng mLocation;
    private Boolean mLocationType;

    private Marker mMarker;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DataSnapshot mGigDataSnapshot;
    private DataSnapshot mVenueDataSnapshot;

    // Declare general variables
    private ArrayList<Gig> mListOfGigs = new ArrayList<>();
    private ArrayList<Venue> mListOfVenues = new ArrayList<>();
    private ArrayList<Marker> mListOfMarkers = new ArrayList<>();

    private String mGigId;
    private String mGigName;

    public MusicianUserGigResultsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
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

        mGoogleMap.setOnInfoWindowClickListener(this);

        // Once the map is ready, it can be set up using SetupMap()
        SetupMap();
    }

    // This initialises the map and takes a data snapshot from both the Gigs and Venues sections
    // of the database which are then stored in global variables so they can be accessed throughout
    // the class
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

                // Once the two snapshots have been taken, the gig markers are added
                // to the map using AddGigMarkers, taking the two snapshots as parameters
                AddGigMarkers(mGigDataSnapshot, mVenueDataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // This zooms the map in to a reasonable level (12) and centers it on the location provided
        float zoomLevel = 12;
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, zoomLevel));
    }

    // This method iterates through the two snapshots to add markers for each gig at the relevant
    // location on the map
    private void AddGigMarkers(DataSnapshot gigsSnapshot, DataSnapshot venuesSnapshot)
    {
        int i;

        // This iterates through the venues and adds them to a list (mListOfVenues)
        Iterable<DataSnapshot> venueChildren = venuesSnapshot.getChildren();
        for (DataSnapshot child : venueChildren)
        {
            Venue venue;
            venue = child.getValue(Venue.class);
            mListOfVenues.add(venue);
        }

        // Each gig is then iterated through and added to a list of gigs (mListOfVenueGigs)
        Iterable<DataSnapshot> gigChildren = gigsSnapshot.getChildren();
        for (DataSnapshot child : gigChildren)
        {
            Gig gig;
            gig = child.getValue(Gig.class);
            gig.setGigId(child.getKey());
            mListOfGigs.add(gig);
        }

        // This then iterates through the list of gigs, and obtains the venue ID's of
        // each gig.
        for(i = 0; i < mListOfGigs.size(); i++)
        {
            String venueId;
            venueId = mListOfGigs.get(i).getVenueID();

            //mGigId = mListOfGigs.get(i).getGigId();
           // mGigName = mListOfGigs.get(i).getTitle();

            // It then iterates through the list of venues to check for a match.
            for(int j = 0; j < mListOfVenues.size(); j++)
            {
                if(mListOfVenues.get(j).getVenueID().equals(venueId))
                {
                    // Once a match is found, a gigLocation object is created which determines the
                    // placement on the map of the marker.
                    com.liamd.giggity_app.LatLng gigLocation = mListOfVenues.get(j).getVenueLocation();

                    // To expose the methods required for marker placement, the gigLocation
                    // variable is then converted back into the standard Google Maps
                    // LatLng object (convertedGigLocation)
                    com.google.android.gms.maps.model.LatLng convertedGigLocation =
                            new com.google.android.gms.maps.model.LatLng(gigLocation.getLatitude(),
                                    gigLocation.getLongitude());

                    mMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(convertedGigLocation)
                            .title(mListOfVenues.get(j).getName())
                            .snippet(mListOfGigs.get(i).getTitle()));

                    mListOfMarkers.add(mMarker);

                    /*mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
                    {
                        @Override
                        public View getInfoWindow(Marker arg0)
                        {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker arg0)
                        {
                            // Getting view from the layout file info_window_layout
                            View v = getActivity().getLayoutInflater().inflate(R.layout.gigwindowlayout, null);

                            // Getting reference to the TextView to set latitude
                            TextView mGigIdTextView = (TextView) v.findViewById(R.id.gigIdTextView);

                            TextView mGigNameTextView = (TextView) v.findViewById(R.id.gigNameTextView);

                            TextView mGigStartDateTextView = (TextView) v.findViewById(R.id.gigStartDateTextView);

                            TextView mGigFinishDateTextView = (TextView) v.findViewById(R.id.gigFinishDateTextView);

                            mGigIdTextView.setText(mGigId);

                            mGigNameTextView.setText(mGigName);

                            // Returning the view containing InfoWindow contents
                            return v;
                        }
                    });
                    */
                }
            }
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker)
    {
        // This then stores the id of the selected gig in a bundle which is then
        // passed to the result fragment to display the gig details
        MusicianUserGigDetailsFragment fragment = new MusicianUserGigDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putString("GigTitle", marker.getTitle());
        arguments.putString("GigTitle", marker.getTitle());
        fragment.setArguments(arguments);

        // Creates a new fragment transaction to display the details of the selected
        // gig. Some custom animation has been added also.
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserGigDetailsFragment")
                .addToBackStack(null).commit();
    }
}
