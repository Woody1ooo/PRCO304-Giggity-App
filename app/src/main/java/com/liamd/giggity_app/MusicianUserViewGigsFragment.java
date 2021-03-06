package com.liamd.giggity_app;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserViewGigsFragment extends Fragment
{
    // Declare Firebase specific variables
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    // Declare Visual Components
    private ListView mGigsListView;
    private MusicianUserViewGigsAdapter adapter;

    // Declare general variables
    private ArrayList<Gig> mListOfGigs = new ArrayList<>();
    private String mBandId;
    private DataSnapshot mSnapshot;
    private boolean mIsFanAccount;

    public MusicianUserViewGigsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_view_gigs, container, false);

        if(getArguments().getString("UserType").equals("Fan"))
        {
            mIsFanAccount = true;
        }

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialise the list view
        mGigsListView = (ListView) fragmentView.findViewById(R.id.gigListView);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mSnapshot = dataSnapshot;

                if(!mIsFanAccount)
                {
                    mBandId = dataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").getValue().toString();

                    Iterable<DataSnapshot> children = dataSnapshot.child("Gigs").getChildren();
                    for (DataSnapshot child : children)
                    {
                        Gig gig;
                        gig = child.getValue(Gig.class);

                        if(gig.getBookedAct().equals(mBandId))
                        {
                            mListOfGigs.add(gig);
                        }
                    }
                }

                else
                {
                    Iterable<DataSnapshot> children = mSnapshot.child("Tickets/").getChildren();
                    for (DataSnapshot child : children)
                    {
                        // Get the gig id of the first ticket
                        String gigId = child.getKey();

                        // If the gig has a child with the current user's id it means the current user has a ticket for that gig
                        if(dataSnapshot.child("Tickets/" + gigId + "/" + mAuth.getCurrentUser().getUid()).exists())
                        {
                            // The details of that gig can then be obtained and posted to the list view
                            Iterable<DataSnapshot> levelDownChildren = mSnapshot.child("Gigs/").getChildren();
                            for (DataSnapshot levelDownChild : levelDownChildren)
                            {
                                if(levelDownChild.getKey().equals(gigId))
                                {
                                    Gig gig;
                                    gig = levelDownChild.getValue(Gig.class);
                                    mListOfGigs.add(gig);
                                }
                            }
                        }
                    }
                }

                PopulateListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // Set the fragment title
        getActivity().setTitle("My Gigs");

        mGigsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                Gig selectedGig = (Gig) mGigsListView.getItemAtPosition(position);

                MusicianUserViewGigsDetailsFragment fragment = new MusicianUserViewGigsDetailsFragment();

                Bundle arguments = new Bundle();
                arguments.putString("GigID", selectedGig.getGigId());
                arguments.putString("GigTitle", selectedGig.getTitle());
                arguments.putString("GigStartDate", selectedGig.getStartDate().toString());
                arguments.putString("GigEndDate", selectedGig.getEndDate().toString());
                arguments.putString("GigVenueID", selectedGig.getVenueID());

                if(mIsFanAccount)
                {
                    arguments.putString("UserType", "Fan");
                }

                else
                {
                    arguments.putString("UserType", "Musician");
                }

                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // gig. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserViewGigsDetailsFragment")
                        .addToBackStack(null).commit();

                mListOfGigs.clear();
            }
        });

        return fragmentView;
    }

    private void PopulateListView()
    {
        // This sorts the list of gigs by date
        Collections.sort(mListOfGigs, new CustomGigComparator());

        // If the list has elements display them
        if(mListOfGigs.size() > 0)
        {
            // Using the custom VenueUserGigsAdapter, the list of users gigs can be displayed
            if(getActivity() != null)
            {
                if(!mIsFanAccount)
                {
                    adapter = new MusicianUserViewGigsAdapter(getActivity(), R.layout.musician_user_gig_list, mListOfGigs, mSnapshot, mBandId, mIsFanAccount);
                }

                else
                {
                    adapter = new MusicianUserViewGigsAdapter(getActivity(), R.layout.musician_user_gig_list, mListOfGigs, mSnapshot, mAuth.getCurrentUser().getUid(), mIsFanAccount);
                }

                mGigsListView.setAdapter(adapter);
            }
        }

        // Otherwise display a dialog
        else
        {
            if(getActivity() != null)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.drawable.ic_info_outline_black_24px);
                builder.setTitle("No Gigs!");
                builder.setMessage("You haven't got any gigs yet! Would you like to search for one?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Bundle arguments = new Bundle();
                        getActivity().setTitle("Gig Finder");
                        MusicianUserGigFinderFragment fragment = new MusicianUserGigFinderFragment();

                        if(mIsFanAccount)
                        {
                            arguments.putString("UserType", "Fan");
                        }

                        else
                        {
                            arguments.putString("UserType", "Musician");
                        }

                        fragment.setArguments(arguments);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserGigFinderFragment")
                                .addToBackStack(null)
                                .commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
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
        }
    }

    private void ReturnToHome()
    {
        if(mIsFanAccount)
        {
            getActivity().finish();
            getActivity().overridePendingTransition(0, 0);

            Intent intent = new Intent(getActivity(), FanUserMainActivity.class);
            startActivity(intent);

            getFragmentManager().popBackStackImmediate();
        }

        else
        {
            getActivity().finish();
            getActivity().overridePendingTransition(0, 0);

            Intent intent = new Intent(getActivity(), MusicianUserMainActivity.class);
            startActivity(intent);

            getFragmentManager().popBackStackImmediate();
        }
    }
}
