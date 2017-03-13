package com.liamd.giggity_app;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import java.util.List;

/**
 * Created by liamd on 27/02/2017.
 */

public class MusicianGigsAdapter extends ArrayAdapter<Gig>
{
    // Declare visual components
    private TextView mVenueNameTextView;
    private TextView mDistanceTextView;
    private TextView mGigNameTextView;
    private TextView mGigDateTextView;
    private ImageView mVenueImageView;

    // Declare general variables
    private String mVenueName;
    private String mDistance;
    private Location mGigLocation;
    private double mGigLocationLat;
    private double mGigLocationLng;
    private Location mUserLocation;
    private double mUserLocationLat;
    private double mUserLocationLng;

    private GoogleApiClient mGoogleApiClient;

    // Declare various variables required
    private int resource;

    public MusicianGigsAdapter(Context context, int resource, List<Gig> items)
    {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext()).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).build();

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
        mGigDateTextView = (TextView) gigsListView.findViewById(R.id.gigDate);
        mVenueImageView = (ImageView) gigsListView.findViewById(R.id.venueImage);

        //placePhotosAsync();

        // Initialise other variables required
        mGigLocation = new Location("");
        mUserLocation = new Location("");

        // Get the name of the gigs venue from the previous fragment
        mVenueName = MusicianUserGigResultsFragment.getVenueSnapshot().child(gig.getVenueID() + "/name").getValue().toString();

        // Get the location of the gig from the previous fragment
        mGigLocationLat = MusicianUserGigResultsFragment.getVenueSnapshot().child(gig.getVenueID() + "/venueLocation/latitude").getValue(Double.class);
        mGigLocationLng = MusicianUserGigResultsFragment.getVenueSnapshot().child(gig.getVenueID() + "/venueLocation/longitude").getValue(Double.class);

        // Then set the data as parameters for the gig location object
        mGigLocation.setLatitude(mGigLocationLat);
        mGigLocation.setLongitude(mGigLocationLng);

        // Get the user's location from the previous fragment and set it against the mUserLocation variable
        mUserLocationLat = MusicianUserGigResultsFragment.getLocation().latitude;
        mUserLocationLng = MusicianUserGigResultsFragment.getLocation().longitude;

        // Then set the data as parameters for the user location object
        mUserLocation.setLatitude(mUserLocationLat);
        mUserLocation.setLongitude(mUserLocationLng);

        // Calculate the distance between the provided location and the gig
        mDistance = Double.toString(CalculateDistance(mGigLocation, mUserLocation));

        // Set list view fields to display the correct information
        mVenueNameTextView.setText(mVenueName);
        mVenueNameTextView.setTypeface(null, Typeface.BOLD);
        mDistanceTextView.setText(mDistance + "km");
        mGigNameTextView.setText(gig.getTitle());
        mGigDateTextView.setText(gig.getStartDate().toString());

        // This calls the method to load the photos, though it doesn't work at the moment...
        placePhotosAsync();

        return gigsListView;
    }

    // This method takes the gig location and the user's location and calculates the distance between the two
    private double CalculateDistance(Location gigLocation, Location userLocation)
    {
        double distance;

        // This calculates the distance between the passed gig location and the user's current location
        distance = gigLocation.distanceTo(userLocation);

        // This converts the distance into km
        distance = distance / 1000;

        // This then rounds the distance to 2DP
        distance = Math.round(distance * 100D) / 100D;

        return distance;
    }


    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback = new ResultCallback<PlacePhotoResult>()
    {
        @Override
        public void onResult(PlacePhotoResult placePhotoResult)
        {
            if (!placePhotoResult.getStatus().isSuccess())
            {
                return;
            }

            mVenueImageView.setImageBitmap(placePhotoResult.getBitmap());
        }
    };


     /**
     * Load a bitmap from the photos API asynchronously
     * by using buffers and result callbacks.
     */
    private void placePhotosAsync()
    {
        final String placeId = "ChIJrTLr-GyuEmsRBfy61i59si0"; // Australian Cruise Group
        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeId).setResultCallback(new ResultCallback<PlacePhotoMetadataResult>()
                {
                    @Override
                    public void onResult(PlacePhotoMetadataResult photos)
                    {
                        if (!photos.getStatus().isSuccess())
                        {
                            return;
                        }

                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                        if (photoMetadataBuffer.getCount() > 0)
                        {
                            // Display the first bitmap in an ImageView in the size of the view
                            photoMetadataBuffer.get(0)
                                    .getScaledPhoto(mGoogleApiClient, mVenueImageView.getWidth(),
                                            mVenueImageView.getHeight())
                                    .setResultCallback(mDisplayPhotoResultCallback);
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }
}
