package com.liamd.giggity_app;

import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
public class MusicianUserBandRequestsInBandFragment extends Fragment
{
    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare Visual Components
    private ListView mSentUserRequestsListView;
    private ListView mReceivedUserRequestsListView;
    private MusicianUserRequestsAdapter mSentUserRequestsAdapter;
    private MusicianUserRequestsAdapter mReceivedUserRequestsAdapter;

    // Declare General Variables
    private ArrayList<BandRequest> mListOfUserRequestsSent = new ArrayList<>();
    private ArrayList<BandRequest> mListOfUserRequestsReceived = new ArrayList<>();
    private ArrayList<BandRequest> mListOfFilteredUserRequestsReceived = new ArrayList<>();
    private DataSnapshot mDataSnapshot;
    private String mUserIdKey;
    private String mBandId;

    private Boolean mPassedThrough;

    public MusicianUserBandRequestsInBandFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_requests_in_band, container, false);

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
        mSentUserRequestsListView = (ListView) fragmentView.findViewById(R.id.sentMusicianRequestsListView);
        mReceivedUserRequestsListView = (ListView) fragmentView.findViewById(R.id.receivedMusicianRequestsListView);

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

        mReceivedUserRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                // This returns the selected request from the list view
                BandRequest selectedRequest = (BandRequest) mReceivedUserRequestsListView.getItemAtPosition(position);
                MusicianUserBandRequestsInBandReceivedDetailsFragment fragment = new MusicianUserBandRequestsInBandReceivedDetailsFragment();
                Bundle arguments = new Bundle();
                arguments.putString("UserID", selectedRequest.getUserID());
                arguments.putString("UserName", selectedRequest.getUserName());
                arguments.putString("BandID", selectedRequest.getBandID());
                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // gig. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserBandRequestsInBandReceivedDetailsFragment")
                        .addToBackStack(null).commit();

                // These must be cleared to prevent duplication as the database is called again
                // when the fragment is returned to. This is required to update any changes made
                // to the gigs by the user
                mListOfUserRequestsSent.clear();
                mListOfUserRequestsReceived.clear();
                mListOfFilteredUserRequestsReceived.clear();
                mSentUserRequestsAdapter.clear();
                mReceivedUserRequestsAdapter.clear();
            }
        });

        mSentUserRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                // This returns the selected request from the list view
                BandRequest selectedRequest = (BandRequest) mSentUserRequestsListView.getItemAtPosition(position);
                MusicianUserBandRequestsInBandSentDetailsFragment fragment = new MusicianUserBandRequestsInBandSentDetailsFragment();
                Bundle arguments = new Bundle();
                arguments.putString("UserID", selectedRequest.getUserID());
                arguments.putString("UserName", selectedRequest.getUserName());
                arguments.putString("BandID", selectedRequest.getBandID());
                arguments.putString("PositionOffered", selectedRequest.getPositionInstruments());
                arguments.putString("UserInstruments", selectedRequest.getUserInstruments());
                arguments.putString("RequestStatus", selectedRequest.getRequestStatus());
                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // gig. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserBandRequestsInBandSentDetailsFragment")
                        .addToBackStack(null).commit();

                // These must be cleared to prevent duplication as the database is called again
                // when the fragment is returned to. This is required to update any changes made
                // to the gigs by the user
                mListOfUserRequestsSent.clear();
                mListOfUserRequestsReceived.clear();
                mListOfFilteredUserRequestsReceived.clear();
                mSentUserRequestsAdapter.clear();
                mReceivedUserRequestsAdapter.clear();
            }
        });

        return fragmentView;
    }

    private void PopulateListViews()
    {
        mBandId = mDataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").getValue().toString();

        // This iterates through the user requests the band has sent and adds them to a list (mListOfUserRequestsSent)
        Iterable<DataSnapshot> sentRequestChildren = mDataSnapshot.child("BandSentMusicianRequests/" + mBandId).getChildren();
        for (DataSnapshot child : sentRequestChildren)
        {
            BandRequest bandRequest;
            bandRequest = child.getValue(BandRequest.class);
            mListOfUserRequestsSent.add(bandRequest);
        }

        mPassedThrough = false;

        // This iterates through the band requests that users have sent and adds them to a list (mListOfUserRequestsSent)
        Iterable<DataSnapshot> receivedRequestChildren = mDataSnapshot.child("MusicianSentBandRequests/").getChildren();
        for (DataSnapshot child : receivedRequestChildren)
        {
            // The key is obtained from the level below to then get the children below that
            mUserIdKey = child.getKey();

            mPassedThrough = false;

            Iterable<DataSnapshot> levelDownReceivedRequestChildren = mDataSnapshot.child("MusicianSentBandRequests/" + mUserIdKey).getChildren();
            for (DataSnapshot levelDownChild : levelDownReceivedRequestChildren)
            {
                if (!mPassedThrough)
                {
                    BandRequest bandRequest;
                    bandRequest = levelDownChild.getValue(BandRequest.class);
                    mListOfUserRequestsReceived.add(bandRequest);
                }
                mPassedThrough = true;
            }
        }

        for (int i = 0; i < mListOfUserRequestsReceived.size(); i++)
        {
            if(mListOfUserRequestsReceived.get(i).getBandID().equals(mBandId))
            {
                mListOfFilteredUserRequestsReceived.add(mListOfUserRequestsReceived.get(i));
            }
        }

        mSentUserRequestsAdapter = new MusicianUserRequestsAdapter(getActivity(), R.layout.musician_user_user_requests_list, mListOfUserRequestsSent);
        mReceivedUserRequestsAdapter = new MusicianUserRequestsAdapter(getActivity(), R.layout.musician_user_user_requests_list, mListOfUserRequestsReceived);

        mSentUserRequestsListView.setAdapter(mSentUserRequestsAdapter);
        mReceivedUserRequestsListView.setAdapter(mReceivedUserRequestsAdapter);
    }
}
