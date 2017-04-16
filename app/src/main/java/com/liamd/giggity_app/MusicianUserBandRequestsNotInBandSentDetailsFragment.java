package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserBandRequestsNotInBandSentDetailsFragment extends Fragment
{
    // Declare visual components
    private ImageView mBandImageView;
    private TextView mBandNameTextView;
    private TextView mBandGenresTextView;
    private TextView mPositionAppliedForTextView;
    private TextView mRequestStatusTextView;
    private Button mCancelRequestButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mBandProfileImageReference;

    // Declare general variables
    private String mBandName;
    private String mBandId;
    private String mBandGenres;
    private String mPositionInstruments;
    private String mRequestStatus;

    public MusicianUserBandRequestsNotInBandSentDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_requests_not_in_band_sent_details, container, false);

        // Initialise visual components
        mBandImageView = (ImageView) fragmentView.findViewById(R.id.bandImageView);
        mBandNameTextView = (TextView) fragmentView.findViewById(R.id.bandNameTextView);
        mBandGenresTextView = (TextView) fragmentView.findViewById(R.id.bandGenresTextView);
        mPositionAppliedForTextView = (TextView) fragmentView.findViewById(R.id.positionAppliedForTextView);
        mRequestStatusTextView = (TextView) fragmentView.findViewById(R.id.requestStatusTextView);
        mCancelRequestButton = (Button) fragmentView.findViewById(R.id.cancelRequestButton);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mBandProfileImageReference = mStorage.getReference();

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
        mBandName = getArguments().getString("BandName");
        mBandId = getArguments().getString("BandID");
        mBandGenres = getArguments().getString("BandGenres");
        mPositionInstruments = getArguments().getString("PositionInstruments");
        mRequestStatus = getArguments().getString("RequestStatus");
        mRequestStatusTextView.setText(mRequestStatus);
        mBandNameTextView.setText(mBandName);
        mBandGenresTextView.setText(mBandGenres);
        mPositionAppliedForTextView.setText(mPositionInstruments);

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
        // This reference looks at the Firebase storage and works out whether the current band has an image
        mBandProfileImageReference.child("BandProfileImages/" + mBandId + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            // If the band has an image this is loaded into the image view
            @Override
            public void onSuccess(Uri uri)
            {
                // The caching and memory features have been disabled to allow only the latest image to display
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("BandProfileImages/" + mBandId + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mBandImageView);
            }
        });
    }

    // This method checks that the user wants to cancel their request and then deletes it from the database
    private void CancelRequest()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cancel Band Join Request");
        builder.setMessage("Are you sure you wish to cancel your request to join this band?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                mDatabase.child("MusicianSentBandRequests/" + mAuth.getCurrentUser().getUid() + "/" + mBandId).removeValue();

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
