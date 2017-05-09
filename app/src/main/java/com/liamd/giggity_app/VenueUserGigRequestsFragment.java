package com.liamd.giggity_app;


import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;


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
    private ArrayList<GigRequest> mListOfFilteredGigRequestsSent = new ArrayList<>();
    private ArrayList<GigRequest> mListOfGigRequestsReceived = new ArrayList<>();
    private ArrayList<GigRequest> mListOfFilteredGigRequestsReceived = new ArrayList<>();
    private DataSnapshot mDataSnapshot;
    private String mBandIdKey;
    private String mGigIdKey;
    private String mVenueId;
    private Boolean mPassedThrough;

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

        mSentGigRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                // This returns the selected request from the list view
                GigRequest selectedRequest = (GigRequest) mSentGigRequestsListView.getItemAtPosition(position);
                VenueUserGigRequestsSentDetailsFragment fragment = new VenueUserGigRequestsSentDetailsFragment();
                Bundle arguments = new Bundle();
                arguments.putString("BandID", selectedRequest.getBandID());
                arguments.putString("GigID", selectedRequest.getGigID());
                arguments.putString("VenueID", selectedRequest.getVenueID());
                arguments.putString("GigName", selectedRequest.getGigName());
                arguments.putString("GigStartDate", selectedRequest.getGigStartDate().toString());
                arguments.putString("GigEndDate", selectedRequest.getGigEndDate().toString());
                arguments.putString("RequestStatus", selectedRequest.getRequestStatus());

                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // request. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "VenueUserGigRequestsSentDetailsFragment")
                        .addToBackStack(null).commit();

                mListOfGigRequestsSent.clear();
                mListOfFilteredGigRequestsSent.clear();
                mListOfGigRequestsReceived.clear();
                mListOfFilteredGigRequestsReceived.clear();
            }
        });

        mReceivedGigRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                GigRequest selectedRequest = (GigRequest) mReceivedGigRequestsListView.getItemAtPosition(position);
                VenueUserGigRequestsReceivedDetailsFragment fragment = new VenueUserGigRequestsReceivedDetailsFragment();
                Bundle arguments = new Bundle();
                arguments.putString("BandID", selectedRequest.getBandID());
                arguments.putString("GigID", selectedRequest.getGigID());
                arguments.putString("VenueID", selectedRequest.getVenueID());
                arguments.putString("GigName", selectedRequest.getGigName());
                arguments.putString("GigStartDate", selectedRequest.getGigStartDate().toString());
                arguments.putString("GigEndDate", selectedRequest.getGigEndDate().toString());
                arguments.putString("RequestStatus", selectedRequest.getRequestStatus());

                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // request. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "VenueUserGigRequestsReceivedDetailsFragment")
                        .addToBackStack(null).commit();

                mListOfGigRequestsSent.clear();
                mListOfFilteredGigRequestsSent.clear();
                mListOfGigRequestsReceived.clear();
                mListOfFilteredGigRequestsReceived.clear();
            }
        });

        getActivity().setTitle("Gig Requests");

        return fragmentView;
    }

    private void PopulateListViews()
    {
        mVenueId = mDataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/venueID").getValue().toString();

        // This iterates through the gig requests that users have sent and adds them to a list (mListOfGigRequestsReceived)
        Iterable<DataSnapshot> receivedRequestChildren = mDataSnapshot.child("BandSentGigRequests/").getChildren();
        for (DataSnapshot child : receivedRequestChildren)
        {
            // The key is obtained from the level below to then get the children below that
            mBandIdKey = child.getKey();

            mPassedThrough = false;

            Iterable<DataSnapshot> levelDownReceivedRequestChildren = mDataSnapshot.child("BandSentGigRequests/" + mBandIdKey).getChildren();
            for (DataSnapshot levelDownChild : levelDownReceivedRequestChildren)
            {
                if(!mPassedThrough)
                {
                    GigRequest gigRequest;
                    gigRequest = levelDownChild.getValue(GigRequest.class);
                    mListOfGigRequestsReceived.add(gigRequest);
                }
            }

            mPassedThrough = true;
        }

        for (int i = 0; i < mListOfGigRequestsReceived.size(); i++)
        {
            if(mListOfGigRequestsReceived.get(i).getVenueID().equals(mVenueId))
            {
                mListOfFilteredGigRequestsReceived.add(mListOfGigRequestsReceived.get(i));
            }
        }

        if(getActivity() != null)
        {
            mReceivedGigRequestsAdapter = new VenueUserGigRequestsAdapter(getActivity(), R.layout.venue_user_gig_requests_list, mListOfFilteredGigRequestsReceived , mDataSnapshot);
            mReceivedGigRequestsListView.setAdapter(mReceivedGigRequestsAdapter);
        }

        mPassedThrough = false;

        // This iterates through the gig requests that the venue have sent and adds them to a list (mListOfGigRequestsSent)
        Iterable<DataSnapshot> sentRequestChildren = mDataSnapshot.child("VenueSentGigRequests/" + mVenueId).getChildren();
        for (DataSnapshot child : sentRequestChildren)
        {
            mGigIdKey = child.getKey();

            mPassedThrough = false;

            Iterable<DataSnapshot> levelDownSentRequestChildren = mDataSnapshot.child("VenueSentGigRequests/" + mVenueId + "/" + mGigIdKey).getChildren();
            for (DataSnapshot levelDownChild : levelDownSentRequestChildren)
            {
                // The key is obtained from the level below to then get the children below that
                mBandIdKey = levelDownChild.getKey();

                Iterable<DataSnapshot> secondLevelDowSentRequestChildren = mDataSnapshot.child("VenueSentGigRequests/" + mVenueId + "/" + mGigIdKey + "/").getChildren();
                for (DataSnapshot secondLevelDownChild : secondLevelDowSentRequestChildren)
                {
                    if(!mPassedThrough)
                    {
                        GigRequest gigRequest;
                        gigRequest = secondLevelDownChild.getValue(GigRequest.class);
                        mListOfGigRequestsSent.add(gigRequest);
                    }
                }

                mPassedThrough = true;
            }
        }

        for (int i = 0; i < mListOfGigRequestsSent.size(); i++)
        {
            if(mListOfGigRequestsSent.get(i).getVenueID().equals(mVenueId))
            {
                mListOfFilteredGigRequestsSent.add(mListOfGigRequestsSent.get(i));
            }
        }

        if(getActivity() != null)
        {
            mSentGigRequestsAdapter = new VenueUserGigRequestsAdapter(getActivity(), R.layout.venue_user_gig_requests_list, mListOfFilteredGigRequestsSent, mDataSnapshot);
            mSentGigRequestsListView.setAdapter(mSentGigRequestsAdapter);
        }
    }
}
