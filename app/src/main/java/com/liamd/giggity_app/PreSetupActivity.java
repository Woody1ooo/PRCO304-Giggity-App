package com.liamd.giggity_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
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

import org.w3c.dom.Text;

public class PreSetupActivity extends AppCompatActivity
{
    // Declare general visual components
    private Button mSaveButton;
    private RadioButton mBandRadio;
    private RadioButton mVenueRadio;
    private ProgressDialog mProgressDialog;
    private TextView mCantFindVenueText;

    // Declare Musician specific visual components
    // to add...

    // Declare Venue specific visual components
    private TextView mVenueFinderHeadingTextView;
    private TextView mVenueDetailsTextView;
    private Button mPlaceFinderButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare Google specific variables
    private int PLACE_PICKER_REQUEST = 1;

    // Declare user data variables required
    private String mVenueName;
    private String mVenueID;

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
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mBandRadio = (RadioButton) findViewById(R.id.bandRadio);
        mVenueRadio = (RadioButton) findViewById(R.id.venueRadio);
        setTitle("Account Preferences");
        mProgressDialog = new ProgressDialog(this);
        mCantFindVenueText = (TextView) findViewById(R.id.venueHelpTextView) ;

        // Initialise specific visual components
        mVenueFinderHeadingTextView = (TextView) findViewById(R.id.venueFinderHeadingTextView);
        mVenueDetailsTextView = (TextView) findViewById(R.id.venueDetails);
        mPlaceFinderButton = (Button) findViewById(R.id.placeFinderButton);

        // Hide venue specific visual components
        HideVenueUserComponents();

        mSaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Creates a new dialog to display when the save button is clicked
                new AlertDialog.Builder(PreSetupActivity.this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle("Set Preferences")
                        .setMessage("Are you sure you want to set these preferences?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (mBandRadio.isChecked())
                                {
                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                            + "/accountType").setValue("Musician");

                                    // Calls the ReturnToMusicianUserMainActivity
                                    ReturnToMusicianUserMainActivity();

                                    // The hasCompletedSetup field is changed to
                                    // true in the database for the currently logged in user
                                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                            + "/hasCompletedSetup").setValue(true);
                                }
                                else
                                {
                                    // Creates a venue object to store the venue information generated
                                    // by the place finder
                                    final Venue venue = new Venue();
                                    venue.setName(mVenueName);
                                    venue.setVenueID(mVenueID);
                                    venue.setUserID(mAuth.getCurrentUser().getUid());

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
                                                // If the place hasn't already been taken by another
                                                // user, update the user's entry in the database
                                                // with the account type selected (venue)
                                                mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                                        + "/accountType").setValue("Venue");

                                                // Add a new entry under the 'Venues' node to store
                                                // the data stored in the venue object above, with the
                                                // venue ID as both the identifier, and as an attribute
                                                mDatabase.child("Venues/" + mVenueID).setValue(venue);

                                                // The hasCompletedSetup field is then changed to
                                                // true in the database for the currently logged in user
                                                mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                                        + "/hasCompletedSetup").setValue(true);

                                                // Calls the ReturnToVenueUserMainActivity
                                                ReturnToVenueUserMainActivity();
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
        });

        // If the musician radio button is checked, call the method to display the relevant components
        mBandRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(mBandRadio.isChecked())
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
                mProgressDialog.setMessage("Loading place picker...");

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
                        .setMessage("If you can't find your venue on the place picker, please use" +
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

    }

    // Method to hide musician user specific visual components
    private void HideMusicianUserComponents()
    {

    }

    // Method to display venue user specific visual components
    private void ShowVenueUserComponents()
    {
        // Show venue specific visual components
        mVenueFinderHeadingTextView.setVisibility(View.VISIBLE);
        mVenueDetailsTextView.setVisibility(View.VISIBLE);
        mPlaceFinderButton.setVisibility(View.VISIBLE);
        mCantFindVenueText.setVisibility(View.VISIBLE);
    }

    // Method to hide venue user specific visual components
    private void HideVenueUserComponents()
    {
        // Hide venue specific visual components
        mVenueFinderHeadingTextView.setVisibility(View.GONE);
        mVenueDetailsTextView.setVisibility(View.GONE);
        mPlaceFinderButton.setVisibility(View.GONE);
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

                // Store the selected place details in the text view and variables
                mVenueDetailsTextView.setText(place.getName() + " \n" + place.getAddress());
                mVenueName = place.getName().toString();
                mVenueID = place.getId();
            }
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
