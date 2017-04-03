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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by liamd on 03/04/2017.
 */

public class VenueUserGigRequestsAdapter extends ArrayAdapter<GigRequest>
{
    // Declare visual components
    private TextView mGigNameTextView;
    private TextView mBandNameTextView;
    private TextView mGigStatusTextView;
    private ImageView mBandImage;

    // Declare general variables
    private String mGigName;
    private String mBandName;
    private DataSnapshot mSnapshot;

    // Declare firebase specific variables
    private FirebaseStorage mStorage;
    private StorageReference mBandProfileImageReference;

    // Declare various variables required
    private int resource;

    public VenueUserGigRequestsAdapter(Context context, int resource, List<GigRequest> items, DataSnapshot snapshot)
    {
        super(context, resource, items);
        this.resource = resource;
        this.mSnapshot = snapshot;

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mBandProfileImageReference = mStorage.getReference();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout gigRequestListView;

        final GigRequest gigRequest = getItem(position);

        if (convertView == null)
        {
            gigRequestListView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater) getContext().getSystemService(inflater);
            layoutInflater.inflate(resource, gigRequestListView, true);
        }

        else
        {
            gigRequestListView = (LinearLayout) convertView;
        }

        mGigName = mSnapshot.child("Gigs/" + gigRequest.getGigID() + "/title").getValue().toString();
        mBandName = mSnapshot.child("Bands/" + gigRequest.getBandID() + "/name").getValue().toString();

        // Initialise visual components
        mGigNameTextView = (TextView) gigRequestListView.findViewById(R.id.requestGigName);
        mBandNameTextView = (TextView) gigRequestListView.findViewById(R.id.requestBandName);
        mGigStatusTextView = (TextView) gigRequestListView.findViewById(R.id.requestStatus);
        mBandImage = (ImageView) gigRequestListView.findViewById(R.id.bandImage);

        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mBandProfileImageReference.child("BandProfileImages/" +  gigRequest.getBandID() +  "/profileImage"))
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mBandImage);

        // Set list view fields to display the correct information
        mGigNameTextView.setText("Gig Name: " + mGigName);
        mGigNameTextView.setTypeface(null, Typeface.BOLD);
        mBandNameTextView.setText("Band: " + mBandName);
        mGigStatusTextView.setText(gigRequest.getRequestStatus());

        // Depending on the status the colour is updated
        if (gigRequest.getRequestStatus().equals("Pending"))
        {
            mGigStatusTextView.setTextColor(Color.parseColor("#ff6100"));
        }

        else if (gigRequest.getRequestStatus().equals("Denied"))
        {
            mGigStatusTextView.setTextColor(Color.RED);
        }

        else if (gigRequest.getRequestStatus().equals("Accepted"))
        {
            mGigStatusTextView.setTextColor(Color.GREEN);
        }

        return gigRequestListView;
    }
}
