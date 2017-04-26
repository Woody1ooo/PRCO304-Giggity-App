package com.liamd.giggity_app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserHomeFragment extends Fragment
{
    // Declare Firebase specific variables
    private DatabaseReference mDatabase;

    // Declare Visual Components
    private ListView mNewsFeedListView;
    private NewsFeedAdapter adapter;

    // Declare general variables
    private ArrayList<NewsFeedItem> mListOfItems = new ArrayList<>();

    public MusicianUserHomeFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_home, container, false);

        // set fragment name
        getActivity().setTitle("Home");

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        mNewsFeedListView = (ListView) fragmentView.findViewById(R.id.newsFeedListView);

        mDatabase.child("NewsFeedItems/").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mListOfItems.clear();

                // This iterates through the venues and adds them to a list (mListOfVenues)
                Iterable<DataSnapshot> newsItemChildren = dataSnapshot.getChildren();
                for (DataSnapshot child : newsItemChildren)
                {
                    NewsFeedItem item;
                    item = child.getValue(NewsFeedItem.class);

                    mListOfItems.add(item);

                    if(getActivity() != null && adapter == null)
                    {
                        adapter = new NewsFeedAdapter(getActivity(), R.layout.news_feed_list, mListOfItems, dataSnapshot);
                        mNewsFeedListView.setAdapter(adapter);
                    }

                    else
                    {
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        return fragmentView;
    }
}
