package com.liamd.giggity_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by liamd on 29/03/2017.
 */

public class MusicianUserRequestsAdapter extends ArrayAdapter<BandRequest>
{
    // Declare visual components
    private TextView mUserNameTextView;
    private TextView mUserInstrumentsTextView;
    private TextView mPositionInstrumentsTextView;
    private TextView mBandRequestStatusTextView;
    private ImageView mProfileImage;

    // Declare various variables required
    private int resource;
    private FirebaseStorage mStorage;
    private StorageReference mBandProfileImageReference;
    private DatabaseReference mDatabase;
    private String userId;

    public MusicianUserRequestsAdapter(Context context, int resource, List<BandRequest> items)
    {
        super(context, resource, items);
        this.resource = resource;

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mBandProfileImageReference = mStorage.getReference();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout bandRequestListView;

        BandRequest bandRequest = getItem(position);

        if (convertView == null)
        {
            bandRequestListView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater) getContext().getSystemService(inflater);
            layoutInflater.inflate(resource, bandRequestListView, true);
        }

        else
        {
            bandRequestListView = (LinearLayout) convertView;
        }

        // Initialise visual components
        mUserNameTextView = (TextView) bandRequestListView.findViewById(R.id.requestUserName);
        mUserInstrumentsTextView = (TextView) bandRequestListView.findViewById(R.id.requestUserInstruments);
        mPositionInstrumentsTextView = (TextView) bandRequestListView.findViewById(R.id.positionInstruments);
        mBandRequestStatusTextView = (TextView) bandRequestListView.findViewById(R.id.requestStatus);
        mProfileImage = (ImageView) bandRequestListView.findViewById(R.id.requestUserImage);
        userId = bandRequest.getUserID();

        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mBandProfileImageReference.child("ProfileImages/" + userId + "/profileImage"))
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mProfileImage);

        // Set list view fields to display the correct information
        mUserNameTextView.setText("Name: " + bandRequest.getUserName());
        mUserNameTextView.setTypeface(null, Typeface.BOLD);
        mUserInstrumentsTextView.setText("Position: " + bandRequest.getPositionInstruments());
        mPositionInstrumentsTextView.setText("User's Instruments: " + bandRequest.getUserInstruments());
        mBandRequestStatusTextView.setText(bandRequest.getRequestStatus());

        // Depending on the status the colour is updated
        if (bandRequest.getRequestStatus().equals("Pending"))
        {
            mBandRequestStatusTextView.setTextColor(Color.parseColor("#ff6100"));
        } else if (bandRequest.getRequestStatus().equals("Denied"))
        {
            mBandRequestStatusTextView.setTextColor(Color.RED);
        } else if (bandRequest.getRequestStatus().equals("Accepted"))
        {
            mBandRequestStatusTextView.setTextColor(Color.GREEN);
        }

        return bandRequestListView;
    }
}
