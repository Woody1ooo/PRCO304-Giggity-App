package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserBandCreatorFragment extends Fragment
{
    // Declare general visual components
    private EditText mBandNameEditText;
    private MultiSelectSpinner mGenreSpinner;
    private Spinner mPositionsSpinner;
    private TextView mLocationChosenTextView;
    private Button mLaunchLocationFinderButton;
    private TextView mHelpTextView;
    private Button mCreateButton;
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

    public MusicianUserBandCreatorFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_creator, container, false);

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
        mCreateButton = (Button) fragmentView.findViewById(R.id.createButton);
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
                    mPositionOneSpinner.setItems(mInstrumentList);

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
                    mPositionOneSpinner.setItems(mInstrumentList);
                    mPositionTwoTitle.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setItems(mInstrumentList);

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
                    mPositionOneSpinner.setItems(mInstrumentList);
                    mPositionTwoTitle.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setItems(mInstrumentList);
                    mPositionThreeTitle.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setItems(mInstrumentList);

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
                    mPositionOneSpinner.setItems(mInstrumentList);
                    mPositionTwoTitle.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setItems(mInstrumentList);
                    mPositionThreeTitle.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setItems(mInstrumentList);
                    mPositionFourTitle.setVisibility(View.VISIBLE);
                    mPositionFourSpinner.setVisibility(View.VISIBLE);
                    mPositionFourSpinner.setItems(mInstrumentList);

                    // Hide the others
                    mPositionFiveTitle.setVisibility(View.GONE);
                    mPositionFiveSpinner.setVisibility(View.GONE);
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("5"))
                {
                    // Display the relevant visual components
                    mPositionOneTitle.setVisibility(View.VISIBLE);
                    mPositionOneSpinner.setVisibility(View.VISIBLE);
                    mPositionOneSpinner.setItems(mInstrumentList);
                    mPositionTwoTitle.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setItems(mInstrumentList);
                    mPositionThreeTitle.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setItems(mInstrumentList);
                    mPositionFourTitle.setVisibility(View.VISIBLE);
                    mPositionFourSpinner.setVisibility(View.VISIBLE);
                    mPositionFourSpinner.setItems(mInstrumentList);
                    mPositionFiveTitle.setVisibility(View.VISIBLE);
                    mPositionFiveSpinner.setVisibility(View.VISIBLE);
                    mPositionFiveSpinner.setItems(mInstrumentList);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
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
        mCreateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CreateBand();
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

    // When called, this should do all the relevant checks to create a band object and post it to the database
    private void CreateBand()
    {
        mBandID = mDatabase.child("Bands/").push().getKey();
        mBandName = mBandNameEditText.getText().toString();
        mGenres = mGenreSpinner.getSelectedItemsAsString();
        mNumberOfPositions = mPositionsSpinner.getSelectedItem().toString();

        // This checks to ensure that the main fields are filled in
        if(mBandName != null && mGenres != null && !mNumberOfPositions.equals("0") && mBandLocationLatLng != null)
        {
            // This checks which items are visible
            if(mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                mPositionTwoSpinner.getVisibility() == View.GONE &&
                mPositionThreeSpinner.getVisibility() == View.GONE &&
                mPositionFourSpinner.getVisibility() == View.GONE &&
                mPositionFiveSpinner.getVisibility() == View.GONE)
            {
                // If only position one is visible, the value is assigned to mPositionOneValue
                mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();

                // If that variable is not empty, create the object and insert it into the database
                if(!TextUtils.isEmpty(mPositionOneValue))
                {
                    Band bandToInsert = new Band(
                            mBandID,
                            mBandName,
                            mGenres,
                            mNumberOfPositions,
                            mPositionOneSpinner.getSelectedItemsAsString(),
                            mBandLocationLatLng);

                    mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                }

                // Otherwise display a message
                else
                {
                    Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                }
            }

            else if(mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                    mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                    mPositionThreeSpinner.getVisibility() == View.GONE &&
                    mPositionFourSpinner.getVisibility() == View.GONE &&
                    mPositionFiveSpinner.getVisibility() == View.GONE)
            {
                mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();

                if(!TextUtils.isEmpty(mPositionOneValue) &&
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
                }

                else
                {
                    Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                }
            }

            else if(mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                    mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                    mPositionThreeSpinner.getVisibility() == View.VISIBLE &&
                    mPositionFourSpinner.getVisibility() == View.GONE &&
                    mPositionFiveSpinner.getVisibility() == View.GONE)
            {
                mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();
                mPositionThreeValue = mPositionThreeSpinner.getSelectedItemsAsString();

                if(!TextUtils.isEmpty(mPositionOneValue) &&
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
                }

                else
                {
                    Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                }
            }

            else if(mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                    mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                    mPositionThreeSpinner.getVisibility() == View.VISIBLE &&
                    mPositionFourSpinner.getVisibility() == View.VISIBLE &&
                    mPositionFiveSpinner.getVisibility() == View.GONE)
            {
                mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();
                mPositionThreeValue = mPositionThreeSpinner.getSelectedItemsAsString();
                mPositionFourValue = mPositionFourSpinner.getSelectedItemsAsString();

                if(!TextUtils.isEmpty(mPositionOneValue) &&
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
                }

                else
                {
                    Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                }
            }

            else if(mPositionOneSpinner.getVisibility() == View.VISIBLE &&
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

                if(!TextUtils.isEmpty(mPositionOneValue)
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
}
