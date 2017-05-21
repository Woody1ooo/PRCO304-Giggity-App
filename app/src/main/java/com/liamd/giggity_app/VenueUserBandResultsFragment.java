package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
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
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserBandResultsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener
{
    // Declare Map/Location specific
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Double mLatitude;
    private Double mLongitude;
    private static com.google.android.gms.maps.model.LatLng mLocation;
    private String mHomeMarkerId;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DataSnapshot mBandDataSnapshot;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    // Declare Visual Components
    private ListView mBandsListView;
    private VenueUserBandsAdapter adapter;
    private TextView mBandNameTextView;

    // Declare variables to be stored to pass to the next fragment
    private String mBandId;
    private String mBandName;
    private String mBandGenres;
    private String mGigId;
    private double mBandDistance;

    // Declare general variables
    private ArrayList<Band> mListOfBands = new ArrayList<>();
    private ArrayList<Integer> mFilteredBandsToRemove = new ArrayList<>();
    private ArrayList<BandMarkerInfo> mListOfBandMarkerInfo = new ArrayList<>();
    private Marker mMarker;
    private int mBandDistanceSelected;
    private String mGenresSelected;

    // Location variables
    private double mDistance;
    private Location mBandLocation;
    private double mBandLocationLat;
    private double mBandLocationLng;
    private Location mVenueLocation;

    public VenueUserBandResultsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.venue_user_fragment_band_results, container, false);

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

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // Initialise other variables required
        mBandLocation = new Location("");
        mVenueLocation = new Location("");

        // Clears the various lists when this fragment is returned to
        mListOfBands.clear();
        mFilteredBandsToRemove.clear();

        // Get the lat lng values from the previous fragment
        mLatitude = getArguments().getDouble("VenueLocationLatitude");
        mLongitude = getArguments().getDouble("VenueLocationLongitude");

        // This creates a location for the current user
        mLocation = new com.google.android.gms.maps.model.LatLng(mLatitude, mLongitude);

        // This gets the distance value the user has selected
        mBandDistanceSelected = getArguments().getInt("DistanceSelected");

        // This gets the genres the user has selected
        mGenresSelected = getArguments().getString("Genres");

        // This gets the gig id from the previous fragment
        mGigId = getArguments().getString("GigId");

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.googleMap);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        // Initialise the list view
        mBandsListView = (ListView) fragmentView.findViewById(R.id.bandsListView);

        getActivity().setTitle("Band Finder");

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

        // Once the map is ready, it can be set up using SetupMap()
        SetupMap();
    }

    // This initialises the map and takes a data snapshot from both the Gigs and Venues sections
    // of the database which are then stored in global variables so they can be accessed throughout
    // the class
    private void SetupMap()
    {
        mDatabase.child("Bands").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mBandDataSnapshot = dataSnapshot;
                AddBandMarkers(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void AddBandMarkers(DataSnapshot bandSnapshot)
    {
        // This iterates through the venues and adds them to a list (mListOfVenues)
        Iterable<DataSnapshot> bandChildren = bandSnapshot.getChildren();
        for (DataSnapshot child : bandChildren)
        {
            Band band;
            band = child.getValue(Band.class);
            mListOfBands.add(band);
        }

        // This method is then called to populate the list view once the map view has its markers in place
        PopulateListView();

        for(int i = 0; i < mListOfBands.size(); i++)
        {
            com.liamd.giggity_app.LatLng bandLocation = mListOfBands.get(i).getBaseLocation();

            // The gig information is then extracted each time and the values assigned
            // to these global variables to be accessed later
            mBandId = mListOfBands.get(i).getBandID();
            mBandName = mListOfBands.get(i).getName();
            mBandGenres = mListOfBands.get(i).getGenres();
            mBandDistance = mListOfBands.get(i).getBandDistance();

            // To expose the methods required for marker placement, the bandLocation
            // variable is then converted back into the standard Google Maps
            // LatLng object (convertedBandLocation)
            final com.google.android.gms.maps.model.LatLng convertedBandLocation = new com.google.android.gms.maps.model.LatLng(bandLocation.getLatitude(),
                    bandLocation.getLongitude());

            // The marker is then added to the map
            int height = 125;
            int width = 125;
            BitmapDrawable bitMapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_pin);
            Bitmap b = bitMapDraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

            // The marker is then added to the map
            mMarker = mGoogleMap.addMarker(new MarkerOptions().position(convertedBandLocation).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

            // A new BandMarkerInfo object is created to store the information about the marker.
            // This needs to be done because a standard marker can only hold a title and snippet
            BandMarkerInfo marker = new BandMarkerInfo(mMarker.getId(), mBandId, mBandName, mBandGenres, mBandDistance, mBandLocation, mVenueLocation);
            mListOfBandMarkerInfo.add(marker);

            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
            {
                @Override
                public View getInfoWindow(Marker marker)
                {
                    return null;
                }

                @Override
                public View getInfoContents(final Marker markerSelected)
                {
                    // Getting view from the layout file
                    View v = getActivity().getLayoutInflater().inflate(R.layout.band_window_layout, null);

                    // This then references the text views found in that layout
                    // (note gig id is a hidden text view)
                    mBandNameTextView = (TextView) v.findViewById(R.id.bandNameTextView);
                    TextView mBandDistanceTextView = (TextView) v.findViewById(R.id.bandDistanceTextView);
                    TextView mBandGenres = (TextView) v.findViewById(R.id.genresTextView);

                    // The marker info list is then iterated through
                    for (int i = 0; i < mListOfBandMarkerInfo.size(); i++)
                    {
                        // When the selected marker id matches one from the list it means there is a match
                        // and the text fields are updated to reflect this.
                        if (mListOfBandMarkerInfo.get(i).getMarkerId().equals(markerSelected.getId()))
                        {
                            mBandNameTextView.setText(mListOfBandMarkerInfo.get(i).getBandName());
                            mBandDistanceTextView.setText(mListOfBandMarkerInfo.get(i).getBandDistance() +"km");
                            mBandGenres.setText(mListOfBandMarkerInfo.get(i).getBandGenres());
                        }

                        else if (markerSelected.getId().equals(mHomeMarkerId))
                        {
                            mBandNameTextView.setText("Your location!");
                            mBandDistanceTextView.setVisibility(View.GONE);
                            mBandGenres.setVisibility(View.GONE);
                        }
                    }
                    return v;
                }
            });
        }

        // When a list item is selected, the same fragment opens as when a pin info window is clicked
        mBandsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                Band selectedBand = (Band) mBandsListView.getItemAtPosition(position);

                VenueUserBandDetailsFragment fragment = new VenueUserBandDetailsFragment();

                Bundle arguments = new Bundle();
                arguments.putString("BandID", selectedBand.getBandID());
                arguments.putString("BandName", selectedBand.getName());
                arguments.putString("BandGenres", selectedBand.getGenres());
                arguments.putDouble("BandDistance", mDistance);
                arguments.putDouble("BandLocationLat", mBandLocationLat);
                arguments.putDouble("BandLocationLng", mBandLocationLng);
                arguments.putDouble("VenueLocationLat", mVenueLocation.getLatitude());
                arguments.putDouble("VenueLocationLng", mVenueLocation.getLongitude());
                arguments.putString("GigId", mGigId);
                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // gig. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "VenueUserBandDetailsFragment")
                        .addToBackStack(null).commit();
            }
        });
    }

    private void PopulateListView()
    {
        GetBandLocation();

        if(getActivity() != null)
        {
            // Using the custom VenueUserGigsAdapter, the list of users gigs can be displayed
            adapter = new VenueUserBandsAdapter(getActivity(), R.layout.venue_user_band_list, mListOfBands);

            mBandsListView.setAdapter(adapter);
        }
    }

    private void GetBandLocation()
    {
        for(int i = 0; i < mListOfBands.size(); i++)
        {
            // Get the location of the gig from the previous fragment
            mBandLocationLat = mBandDataSnapshot.child(mListOfBands.get(i).getBandID() + "/baseLocation/latitude").getValue(Double.class);
            mBandLocationLng = mBandDataSnapshot.child(mListOfBands.get(i).getBandID() + "/baseLocation/longitude").getValue(Double.class);

            // Then set the data as parameters for the gig location object
            mBandLocation.setLatitude(mBandLocationLat);
            mBandLocation.setLongitude(mBandLocationLng);

            // Then set the data as parameters for the user location object
            mVenueLocation.setLatitude(mLocation.latitude);
            mVenueLocation.setLongitude(mLocation.longitude);

            // Calculate the distance between the provided location and the gig
            mDistance = CalculateDistance(mBandLocation, mVenueLocation);

            mListOfBands.get(i).setBandDistance(mDistance);

            FilterBandsList(i);
        }

        for(int i = mFilteredBandsToRemove.size() - 1; i >= 0; i--)
        {
            int itemToRemove;

            itemToRemove = mFilteredBandsToRemove.get(i);
            mListOfBands.remove(itemToRemove);
        }

        // If there are no results inform the user
        if(mListOfBands.size() == 0)
        {
            // A dialog is then shown to alert the user that the changes have been made
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("No Results!");
            builder.setMessage("Oh dear! No bands found. You might need to widen your search preferences.");
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

    // This method takes the band location and the user's location and calculates the distance between the two
    private double CalculateDistance(Location bandLocation, Location userLocation)
    {
        double distance;

        // This calculates the distance between the passed band location and the user's current location
        distance = bandLocation.distanceTo(userLocation);

        // This converts the distance into km
        distance = distance / 1000;

        // This then rounds the distance to 2DP
        distance = Math.round(distance * 100D) / 100D;

        return distance;
    }

    // Each time this method is called it looks at an item from the list of bands and works out whether it fits the users
    // search preferences
    private void FilterBandsList(int listIndex)
    {
        List<String> splitUserChosenGenres;
        splitUserChosenGenres = Arrays.asList(mGenresSelected.split(","));
        ArrayList<String> splitUserChosenGenresTrimmed = new ArrayList<>();

        // This loops through the split genres chosen by the user and trims the spaces
        for(int i = 0; i < splitUserChosenGenres.size(); i++)
        {
            splitUserChosenGenresTrimmed.add(splitUserChosenGenres.get(i).trim());
        }

        // This initially checks that the band is within the distance selected by the user
        if (mListOfBands.get(listIndex).getBandDistance() > mBandDistanceSelected)
        {
            mFilteredBandsToRemove.add(listIndex);
        }

        // This then loops through the trimmed array list checking if the genres the band has matches those submitted by the user
        for(int i = 0; i < splitUserChosenGenresTrimmed.size(); i++)
        {
            // If it finds a match break out of the loop and carry on
            if (mListOfBands.get(listIndex).getGenres().contains(splitUserChosenGenresTrimmed.get(i)))
            {
                break;
            }

            // Otherwise once every element has been looped through add this element to the list to be removed
            else
            {
                if(i + 1 == splitUserChosenGenresTrimmed.size())
                {
                    if(!mFilteredBandsToRemove.contains(listIndex))
                    {
                        mFilteredBandsToRemove.add(listIndex);
                        break;
                    }

                    else
                    {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        if (!mBandNameTextView.getText().equals("Your location!"))
        {
            VenueUserBandDetailsFragment fragment = new VenueUserBandDetailsFragment();
            Bundle arguments = new Bundle();

            for(int i = 0; i < mListOfBandMarkerInfo.size(); i++)
            {
                mListOfBandMarkerInfo.get(i);

                if(mListOfBandMarkerInfo.get(i).getMarkerId().equals(marker.getId()))
                {
                    arguments.putString("BandID", mListOfBandMarkerInfo.get(i).getBandId());
                    arguments.putString("BandName", mListOfBandMarkerInfo.get(i).getBandName());
                    arguments.putString("BandGenres", mListOfBandMarkerInfo.get(i).getBandGenres());
                    arguments.putDouble("BandDistance", mListOfBandMarkerInfo.get(i).getBandDistance());
                    arguments.putDouble("BandLocationLat", mListOfBandMarkerInfo.get(i).getBandLocation().getLatitude());
                    arguments.putDouble("BandLocationLng", mListOfBandMarkerInfo.get(i).getBandLocation().getLongitude());
                    arguments.putDouble("VenueLocationLat", mListOfBandMarkerInfo.get(i).getVenueLocation().getLatitude());
                    arguments.putDouble("VenueLocationLng", mListOfBandMarkerInfo.get(i).getVenueLocation().getLongitude());
                    arguments.putString("GigId", mGigId);
                }
            }

            fragment.setArguments(arguments);

            // Creates a new fragment transaction to display the details of the selected
            // gig. Some custom animation has been added also.
            FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                    .beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
            fragmentTransaction.replace(R.id.frame, fragment, "VenueUserBandDetailsFragment")
                    .addToBackStack(null).commit();
        }
    }
}
