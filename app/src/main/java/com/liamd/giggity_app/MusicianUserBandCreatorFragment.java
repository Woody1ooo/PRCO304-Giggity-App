package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.content.res.TypedArrayUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserBandCreatorFragment extends Fragment implements YouTubePlayer.OnInitializedListener
{
    // Declare general visual components
    private EditText mBandNameEditText;
    private MultiSelectSpinner mGenreSpinner;
    private Spinner mPositionsSpinner;
    private TextView mLocationChosenTextView;
    private Button mLaunchLocationFinderButton;
    private TextView mHelpTextView;
    private Button checkUrlButton;
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
    private Spinner mUserChosenPositionSpinner;
    private EditText youtubeUrlEditText;

    // Declare general variables
    private List<String> mGenreList;
    private List<String> mInstrumentList;
    private ArrayList<String> mUserPositionList;
    private ArrayAdapter<String> mUserPositionAdapter;
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
    private String youtubeUrlEntered;

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
        checkUrlButton = (Button) fragmentView.findViewById(R.id.checkUrlButton);
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
        mUserChosenPositionSpinner = (Spinner) fragmentView.findViewById(R.id.userPositionSpinner);
        youtubeUrlEditText = (EditText) fragmentView.findViewById(R.id.youtubeUrlEditText);

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

        // This list items are added here to prepare for obtaining the users chosen position
        mUserPositionList = new ArrayList<>();
        mUserPositionList.add("Position One");
        mUserPositionList.add("Position Two");
        mUserPositionList.add("Position Three");
        mUserPositionList.add("Position Four");
        mUserPositionList.add("Position Five");

        // If one position is selected then this is the element added to the spinner for the user to select
        mUserPositionAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, mUserPositionList);
        mUserPositionAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mUserChosenPositionSpinner.setAdapter(mUserPositionAdapter);

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

        checkUrlButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!TextUtils.isEmpty(youtubeUrlEditText.getText()))
                {
                    youtubeUrlEntered = ParseURL(youtubeUrlEditText.getText());
                    LoadYoutubePlayer();
                }
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
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Create Band");
        builder.setMessage("Are you sure you wish to create this band?");
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                mBandID = mDatabase.child("Bands/").push().getKey();
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
                            mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue("Vacant");

                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());
                            }

                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                                    // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Created!");
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
                            mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue("Vacant");

                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());
                            }

                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Created!");
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
                            mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionThreeMember").setValue("Vacant");

                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());
                            }

                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Created!");
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
                            mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionThreeMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionFourMember").setValue("Vacant");

                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());

                            }
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Created!");
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
                            mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionThreeMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionFourMember").setValue("Vacant");
                            mDatabase.child("Bands/" + mBandID + "/positionFiveMember").setValue("Vacant");

                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());
                            }

                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/isInBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Created!");
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

    // If the youtube initialisation is successful load the URL from the text box if there is one
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored)
    {
        // Determines whether the player was restored from a saved state. If not cue the video
        if (!wasRestored)
        {
            youTubePlayer.cueVideo(youtubeUrlEntered);
        }
    }

    // If the youtube initialisation fails this is called. Usually due to not having youtube installed
    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult)
    {
        Toast.makeText(getActivity(), "The YouTube player can't be initialised! Please ensure you have the YouTube app installed.", Toast.LENGTH_LONG).show();
    }

    // Using some REGEX this trims the youtube url entered to just get the video id at the end
    private String ParseURL(CharSequence youtubeURL)
    {
        String videoIdPattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

        Pattern compiledPattern = Pattern.compile(videoIdPattern);
        Matcher matcher = compiledPattern.matcher(youtubeURL);

        if (matcher.find())
        {
            return matcher.group();
        }

        // If the URL doesn't match this it means the url is probably a share link which is shortened
        // This block will determine this if it's the case
        else
        {
            String URL;
            String[] parsedURL;

            URL = youtubeURL.toString();
            parsedURL = URL.split("/");

            return parsedURL[3];
        }
    }

    // This method initialises the player using the api key, relevant layout, fragment etc
    private void LoadYoutubePlayer()
    {
        // Initialise and setup the embedded youtube player
        YouTubePlayerFragment youtubePlayerFragment = new YouTubePlayerFragment();
        youtubePlayerFragment.initialize(getString(R.string.api_key), this);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.youtubeLayout, youtubePlayerFragment);
        fragmentTransaction.commit();
    }

    private void ReturnToHome()
    {
        getActivity().finish();
        getActivity().overridePendingTransition(0,0);

        Intent intent = new Intent(getActivity(), MusicianUserMainActivity.class);
        startActivity(intent);

        getFragmentManager().popBackStackImmediate();
    }
}
