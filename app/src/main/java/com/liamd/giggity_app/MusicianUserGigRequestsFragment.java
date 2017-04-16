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

            if(getActivity() != null)
            {
                mSentGigRequestsAdapter = new MusicianUserGigRequestsAdapter(getActivity(), R.layout.musician_user_gig_requests_list, mListOfGigRequestsSent, mDataSnapshot);
                mSentGigRequestsListView.setAdapter(mSentGigRequestsAdapter);
            }
        }
    }
}
