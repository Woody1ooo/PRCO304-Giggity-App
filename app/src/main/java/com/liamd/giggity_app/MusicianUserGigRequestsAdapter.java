package com.liamd.giggity_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import java.util.List;

/**
 * Created by liamd on 03/04/2017.
 */

public class MusicianUserGigRequestsAdapter extends ArrayAdapter<GigRequest>
{
    // Declare Firebase specific variables
    private DatabaseReference mDatabase;

    // Declare visual components
    private TextView mGigNameTextView;
    private TextView mVenueNameTextView;
    private TextView mGigStatusTextView;

    // Declare general variables
    private String mGigName;
    private String mVenueID;
    private String mVenueName;
    private DataSnapshot mSnapshot;

    // Declare various variables required
    private int resource;

    public MusicianUserGigRequestsAdapter(Context context, int resource, List<GigRequest> items, DataSnapshot snapshot)
    {
        super(context, resource, items);
        this.resource = resource;
        this.mSnapshot = snapshot;
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

        mGigName = gigRequest.getGigName();
        mVenueID = gigRequest.getVenueID();
        mVenueName = gigRequest.getVenueName();

        // Initialise visual components
        mGigNameTextView = (TextView) gigRequestListView.findViewById(R.id.requestGigName);
        mVenueNameTextView = (TextView) gigRequestListView.findViewById(R.id.requestVenueName);
        mGigStatusTextView = (TextView) gigRequestListView.findViewById(R.id.requestStatus);

        // Set list view fields to display the correct information
        mGigNameTextView.setText("Gig Name: " + mGigName);
        mGigNameTextView.setTypeface(null, Typeface.BOLD);
        mVenueNameTextView.setText("Venue: " + mVenueName);
        mGigStatusTextView.setText(gigRequest.getRequestStatus());

        // Depending on the status the colour is updated
        if (gigRequest.getRequestStatus().equals("Pending"))
        {
            mGigStatusTextView.setTextColor(Color.parseColor("#ff6100"));
        } else if (gigRequest.getRequestStatus().equals("Rejected"))
        {
            mGigStatusTextView.setTextColor(Color.RED);
        } else if (gigRequest.getRequestStatus().equals("Accepted"))
        {
            mGigStatusTextView.setTextColor(Color.GREEN);
        }

        return gigRequestListView;
    }
}
