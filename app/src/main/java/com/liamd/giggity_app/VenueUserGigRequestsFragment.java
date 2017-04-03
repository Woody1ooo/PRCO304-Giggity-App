package com.liamd.giggity_app;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserGigRequestsFragment extends Fragment
{
    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare Visual Components
    private ListView mSentGigRequestsListView;
    private ListView mReceivedGigRequestsListView;
    private VenueUserGigRequestsAdapter mSentGigRequestsAdapter;
    private VenueUserGigRequestsAdapter mReceivedGigRequestsAdapter;

    // Declare General Variables
    private ArrayList<GigRequest> mListOfGigRequestsSent = new ArrayList<>();
    private ArrayList<GigRequest> mListOfGigRequestsReceived = new ArrayList<>();
    private ArrayList<GigRequest> mListOfFilteredGigRequestsReceived = new ArrayList<>();
    private DataSnapshot mDataSnapshot;
    private String mBandIdKey;
    private String mVenueId;

    public VenueUserGigRequestsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.venue_user_fragment_gig_requests, container, false);

        // This initialises the tabs used to hold the different views
        TabHost tabs = (TabHost) fragmentView.findViewById(R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec tabSpec = tabs.newTabSpec("tag1");

        tabSpec.setContent(R.id.SentRequestsListTab);
        tabSpec.setIndicator("Sent");
        tabs.addTab(tabSpec);

        tabSpec = tabs.newTabSpec("tag2");
        tabSpec.setContent(R.id.ReceivedRequestsListTab);
        tabSpec.setIndicator("Received");
        tabs.addTab(tabSpec);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise the list views
        mSentGigRequestsListView = (ListView) fragmentView.findViewById(R.id.sentGigRequestsListView);
        mReceivedGigRequestsListView = (ListView) fragmentView.findViewById(R.id.receivedGigRequestsListView);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mDataSnapshot = dataSnapshot;
                PopulateListViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        return fragmentView;
    }

    private void PopulateListViews()
    {
        mVenueId = mDataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/venueID").getValue().toString();

        // This iterates through the band requests that users have sent and adds them to a list (mListOfUserRequestsSent)
        Iterable<DataSnapshot> receivedRequestChildren = mDataSnapshot.child("MusicianSentGigRequests/").getChildren();
        for (DataSnapshot child : receivedRequestChildren)
        {
            // The key is obtained from the level below to then get the children below that
            mBandIdKey = child.getKey();

            Iterable<DataSnapshot> levelDownReceivedRequestChildren = mDataSnapshot.child("MusicianSentGigRequests/" + mBandIdKey).getChildren();
            for (DataSnapshot levelDownChild : levelDownReceivedRequestChildren)
            {
                GigRequest gigRequest;
                gigRequest = levelDownChild.getValue(GigRequest.class);
                mListOfGigRequestsReceived.add(gigRequest);

                for (int i = 0; i < mListOfGigRequestsReceived.size(); i++)
                {
                    if(mListOfGigRequestsReceived.get(i).getVenueID().equals(mVenueId))
                    {
                        mListOfFilteredGigRequestsReceived.add(mListOfGigRequestsReceived.get(i));
                    }
                }
            }
        }

        mReceivedGigRequestsAdapter = new VenueUserGigRequestsAdapter(getActivity(), R.layout.venue_user_gig_requests_list, mListOfFilteredGigRequestsReceived , mDataSnapshot);
        mReceivedGigRequestsListView.setAdapter(mReceivedGigRequestsAdapter);
    }
}
