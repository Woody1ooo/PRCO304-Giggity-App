package com.liamd.giggity_app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsFragment extends Fragment
{
    // Declare visual components
    private Spinner mLanguageSpinner;
    private Button mDeleteAccountButton;

    // Declare general variables
    private Band mUserBand;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public SettingsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise variables
        mLanguageSpinner = (Spinner) fragmentView.findViewById(R.id.languageSpinner);
        mDeleteAccountButton = (Button) fragmentView.findViewById(R.id.deleteAccountButton);

        // Display a message for the user when a different language is chosen
        mLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                Toast.makeText(getActivity(), mLanguageSpinner.getSelectedItem().toString() + " chosen.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        // Check the database for data relating to this user
        mDeleteAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // This dialog is created to confirm that the user wants to edit the chosen fields
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete Account");
                builder.setIcon(R.drawable.ic_info_outline_black_24px);
                builder.setMessage("Are you sure you wish to delete your account?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot)
                            {
                                final String[] bandID = new String[1];
                                final String[] venueID = new String[1];
                                final String userID = mAuth.getCurrentUser().getUid();

                                // Delete user from firebase
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            // If the user is a venue user delete their venue
                                            if(dataSnapshot.child("Users/" + userID + "/accountType").getValue().toString().equals("Venue"))
                                            {
                                                venueID[0] = dataSnapshot.child("Users/" + userID + "/venueID").getValue().toString();
                                                mDatabase.child("Venues/" + venueID[0]).removeValue();
                                            }

                                            else if(dataSnapshot.child("Users/" + userID + "/accountType").getValue().toString().equals("Musician"))
                                            {
                                                // Check if user is in a band
                                                if(dataSnapshot.child("Users/" + userID + "/inBand").getValue().toString().equals("true"))
                                                {
                                                    bandID[0] = dataSnapshot.child("Users/" + userID + "/bandID").getValue().toString();

                                                    Iterable<DataSnapshot> children = dataSnapshot.child("Bands/" + bandID[0]).getChildren();
                                                    for (DataSnapshot child : children)
                                                    {
                                                        mUserBand = new Band();
                                                        mUserBand = child.getValue(Band.class);
                                                    }

                                                    if(mUserBand.getPositionOneMember().equals(userID))
                                                    {
                                                        mDatabase.child("Bands/" + bandID[0] + "/positionOneMember").setValue("Vacant");
                                                    }

                                                    else if(mUserBand.getPositionTwoMember().equals(userID))
                                                    {
                                                        mDatabase.child("Bands/" + bandID[0] + "/positionTwoMember").setValue("Vacant");
                                                    }

                                                    else if(mUserBand.getPositionThreeMember().equals(userID))
                                                    {
                                                        mDatabase.child("Bands/" + bandID[0] + "/positionThreeMember").setValue("Vacant");
                                                    }

                                                    else if(mUserBand.getPositionFourMember().equals(userID))
                                                    {
                                                        mDatabase.child("Bands/" + bandID[0] + "/positionFourMember").setValue("Vacant");
                                                    }

                                                    else if(mUserBand.getPositionFiveMember().equals(userID))
                                                    {
                                                        mDatabase.child("Bands/" + bandID[0] + "/positionFiveMember").setValue("Vacant");
                                                    }
                                                }

                                                // Delete any gig information associated with the user
                                                if(dataSnapshot.child("UserGigInformation/" + userID).exists())
                                                {
                                                    mDatabase.child("UserGigInformation/" + userID).removeValue();
                                                }
                                            }

                                            // Delete user from database
                                            mDatabase.child("Users/" + userID).removeValue();

                                            // A dialog is then shown to alert the user that the changes have been made
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle("Confirmation");
                                            builder.setMessage("Account Deleted!");
                                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i)
                                                {
                                                    Logout();
                                                }
                                            });
                                            builder.setCancelable(false);
                                            builder.show();
                                        }

                                        else
                                        {
                                            Toast.makeText(getActivity(), "Error! Action could not be completed. Please contact the Giggity admin team.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                    }
                });
                builder.show();
            }
        });

        return fragmentView;
    }

    private void Logout()
    {
        // Stop the notification service on logout
        getActivity().stopService(new Intent(getActivity(), NotificationService.class));

        // Will log the user out of Gmail or email/password login
        mAuth.signOut();

        FirebaseAuth.getInstance().signOut();

        // Will log the user out of facebook
        LoginManager.getInstance().logOut();

        getActivity().finish();

        if(!getActivity().isFinishing())
        {
            getActivity().finish();
        }

        // Returns to the login activity
        Intent returnToLoginActivity= new Intent(getActivity(), LoginActivity.class);
        returnToLoginActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(returnToLoginActivity);
    }
}
