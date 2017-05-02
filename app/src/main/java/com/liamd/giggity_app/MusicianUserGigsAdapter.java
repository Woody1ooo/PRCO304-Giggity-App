package com.liamd.giggity_app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import java.util.Date;
import java.util.List;

/**
 * Created by liamd on 27/02/2017.
 */

public class MusicianUserGigsAdapter extends ArrayAdapter<Gig>
{
    // Declare visual components
    private TextView mVenueNameTextView;
    private TextView mDistanceTextView;
    private TextView mGigNameTextView;
    private TextView mGigStartDateTextView;
    private TextView mGigEndDateTextView;

    // Declare general variables
    private String mVenueName;
    private Date mGigStartDate;
    private Date mGigFinishDate;

    // Declare various variables required
    private int resource;

    public MusicianUserGigsAdapter(Context context, int resource, List<Gig> items)
    {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout gigsListView;

        Gig gig = getItem(position);

        if(convertView == null)
        {
            gigsListView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
            layoutInflater.inflate(resource, gigsListView, true);
        }

        else
        {
            gigsListView = (LinearLayout) convertView;
        }

        // Initialise visual components
        mVenueNameTextView = (TextView) gigsListView.findViewById(R.id.venueName);
        mDistanceTextView = (TextView) gigsListView.findViewById(R.id.distance);
        mGigNameTextView = (TextView) gigsListView.findViewById(R.id.gigName);
        mGigStartDateTextView = (TextView) gigsListView.findViewById(R.id.gigStartDate);
        mGigEndDateTextView = (TextView) gigsListView.findViewById(R.id.gigEndDate);

        // Get the name of the gigs venue from the previous fragment
        mVenueName = MusicianUserGigResultsFragment.getVenueSnapshot().child(gig.getVenueID() + "/name").getValue().toString();

        // Set list view fields to display the correct information
        mGigNameTextView.setText(gig.getTitle());
        mGigNameTextView.setTypeface(null, Typeface.BOLD);
        mVenueNameTextView.setText(mVenueName);
        mDistanceTextView.setText(gig.getGigDistance() + "km");

        mGigStartDate = gig.getStartDate();
        mGigFinishDate = gig.getEndDate();

        // This takes the start and end dates and reformats them to look more visually appealing
        String formattedStartDateSectionOne = mGigStartDate.toString().split(" ")[0];
        String formattedStartDateSectionTwo = mGigStartDate.toString().split(" ")[1];
        String formattedStartDateSectionThree = mGigStartDate.toString().split(" ")[2];
        String formattedStartDateSectionFour = mGigStartDate.toString().split(" ")[3];

        String formattedFinishDateSectionOne = mGigFinishDate.toString().split(" ")[0];
        String formattedFinishDateSectionTwo = mGigFinishDate.toString().split(" ")[1];
        String formattedFinishDateSectionThree = mGigFinishDate.toString().split(" ")[2];
        String formattedFinishDateSectionFour = mGigFinishDate.toString().split(" ")[3];

        mGigStartDateTextView.setText("Start Date/Time: " + formattedStartDateSectionOne + " " + formattedStartDateSectionTwo + " " + formattedStartDateSectionThree + " " + formattedStartDateSectionFour);
        mGigEndDateTextView.setText("Finish Date/Time: " + formattedFinishDateSectionOne + " " + formattedFinishDateSectionTwo + " " + formattedFinishDateSectionThree + " " + formattedFinishDateSectionFour);

        return gigsListView;
    }
}
