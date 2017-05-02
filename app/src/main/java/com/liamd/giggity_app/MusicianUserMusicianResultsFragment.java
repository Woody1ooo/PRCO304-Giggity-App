package com.liamd.giggity_app;


import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Typeface;
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
public class MusicianUserMusicianResultsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener
{
    // Declare Map/Location specific
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Double mLatitude;
    private Double mLongitude;
    private static com.google.android.gms.maps.model.LatLng mLocation;
    private String mHomeMarkerId;

    // Declare Firebase specific variables
    private DatabaseReference mDatabase;
    private DataSnapshot mUserDataSnapshot;

    // Declare Visual Components
    private ListView mMusiciansListView;
    private MusicianUserMusiciansAdapter adapter;
    private TextView mUserNameTextView;

    // Declare general variables
    private ArrayList<User> mListOfMusicians = new ArrayList<>();
    private ArrayList<Integer> mFilteredMusiciansToRemove = new ArrayList<>();
    private ArrayList<UserMarkerInfo> mListOfMusicianMarkerInfo = new ArrayList<>();
    private Marker mMarker;
    private int mMusicianDistanceSelected;
    private String mGenresSelected;
    private String mInstrumentsSelected;
    private String mPositionInstruments;

    // User variables
    private String mUserId;
    private String mUserName;
    private String mUserGenres;
    private String mUserInstruments;
    private String mBandId;
    private String mBandPosition;

    // Location variables
    private double mDistance;
    private Location mMusicianLocation;
    private double mMusicianLocationLat;
    private double mMusicianLocationLng;
    private Location mBandLocation;

    public MusicianUserMusicianResultsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_musician_results, container, false);

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

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise other variables required
        mBandLocation = new Location("");
        mMusicianLocation = new Location("");

        // Clears the various lists when this fragment is returned to
        mListOfMusicians.clear();
        mFilteredMusiciansToRemove.clear();

        mLatitude = getArguments().getDouble("BandLocationLatitude");
        mLongitude = getArguments().getDouble("BandLocationLongitude");

        // This creates a location for the current user
        mLocation = new com.google.android.gms.maps.model.LatLng(mLatitude, mLongitude);

        // This gets the distance value the user has selected
        mMusicianDistanceSelected = getArguments().getInt("DistanceSelected");

        // This gets the genres the user has selected
        mGenresSelected = getArguments().getString("Genres");

        // This gets the instruments the user has selected
        mInstrumentsSelected = getArguments().getString("Instruments");
        mPositionInstruments = getArguments().getString("Instruments");

        // This gets the id of the band searching for members
        mBandId = getArguments().getString("BandId");

        // This gets the position that the band is searching for a musician for
        mBandPosition = getArguments().getString("BandPosition");

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.googleMap);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        // Initialise the list view
        mMusiciansListView = (ListView) fragmentView.findViewById(R.id.musiciansListView);

        // Set the fragment title
        getActivity().setTitle("Musician Finder");

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
            BitmapDrawable bitMapDraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_home_pin);
            Bitmap bitmap = bitMapDraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);

            // This places a marker at the users chosen location
            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(mLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

            // The marker id is then extracted to determine whether the marker is home when clicked
            mHomeMarkerId = marker.getId();

            // This zooms the map in to a reasonable level (12) and centers it on the location provided
            float zoomLevel = 15;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, zoomLevel));
            mGoogleMap.setOnInfoWindowClickListener(this);

            // Once the map is ready, it can be set up using SetupMap()
            SetupMap();
        }
    }

    // This initialises the map and takes a data snapshot from the users section
    // of the database which is then stored in a global variable so they can be accessed throughout
    // the class
    private void SetupMap()
    {
        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mUserDataSnapshot = dataSnapshot;
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
            User user;
            user = child.getValue(User.class);

            if(user.getAccountType().equals("Musician"))
            {
                mListOfMusicians.add(user);
            }
        }

        // This method is then called to populate the list view once the map view has its markers in place
        PopulateListView();

        for(int i = 0; i < mListOfMusicians.size(); i++)
        {
            com.liamd.giggity_app.LatLng userLocation = mListOfMusicians.get(i).getHomeLocation();

            // The gig information is then extracted each time and the values assigned
            // to these global variables to be accessed later
            mUserId = mListOfMusicians.get(i).getUserID();
            mUserName = mListOfMusicians.get(i).getFirstName() + " " + mListOfMusicians.get(i).getLastName();
            mUserGenres = mListOfMusicians.get(i).getGenres();
            mUserInstruments = mListOfMusicians.get(i).getInstruments();

            // To expose the methods required for marker placement, the bandLocation
            // variable is then converted back into the standard Google Maps
            // LatLng object (convertedBandLocation)
            final com.google.android.gms.maps.model.LatLng convertedUserLocation = new com.google.android.gms.maps.model.LatLng(userLocation.getLatitude(),
                    userLocation.getLongitude());

            // The marker is then added to the map
            int height = 125;
            int width = 125;
            BitmapDrawable bitMapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_pin);
            Bitmap b = bitMapDraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

            // The marker is then added to the map
            mMarker = mGoogleMap.addMarker(new MarkerOptions().position(convertedUserLocation).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

            // A new BandMarkerInfo object is created to store the information about the marker.
            // This needs to be done because a standard marker can only hold a title and snippet
            UserMarkerInfo marker = new UserMarkerInfo(mMarker.getId(), mUserId, mUserName, mUserGenres, mUserInstruments, mDistance, mMusicianLocation, mBandLocation);
            mListOfMusicianMarkerInfo.add(marker);

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
                    View v = getActivity().getLayoutInflater().inflate(R.layout.user_window_layout, null);

                    // This then references the text views found in that layout
                    // (note gig id is a hidden text view)
                    mUserNameTextView = (TextView) v.findViewById(R.id.userNameTextView);
                    mUserNameTextView.setTypeface(null, Typeface.BOLD);
                    TextView mUserDistanceTextView = (TextView) v.findViewById(R.id.userDistanceTextView);
                    TextView mUserGenresTextView = (TextView) v.findViewById(R.id.userGenresTextView);
                    TextView mUserInstrumentsTextView = (TextView) v.findViewById(R.id.userInstrumentsTextView);

                    // The marker info list is then iterated through
                    for (int i = 0; i < mListOfMusicianMarkerInfo.size(); i++)
                    {
                        // When the selected marker id matches one from the list it means there is a match
                        // and the text fields are updated to reflect this.
                        if (mListOfMusicianMarkerInfo.get(i).getMarkerId().equals(markerSelected.getId()))
                        {
                            mUserNameTextView.setText(mListOfMusicianMarkerInfo.get(i).getUserName());
                            mUserDistanceTextView.setText(mListOfMusicianMarkerInfo.get(i).getUserDistance() +"km");
                            mUserGenresTextView.setText(mListOfMusicianMarkerInfo.get(i).getUserGenres());
                            mUserInstrumentsTextView.setText(mListOfMusicianMarkerInfo.get(i).getUserInstruments());
                        }

                        else if (markerSelected.getId().equals(mHomeMarkerId))
                        {
                            mUserNameTextView.setText("Your location!");
                            mUserDistanceTextView.setVisibility(View.GONE);
                            mUserGenresTextView.setVisibility(View.GONE);
                            mUserInstrumentsTextView.setVisibility(View.GONE);
                        }
                    }
                    return v;
                }
            });
        }


        // When a list item is selected, the same fragment opens as when a pin info window is clicked
        mMusiciansListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                User selectedUser = (User) mMusiciansListView.getItemAtPosition(position);

                MusicianUserMusicianDetailsFragment fragment = new MusicianUserMusicianDetailsFragment();

                Bundle arguments = new Bundle();
                arguments.putString("UserID", selectedUser.getUserID());
                arguments.putString("UserName", selectedUser.getFirstName() + " " + selectedUser.getLastName());
                arguments.putString("UserGenres", selectedUser.getGenres());
                arguments.putString("UserInstruments", selectedUser.getInstruments());
                arguments.putString("PositionInstruments", mPositionInstruments);
                arguments.putString("BandId", mBandId);
                arguments.putString("BandPosition", mBandPosition);
                arguments.putDouble("Distance", mDistance);
                arguments.putDouble("Lat", mMusicianLocationLat);
                arguments.putDouble("Lng", mMusicianLocationLng);
                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // user. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserMusicianDetailsFragment")
                        .addToBackStack(null).commit();
            }
        });
    }

    private void PopulateListView()
    {
        GetUserLocation();

        // Using the custom VenueUserGigsAdapter, the list of users gigs can be displayed
        adapter = new MusicianUserMusiciansAdapter(getActivity(), R.layout.musician_user_musician_list, mListOfMusicians);

        mMusiciansListView.setAdapter(adapter);
    }

    private void GetUserLocation()
    {
        for(int i = 0; i < mListOfMusicians.size(); i++)
        {
            // Get the location of the gig from the previous fragment
            mMusicianLocationLat = mUserDataSnapshot.child(mListOfMusicians.get(i).getUserID() + "/homeLocation/latitude").getValue(Double.class);
            mMusicianLocationLng = mUserDataSnapshot.child(mListOfMusicians.get(i).getUserID() + "/homeLocation/longitude").getValue(Double.class);

            // Then set the data as parameters for the gig location object
            mMusicianLocation.setLatitude(mMusicianLocationLat);
            mMusicianLocation.setLongitude(mMusicianLocationLng);

            // Then set the data as parameters for the user location object
            mBandLocation.setLatitude(mLocation.latitude);
            mBandLocation.setLongitude(mLocation.longitude);

            // Calculate the distance between the provided location and the gig
            mDistance = CalculateDistance(mMusicianLocation, mBandLocation);

            mListOfMusicians.get(i).setMusicianDistance(mDistance);

            FilterMusiciansList(i);

        }

        for(int i = mFilteredMusiciansToRemove.size() - 1; i >= 0; i--)
        {
            int itemToRemove;

            itemToRemove = mFilteredMusiciansToRemove.get(i);
            mListOfMusicians.remove(itemToRemove);
        }
    }

    // This method takes the band location and the musicians's location and calculates the distance between the two
    private double CalculateDistance(Location userLocation, Location bandLocation)
    {
        double distance;

        // This calculates the distance between the passed band location and the musician's location
        distance = bandLocation.distanceTo(userLocation);

        // This converts the distance into km
        distance = distance / 1000;

        // This then rounds the distance to 2DP
        distance = Math.round(distance * 100D) / 100D;

        return distance;
    }

    // Each time this method is called it looks at an item from the list of bands and works out whether it fits the users
    // search preferences
    private void FilterMusiciansList(int listIndex)
    {
        List<String> splitUserChosenGenres;
        splitUserChosenGenres = Arrays.asList(mGenresSelected.split(","));
        ArrayList<String> splitUserChosenGenresTrimmed = new ArrayList<>();

        // This loops through the split genres chosen by the user and trims the spaces
        for(int i = 0; i < splitUserChosenGenres.size(); i++)
        {
            splitUserChosenGenresTrimmed.add(splitUserChosenGenres.get(i).trim());
        }

        List<String> splitUserChosenInstruments;
        splitUserChosenInstruments = Arrays.asList(mInstrumentsSelected.split(","));
        ArrayList<String> splitUserChosenInstrumentsTrimmed = new ArrayList<>();

        // This loops through the split instruments chosen by the user and trims the spaces
        for(int i = 0; i < splitUserChosenInstruments.size(); i++)
        {
            splitUserChosenInstrumentsTrimmed.add(splitUserChosenInstruments.get(i).trim());
        }

        // This initially checks that the band is within the distance selected by the user
        if (mListOfMusicians.get(listIndex).getMusicianDistance() > mMusicianDistanceSelected)
        {
            mFilteredMusiciansToRemove.add(listIndex);
        }

        if(mListOfMusicians.get(listIndex).isInBand())
        {
            if(!mFilteredMusiciansToRemove.contains(listIndex))
            {
                mFilteredMusiciansToRemove.add(listIndex);
            }
        }

        // This then loops through the trimmed array list checking if the genres the band has matches those submitted by the user
        for(int i = 0; i < splitUserChosenGenresTrimmed.size(); i++)
        {
            // If it finds a match break out of the loop and carry on
            if (mListOfMusicians.get(listIndex).getGenres().contains(splitUserChosenGenresTrimmed.get(i)))
            {
                break;
            }

            // Otherwise once every element has been looped through add this element to the list to be removed
            else
            {
                if(i + 1 == splitUserChosenGenresTrimmed.size())
                {
                    if(!mFilteredMusiciansToRemove.contains(listIndex))
                    {
                        mFilteredMusiciansToRemove.add(listIndex);
                        break;
                    }

                    else
                    {
                        break;
                    }
                }
            }
        }

        // This then loops through the trimmed array list checking if the genres the user has matches those submitted by the user
        for(int i = 0; i < splitUserChosenInstrumentsTrimmed.size(); i++)
        {
            try
            {
                // If it finds a match break out of the loop and carry on
                if (mListOfMusicians.get(listIndex).getInstruments().contains(splitUserChosenInstrumentsTrimmed.get(i)))
                {
                    break;
                }

                // Otherwise once every element has been looped through add this element to the list to be removed
                else
                {
                    if(i + 1 == splitUserChosenInstrumentsTrimmed.size())
                    {
                        if(!mFilteredMusiciansToRemove.contains(listIndex))
                        {
                            mFilteredMusiciansToRemove.add(listIndex);
                            break;
                        }

                        else
                        {
                            break;
                        }
                    }
                }
            }

            catch(NullPointerException e)
            {
                if(!mFilteredMusiciansToRemove.contains(listIndex) && i + 1 == splitUserChosenGenresTrimmed.size())
                {
                    mFilteredMusiciansToRemove.add(listIndex);
                    break;
                }
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        if(!mUserNameTextView.getText().equals("Your location!"))
        {
            MusicianUserMusicianDetailsFragment fragment = new MusicianUserMusicianDetailsFragment();
            Bundle arguments = new Bundle();

            for (int i = 0; i < mListOfMusicianMarkerInfo.size(); i++)
            {
                mListOfMusicianMarkerInfo.get(i);

                if (mListOfMusicianMarkerInfo.get(i).getMarkerId().equals(marker.getId()))
                {
                    arguments.putString("UserID", mListOfMusicianMarkerInfo.get(i).getUserId());
                    arguments.putString("UserName", mListOfMusicianMarkerInfo.get(i).getUserName());
                    arguments.putString("UserGenres", mListOfMusicianMarkerInfo.get(i).getUserGenres());
                    arguments.putString("UserInstruments", mListOfMusicianMarkerInfo.get(i).getUserInstruments());
                    arguments.putString("PositionInstruments", mPositionInstruments);
                    arguments.putString("BandId", mBandId);
                    arguments.putString("BandPosition", mBandPosition);
                    arguments.putDouble("Distance", mListOfMusicianMarkerInfo.get(i).getUserDistance());
                    arguments.putDouble("Lat", mListOfMusicianMarkerInfo.get(i).getUserLocation().getLatitude());
                    arguments.putDouble("Lng", mListOfMusicianMarkerInfo.get(i).getUserLocation().getLongitude());
                    fragment.setArguments(arguments);
                }
            }

            fragment.setArguments(arguments);

            // Creates a new fragment transaction to display the details of the selected
            // user. Some custom animation has been added also.
            FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                    .beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
            fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserMusicianDetailsFragment")
                    .addToBackStack(null).commit();
        }
    }
}
