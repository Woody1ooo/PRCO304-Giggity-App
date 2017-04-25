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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liamd on 24/04/2017.
 */

public class NewsFeedAdapter extends ArrayAdapter<NewsFeedItem>
{
    // Declare visual components
    private TextView mUserNameTextView;
    private TextView mNewsFeedMessageTextView;
    private ImageView mImageView;

    // Declare various variables required
    private int resource;

    // Declare firebase variables
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    public NewsFeedAdapter(Context context, int resource, List<NewsFeedItem> items)
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
        LinearLayout newsFeedListView;

        NewsFeedItem item = getItem(position);

        if(convertView == null)
        {
            newsFeedListView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
            layoutInflater.inflate(resource, newsFeedListView, true);
        }

        else
        {
            newsFeedListView = (LinearLayout) convertView;
        }

        // Initialise visual components
        mUserNameTextView = (TextView)newsFeedListView.findViewById(R.id.userNameTextView);
        mNewsFeedMessageTextView = (TextView)newsFeedListView.findViewById(R.id.newsFeedMessageTextView);
        mImageView = (ImageView)newsFeedListView.findViewById(R.id.userImageView);

        mUserNameTextView.setText(item.getUserName());
        mUserNameTextView.setTypeface(null, Typeface.BOLD);

        mNewsFeedMessageTextView.setText(item.getMessage());

        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mProfileImageReference.child("ProfileImages/" +  item.getBandId() +  "/profileImage"))
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mImageView);

        return newsFeedListView;
    }
}
