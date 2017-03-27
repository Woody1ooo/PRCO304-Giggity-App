package com.liamd.giggity_app;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by liamd on 25/03/2017.
 */

public class MusicianBandsAdapter extends ArrayAdapter<Band>
{
    // Declare visual components
    private TextView mBandNameTextView;
    private TextView mNumberOfMembers;
    private TextView mGenresTextView;
    private TextView mDistanceTextView;
    private TextView mNumberOfVacancies;
    private ImageView mBandImage;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    // Declare various variables required
    private int resource;

    public MusicianBandsAdapter(Context context, int resource, List<Band> items)
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

        if(convertView == null)
        {
            bandsListView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
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
        mNumberOfVacancies = (TextView) bandsListView.findViewById(R.id.numberOfVacancies);

        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mProfileImageReference.child("BandProfileImages/" +  band.getBandID() +  "/profileImage"))
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mBandImage);


        // Set list view fields to display the correct information
        mBandNameTextView.setText("Name: " + band.getName());
        mBandNameTextView.setTypeface(null, Typeface.BOLD);
        mDistanceTextView.setText("Distance: " + band.getBandDistance() + "km");
        mGenresTextView.setText("Genres: " + band.getGenres());
        mNumberOfMembers.setText("Total Band Positions: " + band.getNumberOfPositions());

        // This checks how many positions the band has and from that determines which class method to call to determine the number of vacancies
        if(band.getNumberOfPositions().equals("1"))
        {
            mNumberOfVacancies.setText("Number of Band Positions Available: " + band.GetNumberOfVacancies(band.getPositionOneMember()));
        }

        else if(band.getNumberOfPositions().equals("2"))
        {
            mNumberOfVacancies.setText("Number of Band Positions Available: " + band.GetNumberOfVacancies(band.getPositionOneMember(), band.getPositionTwoMember()));
        }

        else if(band.getNumberOfPositions().equals("3"))
        {
            mNumberOfVacancies.setText("Number of Band Positions Available: " + band.GetNumberOfVacancies(band.getPositionOneMember(), band.getPositionTwoMember(), band.getPositionThreeMember()));
        }

        else if(band.getNumberOfPositions().equals("4"))
        {
            mNumberOfVacancies.setText("Number of Band Positions Available: " + band.GetNumberOfVacancies(band.getPositionOneMember(), band.getPositionTwoMember(), band.getPositionThreeMember(), band.getPositionFourMember()));
        }

        else if(band.getNumberOfPositions().equals("5"))
        {
            mNumberOfVacancies.setText("Number of Band Positions Available: " + band.GetNumberOfVacancies(band.getPositionOneMember(), band.getPositionTwoMember(), band.getPositionThreeMember(), band.getPositionFourMember(), band.getPositionFiveMember()));
        }

        return bandsListView;
    }
}
