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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.List;

/**
 * Created by liamd on 27/02/2017.
 */

public class VenueUserGigsAdapter extends ArrayAdapter<Gig>
{
    // Declare visual components
    private TextView mGigNameTextView;
    private TextView mGigActTextView;
    private ImageView mGigActImageView;
    private TextView mGigStartDateTextView;
    private TextView mGigFinishDateTextView;

    // Declare various variables required
    private int resource;
    private Date mGigStartDate;
    private Date mGigFinishDate;
    private DataSnapshot mDataSnapshot;
    private String mBandId;
    private String mBandName;

    // Declare firebase variables
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    public VenueUserGigsAdapter(Context context, int resource, List<Gig> items, DataSnapshot snapshot)
    {
        super(context, resource, items);
        this.resource = resource;
        this.mDataSnapshot = snapshot;

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();
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
        mGigNameTextView = (TextView)gigsListView.findViewById(R.id.gigNameTextView);
        mGigActTextView = (TextView)gigsListView.findViewById(R.id.gigActTextView);
        mGigActImageView = (ImageView)gigsListView.findViewById(R.id.gigActImageView);
        mGigStartDateTextView = (TextView)gigsListView.findViewById(R.id.gigStartDateTextView);
        mGigFinishDateTextView = (TextView)gigsListView.findViewById(R.id.gigFinishDateTextView);

        mGigNameTextView.setText(gig.getTitle());
        mGigNameTextView.setTypeface(null, Typeface.BOLD);

        if(!mDataSnapshot.child("Gigs/" + gig.getGigId() + "/bookedAct").getValue().equals("Vacant"))
        {
            mBandId = mDataSnapshot.child("Gigs/" + gig.getGigId() + "/bookedAct").getValue().toString();
            mBandName = mDataSnapshot.child("Bands/" + mBandId + "/name").getValue().toString();

            mGigActTextView.setText(mBandName);

            Glide.with(getContext()).using(new FirebaseImageLoader()).load
                    (mProfileImageReference.child("BandProfileImages/" +  mBandId +  "/profileImage"))
                    .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mGigActImageView);
        }

        else
        {
            mGigActTextView.setText("No act currently booked!");
        }

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
        mGigFinishDateTextView.setText("Finish Date/Time: " + formattedFinishDateSectionOne + " " + formattedFinishDateSectionTwo + " " + formattedFinishDateSectionThree + " " + formattedFinishDateSectionFour);
        return gigsListView;
    }
}
