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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

        // Display a message for the user
        Toast.makeText(getActivity(), mLanguageSpinner.getSelectedItem().toString() + " chosen.", Toast.LENGTH_SHORT).show();

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
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                String bandID;

                                // Check if user is in a band
                                if(dataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/inBand").getValue().toString().equals("true"))
                                {
                                    bandID = dataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").getValue().toString();

                                    Iterable<DataSnapshot> children = dataSnapshot.child("Bands/" + bandID).getChildren();
                                    for (DataSnapshot child : children)
                                    {
                                        mUserBand = new Band();
                                        mUserBand = child.getValue(Band.class);
                                    }

                                    if(mUserBand.getPositionOneMember().equals(mAuth.getCurrentUser().getUid()))
                                    {
                                        mDatabase.child("Bands/" + bandID + "/positionOneMember").setValue("Vacant");
                                    }

                                    else if(mUserBand.getPositionTwoMember().equals(mAuth.getCurrentUser().getUid()))
                                    {
                                        mDatabase.child("Bands/" + bandID + "/positionTwoMember").setValue("Vacant");
                                    }

                                    else if(mUserBand.getPositionThreeMember().equals(mAuth.getCurrentUser().getUid()))
                                    {
                                        mDatabase.child("Bands/" + bandID + "/positionThreeMember").setValue("Vacant");
                                    }

                                    else if(mUserBand.getPositionFourMember().equals(mAuth.getCurrentUser().getUid()))
                                    {
                                        mDatabase.child("Bands/" + bandID + "/positionFourMember").setValue("Vacant");
                                    }

                                    else if(mUserBand.getPositionFiveMember().equals(mAuth.getCurrentUser().getUid()))
                                    {
                                        mDatabase.child("Bands/" + bandID + "/positionFiveMember").setValue("Vacant");
                                    }
                                }

                                // Delete any gig information associated with the user
                                if(dataSnapshot.child("UserGigInformation/" + mAuth.getCurrentUser().getUid()).exists())
                                {
                                    mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid()).removeValue();
                                }

                                // Delete user from database
                                mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).removeValue();

                                // Delete user from firebase
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
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
