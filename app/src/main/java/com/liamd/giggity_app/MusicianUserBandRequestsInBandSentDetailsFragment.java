package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserBandRequestsInBandSentDetailsFragment extends Fragment
{
    // Declare visual components
    private CircleImageView mUserImageView;
    private TextView mUserNameTextView;
    private TextView mUserInstrumentsTextView;
    private TextView mPositionOfferedTextView;
    private TextView mRequestStatusTextView;
    private Button mCancelRequestButton;

    // Declare Firebase specific variables
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mUserProfileImageReference;

    // Declare general variables
    private String mUserName;
    private String mUserId;
    private String mBandId;
    private String mUserInstruments;
    private String mPositionInstruments;
    private String mRequestStatus;

    public MusicianUserBandRequestsInBandSentDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_requests_in_band_sent_details, container, false);

        // Initialise visual components
        mUserImageView = (CircleImageView) fragmentView.findViewById(R.id.userImageView);
        mUserNameTextView = (TextView) fragmentView.findViewById(R.id.userNameTextView);
        mUserInstrumentsTextView = (TextView) fragmentView.findViewById(R.id.userInstrumentsTextView);
        mPositionOfferedTextView = (TextView) fragmentView.findViewById(R.id.positionOfferedTextView);
        mRequestStatusTextView = (TextView) fragmentView.findViewById(R.id.requestStatusTextView);
        mCancelRequestButton = (Button) fragmentView.findViewById(R.id.cancelRequestButton);

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mUserProfileImageReference = mStorage.getReference();

        // This populates the visual components with the data about the chosen sent request
        PopulateFields();

        mCancelRequestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CancelRequest();
            }
        });

        // Set the fragment title
        getActivity().setTitle("Band Requests");

        return fragmentView;
    }

    // This populates the visual components with data
    private void PopulateFields()
    {
        mUserId = getArguments().getString("UserID");
        mUserName = getArguments().getString("UserName");
        mBandId = getArguments().getString("BandID");
        mUserInstruments = getArguments().getString("UserInstruments");
        mPositionInstruments = getArguments().getString("PositionOffered");

        mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId + "/requestStatus").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mRequestStatus = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mRequestStatus = getArguments().getString("RequestStatus");

        mUserNameTextView.setText(mUserName);
        mUserInstrumentsTextView.setText(mUserInstruments);
        mPositionOfferedTextView.setText(mPositionInstruments);
        mRequestStatusTextView.setText(mRequestStatus);

        // These blocks set the colour of the text depending on the status
        if(mRequestStatusTextView.getText().equals("Pending"))
        {
            mRequestStatusTextView.setTextColor(Color.parseColor("#ff6100"));
        }

        else if(mRequestStatusTextView.getText().equals("Accepted"))
        {
            mRequestStatusTextView.setTextColor(Color.GREEN);
        }

        else if(mRequestStatusTextView.getText().equals("Rejected"))
        {
            mRequestStatusTextView.setTextColor(Color.RED);
        }

        LoadProfilePicture();
    }

    // This method loads the band's profile picture assuming they have one.
    private void LoadProfilePicture()
    {
        // This reference looks at the Firebase storage and works out whether the current user has an image
        mUserProfileImageReference.child("ProfileImages/" + mUserId+ "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            // If the user has an image this is loaded into the image view
            @Override
            public void onSuccess(Uri uri)
            {
                // The caching and memory features have been disabled to allow only the latest image to display
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mUserProfileImageReference.child("ProfileImages/" + mUserId+ "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mUserImageView);
            }
        });
    }

    // This method checks that the user wants to cancel their request and then deletes it from the database
    private void CancelRequest()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cancel Band Request");
        builder.setMessage("Are you sure you wish to withdraw your request for this user to join your band?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(mRequestStatus.equals("Pending"))
                {
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).removeValue();

                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirmation");
                    builder.setMessage("Request Deleted!");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            ReturnToRequests();
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }

                else
                {
                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Error!");
                    builder.setMessage("You cannot change a request that has already been handled!");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                        }
                    });
                    builder.show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

            }
        });
        builder.show();
    }

    private void ReturnToRequests()
    {
        // The user is then taken to the home fragment
        getFragmentManager().popBackStack();
    }

}
