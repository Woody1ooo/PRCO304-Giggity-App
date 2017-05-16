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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by liamd on 24/04/2017.
 */

public class NewsFeedAdapter extends ArrayAdapter<NewsFeedItem>
{
    // Declare various variables required
    private int resource;
    private int mLikeCount;
    private boolean mPassedThrough = false;
    private boolean mInitialRead = true;
    private int mInitialReadCounter = 0;
    private List<Map> mListOfLikedItems = new ArrayList<>();
    private String mPostDate;

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
        final ImageView mFeaturedImageView = (ImageView)newsFeedListView.findViewById(R.id.featuredImageView);
        final TextView mUserNameTextView = (TextView)newsFeedListView.findViewById(R.id.userNameTextView);
        final TextView mNewsFeedMessageTextView = (TextView)newsFeedListView.findViewById(R.id.newsFeedMessageTextView);
        final TextView mPostDateTextView = (TextView)newsFeedListView.findViewById(R.id.dateTextView);
        final CircleImageView mImageView = (CircleImageView)newsFeedListView.findViewById(R.id.userImageView);
        final ImageButton mLikeButton = (ImageButton) newsFeedListView.findViewById(R.id.likeButton);
        final TextView mLikeCountTextView = (TextView)newsFeedListView.findViewById(R.id.likeCountTextView);

        // This gets the date of the post and splits it into a more readable format
        mPostDate = item.getPostDate().toString();
        String formattedDateSectionTwo = mPostDate.split(" ")[1];
        String formattedDateSectionThree = mPostDate.split(" ")[2];
        String formattedDateSectionFour = mPostDate.split(" ")[3];

        mPostDateTextView.setText(formattedDateSectionTwo + " " + formattedDateSectionThree + " " + formattedDateSectionFour);

