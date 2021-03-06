package com.liamd.giggity_app;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigFinderFragment extends Fragment implements LocationListener, DatePickerDialog.OnDateSetListener
{
    // Declare visual components
    private SeekBar mDistanceSeekbar;
    private TextView mDistanceTextView;
    private RadioButton mCurrentLocationRadio;
    private RadioButton mHomeLocationRadio;
    private MultiSelectSpinner mGenreSelectSpinner;
    private TextView mEarliestDateSelectedTextView;
    private Button mEarliestDateSelectorButton;
    private TextView mLatestDateSelectedTextView;
    private Button mLatestDateSelectorButton;
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
    private Date mEarliestDate;
    private Date mLatestDate;
    private boolean mIsEarliestDate;
    private boolean mIsInBand = true;
    private boolean mIsFanAccount;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

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

        // If the user holds a fan account set the flag
        if (getArguments().getString("UserType").equals("Fan"))
        {
            mIsFanAccount = true;
        }

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        mDistanceSeekbar = (SeekBar) fragmentView.findViewById(R.id.distanceSeekBar);
        mDistanceTextView = (TextView) fragmentView.findViewById(R.id.distanceValueTextView);
        mCurrentLocationRadio = (RadioButton) fragmentView.findViewById(R.id.currentLocationRadio);
        mHomeLocationRadio = (RadioButton) fragmentView.findViewById(R.id.homeLocationRadio);
        mGenreSelectSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);
        mEarliestDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.EarliestDateSelectedTextView);
        mEarliestDateSelectorButton = (Button) fragmentView.findViewById(R.id.SelectEarliestDateButton);
        mLatestDateSelectedTextView = (TextView) fragmentView.findViewById(R.id.LatestDateSelectedTextView);
        mLatestDateSelectorButton = (Button) fragmentView.findViewById(R.id.SelectLatestDateButton);
        mSearchButton = (Button) fragmentView.findViewById(R.id.searchButton);

        // Initialise various variables
        mLocationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);

        // Add items to the genre list, and set the spinner to use these
        mGenreList = new ArrayList<>();
        mGenreList.add("Acoustic");
        mGenreList.add("Alternative Rock");
        mGenreList.add("Blues");
        mGenreList.add("Classic Rock");
        mGenreList.add("Classical");
        mGenreList.add("Country");
        mGenreList.add("Death Metal");
        mGenreList.add("Disco");
        mGenreList.add("Electronic");
        mGenreList.add("Folk");
        mGenreList.add("Funk");
        mGenreList.add("Garage");
        mGenreList.add("Grunge");
        mGenreList.add("Hip-Hop");
        mGenreList.add("House");
        mGenreList.add("Indie");
        mGenreList.add("Jazz");
        mGenreList.add("Metal");
        mGenreList.add("Pop");
        mGenreList.add("Psychedelic Rock");
        mGenreList.add("Punk");
        mGenreList.add("Rap");
        mGenreList.add("Reggae");
        mGenreList.add("R&B");
        mGenreList.add("Ska");
        mGenreList.add("Techno");
        mGenreList.add("Thrash Metal");
        mGenreSelectSpinner.setItems(mGenreList);

        // Initialise the date values and UI components
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        mEarliestDate = new Date();
        mLatestDate = new Date();

        // Initialise the date variables
        mEarliestDate.setDate(date.getDate());
        mEarliestDate.setMonth(date.getMonth());
        mEarliestDate.setYear(date.getYear());
        mLatestDate.setDate(date.getDate());
        mLatestDate.setMonth(date.getMonth());
        mLatestDate.setYear(date.getYear());

        // Set the visual components to the current date
        mEarliestDateSelectedTextView.setText(date.getDate() + "/" + (date.getMonth() + 1) + "/" + (date.getYear() + 1900));
        mLatestDateSelectedTextView.setText(date.getDate() + "/" + (date.getMonth() + 1) + "/" + (date.getYear() + 1900));

        // This method gets the users current/last known location
        GetUserCurrentLocation();

        mDatabase.child("Users/").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This method populates the genre spinners with the genres the user
                // selected when setting up their account
                mGenreSelectSpinner.setSelection(PopulateUserGenreData(dataSnapshot));

                // This checks the database to see if the user is currently a member of a band.
                // If not inform them that they can't apply for gig opportunities.
                if (dataSnapshot.child(mAuth.getCurrentUser().getUid() + "/inBand").getValue().equals(false))
                {
                    if (getActivity() != null && !mIsFanAccount)
                    {
                        // A dialog is then shown to alert the user that the changes have been made
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Notice");
                        builder.setIcon(R.drawable.ic_info_outline_black_24px);
                        builder.setMessage("Please note that as you are not currently a member of a band or a solo artist, you cannot apply for any gig opportunities." +
                                " You are however still able to browse.");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {

                            }
                        });
                        builder.show();

                        mIsInBand = false;
                    }
                }

                // This method gets the value from the database of the users set home location
                // and assigns its value to mHomeLocation
                GetUserHomeLocation(dataSnapshot);
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

                if (mDistanceSelected == 250)
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

        // When the date button is selected load the calendar widget
        mEarliestDateSelectorButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mIsEarliestDate = true;

                // The calendar is then initialised with today's date
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        MusicianUserGigFinderFragment.this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                dpd.show(getActivity().getFragmentManager(), "DatePickerDialog");

                // By setting the minimum date to today, it prevents gigs being
                // searched for in the past
                dpd.setMinDate(Calendar.getInstance());
            }
        });

        // When the date button is selected load the calendar widget
        mLatestDateSelectorButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mIsEarliestDate = false;

                // The calendar is then initialised with today's date
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        MusicianUserGigFinderFragment.this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                dpd.show(getActivity().getFragmentManager(), "DatePickerDialog");

                // By setting the minimum date to today, it prevents gigs being
                // searched for in the past
                dpd.setMinDate(Calendar.getInstance());
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

        // Set the fragment title
        getActivity().setTitle("Gig Finder");

        return fragmentView;
    }

    // This method calls the getLastKnownLocation method which returns a location object. This
    // is then split into a latitude and longitude to be passed to the maps fragment after search is
    // selected.
    private void GetUserCurrentLocation()
    {
        location = getLastKnownLocation();

        if (location == null)
        {
            Toast.makeText(getActivity(), "Are you sure you have location services enabled?" +
                    " We can't find you!", Toast.LENGTH_SHORT).show();
        } else
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
        Boolean isGPSEnabled;
        Boolean isNetworkEnabled;

        try
        {
            mLocationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled)
            {
                // no network provider is enabled
            }

            else
            {
                if (isNetworkEnabled)
                {
                    if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }

                    else
                    {
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network Enabled");

                        if (mLocationManager != null)
                        {
                            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled)
                {
                    if (location == null)
                    {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS", "GPS Enabled");

                        if (mLocationManager != null)
                        {
                            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

        return location;
    }

    // This method should process the result of the permission selected, though
    // at the moment doesn't get called for some reason
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        {
            if (permissions.length == 1 && permissions[0].equals(android.Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                GetUserCurrentLocation();
            }

            else
            {
                Toast.makeText(getActivity(), "If you wish to use your current location," +
                        " please ensure you have given the permission.", Toast.LENGTH_SHORT).show();

                mHomeLocationRadio.isSelected();
                mCurrentLocationRadio.setEnabled(false);
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
        for (int i = 0; i < splitUserPulledGenres.size(); i++)
        {
            String formattedGenreStringToAdd;

            formattedGenreStringToAdd = splitUserPulledGenres.get(i).trim();

            splitUserPulledGenresFormatted.add(formattedGenreStringToAdd);
        }

        return splitUserPulledGenresFormatted;
    }

    private void Search()
    {
        if(!mEarliestDate.after(mLatestDate) || !mLatestDate.before(mEarliestDate))
        {
            String mGenreListString = mGenreSelectSpinner.getSelectedItemsAsString();

            // This then stores the id of the selected gig in a bundle which is then
            // passed to the result fragment to display the gig details
            MusicianUserGigResultsFragment fragment = new MusicianUserGigResultsFragment();
            Bundle arguments = new Bundle();

            arguments.putString("Genres", mGenreListString);

            if (mCurrentLocationRadio.isChecked())
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
            arguments.putBoolean("IsInBand", mIsInBand);
            arguments.putString("EarliestDate", mEarliestDate.toString());
            arguments.putString("LatestDate", mLatestDate.toString());

            // If the flag is set pass this value on
            if (mIsFanAccount)
            {
                arguments.putString("UserType", "Fan");
            } else
            {
                arguments.putString("UserType", "Musician");
            }

            fragment.setArguments(arguments);

            // Creates a new fragment transaction to display the details of the selected
            // preferences. Some custom animation has been added also.
            FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                    .beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
            fragmentTransaction.replace(R.id.frame, fragment, "GigResultsMapFragment")
                    .addToBackStack(null).commit();
        }

        else
        {
            Toast.makeText(getActivity(), "Please ensure that the dates are set correctly!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth)
    {
        // These variables store the dates selected on the picker
        String mYearSelected = Integer.toString(year);
        String mMonthSelected = Integer.toString(monthOfYear + 1);
        String mDaySelected = Integer.toString(dayOfMonth);

        // if the isStartDate boolean is true, this means the start date button was selected, therefore the relevant variables are populated
        if(mIsEarliestDate)
        {
            mEarliestDateSelectedTextView.setText(mDaySelected + "/" + mMonthSelected + "/" + mYearSelected);
            mEarliestDate.setDate(dayOfMonth);
            mEarliestDate.setMonth(monthOfYear);
            mEarliestDate.setYear(year - 1900);
        }

        else
        {
            mLatestDateSelectedTextView.setText(mDaySelected + "/" + mMonthSelected + "/" + mYearSelected);
            mLatestDate.setDate(dayOfMonth);
            mLatestDate.setMonth(monthOfYear);
            mLatestDate.setYear(year - 1900);
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }

    @Override
    public void onProviderEnabled(String s)
    {

    }

    @Override
    public void onProviderDisabled(String s)
    {

    }
}