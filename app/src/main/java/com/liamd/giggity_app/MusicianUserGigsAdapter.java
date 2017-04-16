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
    private ImageView mVenueImageView;

    // Declare general variables
    private String mVenueName;
    private Date mGigStartDate;
    private Date mGigFinishDate;

    private GoogleApiClient mGoogleApiClient;

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
        mGigStartDateTextView = (TextView) gigsListView.findViewById(R.id.gigStartDate);
        mGigEndDateTextView = (TextView) gigsListView.findViewById(R.id.gigEndDate);
        mVenueImageView = (ImageView) gigsListView.findViewById(R.id.venueImage);

        //placePhotosAsync();

        // Get the name of the gigs venue from the previous fragment
        mVenueName = MusicianUserGigResultsFragment.getVenueSnapshot().child(gig.getVenueID() + "/name").getValue().toString();

        // Set list view fields to display the correct information
        mGigNameTextView.setText(gig.getTitle());
        mGigNameTextView.setTypeface(null, Typeface.BOLD);
        mVenueNameTextView.setText(mVenueName);
        mDistanceTextView.setText(gig.getGigDistance() + "km");

        // This calls the method to load the photos, though it doesn't work at the moment...
        placePhotosAsync();

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
