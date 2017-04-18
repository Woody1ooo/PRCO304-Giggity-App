package com.liamd.giggity_app;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.util.Date;
import java.util.List;

/**
 * Created by liamd on 17/04/2017.
 */

public class MusicianUserViewGigsAdapter extends ArrayAdapter<Gig>
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
    private DataSnapshot mSnapshot;
    private Gig mGig;
    private Location mGigLocation;
    private double mGigLocationLat;
    private double mGigLocationLng;
    private Location mBandLocation;
    private LatLng mBandLocationFromDatabase;
    private double mDistance;
    private String mBandId;

    // Declare various variables required
    private int resource;

    public MusicianUserViewGigsAdapter(Context context, int resource, List<Gig> items, DataSnapshot dataSnapshot, String bandId)
    {
        super(context, resource, items);
        this.resource = resource;
        this.mSnapshot = dataSnapshot;
        this.mBandId = bandId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout gigsListView;

        Gig gig = getItem(position);
        mGig = gig;

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

        // Initialise the location variables
        mBandLocation = new Location("");
        mGigLocation = new Location("");

        // Initialise visual components
        mVenueNameTextView = (TextView) gigsListView.findViewById(R.id.venueName);
        mDistanceTextView = (TextView) gigsListView.findViewById(R.id.distance);
        mGigNameTextView = (TextView) gigsListView.findViewById(R.id.gigName);
        mGigStartDateTextView = (TextView) gigsListView.findViewById(R.id.gigStartDate);
        mGigEndDateTextView = (TextView) gigsListView.findViewById(R.id.gigEndDate);

        // Get the name of the gigs venue from the fragment
        mVenueName = mSnapshot.child("Venues/" + gig.getVenueID() + "/name").getValue().toString();

        // This method sets the required variables to get the distance between the band and the gig
        GetGigLocation();

        gig.setGigDistance(mGig.getGigDistance());

        // Set list view fields to display the correct information
        mGigNameTextView.setText(gig.getTitle());
        mGigNameTextView.setTypeface(null, Typeface.BOLD);
        mVenueNameTextView.setText(mVenueName);
        mDistanceTextView.setText(mGig.getGigDistance() + "km");

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

    private void GetGigLocation()
    {
        // Get the location of the gig from the previous fragment
        mGigLocationLat = mSnapshot.child("Venues/" + mGig.getVenueID() + "/venueLocation/latitude").getValue(Double.class);
        mGigLocationLng = mSnapshot.child("Venues/" + mGig.getVenueID() + "/venueLocation/longitude").getValue(Double.class);

        mBandLocationFromDatabase = new LatLng(Double.parseDouble(mSnapshot.child("Bands/" + mBandId + "/baseLocation/latitude").getValue().toString())
                , Double.parseDouble(mSnapshot.child("Bands/" + mBandId + "/baseLocation/longitude").getValue().toString()));

        // Then set the data as parameters for the gig location object
        mGigLocation.setLatitude(mGigLocationLat);
        mGigLocation.setLongitude(mGigLocationLng);

        // Then set the data as parameters for the band location object
        mBandLocation.setLatitude(mBandLocationFromDatabase.latitude);
        mBandLocation.setLongitude(mBandLocationFromDatabase.longitude);

        // Calculate the distance between the provided location and the gig
        mDistance = CalculateDistance(mGigLocation, mBandLocation);

        mGig.setGigDistance(mDistance);
    }

    // This method takes the gig location and the band's location and calculates the distance between the two
    private double CalculateDistance(Location gigLocation, Location bandLocation)
    {
        double distance;

        // This calculates the distance between the passed gig location and the band's location
        distance = gigLocation.distanceTo(bandLocation);

        // This converts the distance into km
        distance = distance / 1000;

        // This then rounds the distance to 2DP
        distance = Math.round(distance * 100D) / 100D;

        return distance;
    }
}
