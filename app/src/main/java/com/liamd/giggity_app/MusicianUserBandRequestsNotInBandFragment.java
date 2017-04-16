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
public class MusicianUserBandRequestsNotInBandFragment extends Fragment
{
    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Declare Visual Components
    private ListView mSentBandRequestsListView;
    private ListView mReceivedBandRequestsListView;
    private MusicianUserBandRequestsAdapter mSentBandRequestsAdapter;
    private MusicianUserBandRequestsAdapter mReceivedBandRequestsAdapter;

    // Declare General Variables
    private ArrayList<BandRequest> mListOfBandRequestsSent = new ArrayList<>();
    private ArrayList<BandRequest> mListOfBandRequestsReceived = new ArrayList<>();
    private ArrayList<BandRequest> mListOfFilteredBandRequestsReceived = new ArrayList<>();
    private DataSnapshot mMusicianSentDataSnapshot;
    private DataSnapshot mBandSentDataSnapshot;
    private Boolean mPassedThrough;
    private String mBandIdKey;

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

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mMusicianSentDataSnapshot = dataSnapshot.child("MusicianSentBandRequests/" + mAuth.getCurrentUser().getUid());
                mBandSentDataSnapshot = dataSnapshot.child("BandSentMusicianRequests/");
                PopulateListViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mSentBandRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                final BandRequest selectedBandRequest = (BandRequest) mSentBandRequestsListView.getItemAtPosition(position);
                MusicianUserBandRequestsNotInBandSentDetailsFragment fragment = new MusicianUserBandRequestsNotInBandSentDetailsFragment();
                final Bundle arguments = new Bundle();

                mDatabase.child("Bands/" + selectedBandRequest.getBandID() + "/genres").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        arguments.putString("BandGenres", GetGenreData(dataSnapshot));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                arguments.putString("BandName", selectedBandRequest.getBandName());
                arguments.putString("BandID", selectedBandRequest.getBandID());
                arguments.putString("PositionInstruments", selectedBandRequest.getPositionInstruments());
                arguments.putString("RequestStatus", selectedBandRequest.getRequestStatus());

                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // request. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserBandRequestsNotInBandSentDetailsFragment")
                        .addToBackStack(null).commit();

                // This clears the lists to prevent duplicates when the fragment is returned to
                mListOfBandRequestsSent.clear();
                mListOfBandRequestsReceived.clear();
                mListOfFilteredBandRequestsReceived.clear();
            }
        });

        mReceivedBandRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                final BandRequest selectedBandRequest = (BandRequest) mReceivedBandRequestsListView.getItemAtPosition(position);
                MusicianUserBandRequestsNotInBandReceivedDetailsFragment fragment = new MusicianUserBandRequestsNotInBandReceivedDetailsFragment();
                final Bundle arguments = new Bundle();

                arguments.putString("BandName", selectedBandRequest.getBandName());
                arguments.putString("BandID", selectedBandRequest.getBandID());
                arguments.putString("PositionInstruments", selectedBandRequest.getPositionInstruments());

                fragment.setArguments(arguments);

                // Creates a new fragment transaction to display the details of the selected
                // request. Some custom animation has been added also.
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager()
                        .beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.enter_from_left);
                fragmentTransaction.replace(R.id.frame, fragment, "MusicianUserBandRequestsNotInBandReceivedDetailsFragment")
                        .addToBackStack(null).commit();

                // This clears the lists to prevent duplicates when the fragment is returned to
                mListOfBandRequestsSent.clear();
                mListOfBandRequestsReceived.clear();
                mListOfFilteredBandRequestsReceived.clear();
            }
        });

        // Set the fragment title
        getActivity().setTitle("Band Requests");

        return fragmentView;
    }

    // This method retrieves the genre data from the database
    private String GetGenreData(DataSnapshot snapshot)
    {
        return snapshot.getValue().toString();
    }

    private void PopulateListViews()
    {
        // This iterates through the band requests the user has sent and adds them to a list (mListOfBandRequestsSent)
        Iterable<DataSnapshot> sentRequestChildren = mMusicianSentDataSnapshot.getChildren();
        for (DataSnapshot child : sentRequestChildren)
        {
            BandRequest bandRequest;
            bandRequest = child.getValue(BandRequest.class);
            mListOfBandRequestsSent.add(bandRequest);
        }

        mPassedThrough = false;

        // This iterates through the band requests the user has received and adds them to a list (mListOfBandRequestsReceived)
        // This has been commented as this feature hasn't yet been enabled
        Iterable<DataSnapshot> receivedRequestChildren = mBandSentDataSnapshot.getChildren();
        for (DataSnapshot child : receivedRequestChildren)
        {
            mBandIdKey = child.getKey();

            mPassedThrough = false;

            Iterable<DataSnapshot> levelDownReceivedRequestChildren = mBandSentDataSnapshot.child(mBandIdKey).getChildren();
            for (DataSnapshot levelDownChild : levelDownReceivedRequestChildren)
            {
                if (!mPassedThrough)
                {
                    BandRequest bandRequest;
                    bandRequest = levelDownChild.getValue(BandRequest.class);
                    mListOfBandRequestsReceived.add(bandRequest);
                }
                mPassedThrough = true;
            }
        }

        for (int i = 0; i < mListOfBandRequestsReceived.size(); i++)
        {
            if(mListOfBandRequestsReceived.get(i).getUserID().equals(mAuth.getCurrentUser().getUid()))
            {
                mListOfFilteredBandRequestsReceived.add(mListOfBandRequestsReceived.get(i));
            }
        }


        mSentBandRequestsAdapter = new MusicianUserBandRequestsAdapter(getActivity(), R.layout.musician_user_band_requests_list, mListOfBandRequestsSent);
        mReceivedBandRequestsAdapter = new MusicianUserBandRequestsAdapter(getActivity(), R.layout.musician_user_band_requests_list, mListOfFilteredBandRequestsReceived);

        mSentBandRequestsListView.setAdapter(mSentBandRequestsAdapter);
        mReceivedBandRequestsListView.setAdapter(mReceivedBandRequestsAdapter);
    }

}
