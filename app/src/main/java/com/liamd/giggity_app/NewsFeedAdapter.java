package com.liamd.giggity_app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by liamd on 24/04/2017.
 */

public class NewsFeedAdapter extends ArrayAdapter<NewsFeedItem>
{
    // Declare visual components
    private ImageView mFeaturedImageView;
    private TextView mUserNameTextView;
    private TextView mNewsFeedMessageTextView;
    private CircleImageView mImageView;
    private TextView mLikeCountTextView;
    private ImageButton mLikeButton;

    // Declare various variables required
    private int resource;
    private int mLikeCount;
    private boolean mPassedThrough = false;

    // Declare firebase variables
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private DataSnapshot mSnapshot;

    private List<NewsFeedItem> mListOfItems = new ArrayList<>();

    public NewsFeedAdapter(Context context, int resource, List<NewsFeedItem> items, DataSnapshot snapshot)
    {
        super(context, resource, items);
        this.resource = resource;
        this.mListOfItems = items;
        this.mSnapshot = snapshot;

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        LinearLayout newsFeedListView;

        final NewsFeedItem item = getItem(position);

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

        mFeaturedImageView = (ImageView)newsFeedListView.findViewById(R.id.featuredImageView);
        mUserNameTextView = (TextView)newsFeedListView.findViewById(R.id.userNameTextView);
        mNewsFeedMessageTextView = (TextView)newsFeedListView.findViewById(R.id.newsFeedMessageTextView);
        mImageView = (CircleImageView)newsFeedListView.findViewById(R.id.userImageView);
        mLikeButton = (ImageButton) newsFeedListView.findViewById(R.id.likeButton);
        mLikeCountTextView = (TextView)newsFeedListView.findViewById(R.id.likeCountTextView);

        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mProfileImageReference.child("ProfileImages/" +  item.getUserID() +  "/profileImage"))
                .override(300, 300).into(mImageView);

        mUserNameTextView.setText(item.getUserName());
        mUserNameTextView.setTypeface(null, Typeface.BOLD);

        mNewsFeedMessageTextView.setText(item.getMessage());

        if(!item.isFeatured())
        {
            mFeaturedImageView.setVisibility(View.GONE);
        }

        mLikeCountTextView.setText("Likes: " + item.getLikeCount());

        // Check if the user has already liked it and if so set it to the red one
        if(mSnapshot.child(item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid()).exists() && !mPassedThrough)
        {
            int id = getContext().getResources().getIdentifier("com.liamd.giggity_app:drawable/ic_like_red", null, null);
            mLikeButton.setImageResource(id);
            mLikeButton.setTag("Red");

            notifyDataSetChanged();
            mPassedThrough = true;
        }

        // Otherwise set it to the white icon
        else if(!mSnapshot.child(item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid()).exists() && !mPassedThrough)
        {
            int id = getContext().getResources().getIdentifier("com.liamd.giggity_app:drawable/ic_like_white", null, null);
            mLikeButton.setImageResource(id);
            mLikeButton.setTag("White");

            notifyDataSetChanged();
            mPassedThrough = true;
        }

        mLikeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // If the drawable is the white image it means the user can like the post
                if(mLikeButton.getTag().equals("White"))
                {
                    mLikeCount = item.getLikeCount();
                    mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likeCount").setValue(mLikeCount + 1);
                    mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid() + "/like").setValue("true");

                    mListOfItems.get(position).setLikeCount(mLikeCount + 1);

                    Picasso.with(getContext()).load(R.drawable.ic_like_red).resize(350, 350).into(mLikeButton);
                    mLikeButton.setTag("Red");

                    notifyDataSetChanged();
                }

                else if(mLikeButton.getTag().equals("Red"))
                {
                    mLikeCount = item.getLikeCount();
                    mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likeCount").setValue(mLikeCount -1);
                    mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid()).removeValue();

                    mListOfItems.get(position).setLikeCount(mLikeCount - 1);

                    Picasso.with(getContext()).load(R.drawable.ic_like_white).resize(350, 350).into(mLikeButton);
                    mLikeButton.setTag("White");

                    notifyDataSetChanged();
                }
            }
        });

        return newsFeedListView;
    }

    @Override
    public int getViewTypeCount()
    {
        return getCount();
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }
}
