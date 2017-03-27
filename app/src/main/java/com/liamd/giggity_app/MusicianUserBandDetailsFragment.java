package com.liamd.giggity_app;


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
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
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
public class MusicianUserBandDetailsFragment extends Fragment
{
    // Declare visual components
    private ImageView mBandImageView;
    private TextView mBandNameTextView;
    private TextView mBandGenresTextView;
    private TextView mNumberOfPositionsTextView;
    private TextView mPositionOneHeadingTextView;
    private TextView mPositionOneInstrumentTextView;
    private TextView mPositionOneNameTextView;
    private CircleImageView mPositionOneProfileImageView;
    private Button mApplyForPositionOneButton;
    private TextView mPositionTwoHeadingTextView;
    private TextView mPositionTwoInstrumentTextView;
    private TextView mPositionTwoNameTextView;
    private CircleImageView mPositionTwoProfileImageView;
    private Button mApplyForPositionTwoButton;
    private TextView mPositionThreeHeadingTextView;
    private TextView mPositionThreeInstrumentTextView;
    private TextView mPositionThreeNameTextView;
    private CircleImageView mPositionThreeProfileImageView;
    private Button mApplyForPositionThreeButton;
    private TextView mPositionFourHeadingTextView;
    private TextView mPositionFourInstrumentTextView;
    private TextView mPositionFourNameTextView;
    private CircleImageView mPositionFourProfileImageView;
    private Button mApplyForPositionFourButton;
    private TextView mPositionFiveHeadingTextView;
    private TextView mPositionFiveInstrumentTextView;
    private TextView mPositionFiveNameTextView;
    private CircleImageView mPositionFiveProfileImageView;
    private Button mApplyForPositionFiveButton;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mBandProfileImageReference;

    // Declare general variables
    private String mBandId;
    private String mNumberOfPositions;
    private DataSnapshot mDataSnapshot;

    public MusicianUserBandDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_details, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mBandProfileImageReference = mStorage.getReference();

        // Initialise visual components
        mBandImageView = (ImageView) fragmentView.findViewById(R.id.bandImageView);

        mBandNameTextView = (TextView) fragmentView.findViewById(R.id.bandNameTextView);
        mBandGenresTextView = (TextView) fragmentView.findViewById(R.id.bandGenresTextView);
        mNumberOfPositionsTextView = (TextView) fragmentView.findViewById(R.id.numberOfPositionsTextView);

        mPositionOneHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionOneHeadingTextView);
        mPositionOneInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionOneInstrumentTextView);
        mPositionOneNameTextView = (TextView) fragmentView.findViewById(R.id.positionOneNameTextView);
        mPositionOneProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionOneProfileImageView);
        mApplyForPositionOneButton = (Button) fragmentView.findViewById(R.id.applyForPositionOneButton);

        mPositionTwoHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionTwoHeadingTextView);
        mPositionTwoInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionTwoInstrumentTextView);
        mPositionTwoNameTextView = (TextView) fragmentView.findViewById(R.id.positionTwoNameTextView);
        mPositionTwoProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionTwoProfileImageView);
        mApplyForPositionTwoButton = (Button) fragmentView.findViewById(R.id.applyForPositionTwoButton);

        mPositionThreeHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionThreeHeadingTextView);
        mPositionThreeInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionThreeInstrumentTextView);
        mPositionThreeNameTextView = (TextView) fragmentView.findViewById(R.id.positionThreeNameTextView);
        mPositionThreeProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionThreeProfileImageView);
        mApplyForPositionThreeButton = (Button) fragmentView.findViewById(R.id.applyForPositionThreeButton);

        mPositionFourHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionFourHeadingTextView);
        mPositionFourInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionFourInstrumentTextView);
        mPositionFourNameTextView = (TextView) fragmentView.findViewById(R.id.positionFourNameTextView);
        mPositionFourProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionFourProfileImageView);
        mApplyForPositionFourButton = (Button) fragmentView.findViewById(R.id.applyForPositionFourButton);

        mPositionFiveHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionFiveHeadingTextView);
        mPositionFiveInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionFiveInstrumentTextView);
        mPositionFiveNameTextView = (TextView) fragmentView.findViewById(R.id.positionFiveNameTextView);
        mPositionFiveProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionFiveProfileImageView);
        mApplyForPositionFiveButton = (Button) fragmentView.findViewById(R.id.applyForPositionFiveButton);

        // Retrieve the variables passed from the previous fragment
        mBandId = getArguments().getString("BandID");
        mBandNameTextView.setText(getArguments().getString("BandName"));
        mBandGenresTextView.setText(getArguments().getString("BandGenres"));
        mNumberOfPositionsTextView.setText(getArguments().getString("BandNumberOfPositions"));
        mNumberOfPositions = getArguments().getString("BandNumberOfPositions");

        if (mNumberOfPositionsTextView.getText().equals("1"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionOneButton.setVisibility(View.VISIBLE);

            mPositionTwoHeadingTextView.setVisibility(View.GONE);
            mPositionTwoInstrumentTextView.setVisibility(View.GONE);
            mPositionTwoNameTextView.setVisibility(View.GONE);
            mPositionTwoProfileImageView.setVisibility(View.GONE);
            mApplyForPositionTwoButton.setVisibility(View.GONE);

            mPositionThreeHeadingTextView.setVisibility(View.GONE);
            mPositionThreeInstrumentTextView.setVisibility(View.GONE);
            mPositionThreeNameTextView.setVisibility(View.GONE);
            mPositionThreeProfileImageView.setVisibility(View.GONE);
            mApplyForPositionThreeButton.setVisibility(View.GONE);

            mPositionFourHeadingTextView.setVisibility(View.GONE);
            mPositionFourInstrumentTextView.setVisibility(View.GONE);
            mPositionFourNameTextView.setVisibility(View.GONE);
            mPositionFourProfileImageView.setVisibility(View.GONE);
            mApplyForPositionFourButton.setVisibility(View.GONE);

            mPositionFiveHeadingTextView.setVisibility(View.GONE);
            mPositionFiveInstrumentTextView.setVisibility(View.GONE);
            mPositionFiveNameTextView.setVisibility(View.GONE);
            mPositionFiveProfileImageView.setVisibility(View.GONE);
            mApplyForPositionFiveButton.setVisibility(View.GONE);
        }

        else if (mNumberOfPositionsTextView.getText().equals("2"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionOneButton.setVisibility(View.VISIBLE);

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionTwoButton.setVisibility(View.VISIBLE);

            mPositionThreeHeadingTextView.setVisibility(View.GONE);
            mPositionThreeInstrumentTextView.setVisibility(View.GONE);
            mPositionThreeNameTextView.setVisibility(View.GONE);
            mPositionThreeProfileImageView.setVisibility(View.GONE);
            mApplyForPositionThreeButton.setVisibility(View.GONE);

            mPositionFourHeadingTextView.setVisibility(View.GONE);
            mPositionFourInstrumentTextView.setVisibility(View.GONE);
            mPositionFourNameTextView.setVisibility(View.GONE);
            mPositionFourProfileImageView.setVisibility(View.GONE);
            mApplyForPositionFourButton.setVisibility(View.GONE);

            mPositionFiveHeadingTextView.setVisibility(View.GONE);
            mPositionFiveInstrumentTextView.setVisibility(View.GONE);
            mPositionFiveNameTextView.setVisibility(View.GONE);
            mPositionFiveProfileImageView.setVisibility(View.GONE);
            mApplyForPositionFiveButton.setVisibility(View.GONE);
        }

        else if (mNumberOfPositionsTextView.getText().equals("3"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionOneButton.setVisibility(View.VISIBLE);

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionTwoButton.setVisibility(View.VISIBLE);

            mPositionThreeHeadingTextView.setVisibility(View.VISIBLE);
            mPositionThreeInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionThreeNameTextView.setVisibility(View.VISIBLE);
            mPositionThreeProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionThreeButton.setVisibility(View.VISIBLE);

            mPositionFourHeadingTextView.setVisibility(View.GONE);
            mPositionFourInstrumentTextView.setVisibility(View.GONE);
            mPositionFourNameTextView.setVisibility(View.GONE);
            mPositionFourProfileImageView.setVisibility(View.GONE);
            mApplyForPositionFourButton.setVisibility(View.GONE);

            mPositionFiveHeadingTextView.setVisibility(View.GONE);
            mPositionFiveInstrumentTextView.setVisibility(View.GONE);
            mPositionFiveNameTextView.setVisibility(View.GONE);
            mPositionFiveProfileImageView.setVisibility(View.GONE);
            mApplyForPositionFiveButton.setVisibility(View.GONE);
        }

        else if (mNumberOfPositionsTextView.getText().equals("4"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionOneButton.setVisibility(View.VISIBLE);

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionTwoButton.setVisibility(View.VISIBLE);

            mPositionThreeHeadingTextView.setVisibility(View.VISIBLE);
            mPositionThreeInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionThreeNameTextView.setVisibility(View.VISIBLE);
            mPositionThreeProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionThreeButton.setVisibility(View.VISIBLE);

            mPositionFourHeadingTextView.setVisibility(View.VISIBLE);
            mPositionFourInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionFourNameTextView.setVisibility(View.VISIBLE);
            mPositionFourProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionFourButton.setVisibility(View.VISIBLE);

            mPositionFiveHeadingTextView.setVisibility(View.GONE);
            mPositionFiveInstrumentTextView.setVisibility(View.GONE);
            mPositionFiveNameTextView.setVisibility(View.GONE);
            mPositionFiveProfileImageView.setVisibility(View.GONE);
            mApplyForPositionFiveButton.setVisibility(View.GONE);
        }

        else if (mNumberOfPositionsTextView.getText().equals("5"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionOneButton.setVisibility(View.VISIBLE);

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionTwoButton.setVisibility(View.VISIBLE);

            mPositionThreeHeadingTextView.setVisibility(View.VISIBLE);
            mPositionThreeInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionThreeNameTextView.setVisibility(View.VISIBLE);
            mPositionThreeProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionThreeButton.setVisibility(View.VISIBLE);

            mPositionFourHeadingTextView.setVisibility(View.VISIBLE);
            mPositionFourInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionFourNameTextView.setVisibility(View.VISIBLE);
            mPositionFourProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionFourButton.setVisibility(View.VISIBLE);

            mPositionFiveHeadingTextView.setVisibility(View.VISIBLE);
            mPositionFiveInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionFiveNameTextView.setVisibility(View.VISIBLE);
            mPositionFiveProfileImageView.setVisibility(View.VISIBLE);
            mApplyForPositionFiveButton.setVisibility(View.VISIBLE);
        }


        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mDataSnapshot = dataSnapshot;
                PopulateFields();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        return fragmentView;
    }

    private void PopulateFields()
    {
        // This populates the bands profile image view from firebase storage
        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                (mBandProfileImageReference.child("BandProfileImages/" + mBandId + "/profileImage"))
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mBandImageView);

        if(mNumberOfPositions.equals("1"))
        {
            String positionOneUserID;
            String positionOneName;

            positionOneUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

            if(!positionOneUserID.equals("Vacant"))
            {
                positionOneName = mDataSnapshot.child("Users/" + positionOneUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionOneUserID + "/lastName").getValue().toString();

                mPositionOneNameTextView.setText(positionOneName);
                mApplyForPositionOneButton.setEnabled(false);
                mApplyForPositionOneButton.setText("Position Taken");
                mApplyForPositionOneButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionOneUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionOneProfileImageView);
            }

            mPositionOneInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString());
        }

        else if(mNumberOfPositions.equals("2"))
        {
            String positionOneUserID;
            String positionOneName;

            String positionTwoUserID;
            String positionTwoName;

            positionOneUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

            if(!positionOneUserID.equals("Vacant"))
            {
                positionOneName = mDataSnapshot.child("Users/" + positionOneUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionOneUserID + "/lastName").getValue().toString();

                mPositionOneNameTextView.setText(positionOneName);
                mApplyForPositionOneButton.setEnabled(false);
                mApplyForPositionOneButton.setText("Position Taken");
                mApplyForPositionOneButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionOneUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionOneProfileImageView);
            }

            mPositionOneInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString());

            positionTwoUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

            if(!positionTwoUserID.equals("Vacant"))
            {
                positionTwoName = mDataSnapshot.child("Users/" + positionTwoUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionTwoUserID + "/lastName").getValue().toString();

                mPositionTwoNameTextView.setText(positionTwoName);
                mApplyForPositionTwoButton.setEnabled(false);
                mApplyForPositionTwoButton.setText("Position Taken");
                mApplyForPositionTwoButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionTwoUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionTwoProfileImageView);
            }

            mPositionTwoInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionTwo").getValue().toString());
        }

        else if(mNumberOfPositions.equals("3"))
        {
            String positionOneUserID;
            String positionOneName;

            String positionTwoUserID;
            String positionTwoName;

            String positionThreeUserID;
            String positionThreeName;

            positionOneUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

            if(!positionOneUserID.equals("Vacant"))
            {
                positionOneName = mDataSnapshot.child("Users/" + positionOneUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionOneUserID + "/lastName").getValue().toString();

                mPositionOneNameTextView.setText(positionOneName);
                mApplyForPositionOneButton.setEnabled(false);
                mApplyForPositionOneButton.setText("Position Taken");
                mApplyForPositionOneButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));

                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionOneUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionOneProfileImageView);
            }

            mPositionOneInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString());

            positionTwoUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

            if(!positionTwoUserID.equals("Vacant"))
            {
                positionTwoName = mDataSnapshot.child("Users/" + positionTwoUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionTwoUserID + "/lastName").getValue().toString();

                mPositionTwoNameTextView.setText(positionTwoName);
                mApplyForPositionTwoButton.setEnabled(false);
                mApplyForPositionTwoButton.setText("Position Taken");
                mApplyForPositionTwoButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));

                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionOneUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionTwoProfileImageView);
            }

            mPositionTwoInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionTwo").getValue().toString());

            positionThreeUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

            if(!positionThreeUserID.equals("Vacant"))
            {
                positionThreeName = mDataSnapshot.child("Users/" + positionThreeUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionThreeUserID + "/lastName").getValue().toString();

                mPositionThreeNameTextView.setText(positionThreeName);
                mApplyForPositionThreeButton.setEnabled(false);
                mApplyForPositionThreeButton.setText("Position Taken");
                mApplyForPositionThreeButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionThreeUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionThreeProfileImageView);
            }

            mPositionThreeInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionThree").getValue().toString());
        }

        else if(mNumberOfPositions.equals("4"))
        {
            String positionOneUserID;
            String positionOneName;

            String positionTwoUserID;
            String positionTwoName;

            String positionThreeUserID;
            String positionThreeName;

            String positionFourUserID;
            String positionFourName;

            positionOneUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

            if(!positionOneUserID.equals("Vacant"))
            {
                positionOneName = mDataSnapshot.child("Users/" + positionOneUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionOneUserID + "/lastName").getValue().toString();

                mPositionOneNameTextView.setText(positionOneName);
                mApplyForPositionOneButton.setEnabled(false);
                mApplyForPositionOneButton.setText("Position Taken");
                mApplyForPositionOneButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));

                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionOneUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionOneProfileImageView);
            }

            mPositionOneInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString());

            positionTwoUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

            if(!positionTwoUserID.equals("Vacant"))
            {
                positionTwoName = mDataSnapshot.child("Users/" + positionTwoUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionTwoUserID + "/lastName").getValue().toString();

                mPositionTwoNameTextView.setText(positionTwoName);
                mApplyForPositionTwoButton.setEnabled(false);
                mApplyForPositionTwoButton.setText("Position Taken");
                mApplyForPositionTwoButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));

                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionOneUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionTwoProfileImageView);
            }

            mPositionTwoInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionTwo").getValue().toString());

            positionThreeUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

            if(!positionThreeUserID.equals("Vacant"))
            {
                positionThreeName = mDataSnapshot.child("Users/" + positionThreeUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionThreeUserID + "/lastName").getValue().toString();

                mPositionThreeNameTextView.setText(positionThreeName);
                mApplyForPositionThreeButton.setEnabled(false);
                mApplyForPositionThreeButton.setText("Position Taken");
                mApplyForPositionThreeButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionThreeUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionThreeProfileImageView);
            }

            mPositionThreeInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionThree").getValue().toString());

            positionFourUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

            if(!positionFourUserID.equals("Vacant"))
            {
                positionFourName = mDataSnapshot.child("Users/" + positionFourUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionFourUserID + "/lastName").getValue().toString();

                mPositionFourNameTextView.setText(positionFourName);
                mApplyForPositionFourButton.setEnabled(false);
                mApplyForPositionFourButton.setText("Position Taken");
                mApplyForPositionFourButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionFourUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionFourProfileImageView);
            }

            mPositionFourInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionFour").getValue().toString());
        }

        else if(mNumberOfPositions.equals("5"))
        {
            String positionOneUserID;
            String positionOneName;

            String positionTwoUserID;
            String positionTwoName;

            String positionThreeUserID;
            String positionThreeName;

            String positionFourUserID;
            String positionFourName;

            String positionFiveUserID;
            String positionFiveName;

            positionOneUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

            if(!positionOneUserID.equals("Vacant"))
            {
                positionOneName = mDataSnapshot.child("Users/" + positionOneUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionOneUserID + "/lastName").getValue().toString();

                mPositionOneNameTextView.setText(positionOneName);
                mApplyForPositionOneButton.setEnabled(false);
                mApplyForPositionOneButton.setText("Position Taken");
                mApplyForPositionOneButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));

                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionOneUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionOneProfileImageView);
            }

            mPositionOneInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionOne").getValue().toString());

            positionTwoUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

            if(!positionTwoUserID.equals("Vacant"))
            {
                positionTwoName = mDataSnapshot.child("Users/" + positionTwoUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionTwoUserID + "/lastName").getValue().toString();

                mPositionTwoNameTextView.setText(positionTwoName);
                mApplyForPositionTwoButton.setEnabled(false);
                mApplyForPositionTwoButton.setText("Position Taken");
                mApplyForPositionTwoButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));

                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionOneUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionTwoProfileImageView);
            }

            mPositionTwoInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionTwo").getValue().toString());

            positionThreeUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

            if(!positionThreeUserID.equals("Vacant"))
            {
                positionThreeName = mDataSnapshot.child("Users/" + positionThreeUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionThreeUserID + "/lastName").getValue().toString();

                mPositionThreeNameTextView.setText(positionThreeName);
                mApplyForPositionThreeButton.setEnabled(false);
                mApplyForPositionThreeButton.setText("Position Taken");
                mApplyForPositionThreeButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionThreeUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionThreeProfileImageView);
            }

            mPositionThreeInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionThree").getValue().toString());

            positionFourUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

            if(!positionFourUserID.equals("Vacant"))
            {
                positionFourName = mDataSnapshot.child("Users/" + positionFourUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionFourUserID + "/lastName").getValue().toString();

                mPositionFourNameTextView.setText(positionFourName);
                mApplyForPositionFourButton.setEnabled(false);
                mApplyForPositionFourButton.setText("Position Taken");
                mApplyForPositionFourButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionFourUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionFourProfileImageView);
            }


            mPositionFourInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionFour").getValue().toString());

            positionFiveUserID = mDataSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString();

            if(!positionFiveUserID.equals("Vacant"))
            {
                positionFiveName = mDataSnapshot.child("Users/" + positionFiveUserID + "/firstName").getValue().toString() + " " +
                        mDataSnapshot.child("Users/" + positionFiveUserID + "/lastName").getValue().toString();

                mPositionFiveNameTextView.setText(positionFiveName);
                mApplyForPositionFiveButton.setEnabled(false);
                mApplyForPositionFiveButton.setText("Position Taken");
                mApplyForPositionFiveButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabledColor));
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("ProfileImages/" + positionFiveUserID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(mPositionFiveProfileImageView);
            }

            mPositionFiveInstrumentTextView.append(mDataSnapshot.child("Bands/" + mBandId + "/positionFive").getValue().toString());
        }
    }
}