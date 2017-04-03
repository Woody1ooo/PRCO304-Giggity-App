package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserBandMembersFragment extends Fragment
{
    // Declare visual components
    private TextView mPositionOneHeadingTextView;
    private TextView mPositionOneInstrumentTextView;
    private TextView mPositionOneNameTextView;
    private CircleImageView mPositionOneProfileImageView;
    private Button mPositionOneHireButton;
    private Button mPositionOneFireButton;

    private TextView mPositionTwoHeadingTextView;
    private TextView mPositionTwoInstrumentTextView;
    private TextView mPositionTwoNameTextView;
    private CircleImageView mPositionTwoProfileImageView;
    private Button mPositionTwoHireButton;
    private Button mPositionTwoFireButton;

    private TextView mPositionThreeHeadingTextView;
    private TextView mPositionThreeInstrumentTextView;
    private TextView mPositionThreeNameTextView;
    private CircleImageView mPositionThreeProfileImageView;
    private Button mPositionThreeHireButton;
    private Button mPositionThreeFireButton;

    private TextView mPositionFourHeadingTextView;
    private TextView mPositionFourInstrumentTextView;
    private TextView mPositionFourNameTextView;
    private CircleImageView mPositionFourProfileImageView;
    private Button mPositionFourHireButton;
    private Button mPositionFourFireButton;

    private TextView mPositionFiveHeadingTextView;
    private TextView mPositionFiveInstrumentTextView;
    private TextView mPositionFiveNameTextView;
    private CircleImageView mPositionFiveProfileImageView;
    private Button mPositionFiveHireButton;
    private Button mPositionFiveFireButton;

    private ProgressDialog mProgressDialog;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;
    private DataSnapshot mSnapshot;

    // Declare general variables
    private String mCurrentUserId;
    private String mBandId;
    private String mNumberOfPositions;
    private String mButtonClicked;

    public MusicianUserBandMembersFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_members, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // Initialise visual components
        mPositionOneHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionOneHeadingTextView);
        mPositionOneInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionOneInstrumentTextView);
        mPositionOneNameTextView = (TextView) fragmentView.findViewById(R.id.positionOneNameTextView);
        mPositionOneProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionOneProfileImageView);
        mPositionOneHireButton = (Button) fragmentView.findViewById(R.id.hirePositionOneButton);
        mPositionOneFireButton = (Button) fragmentView.findViewById(R.id.firePositionOneButton);

        mPositionTwoHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionTwoHeadingTextView);
        mPositionTwoInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionTwoInstrumentTextView);
        mPositionTwoNameTextView = (TextView) fragmentView.findViewById(R.id.positionTwoNameTextView);
        mPositionTwoProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionTwoProfileImageView);
        mPositionTwoHireButton = (Button) fragmentView.findViewById(R.id.hirePositionTwoButton);
        mPositionTwoFireButton = (Button) fragmentView.findViewById(R.id.firePositionTwoButton);

        mPositionThreeHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionThreeHeadingTextView);
        mPositionThreeInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionThreeInstrumentTextView);
        mPositionThreeNameTextView = (TextView) fragmentView.findViewById(R.id.positionThreeNameTextView);
        mPositionThreeProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionThreeProfileImageView);
        mPositionThreeHireButton = (Button) fragmentView.findViewById(R.id.hirePositionThreeButton);
        mPositionThreeFireButton = (Button) fragmentView.findViewById(R.id.firePositionThreeButton);

        mPositionFourHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionFourHeadingTextView);
        mPositionFourInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionFourInstrumentTextView);
        mPositionFourNameTextView = (TextView) fragmentView.findViewById(R.id.positionFourNameTextView);
        mPositionFourProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionFourProfileImageView);
        mPositionFourHireButton = (Button) fragmentView.findViewById(R.id.hirePositionFourButton);
        mPositionFourFireButton = (Button) fragmentView.findViewById(R.id.firePositionFourButton);

        mPositionFiveHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionFiveHeadingTextView);
        mPositionFiveInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionFiveInstrumentTextView);
        mPositionFiveNameTextView = (TextView) fragmentView.findViewById(R.id.positionFiveNameTextView);
        mPositionFiveProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionFiveProfileImageView);
        mPositionFiveHireButton = (Button) fragmentView.findViewById(R.id.hirePositionFiveButton);
        mPositionFiveFireButton = (Button) fragmentView.findViewById(R.id.firePositionFiveButton);

        // This takes a snapshot from the database and passes it to be used to populate the visual components
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mSnapshot = dataSnapshot;
                PopulateFields(mSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mPositionOneHireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionOne";
                Hire(mButtonClicked);
            }
        });

        mPositionOneFireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionOne";
                Fire(mButtonClicked);
            }
        });

        mPositionTwoHireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionTwo";
                Hire(mButtonClicked);
            }
        });

        mPositionTwoFireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionTwo";
                Fire(mButtonClicked);
            }
        });

        mPositionThreeHireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionThree";
                Hire(mButtonClicked);
            }
        });

        mPositionThreeFireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionThree";
                Fire(mButtonClicked);
            }
        });

        mPositionFourHireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionFour";
                Hire(mButtonClicked);
            }
        });

        mPositionFourFireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionFour";
                Fire(mButtonClicked);
            }
        });

        mPositionFiveHireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionFive";
                Hire(mButtonClicked);
            }
        });

        mPositionFiveFireButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mButtonClicked = "positionFive";
                Fire(mButtonClicked);
            }
        });

        return fragmentView;
    }

    // When called this method populates the fields with data from the database
    private void PopulateFields(DataSnapshot dataSnapshot)
    {
        String mBandUserId;
        String mInstruments;
        String mMemberName;
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mBandId = dataSnapshot.child("Users/" + mCurrentUserId + "/bandID").getValue().toString();
        mNumberOfPositions = dataSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString();

        // This looks at how many positions the band has and displays/hides components accordingly
        // It also populates the fields with data from the database
        if(mNumberOfPositions.equals("1"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);

            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString();
            mPositionOneInstrumentTextView.setText(mInstruments);

            if(dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().equals("Vacant"))
            {
                mPositionOneHireButton.setVisibility(View.VISIBLE);
                mPositionOneFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionOneHireButton.setVisibility(View.GONE);
                mPositionOneFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionOneProfileImageView);
                mPositionOneNameTextView.setText(mMemberName);
            }

            mPositionTwoHeadingTextView.setVisibility(View.GONE);
            mPositionTwoInstrumentTextView.setVisibility(View.GONE);
            mPositionTwoNameTextView.setVisibility(View.GONE);
            mPositionTwoProfileImageView.setVisibility(View.GONE);
            mPositionTwoHireButton.setVisibility(View.GONE);
            mPositionTwoFireButton.setVisibility(View.GONE);

            mPositionThreeHeadingTextView.setVisibility(View.GONE);
            mPositionThreeInstrumentTextView.setVisibility(View.GONE);
            mPositionThreeNameTextView.setVisibility(View.GONE);
            mPositionThreeProfileImageView.setVisibility(View.GONE);
            mPositionThreeHireButton.setVisibility(View.GONE);
            mPositionThreeFireButton.setVisibility(View.GONE);

            mPositionFourHeadingTextView.setVisibility(View.GONE);
            mPositionFourInstrumentTextView.setVisibility(View.GONE);
            mPositionFourNameTextView.setVisibility(View.GONE);
            mPositionFourProfileImageView.setVisibility(View.GONE);
            mPositionFourHireButton.setVisibility(View.GONE);
            mPositionFourFireButton.setVisibility(View.GONE);

            mPositionFiveHeadingTextView.setVisibility(View.GONE);
            mPositionFiveInstrumentTextView.setVisibility(View.GONE);
            mPositionFiveNameTextView.setVisibility(View.GONE);
            mPositionFiveProfileImageView.setVisibility(View.GONE);
            mPositionFiveHireButton.setVisibility(View.GONE);
            mPositionFiveFireButton.setVisibility(View.GONE);
        }

        else if (mNumberOfPositions.equals("2"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString();
            mPositionOneInstrumentTextView.setText(mInstruments);

            if(dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().equals("Vacant"))
            {
                mPositionOneHireButton.setVisibility(View.VISIBLE);
                mPositionOneFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionOneHireButton.setVisibility(View.GONE);
                mPositionOneFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionOneProfileImageView);
                mPositionOneNameTextView.setText(mMemberName);
            }

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionTwo").getValue().toString();
            mPositionTwoInstrumentTextView.setText(mInstruments);

            if(dataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().equals("Vacant"))
            {
                mPositionTwoHireButton.setVisibility(View.VISIBLE);
                mPositionTwoFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionTwoHireButton.setVisibility(View.GONE);
                mPositionTwoFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionTwoProfileImageView);
                mPositionTwoNameTextView.setText(mMemberName);
            }

            mPositionThreeHeadingTextView.setVisibility(View.GONE);
            mPositionThreeInstrumentTextView.setVisibility(View.GONE);
            mPositionThreeNameTextView.setVisibility(View.GONE);
            mPositionThreeProfileImageView.setVisibility(View.GONE);
            mPositionThreeHireButton.setVisibility(View.GONE);
            mPositionThreeFireButton.setVisibility(View.GONE);

            mPositionFourHeadingTextView.setVisibility(View.GONE);
            mPositionFourInstrumentTextView.setVisibility(View.GONE);
            mPositionFourNameTextView.setVisibility(View.GONE);
            mPositionFourProfileImageView.setVisibility(View.GONE);
            mPositionFourHireButton.setVisibility(View.GONE);
            mPositionFourFireButton.setVisibility(View.GONE);

            mPositionFiveHeadingTextView.setVisibility(View.GONE);
            mPositionFiveInstrumentTextView.setVisibility(View.GONE);
            mPositionFiveNameTextView.setVisibility(View.GONE);
            mPositionFiveProfileImageView.setVisibility(View.GONE);
            mPositionFiveHireButton.setVisibility(View.GONE);
            mPositionFiveFireButton.setVisibility(View.GONE);
        }

        else if (mNumberOfPositions.equals("3"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString();
            mPositionOneInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().equals("Vacant"))
            {
                mPositionOneHireButton.setVisibility(View.VISIBLE);
                mPositionOneFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionOneHireButton.setVisibility(View.GONE);
                mPositionOneFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionOneProfileImageView);
                mPositionOneNameTextView.setText(mMemberName);
            }

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionTwo").getValue().toString();
            mPositionTwoInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().equals("Vacant"))
            {
                mPositionTwoHireButton.setVisibility(View.VISIBLE);
                mPositionTwoFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionTwoHireButton.setVisibility(View.GONE);
                mPositionTwoFireButton.setVisibility(View.VISIBLE);

                PopulateImageView(mBandUserId, mPositionTwoProfileImageView);
                mPositionTwoNameTextView.setText(mMemberName);
            }

            mPositionThreeHeadingTextView.setVisibility(View.VISIBLE);
            mPositionThreeInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionThreeNameTextView.setVisibility(View.VISIBLE);
            mPositionThreeProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionThree").getValue().toString();
            mPositionThreeInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().equals("Vacant"))
            {
                mPositionThreeHireButton.setVisibility(View.VISIBLE);
                mPositionThreeFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionThreeHireButton.setVisibility(View.GONE);
                mPositionThreeFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionThreeProfileImageView);
                mPositionThreeNameTextView.setText(mMemberName);
            }

            mPositionFourHeadingTextView.setVisibility(View.GONE);
            mPositionFourInstrumentTextView.setVisibility(View.GONE);
            mPositionFourNameTextView.setVisibility(View.GONE);
            mPositionFourProfileImageView.setVisibility(View.GONE);
            mPositionFourHireButton.setVisibility(View.GONE);
            mPositionFourFireButton.setVisibility(View.GONE);

            mPositionFiveHeadingTextView.setVisibility(View.GONE);
            mPositionFiveInstrumentTextView.setVisibility(View.GONE);
            mPositionFiveNameTextView.setVisibility(View.GONE);
            mPositionFiveProfileImageView.setVisibility(View.GONE);
            mPositionFiveHireButton.setVisibility(View.GONE);
            mPositionFiveFireButton.setVisibility(View.GONE);
        }

        else if (mNumberOfPositions.equals("4"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString();
            mPositionOneInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().equals("Vacant"))
            {
                mPositionOneHireButton.setVisibility(View.VISIBLE);
                mPositionOneFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionOneHireButton.setVisibility(View.GONE);
                mPositionOneFireButton.setVisibility(View.VISIBLE);

                PopulateImageView(mBandUserId, mPositionOneProfileImageView);
                mPositionOneNameTextView.setText(mMemberName);
            }

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionTwo").getValue().toString();
            mPositionTwoInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().equals("Vacant"))
            {
                mPositionTwoHireButton.setVisibility(View.VISIBLE);
                mPositionTwoFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionTwoHireButton.setVisibility(View.GONE);
                mPositionTwoFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionTwoProfileImageView);
                mPositionTwoNameTextView.setText(mMemberName);
            }

            mPositionThreeHeadingTextView.setVisibility(View.VISIBLE);
            mPositionThreeInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionThreeNameTextView.setVisibility(View.VISIBLE);
            mPositionThreeProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionThree").getValue().toString();
            mPositionThreeInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().equals("Vacant"))
            {
                mPositionThreeHireButton.setVisibility(View.VISIBLE);
                mPositionThreeFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionThreeHireButton.setVisibility(View.GONE);
                mPositionThreeFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionThreeProfileImageView);
                mPositionThreeNameTextView.setText(mMemberName);
            }

            mPositionFourHeadingTextView.setVisibility(View.VISIBLE);
            mPositionFourInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionFourNameTextView.setVisibility(View.VISIBLE);
            mPositionFourProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionFour").getValue().toString();
            mPositionFourInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().equals("Vacant"))
            {
                mPositionFourHireButton.setVisibility(View.VISIBLE);
                mPositionFourFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionFourHireButton.setVisibility(View.GONE);
                mPositionFourFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionFourProfileImageView);
                mPositionFourNameTextView.setText(mMemberName);
            }

            mPositionFiveHeadingTextView.setVisibility(View.GONE);
            mPositionFiveInstrumentTextView.setVisibility(View.GONE);
            mPositionFiveNameTextView.setVisibility(View.GONE);
            mPositionFiveProfileImageView.setVisibility(View.GONE);
            mPositionFiveHireButton.setVisibility(View.GONE);
            mPositionFiveFireButton.setVisibility(View.GONE);
        }

        else if (mNumberOfPositions.equals("5"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString();
            mPositionOneInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().equals("Vacant"))
            {
                mPositionOneHireButton.setVisibility(View.VISIBLE);
                mPositionOneFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionOneHireButton.setVisibility(View.GONE);
                mPositionOneFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionOneProfileImageView);
                mPositionOneNameTextView.setText(mMemberName);
            }

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionTwo").getValue().toString();
            mPositionTwoInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().equals("Vacant"))
            {
                mPositionTwoHireButton.setVisibility(View.VISIBLE);
                mPositionTwoFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionTwoHireButton.setVisibility(View.GONE);
                mPositionTwoFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionTwoProfileImageView);
                mPositionTwoNameTextView.setText(mMemberName);
            }

            mPositionThreeHeadingTextView.setVisibility(View.VISIBLE);
            mPositionThreeInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionThreeNameTextView.setVisibility(View.VISIBLE);
            mPositionThreeProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionThree").getValue().toString();
            mPositionThreeInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().equals("Vacant"))
            {
                mPositionThreeHireButton.setVisibility(View.VISIBLE);
                mPositionThreeFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionThreeHireButton.setVisibility(View.GONE);
                mPositionThreeFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionThreeProfileImageView);
                mPositionThreeNameTextView.setText(mMemberName);
            }

            mPositionFourHeadingTextView.setVisibility(View.VISIBLE);
            mPositionFourInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionFourNameTextView.setVisibility(View.VISIBLE);
            mPositionFourProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionFour").getValue().toString();
            mPositionFourInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().equals("Vacant"))
            {
                mPositionFourHireButton.setVisibility(View.VISIBLE);
                mPositionFourFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();
                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionFourHireButton.setVisibility(View.GONE);
                mPositionFourFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionFourProfileImageView);
                mPositionFourNameTextView.setText(mMemberName);
            }

            mPositionFiveHeadingTextView.setVisibility(View.VISIBLE);
            mPositionFiveInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionFiveNameTextView.setVisibility(View.VISIBLE);
            mPositionFiveProfileImageView.setVisibility(View.VISIBLE);
            mInstruments = dataSnapshot.child("Bands/" + mBandId + "/positionFive").getValue().toString();
            mPositionFiveInstrumentTextView.setText(mInstruments);

            if (dataSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().equals("Vacant"))
            {
                mPositionFiveHireButton.setVisibility(View.VISIBLE);
                mPositionFiveFireButton.setVisibility(View.GONE);
            }

            else
            {
                mBandUserId = dataSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString();

                mMemberName = dataSnapshot.child("Users/" + mBandUserId + "/firstName").getValue().toString()
                        + " " + dataSnapshot.child("Users/" + mBandUserId + "/lastName").getValue().toString();
                mPositionFiveHireButton.setVisibility(View.GONE);
                mPositionFiveFireButton.setVisibility(View.VISIBLE);
                PopulateImageView(mBandUserId, mPositionFiveProfileImageView);
                mPositionFiveNameTextView.setText(mMemberName);
            }
        }

        mProgressDialog.hide();
    }

    // This method takes the relevant user id and image view and populates it with each user's profile image
    private void PopulateImageView(final String userId, final CircleImageView imageView)
    {
        // This reference looks at the Firebase storage and works out whether the current user has an image
        mProfileImageReference.child("ProfileImages/" + userId + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            // If the user has an image this is loaded into the image view
            @Override
            public void onSuccess(Uri uri)
            {
                // The caching and memory features have been disabled to allow only the latest image to display
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mProfileImageReference.child("ProfileImages/" + userId + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(imageView);
            }

            // If the user doesn't have an image the default image is loaded
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(350, 350).into(imageView);
            }
        });
    }

    private void Hire(String positionSelected)
    {

    }

    // Upon confirmation this replaces the position removed with Vacant, sets the users isInBand value to false and removes the bandID field from their profile
    // Once this is complete the fragment is refreshed
    private void Fire(final String positionSelected)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update Band Members");
        builder.setMessage("Are you sure you wish to fire this band member?");
        builder.setPositiveButton("Fire", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String userIdToRemove = mSnapshot.child("Bands/" + mBandId + "/" + positionSelected + "Member").getValue().toString();
                mDatabase.child("Bands/" + mBandId + "/" + positionSelected + "Member").setValue("Vacant");
                mDatabase.child("Users/" + userIdToRemove + "/isInBand").setValue(false);
                mDatabase.child("Users/" + userIdToRemove + "/bandID").removeValue();

                // A dialog is then shown to alert the user that the changes have been made
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setMessage("Member Fired!");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        RefreshFragment();
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

    private void RefreshFragment()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }
}
