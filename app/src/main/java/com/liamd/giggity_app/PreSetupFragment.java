package com.liamd.giggity_app;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PreSetupFragment extends Fragment
{
    // Declare visual components
    private Button mSaveButton;
    private RadioButton mBandRadio;

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

        // Disables the navigation drawer
        ((DrawerLock) getActivity()).setDrawerEnabled(false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        mSaveButton = (Button) v.findViewById(R.id.saveButton);
        mBandRadio = (RadioButton) v.findViewById(R.id.bandRadio);

        // Dialog to determine whether the user is happy with their choices
        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:

                        // If 'Yes' is clicked, the hasCompletedSetup field is changed to
                        // true in the database for the currently logged in user
                        mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                + "/hasCompletedSetup").setValue(true);

                        if(mBandRadio.isChecked())
                        {
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                    + "/accountType").setValue("Musician");
                            // Calls the CloseFragment method
                            CloseFragment();
                        }

                        else
                        {
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()
                                    + "/accountType").setValue("Venue");
                            // Calls the CloseFragment method
                            CloseFragment();
                        }

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        // If 'No' is selected simply dismiss the dialog
                        dialog.dismiss();
                        break;
                }
            }
        };

        mSaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Creates the dialog to be displayed when the Save button is clicked
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you wish to set these preferences?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        return v;
    }

    // This method replaces the current fragment with the home fragment.
    private void CloseFragment()
    {
        FragmentManager fragmentManager = getFragmentManager();

        Fragment fragment = new HomeFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();

        // Corrects the navigation bar title to the returning activity
        getActivity().setTitle("Home");

        // Re-enable the navigation drawer
        ((DrawerLock) getActivity()).setDrawerEnabled(true);
    }
}
