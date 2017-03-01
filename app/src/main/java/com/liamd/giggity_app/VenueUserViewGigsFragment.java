package com.liamd.giggity_app;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserViewGigsFragment extends Fragment
{
    private ArrayList<Gig> mListOfGigs = new ArrayList<>();
    private List<Gig> mListOfUsersGigs = new ArrayList<>();

    private ListView mGigsListView;
    private GigsAdapter adapter;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String mVenueId;

    public VenueUserViewGigsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.venue_user_fragment_view_gigs, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mGigsListView = (ListView) fragmentView.findViewById(R.id.gigListView);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // This checks the database to find the user's venueID
                mVenueId = dataSnapshot.child("Users/" +
                        mAuth.getCurrentUser().getUid() + "/venueID").getValue().toString();

                // Each gig is then iterated through and added to an
                // array list of gigs (mListOfVenueGigs)
                Iterable<DataSnapshot> children = dataSnapshot.child("Gigs/").getChildren();

                for (DataSnapshot child : children)
                {
                    Gig gig;
                    gig = child.getValue(Gig.class);

                    // This line ensures that we have the gig ID to pass to the
                    // gig details fragment
                    gig.setGigId(child.getKey());
                    mListOfGigs.add(gig);
                }

                // We then iterate through the list of gigObjects populated above
                // to find any gigs at the user's venue. If there are any, these are
                // added to a separate list of gigs specific to the user
                for (int i = 0; i < mListOfGigs.size(); i++)
                {
                    if (mListOfGigs.get(i).getVenueID().equals(mVenueId))
                    {
                        // These are then added to a separate list of just dates
                        mListOfUsersGigs.add(mListOfGigs.get(i));
                    }
                }

                // This sorts the list of gigs by date
                Collections.sort(mListOfUsersGigs, new CustomComparator());

                // Using the custom GigsAdapter, the list of users gigs can be displayed
                adapter = new GigsAdapter(getActivity(), R.layout.gig_list, mListOfUsersGigs);
                mGigsListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mGigsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                // This returns the selected gig from the list view
                Gig selectedGig = (Gig)mGigsListView.getItemAtPosition(position);

                // This then stores the id of the selected gig in a bundle which is then
                // passed to the result fragment to display the gig details
                VenueUserViewGigDetailsFragment fragment = new VenueUserViewGigDetailsFragment();
                Bundle arguments = new Bundle();
                arguments.putString("GigID", selectedGig.getGigId());
                arguments.putString("GigTitle", selectedGig.getTitle());
                arguments.putString("GigStartDate", selectedGig.getStartDate().toString());
                arguments.putString("GigEndDate", selectedGig.getEndDate().toString());
                arguments.putString("GigVenueID", selectedGig.getVenueID());
                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // gig. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                fragmentTransaction.replace(R.id.frame, fragment, "VenueUserGigDetailsFragment")
                        .addToBackStack(null).commit();

                // These must be cleared to prevent duplication as the database is called again
                // when the fragment is returned to. This is required to update any changes made
                // to the gigs by the user
                mListOfGigs.clear();
                mListOfUsersGigs.clear();
                adapter.clear();
            }
        });

        return fragmentView;
    }
}
