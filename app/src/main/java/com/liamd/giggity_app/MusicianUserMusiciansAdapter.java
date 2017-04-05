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
 * Created by liamd on 05/04/2017.
 */

public class MusicianUserMusiciansAdapter extends ArrayAdapter<User>
{
    // Declare visual components
    private TextView mUserNameTextView;
    private TextView mUserInstrumentsTextView;
    private TextView mGenresTextView;
    private TextView mDistanceTextView;
    private ImageView mUserImage;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    // Declare various variables required
    private int resource;

    public MusicianUserMusiciansAdapter(Context context, int resource, List<User> items)
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
        LinearLayout musiciansListView;

        User user = getItem(position);

        if(convertView == null)
        {
            musiciansListView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
            layoutInflater.inflate(resource, musiciansListView, true);
        }

        else
        {
            musiciansListView = (LinearLayout) convertView;
        }

        // Initialise visual components
        mUserNameTextView = (TextView) musiciansListView.findViewById(R.id.userNameTextView);
        mUserInstrumentsTextView = (TextView) musiciansListView.findViewById(R.id.userInstrumentsTextView);
        mDistanceTextView = (TextView) musiciansListView.findViewById(R.id.userDistanceTextView);
        mGenresTextView = (TextView) musiciansListView.findViewById(R.id.userGenresTextView);
        mUserImage = (ImageView) musiciansListView.findViewById(R.id.userProfileImage);

        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mProfileImageReference.child("ProfileImages/" +  user.getUserID() +  "/profileImage"))
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mUserImage);

        // Set list view fields to display the correct information
        mUserNameTextView.setText("Name: " + user.getFirstName() + " " + user.getLastName());
        mUserNameTextView.setTypeface(null, Typeface.BOLD);
        mDistanceTextView.setText("Distance: " + user.getMusicianDistance() + "km");
        mGenresTextView.setText("Genres: " + user.getGenres());
        mUserInstrumentsTextView.setText("Instruments: " + user.getInstruments());

        return musiciansListView;
    }
}
