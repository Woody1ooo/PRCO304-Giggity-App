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


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigRequestsFragment extends Fragment
{
    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare Visual Components
    private ListView mSentGigRequestsListView;
    private ListView mReceivedGigRequestsListView;
    private MusicianUserGigRequestsAdapter mSentGigRequestsAdapter;
    private MusicianUserGigRequestsAdapter mReceivedGigRequestsAdapter;

    // Declare General Variables
    private ArrayList<GigRequest> mListOfGigRequestsSent = new ArrayList<>();
    private ArrayList<GigRequest> mListOfGigRequestsReceived = new ArrayList<>();
    private ArrayList<GigRequest> mListOfFilteredGigRequestsReceived = new ArrayList<>();
    private DataSnapshot mDataSnapshot;
    private String mGigIdKey;
    private String mBandId;
    private String mVenueIdKey;
    private Boolean mPassedThrough;

    public MusicianUserGigRequestsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_gig_requests, container, false);

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

        // When a sent gig request list item is selected this can then be viewed in further detail
        mSentGigRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                // This returns the selected request from the list view
                GigRequest selectedRequest = (GigRequest) mSentGigRequestsListView.getItemAtPosition(position);
                MusicianUserGigRequestsSentDetailsFragment fragment = new MusicianUserGigRequestsSentDetailsFragment();
                Bundle arguments = new Bundle();
                arguments.putString("BandID", selectedRequest.getBandID());
                arguments.putString("GigID", selectedRequest.getGigID());
                arguments.putString("VenueID", selectedRequest.getVenueID());
                arguments.putString("GigName", selectedRequest.getGigName());
                arguments.putString("VenueName", selectedRequest.getVenueName());
                arguments.putString("GigStartDate", selectedRequest.getGigStartDate().toString());
                arguments.putString("GigEndDate", selectedRequest.getGigEndDate().toString());
                arguments.putString("RequestStatus", selectedRequest.getRequestStatus());

                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // request. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserGigRequestsSentDetailsFragment")
                        .addToBackStack(null).commit();

                mListOfGigRequestsSent.clear();
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
                MusicianUserGigRequestsReceivedDetailsFragment fragment = new MusicianUserGigRequestsReceivedDetailsFragment();
                Bundle arguments = new Bundle();
                arguments.putString("BandID", selectedRequest.getBandID());
                arguments.putString("GigID", selectedRequest.getGigID());
                arguments.putString("VenueID", selectedRequest.getVenueID());
                arguments.putString("GigName", selectedRequest.getGigName());
                arguments.putString("VenueName", selectedRequest.getVenueName());
                arguments.putString("GigStartDate", selectedRequest.getGigStartDate().toString());
                arguments.putString("GigEndDate", selectedRequest.getGigEndDate().toString());

                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // request. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserGigRequestsReceivedDetailsFragment")
                        .addToBackStack(null).commit();

                mListOfGigRequestsSent.clear();
                mListOfGigRequestsReceived.clear();
                mListOfFilteredGigRequestsReceived.clear();
            }
        });

        // Set the fragment title
        getActivity().setTitle("Gig Requests");

        return fragmentView;
    }

    private void PopulateListViews()
    {
        if(mDataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").exists())
        {
            mBandId = mDataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").getValue().toString();

            // This iterates through the band requests that users have sent and adds them to a list (mListOfUserRequestsSent)
            Iterable<DataSnapshot> sentRequestChildren = mDataSnapshot.child("BandSentGigRequests/" + mBandId).getChildren();
            for (DataSnapshot child : sentRequestChildren)
            {
                GigRequest gigRequest;
                gigRequest = child.getValue(GigRequest.class);
                mListOfGigRequestsSent.add(gigRequest);
            }

            mPassedThrough = false;

            // This iterates through the band requests that users have sent and adds them to a list (mListOfUserRequestsSent)
            Iterable<DataSnapshot> receivedRequestChildren = mDataSnapshot.child("VenueSentGigRequests/").getChildren();
            for (DataSnapshot child : receivedRequestChildren)
            {
                // The key is obtained from the level below to then get the children below that
                mVenueIdKey = child.getKey();

                mPassedThrough = false;

                // This iterates through the gig requests that the venue have sent and obtains the gig id
                Iterable<DataSnapshot> levelDownReceivedRequestChildren = mDataSnapshot.child("VenueSentGigRequests/" + mVenueIdKey).getChildren();
                for (DataSnapshot levelDownChild : levelDownReceivedRequestChildren)
                {
                    mGigIdKey = levelDownChild.getKey();

                    mPassedThrough = false;

                    // This then iterates through and creates a gig request object to be added to the mListOfGigRequestsReceived list
                    Iterable<DataSnapshot> secondLevelDownSentRequestChildren = mDataSnapshot.child("VenueSentGigRequests/" + mVenueIdKey + "/" + mGigIdKey + "/").getChildren();
                    for (DataSnapshot secondLevelDownChild : secondLevelDownSentRequestChildren)
                    {
                        if (!mPassedThrough)
                        {
                            GigRequest gigRequest;
                            gigRequest = secondLevelDownChild.getValue(GigRequest.class);
                            mListOfGigRequestsReceived.add(gigRequest);
                        }

                        mPassedThrough = true;
                    }
                }

                // These are then filtered to only show those from this band
                for (int i = 0; i < mListOfGigRequestsReceived.size(); i++)
                {
                    if (mListOfGigRequestsReceived.get(i).getBandID().equals(mBandId))
                    {
                        mListOfFilteredGigRequestsReceived.add(mListOfGigRequestsReceived.get(i));
                    }
                }
            }

            if(getActivity() != null)
            {
                mSentGigRequestsAdapter = new MusicianUserGigRequestsAdapter(getActivity(), R.layout.musician_user_gig_requests_list, mListOfGigRequestsSent, mDataSnapshot);
                mReceivedGigRequestsAdapter = new MusicianUserGigRequestsAdapter(getActivity(), R.layout.musician_user_gig_requests_list, mListOfGigRequestsReceived, mDataSnapshot);
                mSentGigRequestsListView.setAdapter(mSentGigRequestsAdapter);
                mReceivedGigRequestsListView.setAdapter(mReceivedGigRequestsAdapter);
            }
        }
    }
}
