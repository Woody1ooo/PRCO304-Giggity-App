package com.liamd.giggity_app;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

                PopulateListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // Set the fragment title
        getActivity().setTitle("My Gigs");

        return fragmentView;
    }

    private void PopulateListView()
    {
        // This sorts the list of gigs by date
        Collections.sort(mListOfGigs, new CustomComparator());

        // Using the custom VenueUserGigsAdapter, the list of users gigs can be displayed
        if(getActivity() != null)
        {
            adapter = new MusicianUserViewGigsAdapter(getActivity(), R.layout.musician_user_gig_list, mListOfGigs, mSnapshot, mBandId);
            mGigsListView.setAdapter(adapter);
        }
    }

}
