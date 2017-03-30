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
public class MusicianUserBandRequestsNotInBandFragment extends Fragment
{
    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare Visual Components
    private ListView mSentBandRequestsListView;
    private ListView mReceivedBandRequestsListView;
    private MusicianBandRequestsAdapter mSentBandRequestsAdapter;
    private MusicianBandRequestsAdapter mReceivedBandRequestsAdapter;

    // Declare General Variables
    private ArrayList<BandRequest> mListOfBandRequestsSent = new ArrayList<>();
    private ArrayList<BandRequest> mListOfBandRequestsReceived = new ArrayList<>();
    private DataSnapshot mDataSnapshot;

    public MusicianUserBandRequestsNotInBandFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_requests_not_in_band, container, false);

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
        mSentBandRequestsListView = (ListView) fragmentView.findViewById(R.id.sentBandRequestsListView);
        mReceivedBandRequestsListView = (ListView) fragmentView.findViewById(R.id.receivedBandRequestsListView);

        mDatabase.child("MusicianSentBandRequests/" + mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener()
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
        // This iterates through the band requests the user has sent and adds them to a list (mListOfBandRequestsSent)
        Iterable<DataSnapshot> sentRequestChildren = mDataSnapshot.getChildren();
        for (DataSnapshot child : sentRequestChildren)
        {
            BandRequest bandRequest;
            bandRequest = child.getValue(BandRequest.class);
            mListOfBandRequestsSent.add(bandRequest);
        }

        // This iterates through the band requests the user has sent and adds them to a list (mListOfBandRequestsSent)
        // This has been commented as this feature hasn't yet been enabled
        /*Iterable<DataSnapshot> receivedRequestChildren = dataSnapshot.child("MusicianUserReceivedBandRequests/" + mAuth.getCurrentUser().getUid()).getChildren();
        for (DataSnapshot child : receivedRequestChildren)
        {
            BandRequest bandRequest;
            bandRequest = child.getValue(BandRequest.class);
            mListOfBandRequestsReceived.add(bandRequest);
        }
        */

        mSentBandRequestsAdapter = new MusicianBandRequestsAdapter(getActivity(), R.layout.musician_user_band_requests_list, mListOfBandRequestsSent);
        //mReceivedBandRequestsAdapter = new MusicianBandRequestsAdapter(getActivity(), R.layout.musician_user_band_requests_list, mReceivedBandRequestsListView);

        mSentBandRequestsListView.setAdapter(mSentBandRequestsAdapter);
        //mReceivedBandRequestsListView.setAdapter(mReceivedBandRequestsAdapter);
    }

}
