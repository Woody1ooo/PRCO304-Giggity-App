package com.liamd.giggity_app;


import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;


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
    private static LatLng mLocation;
    private Boolean mLocationType;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DataSnapshot mGigDataSnapshot;
    private static DataSnapshot mVenueDataSnapshot;

    // Declare general variables
    private ArrayList<Gig> mListOfGigs = new ArrayList<>();
    private ArrayList<Venue> mListOfVenues = new ArrayList<>();
    private ArrayList<GigMarkerInfo> mListOfGigMarkerInfo = new ArrayList<>();
    private ArrayList<Integer> mFilteredGigsToRemove = new ArrayList<>();
    private Marker mMarker;
    private Boolean multipleGigs = false;
    private int mGigDistanceSelected;

    // Declare Visual Components
    private ListView mGigsListView;
    private MusicianUserGigsAdapter adapter;

    // Declare variables to be stored to pass to the next fragment
    private String mGigId;
    private String mGigName;
    private String mVenueName;
    private String mVenueId;
    private Date mGigStartDate;
    private Date mGigFinishDate;

    // Location variables
    private double mDistance;
    private Location mGigLocation;
    private double mGigLocationLat;
    private double mGigLocationLng;
    private Location mUserLocation;

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

        // This initialises the tabs used to hold the different views
        TabHost tabs = (TabHost) fragmentView.findViewById(R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec tabSpec = tabs.newTabSpec("tag1");

        tabSpec.setContent(R.id.ListTab);
        tabSpec.setIndicator("List");
        tabs.addTab(tabSpec);

        tabSpec = tabs.newTabSpec("tag2");
        tabSpec.setContent(R.id.mapTab);
        tabSpec.setIndicator("Map");
        tabs.addTab(tabSpec);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise variables
        mLocationType = getArguments().getBoolean("CurrentLocation");

        // Initialise other variables required
        mGigLocation = new Location("");
        mUserLocation = new Location("");

        // Clears the various lists when this fragment is returned to
        mListOfGigs.clear();
        mListOfVenues.clear();
        mListOfGigMarkerInfo.clear();
        mFilteredGigsToRemove.clear();

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

        // This gets the distance value the user has selected
        mGigDistanceSelected = getArguments().getInt("DistanceSelected");

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.googleMap);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        // Initialise the list view
        mGigsListView = (ListView) fragmentView.findViewById(R.id.gigsListView);

        return fragmentView;
    }

    // When the map is ready, call the SetupMap method
    @Override
    public void onMapReady(GoogleMap map)
    {
        mGoogleMap = map;

        // This places a marker at the users chosen location
        mGoogleMap.addMarker(new MarkerOptions()
                .position(mLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(HUE_AZURE)));

        // This zooms the map in to a reasonable level (12) and centers it on the location provided
        float zoomLevel = 15;
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, zoomLevel));
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
    }

    // This method iterates through the two snapshots to add markers for each gig at the relevant
    // location on the map
    private void AddGigMarkers(DataSnapshot gigsSnapshot, DataSnapshot venuesSnapshot)
    {
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

        // This method is then called to populate the list view once the map view has its markers in place
        PopulateListView();

        // This then iterates through the list of gigs, and obtains the venue ID's of
        // each gig.
        for(int i = 0; i < mListOfGigs.size(); i++)
        {
            mVenueId = mListOfGigs.get(i).getVenueID();

            // The gig information is then extracted each time and the values assigned
            // to these global variables to be accessed later
            mGigId = mListOfGigs.get(i).getGigId();
            mGigName = mListOfGigs.get(i).getTitle();
            mGigStartDate = mListOfGigs.get(i).getStartDate();
            mGigFinishDate = mListOfGigs.get(i).getEndDate();
            mVenueName = mVenueDataSnapshot.child(mVenueId + "/name").getValue().toString();

            // It then iterates through the list of venues to check for a match.
            for(int j = 0; j < mListOfVenues.size(); j++)
            {
                if(mListOfVenues.get(j).getVenueID().equals(mVenueId))
                {
                    // Once a match is found, a gigLocation object is created which determines the
                    // placement on the map of the marker.
                    com.liamd.giggity_app.LatLng gigLocation = mListOfVenues.get(j).getVenueLocation();

                    // To expose the methods required for marker placement, the gigLocation
                    // variable is then converted back into the standard Google Maps
                    // LatLng object (convertedGigLocation)
                    final com.google.android.gms.maps.model.LatLng convertedGigLocation =
                            new com.google.android.gms.maps.model.LatLng(gigLocation.getLatitude(),
                                    gigLocation.getLongitude());

                    // The marker is then added to the map
                    mMarker = mGoogleMap.addMarker(new MarkerOptions().position(convertedGigLocation));


                    // A new GigMarkerInfo object is created to store the information about the marker.
                    // This needs to be done because a standard marker can only hold a title and snippet
                    GigMarkerInfo marker = new GigMarkerInfo(mGigFinishDate, mGigId, mGigName, mGigStartDate,
                            mMarker.getId(), mVenueId, mVenueName);
                    mListOfGigMarkerInfo.add(marker);

                    // This sets the custom window adapter (gig_window_layout)
                    mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
                    {
                        @Override
                        public View getInfoWindow(Marker markerSelected)
                        {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker markerSelected)
                        {
                            // Getting view from the layout file
                            View v = getActivity().getLayoutInflater().inflate(R.layout.gig_window_layout, null);

                            // This then references the text views found in that layout
                            // (note gig id is a hidden text view)
                            TextView mGigIdTextView = (TextView) v.findViewById(R.id.gigIdTextView);
                            TextView mGigNameTextView = (TextView) v.findViewById(R.id.gigNameTextView);
                            TextView mGigStartDateTextView = (TextView) v.findViewById(R.id.gigStartDateTextView);
                            TextView mGigFinishDateTextView = (TextView) v.findViewById(R.id.gigFinishDateTextView);
                            TextView mVenueNameTextView = (TextView) v.findViewById(R.id.venueNameTextView);

                            // The marker info list is then iterated through
                            for (int i = 0; i < mListOfGigMarkerInfo.size(); i++)
                            {
                                // When the selected marker id matches one from the list it means there is a match
                                // and the text fields are updated to reflect this.
                                if (mListOfGigMarkerInfo.get(i).getMarkerId().equals(markerSelected.getId()))
                                {
                                    mGigIdTextView.setText(mListOfGigMarkerInfo.get(i).getGigId());
                                    mGigNameTextView.setText(mListOfGigMarkerInfo.get(i).getGigName());
                                    mGigStartDateTextView.setText(mListOfGigMarkerInfo.get(i).getGigStartDate().toString());
                                    mGigFinishDateTextView.setText(mListOfGigMarkerInfo.get(i).getGigEndDate().toString());
                                    mVenueNameTextView.setText(mListOfGigMarkerInfo.get(i).getVenueName());
                                }

                                // Loop through the marker info to see if any existing markers have the same venue ID
                                // If they do it means there are multiple gigs at the selected venue and the UI should be
                                // updated to accommodate this
                                for (int k = 0; k < mListOfGigMarkerInfo.size(); k++)
                                {
                                    int gigCounter = 0;

                                    // Get the id of the marker selected
                                    String markerSelectedId = markerSelected.getId();

                                    // If the marker id from the list matches the selected one
                                    // extract its venue Id so we know which venue to check for
                                    // multiple gigs at.
                                    if (mListOfGigMarkerInfo.get(k).getMarkerId().equals(markerSelectedId))
                                    {
                                        String venueId = mListOfGigMarkerInfo.get(k).getVenueId();

                                        // With the venue id, we can now go through the list and find any gigs
                                        // at that venue id. If we find more than one, it means that there are
                                        // multiple gigs at that venue, therefore the UI needs to reflect that.
                                        for(int l = 0; l < mListOfGigMarkerInfo.size(); l++)
                                        {
                                            // If the list contains a gig at the venue selected by the user
                                            // increment the gigCounter variable
                                            if(mListOfGigMarkerInfo.get(l).getVenueId().equals(venueId))
                                            {
                                                gigCounter++;
                                            }

                                            // If the gigCounter variable contains 2 or more gigs then set the
                                            // multipleGigs boolean to true
                                            if(gigCounter >= 2)
                                            {
                                                multipleGigs = true;
                                            }
                                        }
                                    }
                                }

                                // If there are multiple gigs update the UI accordingly
                                if (multipleGigs)
                                {
                                    mGigNameTextView.setText("Multiple Gigs at this venue!");
                                    mGigStartDateTextView.setText("Click to see what's on offer!");
                                    mGigFinishDateTextView.setText("");
                                }

                                // Reset multiple gigs for when another marker is clicked
                                multipleGigs = false;
                            }

                            // Returning the view containing InfoWindow contents
                            return v;
                        }
                    });
                }
            }
        }

        // When a list item is selected, the same fragment opens as when a pin info window is clicked
        mGigsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                Gig selectedGig = (Gig) mGigsListView.getItemAtPosition(position);

                MusicianUserGigDetailsFragment fragment = new MusicianUserGigDetailsFragment();

                Bundle arguments = new Bundle();
                arguments.putString("GigID", selectedGig.getGigId());
                arguments.putString("GigTitle", selectedGig.getTitle());
                arguments.putString("GigStartDate", selectedGig.getStartDate().toString());
                arguments.putString("GigEndDate", selectedGig.getEndDate().toString());
                arguments.putString("GigVenueID", selectedGig.getVenueID());
                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // gig. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserGigDetailsFragment")
                        .addToBackStack(null).commit();
            }
        });
    }

    // If a marker info window is clicked the data is passed to the next fragment as bundle arguments
    @Override
    public void onInfoWindowClick(Marker marker)
    {
        MusicianUserGigDetailsFragment fragment = new MusicianUserGigDetailsFragment();
        Bundle arguments = new Bundle();

        // This loops through the list of marker info to determine the marker clicked
        for(int i = 0; i < mListOfGigMarkerInfo.size(); i++)
        {
            mListOfGigMarkerInfo.get(i);

            // Once a match has been found the data can be extracted and passed as a bundle argument
            if(mListOfGigMarkerInfo.get(i).getMarkerId().equals(marker.getId()))
            {
                arguments.putString("GigID", mListOfGigMarkerInfo.get(i).getGigId());
                arguments.putString("GigTitle", mListOfGigMarkerInfo.get(i).getGigName());
                arguments.putString("GigStartDate", mListOfGigMarkerInfo.get(i).getGigStartDate().toString());
                arguments.putString("GigEndDate", mListOfGigMarkerInfo.get(i).getGigEndDate().toString());
                arguments.putString("GigVenueID", mListOfGigMarkerInfo.get(i).getVenueId());
            }
        }

        fragment.setArguments(arguments);

        // Creates a new fragment transaction to display the details of the selected
        // gig. Some custom animation has been added also.
        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
        fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserGigDetailsFragment")
                .addToBackStack(null).commit();
    }

    private void PopulateListView()
    {
        // This sorts the list of gigs by date
        Collections.sort(mListOfGigs, new CustomComparator());

        GetGigLocation();

        // Using the custom VenueGigsAdapter, the list of users gigs can be displayed
        adapter = new MusicianUserGigsAdapter(getActivity(), R.layout.musician_user_gig_list, mListOfGigs);

        mGigsListView.setAdapter(adapter);
    }

    private void GetGigLocation()
    {
        for(int i = 0; i < mListOfGigs.size(); i++)
        {
            // Get the location of the gig from the previous fragment
            mGigLocationLat = mVenueDataSnapshot.child(mListOfGigs.get(i).getVenueID() + "/venueLocation/latitude").getValue(Double.class);
            mGigLocationLng = mVenueDataSnapshot.child(mListOfGigs.get(i).getVenueID() + "/venueLocation/longitude").getValue(Double.class);

            // Then set the data as parameters for the gig location object
            mGigLocation.setLatitude(mGigLocationLat);
            mGigLocation.setLongitude(mGigLocationLng);

            // Then set the data as parameters for the user location object
            mUserLocation.setLatitude(mLocation.latitude);
            mUserLocation.setLongitude(mLocation.longitude);

            // Calculate the distance between the provided location and the gig
            mDistance = CalculateDistance(mGigLocation, mUserLocation);

            mListOfGigs.get(i).setGigDistance(mDistance);

            FilterByDistance(i);
        }

        for(int i = mFilteredGigsToRemove.size() - 1; i >= 0; i--)
        {
            int itemToRemove;

            itemToRemove = mFilteredGigsToRemove.get(i);
            mListOfGigs.remove(itemToRemove);
        }
    }

    // This method takes the gig location and the user's location and calculates the distance between the two
    private double CalculateDistance(Location gigLocation, Location userLocation)
    {
        double distance;

        // This calculates the distance between the passed gig location and the user's current location
        distance = gigLocation.distanceTo(userLocation);

        // This converts the distance into km
        distance = distance / 1000;

        // This then rounds the distance to 2DP
        distance = Math.round(distance * 100D) / 100D;

        return distance;
    }

    // Each time this method is called it looks at an item from the list of gigs and works out whether the distance between the gig
    // and the users chosen location is larger than the distance specified on the slider
    private void FilterByDistance(int listIndex)
    {
        if(mListOfGigs.get(listIndex).getGigDistance() > mGigDistanceSelected)
        {
            mFilteredGigsToRemove.add(listIndex);
        }
    }

    public static DataSnapshot getVenueSnapshot()
    {
        return mVenueDataSnapshot;
    }
}
