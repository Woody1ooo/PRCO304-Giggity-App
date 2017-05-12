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
import android.widget.NumberPicker;
import android.widget.RadioButton;
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
    private RadioButton mFanRadio;
    private RadioButton mMusicianRadio;
    private RadioButton mVenueRadio;
    private ProgressDialog mProgressDialog;
    private TextView mCantFindVenueText;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private TextView mVenueNameHeadingTextView;
    private EditText mVenueNameEditText;
    private TextView mFirstNameHeadingTextView;
    private TextView mLastNameHeadingTextView;
    private TextView mAgeHeadingTextView;
    private TextView mAgeSelectedTextView;
    private NumberPicker mAgeNumberPicker;

    // Declare general user variables
    private String[] splitName;

    // Declare Musician specific visual components
    private MultiSelectSpinner mGenreSelectSpinner;
    private MultiSelectSpinner mInstrumentSelectSpinner;
    private List<String> mGenreList;
    private List<String> mInstrumentList;
    private TextView mInstrumentHeadingTextView;

    // Declare Venue specific visual components
    private TextView mVenueFinderHeadingTextView;
    private TextView mVenueDetailsTextView;
    private Button mPlaceFinderButton;
    private TextView mVenueCapacityHeadingTextView;
    private TextView mVenueCapacitySelectedTextView;
    private NumberPicker mVenueCapacityNumberPicker;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare Google specific variables
    private int PLACE_PICKER_REQUEST = 1;

    // Declare venue user data variables required
    private String mVenueName;
    private String mVenueID;
    private LatLng mVenueLocation;

    // This variable is the place information that is stored in the database.
    // This is required because when the location data is retrieved, the built-in
    // google maps latlng object doesn't have an empty constructor which is required
    // by firebase for retrieving data. This therefore stores the lat lng data in my
    // own LatLng class.
    private com.liamd.giggity_app.LatLng mPlaceToStoreLatLng;

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
        mFanRadio = (RadioButton) findViewById(R.id.fanRadio);
        mMusicianRadio = (RadioButton) findViewById(R.id.musicianRadio);
        mVenueRadio = (RadioButton) findViewById(R.id.venueRadio);
        mProgressDialog = new ProgressDialog(this);
        mCantFindVenueText = (TextView) findViewById(R.id.venueHelpTextView) ;

        // Initialise specific visual components
        mVenueFinderHeadingTextView = (TextView) findViewById(R.id.venueFinderHeadingTextView);
        mVenueDetailsTextView = (TextView) findViewById(R.id.venueDetails);
        mPlaceFinderButton = (Button) findViewById(R.id.placeFinderButton);
        mGenreSelectSpinner = (MultiSelectSpinner) findViewById(R.id.genreSpinner);
        mInstrumentSelectSpinner = (MultiSelectSpinner) findViewById(R.id.instrumentSpinner);
        mInstrumentHeadingTextView = (TextView) findViewById(R.id.instrumentHeadingTextView);
        mFirstNameEditText = (EditText) findViewById(R.id.firstNameTxt);
        mLastNameEditText = (EditText) findViewById(R.id.lastNameTxt);
        mVenueNameEditText = (EditText) findViewById(R.id.venueNameEditText);
        mVenueNameHeadingTextView = (TextView) findViewById(R.id.venueNameHeadingTextView);
        mFirstNameHeadingTextView = (TextView) findViewById(R.id.firstNameHeadingTextView);
        mLastNameHeadingTextView = (TextView) findViewById(R.id.lastNameHeadingTextView);
        mVenueCapacityHeadingTextView = (TextView) findViewById(R.id.venueCapacityHeadingTextView);
        mVenueCapacitySelectedTextView = (TextView) findViewById(R.id.VenueCapacitySelectedTextView);
        mVenueCapacityNumberPicker = (NumberPicker) findViewById(R.id.venueCapacityNumberPicker);
        mVenueCapacityNumberPicker.setMinValue(1);
        mVenueCapacityNumberPicker.setMaxValue(1000);
        mAgeHeadingTextView = (TextView) findViewById(R.id.userAgeHeadingTextView);
        mAgeSelectedTextView = (TextView) findViewById(R.id.userAgeSelectedTextView);
        mAgeNumberPicker = (NumberPicker) findViewById(R.id.userAgeNumberPicker);
        mAgeNumberPicker.setMinValue(1);
        mAgeNumberPicker.setMaxValue(100);

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

        // Add items to the instrument list, and set the spinner to use these
        mInstrumentList = new ArrayList<>();
        mInstrumentList.add("Acoustic Guitar");
        mInstrumentList.add("Backing Vocals");
        mInstrumentList.add("Banjo");
        mInstrumentList.add("Bass Guitar");
        mInstrumentList.add("Cajon");
        mInstrumentList.add("Cello");
        mInstrumentList.add("Clarinet");
        mInstrumentList.add("Classical Guitar");
        mInstrumentList.add("DJ");
        mInstrumentList.add("Drums");
        mInstrumentList.add("Flute");
        mInstrumentList.add("Keyboards");
        mInstrumentList.add("Lead Guitar");
        mInstrumentList.add("Lead Vocals");
        mInstrumentList.add("Piano");
        mInstrumentList.add("Rhythm Guitar");
        mInstrumentList.add("Saxophone");
        mInstrumentList.add("Synthesiser");
        mInstrumentList.add("Trumpet");
        mInstrumentList.add("Violin");

        mInstrumentSelectSpinner.setItems(mInstrumentList);

        // Hide/Show specific visual components
        HideVenueUserComponents();
        HideMusicianUserComponents();
        ShowFanUserComponents();

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

        mFanRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(mFanRadio.isChecked())
                {
                    // Show and hide the relevant visual components
                    HideVenueUserComponents();
                    HideMusicianUserComponents();
                    ShowFanUserComponents();
                }
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

        mAgeNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue)
            {
                mAgeSelectedTextView.setText(newValue + " years");
            }
        });

        // This updates the venue capacity text view as the number picker is updated
        mVenueCapacityNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue)
            {
                mVenueCapacitySelectedTextView.setText(newValue + " person(s)");
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
                                " overwritten below or later in the 'My Venue Profile' section.")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(R.drawable.ic_info_outline_black_24px)
                        .show();
            }
        });
    }

    // Method called when the save button is selected
    private void Save()
    {
        // Creates a new dialog to display when the save button is clicked
        new AlertDialog.Builder(PreSetupActivity.this)
                .setIcon(R.drawable.ic_info_outline_black_24px)
                .setTitle("Set Preferences")
                .setMessage("Are you sure you want to set these preferences? By clicking 'Yes' you" +
                        " are also confirming that you are 16 years of age or older.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(!mVenueRadio.isChecked())
                        {
                            // Ensure that a value has been entered for first and last names
                            if(TextUtils.isEmpty(mFirstNameEditText.getText())
                                    || TextUtils.isEmpty(mLastNameEditText.getText()))
                            {
                                Toast.makeText(PreSetupActivity.this,
                                        "Please enter a value for both first and last names!"
                                        , Toast.LENGTH_SHORT).show();
                            }

                            else if(mAgeNumberPicker.getValue() < 16)
                            {
                                Toast.makeText(PreSetupActivity.this,
                                        "The minimum required age to use Giggity is 16!"
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
                                        User userToInsert = new User();
                                        userToInsert.setAccountType("Musician");
                                        userToInsert.setHasCompletedSetup(true);
                                        userToInsert.setFirstName(mFirstNameEditText.getText().toString());
                                        userToInsert.setLastName(mLastNameEditText.getText().toString());
                                        userToInsert.setHomeAddress(mMusicianUserAddress);
                                        userToInsert.setHomeLocation(mPlaceToStoreLatLng);
                                        userToInsert.setInstruments(mInstrumentListToString);
                                        userToInsert.setGenres(mGenreListToString);
                                        userToInsert.setEmail(mAuth.getCurrentUser().getEmail());
                                        userToInsert.setUserID(mAuth.getCurrentUser().getUid());
                                        userToInsert.setMusicianDistance(0);
                                        userToInsert.setInBand(false);
                                        userToInsert.setAge(mAgeNumberPicker.getValue());
                                        mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).setValue(userToInsert);

                                        // A dialog is then shown to alert the user that the changes have been made
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(PreSetupActivity.this);
                                        builder.setTitle("Confirmation");
                                        builder.setMessage("Preferences Set!");
                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i)
                                            {
                                                // Calls the ReturnToMusicianUserMainActivity
                                                ReturnToMusicianUserMainActivity();
                                            }
                                        });
                                        builder.setCancelable(false);
                                        builder.show();
                                    }

                                    else
                                    {
                                        Toast.makeText(PreSetupActivity.this,
                                                "Please ensure you have selected your instruments, genres," +
                                                        " and a valid location!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                else if (mFanRadio.isChecked())
                                {
                                    // Get the selected items from the spinners as a string to store
                                    // in the database. These can then be parsed later when required.
                                    mGenreListToString = mGenreSelectSpinner.getSelectedItemsAsString();

                                    // ensure the location, genres, and instruments aren't null
                                    if(mMusicianUserAddress != null && mMusicianUserLatLng != null && !mGenreListToString.equals(""))
                                    {
                                        User userToInsert = new User();
                                        userToInsert.setAccountType("Fan");
                                        userToInsert.setHasCompletedSetup(true);
                                        userToInsert.setFirstName(mFirstNameEditText.getText().toString());
                                        userToInsert.setLastName(mLastNameEditText.getText().toString());
                                        userToInsert.setHomeAddress(mMusicianUserAddress);
                                        userToInsert.setHomeLocation(mPlaceToStoreLatLng);
                                        userToInsert.setGenres(mGenreListToString);
                                        userToInsert.setEmail(mAuth.getCurrentUser().getEmail());
                                        userToInsert.setUserID(mAuth.getCurrentUser().getUid());
                                        userToInsert.setMusicianDistance(0);
                                        userToInsert.setAge(mAgeNumberPicker.getValue());
                                        mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).setValue(userToInsert);

                                        // A dialog is then shown to alert the user that the changes have been made
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(PreSetupActivity.this);
                                        builder.setTitle("Confirmation");
                                        builder.setMessage("Preferences Set!");
                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i)
                                            {
                                                // Calls the ReturnToFanUserMainActivity
                                                ReturnToFanUserMainActivity();
                                            }
                                        });
                                        builder.setCancelable(false);
                                        builder.show();
                                    }

                                    else
                                    {
                                        Toast.makeText(PreSetupActivity.this,
                                                "Please ensure you have selected your favourite genres and a valid location!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }

                        else
                        {
                            // This block handles venue users
                            if (mVenueRadio.isChecked())
                            {
                                // Get the selected items from the spinners as a string to store
                                // in the database. These can then be parsed later when required.
                                mGenreListToString = mGenreSelectSpinner.getSelectedItemsAsString();

                                // Creates a venue object to store the venue information generated
                                // by the place finder
                                final Venue venue = new Venue();

                                // If the user has manually overriden the venue name
                                if(!mVenueNameEditText.getText().toString().equals(""))
                                {
                                    venue.setName(mVenueNameEditText.getText().toString());
                                }

                                else
                                {
                                    venue.setName(mVenueName);
                                }

                                venue.setVenueID(mVenueID);
                                venue.setUserID(mAuth.getCurrentUser().getUid());

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
                                                if(!mGenreListToString.equals("") || !mVenueCapacitySelectedTextView.getText().equals("No capacity selected!"))
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

                                                    mDatabase.child("Venues/" + mVenueID + "/venueLocation")
                                                            .setValue(mPlaceToStoreLatLng);

                                                    mDatabase.child("Venues/" + mVenueID + "/capacity")
                                                            .setValue(mVenueCapacityNumberPicker.getValue());

                                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                                            +"/venueID").setValue(mVenueID);

                                                    mDatabase.child("Venues/" + mVenueID + "/"
                                                            +"/minimumPerformerAge").setValue(mAgeNumberPicker.getValue());

                                                    // The inBand value is removed as this is no longer relevant
                                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                                            +"/inBand").removeValue();

                                                    // The hasCompletedSetup field is then changed to
                                                    // true in the database for the currently logged in user
                                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                                            + "/hasCompletedSetup").setValue(true);

                                                    // A dialog is then shown to alert the user that the changes have been made
                                                    final AlertDialog.Builder builder = new AlertDialog.Builder(PreSetupActivity.this);
                                                    builder.setTitle("Confirmation");
                                                    builder.setMessage("Preferences Set!");
                                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i)
                                                        {
                                                            // Calls the ReturnToVenueUserMainActivity
                                                            ReturnToVenueUserMainActivity();
                                                        }
                                                    });
                                                    builder.setCancelable(false);
                                                    builder.show();
                                                }

                                                else
                                                {
                                                    Toast.makeText(PreSetupActivity.this,
                                                            "Please ensure you have chosen the genres" +
                                                                    " your venue is associated with, and the capacity your venue holds!",
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

    private void ReturnToFanUserMainActivity()
    {
        finish();
        Intent startFanUserMainActivity = new Intent(PreSetupActivity.this, FanUserMainActivity.class);
        startActivity(startFanUserMainActivity);
    }

    // Method to display musician user specific visual components
    private void ShowMusicianUserComponents()
    {
        mInstrumentSelectSpinner.setVisibility(View.VISIBLE);
        mInstrumentHeadingTextView.setVisibility(View.VISIBLE);
        mVenueFinderHeadingTextView.setText("Find Your Home Location");
        mVenueDetailsTextView.setText("No Location Chosen");
        mPlaceFinderButton.setText("Launch Location Finder");
        mVenueNameEditText.setVisibility(View.GONE);
        mVenueNameHeadingTextView.setVisibility(View.GONE);
        mFirstNameHeadingTextView.setVisibility(View.VISIBLE);
        mLastNameHeadingTextView.setVisibility(View.VISIBLE);
        mFirstNameEditText.setVisibility(View.VISIBLE);
        mLastNameEditText.setVisibility(View.VISIBLE);
    }

    // Method to hide musician user specific visual components
    private void HideMusicianUserComponents()
    {
        mInstrumentSelectSpinner.setVisibility(View.GONE);
        mInstrumentHeadingTextView.setVisibility(View.GONE);
        mFirstNameHeadingTextView.setVisibility(View.GONE);
        mLastNameHeadingTextView.setVisibility(View.GONE);
        mFirstNameEditText.setVisibility(View.GONE);
        mLastNameEditText.setVisibility(View.GONE);
    }

    // Method to display venue user specific visual components
    private void ShowVenueUserComponents()
    {
        // Show venue specific visual components
        mCantFindVenueText.setVisibility(View.VISIBLE);
        mVenueFinderHeadingTextView.setText("Find Your Venue");
        mVenueDetailsTextView.setText("No Venue Chosen");
        mPlaceFinderButton.setText("Launch Venue Finder");
        mVenueCapacityHeadingTextView.setVisibility(View.VISIBLE);
        mVenueCapacitySelectedTextView.setVisibility(View.VISIBLE);
        mVenueCapacityNumberPicker.setVisibility(View.VISIBLE);
        mVenueNameEditText.setVisibility(View.VISIBLE);
        mVenueNameHeadingTextView.setVisibility(View.VISIBLE);
        mAgeHeadingTextView.setText("Minimum Performer Age");
    }

    // Method to hide venue user specific visual components
    private void HideVenueUserComponents()
    {
        mVenueNameEditText.setVisibility(View.GONE);
        mVenueNameHeadingTextView.setVisibility(View.GONE);
        mCantFindVenueText.setVisibility(View.GONE);
        mVenueCapacityHeadingTextView.setVisibility(View.GONE);
        mVenueCapacitySelectedTextView.setVisibility(View.GONE);
        mVenueCapacityNumberPicker.setVisibility(View.GONE);
        mAgeHeadingTextView.setText("Age");
    }

    private void ShowFanUserComponents()
    {
        mFirstNameHeadingTextView.setVisibility(View.VISIBLE);
        mLastNameHeadingTextView.setVisibility(View.VISIBLE);
        mFirstNameEditText.setVisibility(View.VISIBLE);
        mLastNameEditText.setVisibility(View.VISIBLE);
        mVenueNameEditText.setVisibility(View.GONE);
        mVenueNameHeadingTextView.setVisibility(View.GONE);
        mInstrumentSelectSpinner.setVisibility(View.GONE);
        mInstrumentHeadingTextView.setVisibility(View.GONE);
        mCantFindVenueText.setVisibility(View.GONE);
        mVenueFinderHeadingTextView.setText("Find Your Home Location");
        mVenueDetailsTextView.setText("No Location Chosen");
        mPlaceFinderButton.setText("Launch Location Finder");
        mAgeHeadingTextView.setVisibility(View.VISIBLE);
        mAgeSelectedTextView.setVisibility(View.VISIBLE);
        mAgeNumberPicker.setVisibility(View.VISIBLE);
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
        mProgressDialog.hide();

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
                    mPlaceToStoreLatLng = new com.liamd.giggity_app.LatLng();

                    // Store the selected place details in the text view and variables
                    mVenueDetailsTextView.setText(place.getName() + " \n" + place.getAddress());
                    mVenueName = place.getName().toString();
                    mVenueID = place.getId();
                    mVenueLocation = place.getLatLng();

                    // This takes the place data from mVenueLocation and stores it in
                    // two double variables
                    double placeLat = mVenueLocation.latitude;
                    double placeLng = mVenueLocation.longitude;

                    // These are then stored in the mPlaceToStoreLatLng variable to be stored
                    // in the database
                    mPlaceToStoreLatLng.setLatitude(placeLat);
                    mPlaceToStoreLatLng.setLongitude(placeLng);
                }

                // If the musician radio button is checked, display just the address of the
                // location, because otherwise it will just display a latlng.
                // Then store the name and ID in the required variables.
                else
                {
                    mPlaceToStoreLatLng = new com.liamd.giggity_app.LatLng();

                    mVenueDetailsTextView.setText(place.getAddress());
                    mMusicianUserAddress = place.getAddress().toString();
                    mMusicianUserLatLng = place.getLatLng();

                    double placeLat = mMusicianUserLatLng.latitude;
                    double placeLng = mMusicianUserLatLng.longitude;

                    mPlaceToStoreLatLng.setLatitude(placeLat);
                    mPlaceToStoreLatLng.setLongitude(placeLng);
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
