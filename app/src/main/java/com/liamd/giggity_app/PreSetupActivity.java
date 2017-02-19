package com.liamd.giggity_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PreSetupActivity extends AppCompatActivity
{
    // Declare visual components
    private Button mSaveButton;
    private RadioButton mBandRadio;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_setup);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mBandRadio = (RadioButton) findViewById(R.id.bandRadio);

        mSaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mBandRadio.isChecked())
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
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                            + "/accountType").setValue("Venue");

                    // Calls the ReturnToVenueUserMainActivity
                    ReturnToVenueUserMainActivity();

                    // The hasCompletedSetup field is changed to
                    // true in the database for the currently logged in user
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                            + "/hasCompletedSetup").setValue(true);
                }
            }
        });
    }

    private void ReturnToMusicianUserMainActivity()
    {
        finish();
        Intent startMusicianUserMainActivity = new Intent(PreSetupActivity.this, MusicianUserMainActivity.class);
        startActivity(startMusicianUserMainActivity);
    }

    private void ReturnToVenueUserMainActivity()
    {
        finish();
        Intent startVenueUserMainActivity = new Intent(PreSetupActivity.this, VenueUserMainActivity.class);
        startActivity(startVenueUserMainActivity);
    }
}
