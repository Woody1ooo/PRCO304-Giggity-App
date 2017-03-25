package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserBandManagementFragment extends Fragment
{
    // Declare general visual components
    private EditText mBandNameEditText;
    private MultiSelectSpinner mGenreSpinner;
    private Spinner mPositionsSpinner;
    private TextView mLocationChosenTextView;
    private Button mLaunchLocationFinderButton;
    private TextView mHelpTextView;
    private Button mUpdateButton;
    private ProgressDialog mProgressDialog;
    private TextView mPositionOneTitle;
    private MultiSelectSpinner mPositionOneSpinner;
    private TextView mPositionTwoTitle;
    private MultiSelectSpinner mPositionTwoSpinner;
    private TextView mPositionThreeTitle;
    private MultiSelectSpinner mPositionThreeSpinner;
    private TextView mPositionFourTitle;
    private MultiSelectSpinner mPositionFourSpinner;
    private TextView mPositionFiveTitle;
    private MultiSelectSpinner mPositionFiveSpinner;

    // Declare general variables
    private List<String> mGenreList;
    private List<String> mInstrumentList;
    private Band mBandFromDatabase;
    private int PLACE_PICKER_REQUEST = 0;
    private String mBandID;
    private String mBandName;
    private String mGenres;
    private String mNumberOfPositions;
    private LatLng mBandLocationLatLng;
    private String mPositionOneValue;
    private String mPositionTwoValue;
    private String mPositionThreeValue;
    private String mPositionFourValue;
    private String mPositionFiveValue;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public MusicianUserBandManagementFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_management, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mBandNameEditText = (EditText) fragmentView.findViewById(R.id.bandNameEditText);
        mGenreSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);
        mPositionsSpinner = (Spinner) fragmentView.findViewById(R.id.bandPositionSpinner);
        mLocationChosenTextView = (TextView) fragmentView.findViewById(R.id.bandLocationDetailsTextView);
        mLaunchLocationFinderButton = (Button) fragmentView.findViewById(R.id.placeFinderButton);
        mHelpTextView = (TextView) fragmentView.findViewById(R.id.locationHelpTextView);
        mUpdateButton = (Button) fragmentView.findViewById(R.id.updateButton);
        mProgressDialog = new ProgressDialog(getActivity());
        mPositionOneTitle = (TextView) fragmentView.findViewById(R.id.positionOneTextView);
        mPositionOneSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionOneSpinner);
        mPositionTwoTitle = (TextView) fragmentView.findViewById(R.id.positionTwoTextView);
        mPositionTwoSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionTwoSpinner);
        mPositionThreeTitle = (TextView) fragmentView.findViewById(R.id.positionThreeTextView);
        mPositionThreeSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionThreeSpinner);
        mPositionFourTitle = (TextView) fragmentView.findViewById(R.id.positionFourTextView);
        mPositionFourSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionFourSpinner);
        mPositionFiveTitle = (TextView) fragmentView.findViewById(R.id.positionFiveTextView);
        mPositionFiveSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionFiveSpinner);

        mProgressDialog.show();
        mProgressDialog.setMessage("Loading...");

        // Initially hide all the position spinners/text views until the number chosen is selected from the spinner
        mPositionOneTitle.setVisibility(View.GONE);
        mPositionOneSpinner.setVisibility(View.GONE);
        mPositionTwoTitle.setVisibility(View.GONE);
        mPositionTwoSpinner.setVisibility(View.GONE);
        mPositionThreeTitle.setVisibility(View.GONE);
        mPositionThreeSpinner.setVisibility(View.GONE);
        mPositionFourTitle.setVisibility(View.GONE);
        mPositionFourSpinner.setVisibility(View.GONE);
        mPositionFiveTitle.setVisibility(View.GONE);
        mPositionFiveSpinner.setVisibility(View.GONE);

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

        mGenreSpinner.setItems(mGenreList);

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

        mPositionOneSpinner.setItems(mInstrumentList);
        mPositionTwoSpinner.setItems(mInstrumentList);
        mPositionThreeSpinner.setItems(mInstrumentList);
        mPositionFourSpinner.setItems(mInstrumentList);
        mPositionFiveSpinner.setItems(mInstrumentList);

        // This gets the number from the band positions spinner and then displays/hides the relevant components as needed
        mPositionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                if(mPositionsSpinner.getItemAtPosition(position).equals("0"))
                {
                    mPositionOneTitle.setVisibility(View.GONE);
                    mPositionOneSpinner.setVisibility(View.GONE);
                    mPositionTwoTitle.setVisibility(View.GONE);
                    mPositionTwoSpinner.setVisibility(View.GONE);
                    mPositionThreeTitle.setVisibility(View.GONE);
                    mPositionThreeSpinner.setVisibility(View.GONE);
                    mPositionFourTitle.setVisibility(View.GONE);
                    mPositionFourSpinner.setVisibility(View.GONE);
                    mPositionFiveTitle.setVisibility(View.GONE);
                    mPositionFiveSpinner.setVisibility(View.GONE);
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("1"))
                {
                    // Display the relevant visual components
                    mPositionOneTitle.setVisibility(View.VISIBLE);
                    mPositionOneSpinner.setVisibility(View.VISIBLE);

                    // Hide the others
                    mPositionTwoTitle.setVisibility(View.GONE);
                    mPositionTwoSpinner.setVisibility(View.GONE);
                    mPositionThreeTitle.setVisibility(View.GONE);
                    mPositionThreeSpinner.setVisibility(View.GONE);
                    mPositionFourTitle.setVisibility(View.GONE);
                    mPositionFourSpinner.setVisibility(View.GONE);
                    mPositionFiveTitle.setVisibility(View.GONE);
                    mPositionFiveSpinner.setVisibility(View.GONE);
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("2"))
                {
                    // Display the relevant visual components
                    mPositionOneTitle.setVisibility(View.VISIBLE);
                    mPositionOneSpinner.setVisibility(View.VISIBLE);
                    mPositionTwoTitle.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setVisibility(View.VISIBLE);

                    // Hide the others
                    mPositionThreeTitle.setVisibility(View.GONE);
                    mPositionThreeSpinner.setVisibility(View.GONE);
                    mPositionFourTitle.setVisibility(View.GONE);
                    mPositionFourSpinner.setVisibility(View.GONE);
                    mPositionFiveTitle.setVisibility(View.GONE);
                    mPositionFiveSpinner.setVisibility(View.GONE);
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("3"))
                {
                    // Display the relevant visual components
                    mPositionOneTitle.setVisibility(View.VISIBLE);
                    mPositionOneSpinner.setVisibility(View.VISIBLE);
                    mPositionTwoTitle.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setVisibility(View.VISIBLE);
                    mPositionThreeTitle.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setVisibility(View.VISIBLE);

                    // Hide the others
                    mPositionFourTitle.setVisibility(View.GONE);
                    mPositionFourSpinner.setVisibility(View.GONE);
                    mPositionFiveTitle.setVisibility(View.GONE);
                    mPositionFiveSpinner.setVisibility(View.GONE);
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("4"))
                {
                    // Display the relevant visual components
                    mPositionOneTitle.setVisibility(View.VISIBLE);
                    mPositionOneSpinner.setVisibility(View.VISIBLE);
                    mPositionTwoTitle.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setVisibility(View.VISIBLE);
                    mPositionThreeTitle.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setVisibility(View.VISIBLE);
                    mPositionFourTitle.setVisibility(View.VISIBLE);
                    mPositionFourSpinner.setVisibility(View.VISIBLE);

                    // Hide the others
                    mPositionFiveTitle.setVisibility(View.GONE);
                    mPositionFiveSpinner.setVisibility(View.GONE);
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("5"))
                {
                    // Display the relevant visual components
                    mPositionOneTitle.setVisibility(View.VISIBLE);
                    mPositionOneSpinner.setVisibility(View.VISIBLE);
                    mPositionTwoTitle.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setVisibility(View.VISIBLE);
                    mPositionThreeTitle.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setVisibility(View.VISIBLE);
                    mPositionFourTitle.setVisibility(View.VISIBLE);
                    mPositionFourSpinner.setVisibility(View.VISIBLE);
                    mPositionFiveTitle.setVisibility(View.VISIBLE);
                    mPositionFiveSpinner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mBandID = dataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").getValue().toString();
                mBandFromDatabase = new Band();
                mBandFromDatabase = dataSnapshot.child("Bands/" + mBandID).getValue(Band.class);
                PopulateFields();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // When clicked this launches the place picker
        mLaunchLocationFinderButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Displays the progress dialog
                mProgressDialog.show();
                mProgressDialog.setMessage("Loading location finder...");

                try
                {
                    LaunchPlacePicker();
                }

                catch (GooglePlayServicesNotAvailableException e)
                {
                    e.printStackTrace();
                }

                catch (GooglePlayServicesRepairableException e)
                {
                    e.printStackTrace();
                }
            }
        });

        // When clicked this displays a message helping the user
        mHelpTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Creates a new dialog to display when the save button is clicked
                new AlertDialog.Builder(getActivity())
                        .setTitle("What does this mean?")
                        .setMessage("In order for your band to find gigs and members, you need to have a base location." +
                                " This is effectively the location that your band members will need to get to for rehearsals, band meetings etc.")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(R.drawable.ic_info_outline_black_24dp)
                        .show();
            }
        });

        // When clicked this calls the create band method
        mUpdateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                UpdateBand();
            }
        });

        return fragmentView;
    }

    // Method to create a new instance of the place picker intent builder
    private void LaunchPlacePicker() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException
    {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        mProgressDialog.hide();

        // If the request is for the place picker (i.e. if it matches the request code)
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK && null != data)
        {
            com.google.android.gms.maps.model.LatLng latLngChosenHolder;

            // Store the relevant location data in the variables and display the address of the location
            // because otherwise it will just display a latlng
            mProgressDialog.hide();
            Place place = PlacePicker.getPlace(data, getActivity());

            mBandLocationLatLng = new LatLng();

            mLocationChosenTextView.setText(place.getAddress());
            latLngChosenHolder = place.getLatLng();

            double placeLat = latLngChosenHolder.latitude;
            double placeLng = latLngChosenHolder.longitude;

            mBandLocationLatLng.setLatitude(placeLat);
            mBandLocationLatLng.setLongitude(placeLng);
        }
    }

    private void PopulateFields()
    {
        mBandName = mBandFromDatabase.getName();
        mBandNameEditText.setText(mBandName);

        mGenreSpinner.setSelection(GetGenres());

        mNumberOfPositions = mBandFromDatabase.getNumberOfPositions();
        mPositionsSpinner.setSelection(getIndex(mPositionsSpinner, mNumberOfPositions));

        GetPositionInstruments();

        mBandLocationLatLng = mBandFromDatabase.getBaseLocation();

        mLocationChosenTextView.setText(GetAddressFromLatLng(mBandLocationLatLng));

        mProgressDialog.hide();
    }

    // This method determines how many positions are selected and then populates each spinner with a string array
    private void GetPositionInstruments()
    {
        List<String> splitUserPulledInstrumentsOne;
        List<String> splitUserPulledInstrumentsTwo;
        List<String> splitUserPulledInstrumentsThree;
        List<String> splitUserPulledInstrumentsFour;
        List<String> splitUserPulledInstrumentsFive;

        // This then splits this string into an array of strings, each separated by a comma
        if(mPositionsSpinner.getSelectedItem().equals("1"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);
        }

        else if(mPositionsSpinner.getSelectedItem().equals("2"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);

            splitUserPulledInstrumentsTwo = Arrays.asList(mBandFromDatabase.getPositionTwo().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsTwoFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsTwo.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsTwo.get(i).trim();
                splitUserPulledInstrumentsTwoFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionTwoSpinner.setSelection(splitUserPulledInstrumentsTwoFormatted);
        }

        else if(mPositionsSpinner.getSelectedItem().equals("3"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);

            splitUserPulledInstrumentsTwo = Arrays.asList(mBandFromDatabase.getPositionTwo().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsTwoFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsTwo.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsTwo.get(i).trim();
                splitUserPulledInstrumentsTwoFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionTwoSpinner.setSelection(splitUserPulledInstrumentsTwoFormatted);

            splitUserPulledInstrumentsThree = Arrays.asList(mBandFromDatabase.getPositionThree().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsThreeFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsThree.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsThree.get(i).trim();
                splitUserPulledInstrumentsThreeFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionThreeSpinner.setSelection(splitUserPulledInstrumentsThreeFormatted);
        }

        else if(mPositionsSpinner.getSelectedItem().equals("4"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);

            splitUserPulledInstrumentsTwo = Arrays.asList(mBandFromDatabase.getPositionTwo().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsTwoFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsTwo.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsTwo.get(i).trim();
                splitUserPulledInstrumentsTwoFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionTwoSpinner.setSelection(splitUserPulledInstrumentsTwoFormatted);

            splitUserPulledInstrumentsThree = Arrays.asList(mBandFromDatabase.getPositionThree().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsThreeFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsThree.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsThree.get(i).trim();
                splitUserPulledInstrumentsThreeFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionThreeSpinner.setSelection(splitUserPulledInstrumentsThreeFormatted);

            splitUserPulledInstrumentsFour = Arrays.asList(mBandFromDatabase.getPositionFour().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsFourFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsFour.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsFour.get(i).trim();
                splitUserPulledInstrumentsFourFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionFourSpinner.setSelection(splitUserPulledInstrumentsFourFormatted);
        }

        else if(mPositionsSpinner.getSelectedItem().equals("5"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);

            splitUserPulledInstrumentsTwo = Arrays.asList(mBandFromDatabase.getPositionTwo().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsTwoFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsTwo.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsTwo.get(i).trim();
                splitUserPulledInstrumentsTwoFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionTwoSpinner.setSelection(splitUserPulledInstrumentsTwoFormatted);

            splitUserPulledInstrumentsThree = Arrays.asList(mBandFromDatabase.getPositionThree().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsThreeFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsThree.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsThree.get(i).trim();
                splitUserPulledInstrumentsThreeFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionThreeSpinner.setSelection(splitUserPulledInstrumentsThreeFormatted);

            splitUserPulledInstrumentsFour = Arrays.asList(mBandFromDatabase.getPositionFour().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsFourFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsFour.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsFour.get(i).trim();
                splitUserPulledInstrumentsFourFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionFourSpinner.setSelection(splitUserPulledInstrumentsFourFormatted);

            splitUserPulledInstrumentsFive = Arrays.asList(mBandFromDatabase.getPositionFive().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsFiveFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsFive.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsFive.get(i).trim();
                splitUserPulledInstrumentsFiveFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionFiveSpinner.setSelection(splitUserPulledInstrumentsFiveFormatted);
        }
    }

    // This takes the genres from the database and
    private ArrayList<String> GetGenres()
    {
        // This then splits this string into an array of strings, each separated by
        // a comma
        List<String> splitUserPulledGenres = Arrays.asList(mBandFromDatabase.getGenres().split(","));

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

    // This takes the latlng stored in the database to get the address using Google's Geocoder
    private String GetAddressFromLatLng(LatLng latLng)
    {
        Geocoder geocoder;
        List<Address> addresses;
        String address = "";
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try
        {
            addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

        return address;
    }

    // This gets the index of a spinner that contains a particular value
    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0; i<spinner.getCount(); i++)
        {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    // When called, this should do all the relevant checks to create a band object and post it to the database
    private void UpdateBand()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update Band");
        builder.setMessage("Are you sure you wish to update these fields?");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                mBandName = mBandNameEditText.getText().toString();
                mGenres = mGenreSpinner.getSelectedItemsAsString();
                mNumberOfPositions = mPositionsSpinner.getSelectedItem().toString();

                // This checks to ensure that the main fields are filled in
                if (mBandName != null && mGenres != null && !mNumberOfPositions.equals("0") && mBandLocationLatLng != null)
                {
                    // This checks which items are visible
                    if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.GONE &&
                            mPositionThreeSpinner.getVisibility() == View.GONE &&
                            mPositionFourSpinner.getVisibility() == View.GONE &&
                            mPositionFiveSpinner.getVisibility() == View.GONE)
                    {
                        // If only position one is visible, the value is assigned to mPositionOneValue
                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();

                        // If that variable is not empty, create the object and insert it into the database
                        if (!TextUtils.isEmpty(mPositionOneValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }

                        // Otherwise display a message
                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    else if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                            mPositionThreeSpinner.getVisibility() == View.GONE &&
                            mPositionFourSpinner.getVisibility() == View.GONE &&
                            mPositionFiveSpinner.getVisibility() == View.GONE)
                    {
                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                        mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();

                        if (!TextUtils.isEmpty(mPositionOneValue) &&
                                !TextUtils.isEmpty(mPositionTwoValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mPositionTwoSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }

                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    else if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                            mPositionThreeSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFourSpinner.getVisibility() == View.GONE &&
                            mPositionFiveSpinner.getVisibility() == View.GONE)
                    {
                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                        mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();
                        mPositionThreeValue = mPositionThreeSpinner.getSelectedItemsAsString();

                        if (!TextUtils.isEmpty(mPositionOneValue) &&
                                !TextUtils.isEmpty(mPositionTwoValue) &&
                                !TextUtils.isEmpty(mPositionThreeValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mPositionTwoSpinner.getSelectedItemsAsString(),
                                    mPositionThreeSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }

                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    } else if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                            mPositionThreeSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFourSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFiveSpinner.getVisibility() == View.GONE)
                    {
                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                        mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();
                        mPositionThreeValue = mPositionThreeSpinner.getSelectedItemsAsString();
                        mPositionFourValue = mPositionFourSpinner.getSelectedItemsAsString();

                        if (!TextUtils.isEmpty(mPositionOneValue) &&
                                !TextUtils.isEmpty(mPositionTwoValue) &&
                                !TextUtils.isEmpty(mPositionThreeValue) &&
                                !TextUtils.isEmpty(mPositionFourValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mPositionTwoSpinner.getSelectedItemsAsString(),
                                    mPositionThreeSpinner.getSelectedItemsAsString(),
                                    mPositionFourSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }

                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                            mPositionThreeSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFourSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFiveSpinner.getVisibility() == View.VISIBLE)
                    {

                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                        mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();
                        mPositionThreeValue = mPositionThreeSpinner.getSelectedItemsAsString();
                        mPositionFourValue = mPositionFourSpinner.getSelectedItemsAsString();
                        mPositionFiveValue = mPositionFiveSpinner.getSelectedItemsAsString();

                        if (!TextUtils.isEmpty(mPositionOneValue)
                                && !TextUtils.isEmpty(mPositionTwoValue)
                                && !TextUtils.isEmpty(mPositionThreeValue)
                                && !TextUtils.isEmpty(mPositionFourValue)
                                && !TextUtils.isEmpty(mPositionFiveValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mPositionTwoSpinner.getSelectedItemsAsString(),
                                    mPositionThreeSpinner.getSelectedItemsAsString(),
                                    mPositionFourSpinner.getSelectedItemsAsString(),
                                    mPositionFiveSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                else
                {
                    Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                }
            }
        })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // close the dialog
                    }
                }).show();
    }

    private void ReturnToHome()
    {
        // The user is then taken to the home fragment
        getActivity().setTitle("Home");
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frame, new MusicianUserHomeFragment(), "MusicianUserHomeFragment");
        ft.commit();
    }
}
