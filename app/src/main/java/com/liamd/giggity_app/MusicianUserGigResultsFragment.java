package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


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
    private String mHomeMarkerId;

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
    private ArrayList<Gig> mListOfMultipleGigs = new ArrayList<>();
    private Marker mMarker;
    private Boolean multipleGigs = false;
    private int mGigDistanceSelected;
    private Boolean mIsInBand = false;
    private boolean mIsFanAccount;
    private String mEarliestDateString;
    private String mLatestDateString;
    private Date mEarliestDate;
    private Date mLatestDate;

    // Declare Visual Components
    private ListView mGigsListView;
    private MusicianUserGigsAdapter adapter;

    // Declare variables to be stored to pass to the next fragment
    private String mGigId;
    private String mGigName;
    private String mGigGenres;
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

    private TextView mGigNameTextView;

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

        if(getArguments().getString("UserType").equals("Fan"))
        {
            mIsFanAccount = true;
        }

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
        mIsInBand = getArguments().getBoolean("IsInBand");
        mEarliestDateString = getArguments().getString("EarliestDate");
        mLatestDateString = getArguments().getString("LatestDate");

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

        // Set the fragment title
        getActivity().setTitle("Gig Finder");

        return fragmentView;
    }

    // When the map is ready, call the SetupMap method
    @Override
    public void onMapReady(GoogleMap map)
    {
        mGoogleMap = map;

        // The marker is then added to the map with set size attributes
        int height = 125;
        int width = 125;

        // This creates a drawable bitmap
        if(getActivity() != null)
        {
            BitmapDrawable bitMapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_home_pin);
            Bitmap bitmap = bitMapDraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);

            // This places a marker at the users chosen location
            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(mLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

            // The marker id is then extracted to determine whether the marker is home when clicked
            mHomeMarkerId = marker.getId();

            // This zooms the map in to a reasonable level (12) and centers it on the location provided
            float zoomLevel = 8;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, zoomLevel));
            mGoogleMap.setOnInfoWindowClickListener(this);

            // This method gets the date range set by the user
            GetDatePreferences();

            // Once the map is ready, it can be set up using SetupMap()
            SetupMap();
        }
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
            mGigGenres = mListOfGigs.get(i).getGenres();

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
                    final com.google.android.gms.maps.model.LatLng convertedGigLocation = new com.google.android.gms.maps.model.LatLng(gigLocation.getLatitude(),
                                    gigLocation.getLongitude());

                    if(getActivity() != null)
                    {
                        // The marker is then added to the map
                        int height = 125;
                        int width = 125;
                        BitmapDrawable bitMapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_pin);
                        Bitmap b = bitMapDraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        mMarker = mGoogleMap.addMarker(new MarkerOptions().position(convertedGigLocation).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                    }

                    // A new GigMarkerInfo object is created to store the information about the marker.
                    // This needs to be done because a standard marker can only hold a title and snippet
                    GigMarkerInfo marker = new GigMarkerInfo(mGigFinishDate, mGigId, mGigName, mGigStartDate,
                            mMarker.getId(), mVenueId, mVenueName, mGigGenres);
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
                            mGigNameTextView = (TextView) v.findViewById(R.id.gigNameTextView);
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
                                    // This takes the start and end dates and reformats them to look more visually appealing
                                    String formattedStartDateSectionOne = mListOfGigMarkerInfo.get(i).getGigStartDate().toString().split(" ")[0];
                                    String formattedStartDateSectionTwo = mListOfGigMarkerInfo.get(i).getGigStartDate().toString().split(" ")[1];
                                    String formattedStartDateSectionThree = mListOfGigMarkerInfo.get(i).getGigStartDate().toString().split(" ")[2];
                                    String formattedStartDateSectionFour = mListOfGigMarkerInfo.get(i).getGigStartDate().toString().split(" ")[3];

                                    String formattedFinishDateSectionOne = mListOfGigMarkerInfo.get(i).getGigEndDate().toString().split(" ")[0];
                                    String formattedFinishDateSectionTwo = mListOfGigMarkerInfo.get(i).getGigEndDate().toString().split(" ")[1];
                                    String formattedFinishDateSectionThree = mListOfGigMarkerInfo.get(i).getGigEndDate().toString().split(" ")[2];
                                    String formattedFinishDateSectionFour = mListOfGigMarkerInfo.get(i).getGigEndDate().toString().split(" ")[3];

                                    mGigIdTextView.setText(mListOfGigMarkerInfo.get(i).getGigId());
                                    mGigNameTextView.setText(mListOfGigMarkerInfo.get(i).getGigName());
                                    mGigNameTextView.setTypeface(null, Typeface.BOLD);
                                    mGigStartDateTextView.setText("Start Date/Time: " + formattedStartDateSectionOne + " " + formattedStartDateSectionTwo + " " + formattedStartDateSectionThree + " " + formattedStartDateSectionFour);
                                    mGigFinishDateTextView.setText("Finish Date/Time: " + formattedFinishDateSectionOne + " " + formattedFinishDateSectionTwo + " " + formattedFinishDateSectionThree + " " + formattedFinishDateSectionFour);
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

                                else if (markerSelected.getId().equals(mHomeMarkerId))
                                {
                                    mGigNameTextView.setText("Your location!");
                                    mGigFinishDateTextView.setVisibility(View.GONE);
                                    mGigStartDateTextView.setVisibility(View.GONE);
                                    mVenueNameTextView.setVisibility(View.GONE);
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
                // If the account is not a fan account continue as usual
                if(!mIsFanAccount)
                {
                    if(mIsInBand)
                    {
                        Gig selectedGig = (Gig) mGigsListView.getItemAtPosition(position);

                        MusicianUserGigDetailsFragment fragment = new MusicianUserGigDetailsFragment();

                        Bundle arguments = new Bundle();
                        arguments.putString("GigID", selectedGig.getGigId());
                        arguments.putString("GigTitle", selectedGig.getTitle());
                        arguments.putString("GigStartDate", selectedGig.getStartDate().toString());
                        arguments.putString("GigEndDate", selectedGig.getEndDate().toString());
                        arguments.putString("GigVenueID", selectedGig.getVenueID());
                        arguments.putString("GigGenres", selectedGig.getGenres());
                        fragment.setArguments(arguments);

                        // Creates a new fragment transaction to display the details of the selected
                        // gig. Some custom animation has been added also.
                        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                                .beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                        fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserGigDetailsFragment")
                                .addToBackStack(null).commit();
                    }

                    else
                    {
                        // This dialog is created to tell the user that they can't go any further as they're not in a band
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Alert");
                        builder.setIcon(R.drawable.ic_info_outline_black_24px);
                        builder.setMessage("We've detected that you're currently not part of a band! " +
                                "You must be part of one to apply for gig opportunities. Would you like to create one now?");
                        builder.setPositiveButton("Create Band", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                MusicianUserBandCreatorFragment fragment = new MusicianUserBandCreatorFragment();
                                getActivity().setTitle("Band Creator");
                                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                                        .beginTransaction();
                                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserBandCreatorFragment")
                                        .addToBackStack(null).commit();
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {

                            }
                        });
                        builder.show();
                    }
                }

                // Otherwise load the fan specific fragments
                else
                {
                    Gig selectedGig = (Gig) mGigsListView.getItemAtPosition(position);

                    FanUserGigDetailsFragment fragment = new FanUserGigDetailsFragment();

                    Bundle arguments = new Bundle();
                    arguments.putString("GigID", selectedGig.getGigId());
                    arguments.putString("GigTitle", selectedGig.getTitle());
                    arguments.putString("GigStartDate", selectedGig.getStartDate().toString());
                    arguments.putString("GigEndDate", selectedGig.getEndDate().toString());
                    arguments.putString("GigVenueID", selectedGig.getVenueID());
                    arguments.putString("GigGenres", selectedGig.getGenres());
                    fragment.setArguments(arguments);

                    // Creates a new fragment transaction to display the details of the selected
                    // gig. Some custom animation has been added also.
                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                            .beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                    fragmentTransaction.replace(R.id.frame, fragment, "FanUserGigDetailsFragment")
                            .addToBackStack(null).commit();
                }
            }
        });
    }

    // If a marker info window is clicked the data is passed to the next fragment as bundle arguments
    @Override
    public void onInfoWindowClick(Marker marker)
    {
        if(!mGigNameTextView.getText().equals("Your location!"))
        {
            if(!mIsFanAccount)
            {
                if(mIsInBand)
                {
                    // If the text matches multiple gigs then loop through the markers and get the venue id selected
                    if(mGigNameTextView.getText().equals("Multiple Gigs at this venue!"))
                    {
                        // This loops through the list of marker info to determine the marker clicked
                        for(int i = 0; i < mListOfGigMarkerInfo.size(); i++)
                        {
                            mListOfGigMarkerInfo.get(i);

                            // Once a match has been found the data can be extracted and passed as a bundle argument
                            if (mListOfGigMarkerInfo.get(i).getMarkerId().equals(marker.getId()))
                            {
                                // Pass this venue id into the method
                                ShowMultipleGigsDialog(mListOfGigMarkerInfo.get(i).getVenueId());
                            }
                        }
                    }

                    else
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
                                arguments.putString("GigGenres", mListOfGigMarkerInfo.get(i).getGigGenres());
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
                }

                else
                {
                    // This dialog is created to tell the user that they can't go any further as they're not in a band
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Alert");
                    builder.setMessage("We've detected that you're currently not part of a band! " +
                            "You must be part of one to apply for gig opportunities. Would you like to create one now?");
                    builder.setIcon(R.drawable.ic_info_outline_black_24px);
                    builder.setPositiveButton("Create Band", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            MusicianUserBandCreatorFragment fragment = new MusicianUserBandCreatorFragment();
                            getActivity().setTitle("Band Creator");
                            FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                                    .beginTransaction();
                            fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                            fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserBandCreatorFragment")
                                    .addToBackStack(null).commit();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {

                        }
                    });
                    builder.show();
                }
            }

            else
            {
                // If the text matches multiple gigs then loop through the markers and get the venue id selected
                if(mGigNameTextView.getText().equals("Multiple Gigs at this venue!"))
                {
                    // This loops through the list of marker info to determine the marker clicked
                    for(int i = 0; i < mListOfGigMarkerInfo.size(); i++)
                    {
                        mListOfGigMarkerInfo.get(i);

                        // Once a match has been found the data can be extracted and passed as a bundle argument
                        if (mListOfGigMarkerInfo.get(i).getMarkerId().equals(marker.getId()))
                        {
                            // Pass this venue id into the method
                            ShowMultipleGigsDialog(mListOfGigMarkerInfo.get(i).getVenueId());
                        }
                    }
                }

                else
                {
                    FanUserGigDetailsFragment fragment = new FanUserGigDetailsFragment();
                    Bundle arguments = new Bundle();

                    // This loops through the list of marker info to determine the marker clicked
                    for (int i = 0; i < mListOfGigMarkerInfo.size(); i++)
                    {
                        mListOfGigMarkerInfo.get(i);

                        // Once a match has been found the data can be extracted and passed as a bundle argument
                        if (mListOfGigMarkerInfo.get(i).getMarkerId().equals(marker.getId()))
                        {
                            arguments.putString("GigID", mListOfGigMarkerInfo.get(i).getGigId());
                            arguments.putString("GigTitle", mListOfGigMarkerInfo.get(i).getGigName());
                            arguments.putString("GigStartDate", mListOfGigMarkerInfo.get(i).getGigStartDate().toString());
                            arguments.putString("GigEndDate", mListOfGigMarkerInfo.get(i).getGigEndDate().toString());
                            arguments.putString("GigVenueID", mListOfGigMarkerInfo.get(i).getVenueId());
                            arguments.putString("GigGenres", mListOfGigMarkerInfo.get(i).getGigGenres());
                        }
                    }

                    fragment.setArguments(arguments);

                    // Creates a new fragment transaction to display the details of the selected
                    // gig. Some custom animation has been added also.
                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                            .beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                    fragmentTransaction.replace(R.id.frame, fragment, "FanUserGigDetailsFragment")
                            .addToBackStack(null).commit();
                }
            }
        }
    }

    private void PopulateListView()
    {
        // This sorts the list of gigs by date
        Collections.sort(mListOfGigs, new CustomGigComparator());

        GetGigLocation();

        // Using the custom VenueUserGigsAdapter, the list of users gigs can be displayed
        if(getActivity() != null)
        {
            adapter = new MusicianUserGigsAdapter(getActivity(), R.layout.musician_user_gig_list, mListOfGigs);
            mGigsListView.setAdapter(adapter);
        }
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

        // If there are no results inform the user
        if(mListOfGigs.size() == 0)
        {
            // A dialog is then shown to alert the user that the changes have been made
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("No Results!");
            builder.setMessage("Oh dear! No gigs found. You might need to widen your search preferences.");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    getFragmentManager().popBackStack();
                }
            });
            builder.setCancelable(false);
            builder.show();
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
        if (mListOfGigs.get(listIndex).getGigDistance() > mGigDistanceSelected)
        {
            mFilteredGigsToRemove.add(listIndex);
        }

        // If the user is a musician filter out the non-vacant gigs
        if (!mIsFanAccount)
        {
            if (!mListOfGigs.get(listIndex).getBookedAct().equals("Vacant"))
            {
                if (!mFilteredGigsToRemove.contains(listIndex))
                {
                    mFilteredGigsToRemove.add(listIndex);
                }
            }
        }

        // If the user is a fan only show gigs with acts
        else
        {
            if (mListOfGigs.get(listIndex).getBookedAct().equals("Vacant"))
            {
                if (!mFilteredGigsToRemove.contains(listIndex))
                {
                    mFilteredGigsToRemove.add(listIndex);
                }
            }
        }

        // If the gigs start date is before the earliest date set by the user remove it
        if(mListOfGigs.get(listIndex).getStartDate().before(mEarliestDate))
        {
            if (!mFilteredGigsToRemove.contains(listIndex))
            {
                mFilteredGigsToRemove.add(listIndex);
            }
        }

        // If the gigs start date is after the latest date set by the user remove it
        else if(mListOfGigs.get(listIndex).getStartDate().after(mLatestDate))
        {
            if (!mFilteredGigsToRemove.contains(listIndex))
            {
                mFilteredGigsToRemove.add(listIndex);
            }
        }

        // If the gigs finish date is after the latest date set by the user remove it
        else if(mListOfGigs.get(listIndex).getEndDate().after(mLatestDate))
        {
            if (!mFilteredGigsToRemove.contains(listIndex))
            {
                mFilteredGigsToRemove.add(listIndex);
            }
        }

        // If the gigs finish date is before the earliest start date set by the user remove it
        else if(mListOfGigs.get(listIndex).getEndDate().before(mEarliestDate))
        {
            if (!mFilteredGigsToRemove.contains(listIndex))
            {
                mFilteredGigsToRemove.add(listIndex);
            }
        }
    }

    private void ShowMultipleGigsDialog(String venueId)
    {
        // Initially clear the list of multiple gigs
        mListOfMultipleGigs.clear();

        // Iterate through the list of gigs and find any that exists at the venue id passed in to the method
        // We can be sure these will all have multiple gigs as this method is only called in that scenario
        for(Gig gig : mListOfGigs)
        {
            if(gig.getVenueID().equals(venueId))
            {
                mListOfMultipleGigs.add(gig);
            }
        }

        // Show the multiple gigs dialog (multiple_gig_dialog_list)
        final Dialog multipleGigSelectorDialog = new Dialog(getActivity());
        multipleGigSelectorDialog.setTitle("Multiple Gigs");
        multipleGigSelectorDialog.setContentView(R.layout.multiple_gig_dialog_list);

        // Initialise the list view
        final ListView mMultipleGigsListView = (ListView) multipleGigSelectorDialog.findViewById(R.id.multipleGigListDialogList);

        // Set the adapter and pass in the list of multiple gigs at the venue chosen
        MusicianUserMultipleGigsAdapter adapter = new MusicianUserMultipleGigsAdapter(getActivity(), R.layout.multiple_gig_dialog_layout, mListOfMultipleGigs);
        mMultipleGigsListView.setAdapter(adapter);

        // show the dialog
        multipleGigSelectorDialog.show();

        // Add an on click listener for each item in the list
        mMultipleGigsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                if(!mIsFanAccount)
                {
                    multipleGigSelectorDialog.dismiss();

                    // This returns the selected gig from the list view
                    Gig selectedGig = (Gig) mMultipleGigsListView.getItemAtPosition(position);

                    MusicianUserGigDetailsFragment fragment = new MusicianUserGigDetailsFragment();
                    Bundle arguments = new Bundle();

                    arguments.putString("GigID", selectedGig.getGigId());
                    arguments.putString("GigTitle", selectedGig.getTitle());
                    arguments.putString("GigStartDate", selectedGig.getStartDate().toString());
                    arguments.putString("GigEndDate", selectedGig.getEndDate().toString());
                    arguments.putString("GigVenueID", selectedGig.getVenueID());
                    arguments.putString("GigGenres", selectedGig.getGenres());

                    fragment.setArguments(arguments);

                    // Creates a new fragment transaction to display the details of the selected
                    // gig. Some custom animation has been added also.
                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                            .beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                    fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserGigDetailsFragment")
                            .addToBackStack(null).commit();
                }

                else
                {
                    multipleGigSelectorDialog.dismiss();

                    // This returns the selected gig from the list view
                    Gig selectedGig = (Gig) mGigsListView.getItemAtPosition(position);

                    FanUserGigDetailsFragment fragment = new FanUserGigDetailsFragment();
                    Bundle arguments = new Bundle();

                    arguments.putString("GigID", selectedGig.getGigId());
                    arguments.putString("GigTitle", selectedGig.getTitle());
                    arguments.putString("GigStartDate", selectedGig.getStartDate().toString());
                    arguments.putString("GigEndDate", selectedGig.getEndDate().toString());
                    arguments.putString("GigVenueID", selectedGig.getVenueID());
                    arguments.putString("GigGenres", selectedGig.getGenres());

                    fragment.setArguments(arguments);

                    // Creates a new fragment transaction to display the details of the selected
                    // gig. Some custom animation has been added also.
                    FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                            .beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                    fragmentTransaction.replace(R.id.frame, fragment, "FanUserGigDetailsFragment")
                            .addToBackStack(null).commit();
                }
            }
        });
    }

    private void GetDatePreferences()
    {
        // These lines splits the existing dates into two string arrays
        // so the individual date elements can be extracted and formatted into
        // the correct format
        String[] earliestDateSplit = mEarliestDateString.split("\\s+");
        String[] latestDateSplit = mLatestDateString.split("\\s+");
        String earliestDateMonth = "";
        String latestDateMonth = "";

        // These switch statements facilitate the conversion from
        // Jan to 01 for example
        switch (earliestDateSplit[1])
        {
            case "Jan":
                earliestDateMonth = "01";
                break;
            case "Feb":
                earliestDateMonth = "02";
                break;
            case "Mar":
                earliestDateMonth = "03";
                break;
            case "Apr":
                earliestDateMonth = "04";
                break;
            case "May":
                earliestDateMonth = "05";
                break;
            case "Jun":
                earliestDateMonth = "06";
                break;
            case "Jul":
                earliestDateMonth = "07";
                break;
            case "Aug":
                earliestDateMonth = "08";
                break;
            case "Sep":
                earliestDateMonth = "09";
                break;
            case "Oct":
                earliestDateMonth = "10";
                break;
            case "Nov":
                earliestDateMonth = "11";
                break;
            case "Dec":
                earliestDateMonth = "12";
                break;
        }

        switch (latestDateSplit[1])
        {
            case "Jan":
                latestDateMonth = "01";
                break;
            case "Feb":
                latestDateMonth = "02";
                break;
            case "Mar":
                latestDateMonth = "03";
                break;
            case "Apr":
                latestDateMonth = "04";
                break;
            case "May":
                latestDateMonth = "05";
                break;
            case "Jun":
                latestDateMonth = "06";
                break;
            case "Jul":
                latestDateMonth = "07";
                break;
            case "Aug":
                latestDateMonth = "08";
                break;
            case "Sep":
                latestDateMonth = "09";
                break;
            case "Oct":
                latestDateMonth = "10";
                break;
            case "Nov":
                latestDateMonth = "11";
                break;
            case "Dec":
                latestDateMonth = "12";
                break;
        }

        // This parses the dates from a String back into a Date object
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        try
        {
            // The elements of the split string are then concatenated together
            // to form the correct date format where they are then converted into
            // date objects and stored within mEarliestDate and mLatestDate
            String startDateToParse = earliestDateSplit[2] + "/" + earliestDateMonth + "/" + earliestDateSplit[5] + " " + earliestDateSplit[3] + "." + "000";
            String endDateToParse = latestDateSplit[2] + "/" + latestDateMonth + "/" + latestDateSplit[5] + " " + latestDateSplit[3] + "." + "000";
            mEarliestDate = format.parse(startDateToParse);
            mLatestDate = format.parse(endDateToParse);
        }

        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public static DataSnapshot getVenueSnapshot()
    {
        return mVenueDataSnapshot;
    }
}
