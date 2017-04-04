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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by liamd on 04/04/2017.
 */

public class VenueUserBandsAdapter extends ArrayAdapter<Band>
{
    // Declare visual components
    private TextView mBandNameTextView;
    private TextView mNumberOfMembers;
    private TextView mGenresTextView;
    private TextView mDistanceTextView;
    private ImageView mBandImage;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    // Declare various variables required
    private int resource;

    public VenueUserBandsAdapter(Context context, int resource, List<Band> items)
    {
        super(context, resource, items);
        this.resource = resource;

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout bandsListView;

        Band band = getItem(position);

        if (convertView == null)
        {
            bandsListView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater) getContext().getSystemService(inflater);
            layoutInflater.inflate(resource, bandsListView, true);
        }

        else
        {
            bandsListView = (LinearLayout) convertView;
        }

        // Initialise visual components
        mBandNameTextView = (TextView) bandsListView.findViewById(R.id.bandName);
        mDistanceTextView = (TextView) bandsListView.findViewById(R.id.distance);
        mGenresTextView = (TextView) bandsListView.findViewById(R.id.genres);
        mNumberOfMembers = (TextView) bandsListView.findViewById(R.id.numberOfMembers);
        mBandImage = (ImageView) bandsListView.findViewById(R.id.bandImage);

        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mProfileImageReference.child("BandProfileImages/" + band.getBandID() + "/profileImage"))
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mBandImage);

        // Set list view fields to display the correct information
        mBandNameTextView.setText("Name: " + band.getName());
        mBandNameTextView.setTypeface(null, Typeface.BOLD);
        mDistanceTextView.setText("Distance: " + band.getBandDistance() + "km");
        mGenresTextView.setText("Genres: " + band.getGenres());
        mNumberOfMembers.setText("Number Of Members: " + band.getNumberOfPositions());

        return bandsListView;
    }
}