        // Load the user's image if that's relevant to the post
        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mProfileImageReference.child("ProfileImages/" +  item.getUserID() +  "/profileImage"))
                .override(300, 300).into(mImageView);

        mUserNameTextView.setText(item.getUserName());
        mUserNameTextView.setTypeface(null, Typeface.BOLD);

        // Set the text view to the news feed message
        mNewsFeedMessageTextView.setText(item.getMessage());

        if(!item.isFeatured())
        {
            mFeaturedImageView.setVisibility(View.GONE);
        }

        // Set the like count text view
        mLikeCountTextView.setText("Likes: " + item.getLikeCount());

        // Check if the user has already liked it and if so set it to the red one
        if(mSnapshot.child(item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid()).getValue() != null && !mPassedThrough)
        {
            int id = getContext().getResources().getIdentifier("com.liamd.giggity_app:drawable/ic_like_red", null, null);
            mLikeButton.setImageResource(id);

            if(mInitialRead)
            {
                // The key is the item id and the value is a boolean determining whether it's liked or not
                Map<String, Boolean> likedItemMap = new HashMap<>();
                likedItemMap.put(item.getItemID(), true);
                mListOfLikedItems.add(likedItemMap);

                notifyDataSetChanged();
            }

            else
            {
                // The key is the item id and the value is a boolean determining whether it's liked or not
                Map<String, Boolean> likedItemMap = new HashMap<>();
                likedItemMap.put(item.getItemID(), true);

                int counter = 0;
                boolean addItem = false;

                for(Map<String, Boolean> map : mListOfLikedItems)
                {
                    counter++;

                    if(map.containsKey(item.getItemID()))
                    {
                        break;
                    }

                    else if(counter == mListOfLikedItems.size())
                    {
                        addItem = true;
                    }
                }

                if(addItem)
                {
                    mListOfLikedItems.add(likedItemMap);
                }
            }

            notifyDataSetChanged();
        }

        // Otherwise set it to the white icon
        else if(mSnapshot.child(item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid()).getValue() == null && !mPassedThrough)
        {
            int id = getContext().getResources().getIdentifier("com.liamd.giggity_app:drawable/ic_like_white", null, null);
            mLikeButton.setImageResource(id);

            if(mInitialRead)
            {
                // The key is the item id and the value is a boolean determining whether it's liked or not
                Map<String, Boolean> likedItemMap = new HashMap<>();
                likedItemMap.put(item.getItemID(), false);
                mListOfLikedItems.add(likedItemMap);

                notifyDataSetChanged();
            }

            else
            {
                // The key is the item id and the value is a boolean determining whether it's liked or not
                Map<String, Boolean> likedItemMap = new HashMap<>();
                likedItemMap.put(item.getItemID(), false);

                int counter = 0;
                boolean addItem = false;

                for(Map<String, Boolean> map : mListOfLikedItems)
                {
                    counter++;

                    if(map.containsKey(item.getItemID()))
                    {
                        break;
                    }

                    else if(counter == mListOfLikedItems.size())
                    {
                        addItem = true;
                    }
                }

                if(addItem)
                {
                    mListOfLikedItems.add(likedItemMap);
                }

                notifyDataSetChanged();
            }
        }

        mLikeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // If this is the first time through then the snapshot will be accurate and can therefore be used to determine whether the user has
                // liked an item or not.
                if(!mPassedThrough)
                {
                    // If the drawable is the white image it means the user can like the post
                    if(mSnapshot.child(item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid() + "/like").getValue() == null)
                    {
                        mLikeCount = item.getLikeCount();
                        mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likeCount").setValue(mLikeCount + 1);
                        mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid() + "/like").setValue("true");

                        mListOfItems.get(position).setLikeCount(mLikeCount + 1);

                        // The key is the item id and the value is a boolean determining whether it's liked or not
                        Map<String, Boolean> likedItemMap = new HashMap<>();
                        likedItemMap.put(item.getItemID(), true);
                        mListOfLikedItems.add(likedItemMap);

                        int id = getContext().getResources().getIdentifier("com.liamd.giggity_app:drawable/ic_like_red", null, null);
                        Glide.with(getContext()).load(id).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mLikeButton);

                        notifyDataSetChanged();

                        mPassedThrough = true;
                    }

                    else if(mSnapshot.child(item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid() + "/like").getValue() != null)
                    {
                        mLikeCount = item.getLikeCount();
                        mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likeCount").setValue(mLikeCount -1);
                        mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid()).removeValue();

                        mListOfItems.get(position).setLikeCount(mLikeCount - 1);

                        // The key is the item id and the value is a boolean determining whether it's liked or not
                        Map<String, Boolean> likedItemMap = new HashMap<>();
                        likedItemMap.put(item.getItemID(), false);
                        mListOfLikedItems.add(likedItemMap);

                        int id = getContext().getResources().getIdentifier("com.liamd.giggity_app:drawable/ic_like_white", null, null);
                        Glide.with(getContext()).load(id).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mLikeButton);

                        notifyDataSetChanged();

                        mPassedThrough = true;
                    }
                }

                // If this isn't the first read (i.e. an item has already been liked) then the map array is referred to, to get the latest state
                else
                {
                    // Determines whether the end of the list has been reached to determine whether or not to u
                    int counter = 0;
                    boolean isLike = true;

                    // Loop through the list of currently liked items to get the map for each
                    for (Map<String, Boolean> map : mListOfLikedItems)
                    {
                        // Then loop through the map and get the key and its value
                        for (Map.Entry<String, Boolean> entry : map.entrySet())
                        {
                            counter++;

                            String key = entry.getKey();
                            Boolean value = entry.getValue();

                            // If the item exists in the list and isn't liked or the key is null (the item hasn't been liked) then like the item
                            if (key.equals(item.getItemID()) && !value || key == null)
                            {
                                mLikeCount = item.getLikeCount();
                                mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likeCount").setValue(mLikeCount + 1);
                                mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid() + "/like").setValue("true");

                                mListOfItems.get(position).setLikeCount(mLikeCount + 1);

                                isLike = true;

                                int id = getContext().getResources().getIdentifier("com.liamd.giggity_app:drawable/ic_like_red", null, null);
                                Glide.with(getContext()).load(id).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mLikeButton);

                                notifyDataSetChanged();

                                break;
                            }

                            // If the counter is at the end it means the item has a value of true and has already been liked
                            else if (counter >= mListOfLikedItems.size())
                            {
                                // Else we can assume it has been liked
                                mLikeCount = item.getLikeCount();
                                mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likeCount").setValue(mLikeCount - 1);
                                mDatabase.child("NewsFeedItems/" + item.getItemID() + "/likes/" + mAuth.getCurrentUser().getUid()).removeValue();

                                mListOfItems.get(position).setLikeCount(mLikeCount - 1);

                                isLike = false;

                                int id = getContext().getResources().getIdentifier("com.liamd.giggity_app:drawable/ic_like_white", null, null);
                                Glide.with(getContext()).load(id).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mLikeButton);

                                notifyDataSetChanged();

                                break;
                            }
                        }
                    }

                    if(isLike)
                    {
                        // The key is the item id and the value is a boolean determining whether it's liked or not
                        Map<String, Boolean> likedItemMap = new HashMap<>();
                        likedItemMap.put(item.getItemID(), true);
                        mListOfLikedItems.add(likedItemMap);
                    }

                    else
                    {
                        // The key is the item id and the value is a boolean determining whether it's liked or not
                        Map<String, Boolean> likedItemMap = new HashMap<>();
                        likedItemMap.put(item.getItemID(), false);
                        mListOfLikedItems.add(likedItemMap);
                    }
                }
            }
        });

        mInitialReadCounter++;

        if(mInitialReadCounter > mListOfItems.size())
        {
            mInitialRead = false;
        }

        return newsFeedListView;
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }
}
