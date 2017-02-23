package com.liamd.giggity_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PreSetupActivity extends AppCompatActivity
{
    // Declare general visual components
    private Button mSaveButton;
    private RadioButton mMusicianRadio;
    private RadioButton mVenueRadio;
    private ProgressDialog mProgressDialog;
    private TextView mCantFindVenueText;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;

    // Declare general user variables
    private String[] splitName;

    // Declare Musician specific visual components
    private MultiSelectSpinner mGenreSelectSpinner;
    private MultiSelectSpinner mInstrumentSelectSpinner;
    private List<String> mGenreList;
    private List<String> mInstrumentList;
    private TextView mGenreHeadingTextView;
    private TextView mInstrumentHeadingTextView;

    // Declare Venue specific visual components
    private TextView mVenueFinderHeadingTextView;
    private TextView mVenueDetailsTextView;
    private Button mPlaceFinderButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare Google specific variables
    private int PLACE_PICKER_REQUEST = 1;

    // Declare venue user data variables required
    private String mVenueName;
    private String mVenueID;

    // Declare musician user data variables required
    private String mMusicianUserAddress;
    private LatLng mMusicianUserLatLng;
    private String mInstrumentListToString;
    private String mGenreListToString;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_setup);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise general visual components
        setTitle("Account Preferences");
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mMusicianRadio = (RadioButton) findViewById(R.id.musicianRadio);
        mVenueRadio = (RadioButton) findViewById(R.id.venueRadio);
        mProgressDialog = new ProgressDialog(this);
        mCantFindVenueText = (TextView) findViewById(R.id.venueHelpTextView) ;

        // Initialise specific visual components
        mVenueFinderHeadingTextView = (TextView) findViewById(R.id.venueFinderHeadingTextView);
        mVenueDetailsTextView = (TextView) findViewById(R.id.venueDetails);
        mPlaceFinderButton = (Button) findViewById(R.id.placeFinderButton);
        mGenreSelectSpinner = (MultiSelectSpinner) findViewById(R.id.genreSpinner);
        mGenreHeadingTextView = (TextView) findViewById(R.id.genreHeadingTextView);
        mInstrumentSelectSpinner = (MultiSelectSpinner) findViewById(R.id.instrumentSpinner);
        mInstrumentHeadingTextView = (TextView) findViewById(R.id.instrumentHeadingTextView);
        mFirstNameEditText = (EditText) findViewById(R.id.firstNameTxt);
        mLastNameEditText = (EditText) findViewById(R.id.lastNameTxt);

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

        mGenreSelectSpinner.setItems(mGenreList);

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

        mInstrumentSelectSpinner.setItems(mInstrumentList);

        // Hide/Show specific visual components
        HideVenueUserComponents();
        ShowMusicianUserComponents();

        // Calls the populate name fields method to pre-fill the text boxes
        PopulateNameFields();


        mSaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // When the save button is clicked, the Save() method is called to handle the
                // insertion of data into the database.
                Save();
            }
        });

        // If the musician radio button is checked, call the method to display the relevant components
        mMusicianRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(mMusicianRadio.isChecked())
                {
                    // Show and hide the relevant visual components
                    HideVenueUserComponents();
                    ShowMusicianUserComponents();
                }
            }
        });

        // If the venue radio button is checked, call the method to display the relevant components
        mVenueRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(mVenueRadio.isChecked())
                {
                    // Show and hide the relevant visual components
                    HideMusicianUserComponents();
                    ShowVenueUserComponents();
                }
            }
        });

        // If the venue finder button is selected, call the LaunchPlacePicker to start
        // the place picker activity
        mPlaceFinderButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Displays the progress dialog
                mProgressDialog.show();

                if (mVenueRadio.isChecked())
                {
                    mProgressDialog.setMessage("Loading venue finder...");
                }

                else
                {
                    mProgressDialog.setMessage("Loading location finder...");
                }


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

        mCantFindVenueText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Creates a new dialog to display when the save button is clicked
                new AlertDialog.Builder(PreSetupActivity.this)
                        .setTitle("I Can't Find My Venue!")
                        .setMessage("If you can't find your venue on the venue finder, please use" +
                                " the closest location you can, and use this instead. The name can be" +
                                " overwritten later in the 'My Venues' section.")
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
    }

    // Method called when the save button is selected
    private void Save()
    {
        // Creates a new dialog to display when the save button is clicked
        new AlertDialog.Builder(PreSetupActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Set Preferences")
                .setMessage("Are you sure you want to set these preferences? By clicking 'Yes' you" +
                        " are also confirming that you are over 18 years of age.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Ensure that a value has been entered for first and last names
                        if(TextUtils.isEmpty(mFirstNameEditText.getText())
                                || TextUtils.isEmpty(mLastNameEditText.getText()))
                        {
                            Toast.makeText(PreSetupActivity.this,
                                    "Please enter a value for both first and last names!"
                                    , Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            // This block handles musician users
                            if (mMusicianRadio.isChecked())
                            {
                                // Get the selected items from the spinners as a string to store
                                // in the database. These can then be parsed later when required.
                                mInstrumentListToString = mInstrumentSelectSpinner.getSelectedItemsAsString();
                                mGenreListToString = mGenreSelectSpinner.getSelectedItemsAsString();

                                // ensure the location, genres, and instruments aren't null
                                if(mMusicianUserAddress != null && mMusicianUserLatLng != null &&
                                        !mInstrumentListToString.equals("") && !mGenreListToString.equals(""))
                                {
                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                            + "/accountType").setValue("Musician");

                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                            + "/firstName").setValue(mFirstNameEditText.getText().toString());

                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                            + "/lastName").setValue(mLastNameEditText.getText().toString());

                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                            + "/homeAddress").setValue(mMusicianUserAddress);

                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                            + "/homeLocation").setValue(mMusicianUserLatLng);

                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                             + "/instruments").setValue(mInstrumentListToString);

                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                             + "/genres").setValue(mGenreListToString);

                                    // The hasCompletedSetup field is changed to
                                    // true in the database for the currently logged in user
                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                            + "/hasCompletedSetup").setValue(true);

                                    // Calls the ReturnToMusicianUserMainActivity
                                    ReturnToMusicianUserMainActivity();
                                }

                                else
                                {
                                    Toast.makeText(PreSetupActivity.this,
                                            "Please ensure you have selected your instruments, genres," +
                                                    " and a valid location!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            // This block handles venue users
                            else
                            {
                                // Get the selected items from the spinners as a string to store
                                // in the database. These can then be parsed later when required.
                                mGenreListToString = mGenreSelectSpinner.getSelectedItemsAsString();

                                // Creates a venue object to store the venue information generated
                                // by the place finder
                                final Venue venue = new Venue();
                                venue.setName(mVenueName);
                                venue.setVenueID(mVenueID);
                                venue.setUserID(mAuth.getCurrentUser().getUid());

                                // Creates a venue user object to store the newly added user information
                                final VenueUser venueUser = new VenueUser();
                                venueUser.setFirstName(mFirstNameEditText.getText().toString());
                                venueUser.setLastName(mLastNameEditText.getText().toString());

                                // Check to ensure that a venue has been chosen
                                if(venue.getVenueID() == null)
                                {
                                    Toast.makeText(PreSetupActivity.this, "Please ensure you " +
                                            "have selected a valid venue!", Toast.LENGTH_SHORT).show();
                                }

                                // If a venue has been chosen insert the data into the database
                                else
                                {
                                    // Adds a database listener at the 'Venues/' database path
                                    mDatabase.child("Venues/").addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            // Checks to see whether a venue with the same ID already
                                            // exists at the database location. These ID's are unique
                                            // to each place through Google My Business
                                            if(!dataSnapshot.hasChild(mVenueID))
                                            {
                                                if(!mGenreListToString.equals(""))
                                                {
                                                    // If the place hasn't already been taken by another
                                                    // user, update the user's entry in the database
                                                    // with the account type selected (venue)
                                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                                            + "/accountType").setValue("Venue");

                                                    // Add a new entry under the 'Venues' node to store
                                                    // the data stored in the venue object above, with the
                                                    // venue ID as both the identifier, and as an attribute
                                                    mDatabase.child("Venues/" + mVenueID).setValue(venue);

                                                    // Stores the genres the venue plays
                                                    mDatabase.child("Venues/" + mVenueID + "/genre")
                                                            .setValue(mGenreListToString);

                                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                                            + "/firstName").setValue(mFirstNameEditText.getText().toString());

                                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                                            + "/lastName").setValue(mLastNameEditText.getText().toString());


                                                    // The hasCompletedSetup field is then changed to
                                                    // true in the database for the currently logged in user
                                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                                            + "/hasCompletedSetup").setValue(true);

                                                    // Calls the ReturnToVenueUserMainActivity
                                                    ReturnToVenueUserMainActivity();
                                                }

                                                else
                                                {
                                                    Toast.makeText(PreSetupActivity.this,
                                                            "Please ensure you have chosen the genres" +
                                                                    " your venue is associated with!",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            else
                                            {
                                                // Inform the user that this venue is already in use
                                                Toast.makeText(PreSetupActivity.this,
                                                        "Venue already in use! Please ensure that this" +
                                                                " is your venue to claim. If you believe" +
                                                                " you are the rightful proprietor then please" +
                                                                " contact a member of our team.",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError)
                                        {

                                        }
                                    });
                                }
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // close the dialog
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.venue_user_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // If sign out is selected, the user is signed out and the StartLoginActivity method
            // is called
            case R.id.action_logout:
                Logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Depending on the account type selected, launch the relevant activity
    private void ReturnToMusicianUserMainActivity()
    {
        finish();
        Intent startMusicianUserMainActivity = new Intent(PreSetupActivity.this, MusicianUserMainActivity.class);
        startActivity(startMusicianUserMainActivity);
    }

    //Depending on the account type selected, launch the relevant activity
    private void ReturnToVenueUserMainActivity()
    {
        finish();
        Intent startVenueUserMainActivity = new Intent(PreSetupActivity.this, VenueUserMainActivity.class);
        startActivity(startVenueUserMainActivity);
    }

    // Method to display musician user specific visual components
    private void ShowMusicianUserComponents()
    {
        mInstrumentSelectSpinner.setVisibility(View.VISIBLE);
        mInstrumentHeadingTextView.setVisibility(View.VISIBLE);
        mVenueFinderHeadingTextView.setText("Find Your Home Location");
        mVenueDetailsTextView.setText("No Location Chosen");
        mPlaceFinderButton.setText("Launch Location Finder");
    }

    // Method to hide musician user specific visual components
    private void HideMusicianUserComponents()
    {
        mInstrumentSelectSpinner.setVisibility(View.GONE);
        mInstrumentHeadingTextView.setVisibility(View.GONE);
    }

    // Method to display venue user specific visual components
    private void ShowVenueUserComponents()
    {
        // Show venue specific visual components
        mCantFindVenueText.setVisibility(View.VISIBLE);
        mVenueFinderHeadingTextView.setText("Find Your Venue");
        mVenueDetailsTextView.setText("No Venue Chosen");
        mPlaceFinderButton.setText("Launch Venue Finder");
    }

    // Method to hide venue user specific visual components
    private void HideVenueUserComponents()
    {
        mCantFindVenueText.setVisibility(View.GONE);
    }

    // Method to create a new instance of the place picker intent builder
    private void LaunchPlacePicker() throws GooglePlayServicesNotAvailableException,
            GooglePlayServicesRepairableException
    {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);

    }

    // Takes the result of the activity and stores the data
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PLACE_PICKER_REQUEST)
        {
            if (resultCode == RESULT_OK)
            {
                mProgressDialog.hide();
                Place place = PlacePicker.getPlace(data, this);

                // If the venue radio button is checked, display the name and the address of the
                // venue, and store the name and ID in the required variables
                if(mVenueRadio.isChecked())
                {
                    // Store the selected place details in the text view and variables
                    mVenueDetailsTextView.setText(place.getName() + " \n" + place.getAddress());
                    mVenueName = place.getName().toString();
                    mVenueID = place.getId();
                }

                // If the musician radio button is checked, display just the address of the
                // location, because otherwise it will just display a latlng.
                // Then store the name and ID in the required variables.
                else
                {
                    mVenueDetailsTextView.setText(place.getAddress());
                    mMusicianUserAddress = place.getAddress().toString();
                    mMusicianUserLatLng = place.getLatLng();
                }
            }
        }
    }

    // Pre enter the user's first and last names if they login through a service that offers
    // this functionality
    private void PopulateNameFields()
    {
        if(mAuth.getCurrentUser().getDisplayName() != null)
        {
            String nameFromAuth = mAuth.getCurrentUser().getDisplayName();
            splitName = nameFromAuth.split("\\s+");
            mFirstNameEditText.setText(splitName[0]);
            mLastNameEditText.setText(splitName[1]);
        }
    }

    private void Logout()
    {
        // Will log the user out of Gmail or email/password login
        FirebaseAuth.getInstance().signOut();

        // Will log the user out of facebook
        LoginManager.getInstance().logOut();

        // Returns to the login activity

        finish();
        Intent returnToLoginActivity= new Intent(PreSetupActivity.this, LoginActivity.class);
        returnToLoginActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(returnToLoginActivity);
    }
}
