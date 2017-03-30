package com.liamd.giggity_app;


import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    // Declare general variables
    private String mUserId;
    private String mBandId;
    private String mNumberOfPositions;
    private Boolean mPositionOneVacant = false;
    private Boolean mPositionTwoVacant = false;
    private Boolean mPositionThreeVacant = false;
    private Boolean mPositionFourVacant = false;
    private Boolean mPositionFiveVacant = false;

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
        mPositionOneHireButton = (Button) fragmentView.findViewById(R.id.hirePositionTwoButton);
        mPositionOneFireButton = (Button) fragmentView.findViewById(R.id.firePositionTwoButton);

        mPositionThreeHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionThreeHeadingTextView);
        mPositionThreeInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionThreeInstrumentTextView);
        mPositionThreeNameTextView = (TextView) fragmentView.findViewById(R.id.positionThreeNameTextView);
        mPositionThreeProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionThreeProfileImageView);
        mPositionOneHireButton = (Button) fragmentView.findViewById(R.id.hirePositionThreeButton);
        mPositionOneFireButton = (Button) fragmentView.findViewById(R.id.firePositionThreeButton);

        mPositionFourHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionFourHeadingTextView);
        mPositionFourInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionFourInstrumentTextView);
        mPositionFourNameTextView = (TextView) fragmentView.findViewById(R.id.positionFourNameTextView);
        mPositionFourProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionFourProfileImageView);
        mPositionOneHireButton = (Button) fragmentView.findViewById(R.id.hirePositionFourButton);
        mPositionOneFireButton = (Button) fragmentView.findViewById(R.id.firePositionFourButton);

        mPositionFiveHeadingTextView = (TextView) fragmentView.findViewById(R.id.positionFiveHeadingTextView);
        mPositionFiveInstrumentTextView = (TextView) fragmentView.findViewById(R.id.positionFiveInstrumentTextView);
        mPositionFiveNameTextView = (TextView) fragmentView.findViewById(R.id.positionFiveNameTextView);
        mPositionFiveProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.positionFiveProfileImageView);
        mPositionOneHireButton = (Button) fragmentView.findViewById(R.id.hirePositionFiveButton);
        mPositionOneFireButton = (Button) fragmentView.findViewById(R.id.firePositionFiveButton);

        // This takes a snapshot from the database and passes it to be used to populate the visual components
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                PopulateFields(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        return fragmentView;
    }

    // When called this method populates the fields with data from the database
    private void PopulateFields(DataSnapshot dataSnapshot)
    {
        mUserId = mAuth.getCurrentUser().getUid();
        mBandId = dataSnapshot.child("Users/" + mUserId + "/bandID").getValue().toString();
        mNumberOfPositions = dataSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString();

        if(mNumberOfPositions.equals("1"))
        {
            mPositionOneHeadingTextView.setVisibility(View.VISIBLE);
            mPositionOneInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionOneNameTextView.setVisibility(View.VISIBLE);
            mPositionOneProfileImageView.setVisibility(View.VISIBLE);
            mPositionOneHireButton.setVisibility(View.VISIBLE);
            mPositionOneFireButton.setVisibility(View.VISIBLE);

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
            mPositionOneHireButton.setVisibility(View.VISIBLE);
            mPositionOneFireButton.setVisibility(View.VISIBLE);

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mPositionTwoHireButton.setVisibility(View.VISIBLE);
            mPositionTwoFireButton.setVisibility(View.VISIBLE);

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
            mPositionOneHireButton.setVisibility(View.VISIBLE);
            mPositionOneFireButton.setVisibility(View.VISIBLE);

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mPositionTwoHireButton.setVisibility(View.VISIBLE);
            mPositionTwoFireButton.setVisibility(View.VISIBLE);

            mPositionThreeHeadingTextView.setVisibility(View.VISIBLE);
            mPositionThreeInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionThreeNameTextView.setVisibility(View.VISIBLE);
            mPositionThreeProfileImageView.setVisibility(View.VISIBLE);
            mPositionThreeHireButton.setVisibility(View.VISIBLE);
            mPositionThreeFireButton.setVisibility(View.VISIBLE);

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
            mPositionOneHireButton.setVisibility(View.VISIBLE);
            mPositionOneFireButton.setVisibility(View.VISIBLE);

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mPositionTwoHireButton.setVisibility(View.VISIBLE);
            mPositionTwoFireButton.setVisibility(View.VISIBLE);

            mPositionThreeHeadingTextView.setVisibility(View.VISIBLE);
            mPositionThreeInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionThreeNameTextView.setVisibility(View.VISIBLE);
            mPositionThreeProfileImageView.setVisibility(View.VISIBLE);
            mPositionThreeHireButton.setVisibility(View.VISIBLE);
            mPositionThreeFireButton.setVisibility(View.VISIBLE);

            mPositionFourHeadingTextView.setVisibility(View.VISIBLE);
            mPositionFourInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionFourNameTextView.setVisibility(View.VISIBLE);
            mPositionFourProfileImageView.setVisibility(View.VISIBLE);
            mPositionFourHireButton.setVisibility(View.VISIBLE);
            mPositionFourFireButton.setVisibility(View.VISIBLE);

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
            mPositionOneHireButton.setVisibility(View.VISIBLE);
            mPositionOneFireButton.setVisibility(View.VISIBLE);

            mPositionTwoHeadingTextView.setVisibility(View.VISIBLE);
            mPositionTwoInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionTwoNameTextView.setVisibility(View.VISIBLE);
            mPositionTwoProfileImageView.setVisibility(View.VISIBLE);
            mPositionTwoHireButton.setVisibility(View.VISIBLE);
            mPositionTwoFireButton.setVisibility(View.VISIBLE);

            mPositionThreeHeadingTextView.setVisibility(View.VISIBLE);
            mPositionThreeInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionThreeNameTextView.setVisibility(View.VISIBLE);
            mPositionThreeProfileImageView.setVisibility(View.VISIBLE);
            mPositionThreeHireButton.setVisibility(View.VISIBLE);
            mPositionThreeFireButton.setVisibility(View.VISIBLE);

            mPositionFourHeadingTextView.setVisibility(View.VISIBLE);
            mPositionFourInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionFourNameTextView.setVisibility(View.VISIBLE);
            mPositionFourProfileImageView.setVisibility(View.VISIBLE);
            mPositionFourHireButton.setVisibility(View.VISIBLE);
            mPositionFourFireButton.setVisibility(View.VISIBLE);

            mPositionFiveHeadingTextView.setVisibility(View.VISIBLE);
            mPositionFiveInstrumentTextView.setVisibility(View.VISIBLE);
            mPositionFiveNameTextView.setVisibility(View.VISIBLE);
            mPositionFiveProfileImageView.setVisibility(View.VISIBLE);
            mPositionFiveHireButton.setVisibility(View.VISIBLE);
            mPositionFiveFireButton.setVisibility(View.VISIBLE);
        }
    }
}
