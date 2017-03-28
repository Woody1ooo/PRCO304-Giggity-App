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
 * Created by liamd on 28/03/2017.
 */

public class MusicianBandRequestsAdapter extends ArrayAdapter<BandRequest>
{
    // Declare visual components
    private TextView mBandNameTextView;
    private TextView mBandInstrumentsTextView;
    private TextView mBandRequestStatusTextView;
    private ImageView mBandImage;
    private FirebaseStorage mStorage;
    private StorageReference mBandProfileImageReference;

    // Declare various variables required
    private int resource;

    public MusicianBandRequestsAdapter(Context context, int resource, List<BandRequest> items)
    {
        super(context, resource, items);
        this.resource = resource;

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mBandProfileImageReference = mStorage.getReference();
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
        mBandNameTextView = (TextView) bandRequestListView.findViewById(R.id.requestBandName);
        mBandInstrumentsTextView = (TextView) bandRequestListView.findViewById(R.id.requestPositionInstruments);
        mBandRequestStatusTextView = (TextView) bandRequestListView.findViewById(R.id.requestStatus);
        mBandImage = (ImageView) bandRequestListView.findViewById(R.id.requestBandImage);

        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mBandProfileImageReference.child("BandProfileImages/" +  bandRequest.getBandID() +  "/profileImage"))
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mBandImage);

        // Set list view fields to display the correct information
        mBandNameTextView.setText("Name: " + bandRequest.getBandName());
        mBandNameTextView.setTypeface(null, Typeface.BOLD);
        mBandInstrumentsTextView.setText("Instruments: " + bandRequest.getPositionInstruments());
        mBandRequestStatusTextView.setText("Request Status: " + bandRequest.getRequestStatus());

        return bandRequestListView;
    }
}
