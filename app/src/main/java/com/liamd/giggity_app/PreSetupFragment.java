package com.liamd.giggity_app;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class PreSetupFragment extends Fragment implements View.OnClickListener
{
    // Declare visual components
    // Currently only a test button to set the db flag to true
    Button mTestButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public PreSetupFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_pre_setup, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mTestButton = (Button) v.findViewById(R.id.testButton);
        mTestButton.setOnClickListener(this);

        return v;
    }

    // Test method to ensure that when the button is clicked, the flag is changed in the db
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.testButton:
                mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                        + "/hasCompletedSetup").setValue(true);
                break;
        }
    }
}
