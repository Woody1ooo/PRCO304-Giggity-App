package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserBandManagementFragment extends Fragment implements YouTubePlayer.OnInitializedListener
{
    // Declare general visual components
    private CircleImageView mBandImageView;
    private ImageButton mChangeImageButton;
    private ImageButton mRemoveImageButton;
    private EditText mBandNameEditText;
    private MultiSelectSpinner mGenreSpinner;
    private Spinner mPositionsSpinner;
    private TextView mLocationChosenTextView;
    private Button mLaunchLocationFinderButton;
    private TextView mHelpTextView;
    private Button checkUrlButton;
    private Button mUpdateButton;
    private Button mDeleteButton;
    private ProgressDialog mProgressDialog;
    private TextView mPositionOneTitle;
    private MultiSelectSpinner mPositionOneSpinner;
    private TextView mPositionTwoTitle;
    private MultiSelectSpinner mPositionTwoSpinner;
    private TextView mPositionThreeTitle;
    private MultiSelectSpinner mPositionThreeSpinner;
    private TextView mPositionFourTitle;
    private MultiSelectSpinner mPositionFourSpinner;
    private TextView mPositionFiveTitle;
    private MultiSelectSpinner mPositionFiveSpinner;
    private EditText youtubeUrlEditText;
    private TextView mYoutubeHelpTextView;
    private String mPositionOneUser = "Vacant";
    private String mPositionTwoUser = "Vacant";
    private String mPositionThreeUser = "Vacant";
    private String mPositionFourUser = "Vacant";
    private String mPositionFiveUser = "Vacant";

    // Declare general variables
    private List<String> mGenreList;
    private List<String> mInstrumentList;
    private Band mBandFromDatabase;
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private int PLACE_PICKER_REQUEST = 0;
    private static int RESULT_LOAD_IMAGE = 1;
    private String mBandID;
    private String mBandName;
    private String mGenres;
    private String mNumberOfPositions;
    private LatLng mBandLocationLatLng;
    private String mPositionOneValue;
    private String mPositionTwoValue;
    private String mPositionThreeValue;
    private String mPositionFourValue;
    private String mPositionFiveValue;
    private String youtubeUrlEntered;
    private String parsedYouTubeURL;
    private int mExistingSelectedPosition;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mBandProfileImageReference;
    private UploadTask mUploadTask;

    public MusicianUserBandManagementFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_management, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mBandImageView = (CircleImageView) fragmentView.findViewById(R.id.bandImageView);
        mChangeImageButton = (ImageButton) fragmentView.findViewById(R.id.changeImageButton);
        mRemoveImageButton= (ImageButton) fragmentView.findViewById(R.id.removeImageButton);
        mBandNameEditText = (EditText) fragmentView.findViewById(R.id.bandNameEditText);
        mGenreSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);
        mPositionsSpinner = (Spinner) fragmentView.findViewById(R.id.bandPositionSpinner);
        mLocationChosenTextView = (TextView) fragmentView.findViewById(R.id.bandLocationDetailsTextView);
        mLaunchLocationFinderButton = (Button) fragmentView.findViewById(R.id.placeFinderButton);
        mHelpTextView = (TextView) fragmentView.findViewById(R.id.locationHelpTextView);
        checkUrlButton = (Button) fragmentView.findViewById(R.id.checkUrlButton);
        mUpdateButton = (Button) fragmentView.findViewById(R.id.updateButton);
        mDeleteButton = (Button) fragmentView.findViewById(R.id.deleteButton);
        mProgressDialog = new ProgressDialog(getActivity());
        mPositionOneTitle = (TextView) fragmentView.findViewById(R.id.positionOneTextView);
        mPositionOneSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionOneSpinner);
        mPositionTwoTitle = (TextView) fragmentView.findViewById(R.id.positionTwoTextView);
        mPositionTwoSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionTwoSpinner);
        mPositionThreeTitle = (TextView) fragmentView.findViewById(R.id.positionThreeTextView);
        mPositionThreeSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionThreeSpinner);
        mPositionFourTitle = (TextView) fragmentView.findViewById(R.id.positionFourTextView);
        mPositionFourSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionFourSpinner);
        mPositionFiveTitle = (TextView) fragmentView.findViewById(R.id.positionFiveTextView);
        mPositionFiveSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.bandPositionFiveSpinner);
        youtubeUrlEditText = (EditText) fragmentView.findViewById(R.id.youtubeUrlEditText);
        mYoutubeHelpTextView = (TextView) fragmentView.findViewById(R.id.youtubeHelpTextView);

        mProgressDialog.show();
        mProgressDialog.setMessage("Loading...");

        // Initially hide all the position spinners/text views until the number chosen is selected from the spinner
        mPositionOneTitle.setVisibility(View.GONE);
        mPositionOneSpinner.setVisibility(View.GONE);
        mPositionTwoTitle.setVisibility(View.GONE);
        mPositionTwoSpinner.setVisibility(View.GONE);
        mPositionThreeTitle.setVisibility(View.GONE);
        mPositionThreeSpinner.setVisibility(View.GONE);
        mPositionFourTitle.setVisibility(View.GONE);
        mPositionFourSpinner.setVisibility(View.GONE);
        mPositionFiveTitle.setVisibility(View.GONE);
        mPositionFiveSpinner.setVisibility(View.GONE);

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mBandProfileImageReference = mStorage.getReference();

        // Add items to the genre list, and set the spinner to use these
        mGenreList = new ArrayList<>();
        mGenreList.add("Acoustic");
        mGenreList.add("Alternative Rock");
        mGenreList.add("Blues");
        mGenreList.add("Classic Rock");
        mGenreList.add("Classical");
        mGenreList.add("Country");
        mGenreList.add("Death Metal");
        mGenreList.add("Disco");
        mGenreList.add("Electronic");
        mGenreList.add("Folk");
        mGenreList.add("Funk");
        mGenreList.add("Garage");
        mGenreList.add("Grunge");
        mGenreList.add("Hip-Hop");
        mGenreList.add("House");
        mGenreList.add("Indie");
        mGenreList.add("Jazz");
        mGenreList.add("Metal");
        mGenreList.add("Pop");
        mGenreList.add("Psychedelic Rock");
        mGenreList.add("Punk");
        mGenreList.add("Rap");
        mGenreList.add("Reggae");
        mGenreList.add("R&B");
        mGenreList.add("Ska");
        mGenreList.add("Techno");
        mGenreList.add("Thrash Metal");

        mGenreSpinner.setItems(mGenreList);

        // Add items to the instrument list, and set the spinner to use these
        mInstrumentList = new ArrayList<>();
        mInstrumentList.add("Acoustic Guitar");
        mInstrumentList.add("Backing Vocals");
        mInstrumentList.add("Banjo");
        mInstrumentList.add("Bass Guitar");
        mInstrumentList.add("Cajon");
        mInstrumentList.add("Cello");
        mInstrumentList.add("Clarinet");
        mInstrumentList.add("Classical Guitar");
        mInstrumentList.add("DJ");
        mInstrumentList.add("Drums");
        mInstrumentList.add("Flute");
        mInstrumentList.add("Keyboards");
        mInstrumentList.add("Lead Guitar");
        mInstrumentList.add("Lead Vocals");
        mInstrumentList.add("Piano");
        mInstrumentList.add("Rhythm Guitar");
        mInstrumentList.add("Saxophone");
        mInstrumentList.add("Synthesiser");
        mInstrumentList.add("Trumpet");
        mInstrumentList.add("Violin");

        mPositionOneSpinner.setItems(mInstrumentList);
        mPositionTwoSpinner.setItems(mInstrumentList);
        mPositionThreeSpinner.setItems(mInstrumentList);
        mPositionFourSpinner.setItems(mInstrumentList);
        mPositionFiveSpinner.setItems(mInstrumentList);

        /// Get the position selected before the value is changed
        mExistingSelectedPosition = mPositionsSpinner.getSelectedItemPosition();

        // This gets the number from the band positions spinner and then displays/hides the relevant components as needed
        mPositionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                if(mPositionsSpinner.getItemAtPosition(position).equals("0"))
                {
                    if(mPositionOneUser.equals("Vacant")
                        && mPositionTwoUser.equals("Vacant")
                        && mPositionThreeUser.equals("Vacant")
                        && mPositionFourUser.equals("Vacant")
                        && mPositionFiveUser.equals("Vacant"))
                    {
                        mPositionOneTitle.setVisibility(View.GONE);
                        mPositionOneSpinner.setVisibility(View.GONE);
                        mPositionTwoTitle.setVisibility(View.GONE);
                        mPositionTwoSpinner.setVisibility(View.GONE);
                        mPositionThreeTitle.setVisibility(View.GONE);
                        mPositionThreeSpinner.setVisibility(View.GONE);
                        mPositionFourTitle.setVisibility(View.GONE);
                        mPositionFourSpinner.setVisibility(View.GONE);
                        mPositionFiveTitle.setVisibility(View.GONE);
                        mPositionFiveSpinner.setVisibility(View.GONE);

                        // Get the position selected before the value is changed
                        mExistingSelectedPosition = mPositionsSpinner.getSelectedItemPosition();
                    }

                    else
                    {
                        Toast.makeText(getActivity(), "You cannot remove band positions unless they are vacant!", Toast.LENGTH_SHORT).show();
                        mPositionsSpinner.setSelection(mExistingSelectedPosition);
                    }
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("1"))
                {
                    if(mPositionTwoUser.equals("Vacant")
                        && mPositionThreeUser.equals("Vacant")
                        && mPositionFourUser.equals("Vacant")
                        && mPositionFiveUser.equals("Vacant"))
                    {
                        // Display the relevant visual components
                        mPositionOneTitle.setVisibility(View.VISIBLE);
                        mPositionOneSpinner.setVisibility(View.VISIBLE);

                        // Hide the others
                        mPositionTwoTitle.setVisibility(View.GONE);
                        mPositionTwoSpinner.setVisibility(View.GONE);
                        mPositionThreeTitle.setVisibility(View.GONE);
                        mPositionThreeSpinner.setVisibility(View.GONE);
                        mPositionFourTitle.setVisibility(View.GONE);
                        mPositionFourSpinner.setVisibility(View.GONE);
                        mPositionFiveTitle.setVisibility(View.GONE);
                        mPositionFiveSpinner.setVisibility(View.GONE);

                        // Get the position selected before the value is changed
                        mExistingSelectedPosition = mPositionsSpinner.getSelectedItemPosition();
                    }

                    else
                    {
                        Toast.makeText(getActivity(), "You cannot remove band positions unless they are vacant!", Toast.LENGTH_SHORT).show();
                        mPositionsSpinner.setSelection(mExistingSelectedPosition);
                    }
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("2"))
                {
                    if(mPositionThreeUser.equals("Vacant")
                        && mPositionFourUser.equals("Vacant")
                        && mPositionFiveUser.equals("Vacant"))
                    {
                        // Display the relevant visual components
                        mPositionOneTitle.setVisibility(View.VISIBLE);
                        mPositionOneSpinner.setVisibility(View.VISIBLE);
                        mPositionTwoTitle.setVisibility(View.VISIBLE);
                        mPositionTwoSpinner.setVisibility(View.VISIBLE);

                        // Hide the others
                        mPositionThreeTitle.setVisibility(View.GONE);
                        mPositionThreeSpinner.setVisibility(View.GONE);
                        mPositionFourTitle.setVisibility(View.GONE);
                        mPositionFourSpinner.setVisibility(View.GONE);
                        mPositionFiveTitle.setVisibility(View.GONE);
                        mPositionFiveSpinner.setVisibility(View.GONE);

                        // Get the position selected before the value is changed
                        mExistingSelectedPosition = mPositionsSpinner.getSelectedItemPosition();
                    }

                    else
                    {
                        Toast.makeText(getActivity(), "You cannot remove band positions unless they are vacant!", Toast.LENGTH_SHORT).show();
                        mPositionsSpinner.setSelection(mExistingSelectedPosition);
                    }
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("3"))
                {
                    if(mPositionFourUser.equals("Vacant")
                        && mPositionFiveUser.equals("Vacant"))
                    {
                        // Display the relevant visual components
                        mPositionOneTitle.setVisibility(View.VISIBLE);
                        mPositionOneSpinner.setVisibility(View.VISIBLE);
                        mPositionTwoTitle.setVisibility(View.VISIBLE);
                        mPositionTwoSpinner.setVisibility(View.VISIBLE);
                        mPositionThreeTitle.setVisibility(View.VISIBLE);
                        mPositionThreeSpinner.setVisibility(View.VISIBLE);

                        // Hide the others
                        mPositionFourTitle.setVisibility(View.GONE);
                        mPositionFourSpinner.setVisibility(View.GONE);
                        mPositionFiveTitle.setVisibility(View.GONE);
                        mPositionFiveSpinner.setVisibility(View.GONE);

                        // Get the position selected before the value is changed
                        mExistingSelectedPosition = mPositionsSpinner.getSelectedItemPosition();
                    }

                    else
                    {
                        Toast.makeText(getActivity(), "You cannot remove band positions unless they are vacant!", Toast.LENGTH_SHORT).show();
                        mPositionsSpinner.setSelection(mExistingSelectedPosition);
                    }
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("4"))
                {
                    if(mPositionFiveUser.equals("Vacant"))
                    {
                        // Display the relevant visual components
                        mPositionOneTitle.setVisibility(View.VISIBLE);
                        mPositionOneSpinner.setVisibility(View.VISIBLE);
                        mPositionTwoTitle.setVisibility(View.VISIBLE);
                        mPositionTwoSpinner.setVisibility(View.VISIBLE);
                        mPositionThreeTitle.setVisibility(View.VISIBLE);
                        mPositionThreeSpinner.setVisibility(View.VISIBLE);
                        mPositionFourTitle.setVisibility(View.VISIBLE);
                        mPositionFourSpinner.setVisibility(View.VISIBLE);

                        // Hide the others
                        mPositionFiveTitle.setVisibility(View.GONE);
                        mPositionFiveSpinner.setVisibility(View.GONE);

                        // Get the position selected before the value is changed
                        mExistingSelectedPosition = mPositionsSpinner.getSelectedItemPosition();
                    }

                    else
                    {
                        Toast.makeText(getActivity(), "You cannot remove band positions unless they are vacant!", Toast.LENGTH_SHORT).show();
                        mPositionsSpinner.setSelection(mExistingSelectedPosition);
                    }
                }

                else if(mPositionsSpinner.getItemAtPosition(position).equals("5"))
                {
                    // Display the relevant visual components
                    mPositionOneTitle.setVisibility(View.VISIBLE);
                    mPositionOneSpinner.setVisibility(View.VISIBLE);
                    mPositionTwoTitle.setVisibility(View.VISIBLE);
                    mPositionTwoSpinner.setVisibility(View.VISIBLE);
                    mPositionThreeTitle.setVisibility(View.VISIBLE);
                    mPositionThreeSpinner.setVisibility(View.VISIBLE);
                    mPositionFourTitle.setVisibility(View.VISIBLE);
                    mPositionFourSpinner.setVisibility(View.VISIBLE);
                    mPositionFiveTitle.setVisibility(View.VISIBLE);
                    mPositionFiveSpinner.setVisibility(View.VISIBLE);

                    // Get the position selected before the value is changed
                    mExistingSelectedPosition = mPositionsSpinner.getSelectedItemPosition();
                    mPositionsSpinner.setSelection(mExistingSelectedPosition);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        // When the changeImageTextView is clicked the CheckForPermissions method is called
        mChangeImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CheckForPermissions();
            }
        });

        // When the removeImageTextView is clicked the CheckForPermissions method is called
        mRemoveImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DeleteProfilePicture();
            }
        });

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mBandID = dataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").getValue().toString();
                mBandFromDatabase = new Band();
                mBandFromDatabase = dataSnapshot.child("Bands/" + mBandID).getValue(Band.class);

                // If the band already has a youtube url stored against their profile append this to the text box and parse this
                // to load the video player
                if (dataSnapshot.child("Bands/" + mBandID + "/youtubeUrl/").exists())
                {
                    youtubeUrlEntered = dataSnapshot.child("Bands/" + mBandID + "/youtubeUrl/").getValue().toString();
                    youtubeUrlEditText.setText(youtubeUrlEntered);

                    parsedYouTubeURL = ParseURL(youtubeUrlEditText.getText());

                    if(parsedYouTubeURL != null)
                    {
                        LoadYoutubePlayer();
                    }
                }

                if(youtubeUrlEditText.getText().toString().equals(""))
                {
                    checkUrlButton.setEnabled(false);
                    checkUrlButton.setTextColor(getResources().getColor(R.color.blackButtonDisabledTextColor));
                }

                PopulateFields();

                // This method loads the profile picture from the chosen login method
                LoadProfilePicture();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // When clicked this launches the place picker
        mLaunchLocationFinderButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Displays the progress dialog
                mProgressDialog.show();
                mProgressDialog.setMessage("Loading location finder...");

                try
                {
                    LaunchPlacePicker();
                }

                catch (GooglePlayServicesNotAvailableException e)
                {
                    e.printStackTrace();
                }

                catch (GooglePlayServicesRepairableException e)
                {
                    e.printStackTrace();
                }
            }
        });

        mYoutubeHelpTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Creates a new dialog to display when the save button is clicked
                new AlertDialog.Builder(getActivity())
                        .setTitle("What should I input here?")
                        .setMessage("To help your band get noticed, Giggity allows you to display your band's best YouTube video on your profile!" +
                                " To use this feature, simply copy the URL of your YouTube video into the text field above and hit the 'Submit URL' button." +
                                " If your video loads it means you're good to go! If not, check the URL to make sure it's correct.")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(R.drawable.ic_info_outline_black_24px)
                        .show();
            }
        });

        // When clicked this displays a message helping the user
        mHelpTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Creates a new dialog to display when the save button is clicked
                new AlertDialog.Builder(getActivity())
                        .setTitle("What does this mean?")
                        .setMessage("In order for your band to find gigs and members, you need to have a base location." +
                                " This is effectively the location that your band members will need to get to for rehearsals, band meetings etc.")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(R.drawable.ic_info_outline_black_24px)
                        .show();
            }
        });

        // When clicked this calls the create band method
        mUpdateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                UpdateBand();
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DeleteBand();
            }
        });

        checkUrlButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!TextUtils.isEmpty(youtubeUrlEditText.getText()))
                {
                    parsedYouTubeURL = ParseURL(youtubeUrlEditText.getText());

                    if(parsedYouTubeURL != null)
                    {
                        LoadYoutubePlayer();
                    }

                    else
                    {
                        Toast.makeText(getActivity(), "Youtube URL invalid!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // If the text box is empty the button to submit the url is disabled
        youtubeUrlEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(charSequence.length() == 0)
                {
                    checkUrlButton.setEnabled(false);
                    checkUrlButton.setTextColor(getResources().getColor(R.color.blackButtonDisabledTextColor));
                }

                else
                {
                    checkUrlButton.setEnabled(true);
                    checkUrlButton.setTextColor(getResources().getColor(R.color.mdtp_white));
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        // Set the fragment title
        getActivity().setTitle("Band Manager");

        return fragmentView;
    }

    // This method loads the user's profile picture from their chosen login method. This will need to be
    // changed to pull the users chosen pictures
    private void LoadProfilePicture()
    {
        // This reference looks at the Firebase storage and works out whether the current user has an image
        mBandProfileImageReference.child("BandProfileImages/" + mBandID + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            // If the user has an image this is loaded into the image view
            @Override
            public void onSuccess(Uri uri)
            {
                // The caching and memory features have been disabled to allow only the latest image to display
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mBandProfileImageReference.child("BandProfileImages/" + mBandID + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mBandImageView);
            }

            // If the user doesn't have an image the default image is loaded
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(500, 500).into(mBandImageView);
            }
        });
    }

    // This method checks to see whether the user has allowed the app to access the storage
    // If the permission is granted call the UpdateProfilePicture method
    private void CheckForPermissions()
    {
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        else
        {
            UpdateProfilePicture();
        }
    }

    // This method starts an intent to open the user's storage to select a photo
    private void UpdateProfilePicture()
    {
        Intent selectImage = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(selectImage, RESULT_LOAD_IMAGE);
    }

    private void DeleteProfilePicture()
    {
        // This dialog is created to confirm that the users want to edit the fields
        // they have chosen
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Band Image");
        builder.setMessage("Are you sure you wish to delete this band image?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // Displays the progress dialog
                mProgressDialog.setMessage("Deleting Band Image...");
                mProgressDialog.show();
                mProgressDialog.setCancelable(false);
                mBandProfileImageReference.child("BandProfileImages/" + mBandID + "/profileImage").delete();
                Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(350, 350).into(mBandImageView);
                mProgressDialog.hide();
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

    // Method to create a new instance of the place picker intent builder
    private void LaunchPlacePicker() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException
    {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        mProgressDialog.hide();

        // If the request is for the place picker (i.e. if it matches the request code)
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK && null != data)
        {
            com.google.android.gms.maps.model.LatLng latLngChosenHolder;

            // Store the relevant location data in the variables and display the address of the location
            // because otherwise it will just display a latlng
            mProgressDialog.hide();
            Place place = PlacePicker.getPlace(data, getActivity());

            mBandLocationLatLng = new LatLng();

            mLocationChosenTextView.setText(place.getAddress());
            latLngChosenHolder = place.getLatLng();

            double placeLat = latLngChosenHolder.latitude;
            double placeLng = latLngChosenHolder.longitude;

            mBandLocationLatLng.setLatitude(placeLat);
            mBandLocationLatLng.setLongitude(placeLng);
        }

        else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data)
        {
            // This dialog is created to confirm that the user wants to edit their picture
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Update Band Image");
            builder.setMessage("Are you sure you wish to use this image for your band?");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    // Displays the progress dialog
                    mProgressDialog.setMessage("Updating Band Image...");
                    mProgressDialog.show();
                    mProgressDialog.setCancelable(false);

                    // This gets the data from the intent and stores it in the selectedImage variable as well as uploading it to the firebase storage
                    final Uri selectedImage = data.getData();
                    StorageReference profileImagesRef = mBandProfileImageReference.child("BandProfileImages/" + mBandID + "/profileImage");

                    mUploadTask = profileImagesRef.putFile(selectedImage);

                    mUploadTask.addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                        }
                    });

                    // If the upload is successful the image is then displayed
                    mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            mBandImageView.setImageDrawable(null);

                            // The caching and memory features have been disabled to allow only the latest image to display
                            StorageReference profileImagesRef = mBandProfileImageReference.child("BandProfileImages/" + mBandID + "/profileImage");
                            Glide.with(getContext()).using(new FirebaseImageLoader()).load
                                    (profileImagesRef).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(mBandImageView);

                            mProgressDialog.hide();
                        }
                    });
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
    }

    private void PopulateFields()
    {
        mBandName = mBandFromDatabase.getName();
        mBandNameEditText.setText(mBandName);

        mGenreSpinner.setSelection(GetGenres());

        mNumberOfPositions = mBandFromDatabase.getNumberOfPositions();
        mPositionsSpinner.setSelection(getIndex(mPositionsSpinner, mNumberOfPositions));

        GetPositionInstruments();

        mBandLocationLatLng = mBandFromDatabase.getBaseLocation();

        mLocationChosenTextView.setText(GetAddressFromLatLng(mBandLocationLatLng));

        // Determine the number of positions and the users in each one
        if(mBandFromDatabase.getNumberOfPositions().equals("1"))
        {
            mPositionOneUser = mBandFromDatabase.getPositionOneMember();
        }

        else if(mBandFromDatabase.getNumberOfPositions().equals("2"))
        {
            mPositionOneUser = mBandFromDatabase.getPositionOneMember();
            mPositionTwoUser = mBandFromDatabase.getPositionTwoMember();
        }

        else if(mBandFromDatabase.getNumberOfPositions().equals("3"))
        {
            mPositionOneUser = mBandFromDatabase.getPositionOneMember();
            mPositionTwoUser = mBandFromDatabase.getPositionTwoMember();
            mPositionThreeUser = mBandFromDatabase.getPositionThreeMember();
        }

        else if(mBandFromDatabase.getNumberOfPositions().equals("4"))
        {
            mPositionOneUser = mBandFromDatabase.getPositionOneMember();
            mPositionTwoUser = mBandFromDatabase.getPositionTwoMember();
            mPositionThreeUser = mBandFromDatabase.getPositionThreeMember();
            mPositionFourUser = mBandFromDatabase.getPositionFourMember();
        }

        else if(mBandFromDatabase.getNumberOfPositions().equals("5"))
        {
            mPositionOneUser = mBandFromDatabase.getPositionOneMember();
            mPositionTwoUser = mBandFromDatabase.getPositionTwoMember();
            mPositionThreeUser = mBandFromDatabase.getPositionThreeMember();
            mPositionFourUser = mBandFromDatabase.getPositionFourMember();
            mPositionFiveUser = mBandFromDatabase.getPositionFiveMember();
        }

        mProgressDialog.hide();
    }

    // This method determines how many positions are selected and then populates each spinner with a string array
    private void GetPositionInstruments()
    {
        List<String> splitUserPulledInstrumentsOne;
        List<String> splitUserPulledInstrumentsTwo;
        List<String> splitUserPulledInstrumentsThree;
        List<String> splitUserPulledInstrumentsFour;
        List<String> splitUserPulledInstrumentsFive;

        // This then splits this string into an array of strings, each separated by a comma
        if(mPositionsSpinner.getSelectedItem().equals("1"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);
        }

        else if(mPositionsSpinner.getSelectedItem().equals("2"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);

            splitUserPulledInstrumentsTwo = Arrays.asList(mBandFromDatabase.getPositionTwo().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsTwoFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsTwo.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsTwo.get(i).trim();
                splitUserPulledInstrumentsTwoFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionTwoSpinner.setSelection(splitUserPulledInstrumentsTwoFormatted);
        }

        else if(mPositionsSpinner.getSelectedItem().equals("3"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);

            splitUserPulledInstrumentsTwo = Arrays.asList(mBandFromDatabase.getPositionTwo().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsTwoFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsTwo.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsTwo.get(i).trim();
                splitUserPulledInstrumentsTwoFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionTwoSpinner.setSelection(splitUserPulledInstrumentsTwoFormatted);

            splitUserPulledInstrumentsThree = Arrays.asList(mBandFromDatabase.getPositionThree().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsThreeFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsThree.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsThree.get(i).trim();
                splitUserPulledInstrumentsThreeFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionThreeSpinner.setSelection(splitUserPulledInstrumentsThreeFormatted);
        }

        else if(mPositionsSpinner.getSelectedItem().equals("4"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);

            splitUserPulledInstrumentsTwo = Arrays.asList(mBandFromDatabase.getPositionTwo().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsTwoFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsTwo.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsTwo.get(i).trim();
                splitUserPulledInstrumentsTwoFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionTwoSpinner.setSelection(splitUserPulledInstrumentsTwoFormatted);

            splitUserPulledInstrumentsThree = Arrays.asList(mBandFromDatabase.getPositionThree().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsThreeFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsThree.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsThree.get(i).trim();
                splitUserPulledInstrumentsThreeFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionThreeSpinner.setSelection(splitUserPulledInstrumentsThreeFormatted);

            splitUserPulledInstrumentsFour = Arrays.asList(mBandFromDatabase.getPositionFour().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsFourFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsFour.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsFour.get(i).trim();
                splitUserPulledInstrumentsFourFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionFourSpinner.setSelection(splitUserPulledInstrumentsFourFormatted);
        }

        else if(mPositionsSpinner.getSelectedItem().equals("5"))
        {
            splitUserPulledInstrumentsOne = Arrays.asList(mBandFromDatabase.getPositionOne().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsOneFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsOne.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsOne.get(i).trim();
                splitUserPulledInstrumentsOneFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionOneSpinner.setSelection(splitUserPulledInstrumentsOneFormatted);

            splitUserPulledInstrumentsTwo = Arrays.asList(mBandFromDatabase.getPositionTwo().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsTwoFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsTwo.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsTwo.get(i).trim();
                splitUserPulledInstrumentsTwoFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionTwoSpinner.setSelection(splitUserPulledInstrumentsTwoFormatted);

            splitUserPulledInstrumentsThree = Arrays.asList(mBandFromDatabase.getPositionThree().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsThreeFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsThree.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsThree.get(i).trim();
                splitUserPulledInstrumentsThreeFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionThreeSpinner.setSelection(splitUserPulledInstrumentsThreeFormatted);

            splitUserPulledInstrumentsFour = Arrays.asList(mBandFromDatabase.getPositionFour().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsFourFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsFour.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsFour.get(i).trim();
                splitUserPulledInstrumentsFourFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionFourSpinner.setSelection(splitUserPulledInstrumentsFourFormatted);

            splitUserPulledInstrumentsFive = Arrays.asList(mBandFromDatabase.getPositionFive().split(","));

            // For the select list to understand this, they need any leading or trailing
            // spaces to be removed
            ArrayList<String> splitUserPulledInstrumentsFiveFormatted = new ArrayList<>();

            // The string array is then iterated through and added to a separate string
            // array and passed to the spinner.
            for (int i = 0; i < splitUserPulledInstrumentsFive.size(); i++)
            {
                String formattedInstrumentsStringToAdd;
                formattedInstrumentsStringToAdd = splitUserPulledInstrumentsFive.get(i).trim();
                splitUserPulledInstrumentsFiveFormatted.add(formattedInstrumentsStringToAdd);
            }

            mPositionFiveSpinner.setSelection(splitUserPulledInstrumentsFiveFormatted);
        }
    }

    // This takes the genres from the database and
    private ArrayList<String> GetGenres()
    {
        // This then splits this string into an array of strings, each separated by
        // a comma
        List<String> splitUserPulledGenres = Arrays.asList(mBandFromDatabase.getGenres().split(","));

        // For the select list to understand this, they need any leading or trailing
        // spaces to be removed
        ArrayList<String> splitUserPulledGenresFormatted = new ArrayList<>();

        // The string array is then iterated through and added to a separate string
        // array and passed to the spinner.
        for (int i = 0; i < splitUserPulledGenres.size(); i++)
        {
            String formattedGenreStringToAdd;

            formattedGenreStringToAdd = splitUserPulledGenres.get(i).trim();

            splitUserPulledGenresFormatted.add(formattedGenreStringToAdd);
        }

        return splitUserPulledGenresFormatted;
    }

    // This takes the latlng stored in the database to get the address using Google's Geocoder
    private String GetAddressFromLatLng(LatLng latLng)
    {
        Geocoder geocoder;
        List<Address> addresses;
        String address = "";
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try
        {
            addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

        return address;
    }

    // This gets the index of a spinner that contains a particular value
    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0; i<spinner.getCount(); i++)
        {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    // When called, this should do all the relevant checks to create a band object and post it to the database
    private void UpdateBand()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update Band");
        builder.setMessage("Are you sure you wish to update these fields?");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                mBandName = mBandNameEditText.getText().toString();
                mGenres = mGenreSpinner.getSelectedItemsAsString();
                mNumberOfPositions = mPositionsSpinner.getSelectedItem().toString();

                // This checks to ensure that the main fields are filled in
                if (mBandName != null && mGenres != null && !mNumberOfPositions.equals("0") && mBandLocationLatLng != null)
                {
                    // This checks which items are visible
                    if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.GONE &&
                            mPositionThreeSpinner.getVisibility() == View.GONE &&
                            mPositionFourSpinner.getVisibility() == View.GONE &&
                            mPositionFiveSpinner.getVisibility() == View.GONE)
                    {
                        // If only position one is visible, the value is assigned to mPositionOneValue
                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();

                        // If that variable is not empty, create the object and insert it into the database
                        if (!TextUtils.isEmpty(mPositionOneValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());
                            mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue(mBandFromDatabase.getPositionOneMember());
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/inBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);


                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());
                            }

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }

                        // Otherwise display a message
                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    else if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                            mPositionThreeSpinner.getVisibility() == View.GONE &&
                            mPositionFourSpinner.getVisibility() == View.GONE &&
                            mPositionFiveSpinner.getVisibility() == View.GONE)
                    {
                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                        mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();

                        if (!TextUtils.isEmpty(mPositionOneValue) &&
                                !TextUtils.isEmpty(mPositionTwoValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mPositionTwoSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());

                            if(mBandFromDatabase.getPositionOneMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue(mBandFromDatabase.getPositionOneMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionTwoMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue(mBandFromDatabase.getPositionTwoMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue("Vacant");
                            }

                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/inBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());
                            }

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }

                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    else if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                            mPositionThreeSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFourSpinner.getVisibility() == View.GONE &&
                            mPositionFiveSpinner.getVisibility() == View.GONE)
                    {
                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                        mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();
                        mPositionThreeValue = mPositionThreeSpinner.getSelectedItemsAsString();

                        if (!TextUtils.isEmpty(mPositionOneValue) &&
                                !TextUtils.isEmpty(mPositionTwoValue) &&
                                !TextUtils.isEmpty(mPositionThreeValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mPositionTwoSpinner.getSelectedItemsAsString(),
                                    mPositionThreeSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());

                            if(mBandFromDatabase.getPositionOneMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue(mBandFromDatabase.getPositionOneMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionTwoMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue(mBandFromDatabase.getPositionTwoMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionThreeMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionThreeMember").setValue(mBandFromDatabase.getPositionThreeMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionThreeMember").setValue("Vacant");
                            }

                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/inBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());
                            }

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }

                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    } else if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                            mPositionThreeSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFourSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFiveSpinner.getVisibility() == View.GONE)
                    {
                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                        mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();
                        mPositionThreeValue = mPositionThreeSpinner.getSelectedItemsAsString();
                        mPositionFourValue = mPositionFourSpinner.getSelectedItemsAsString();

                        if (!TextUtils.isEmpty(mPositionOneValue) &&
                                !TextUtils.isEmpty(mPositionTwoValue) &&
                                !TextUtils.isEmpty(mPositionThreeValue) &&
                                !TextUtils.isEmpty(mPositionFourValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mPositionTwoSpinner.getSelectedItemsAsString(),
                                    mPositionThreeSpinner.getSelectedItemsAsString(),
                                    mPositionFourSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());

                            if(mBandFromDatabase.getPositionOneMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue(mBandFromDatabase.getPositionOneMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionTwoMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue(mBandFromDatabase.getPositionTwoMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionThreeMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionThreeMember").setValue(mBandFromDatabase.getPositionThreeMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionThreeMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionFourMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionFourMember").setValue(mBandFromDatabase.getPositionFourMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionFourMember").setValue("Vacant");
                            }

                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/inBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());
                            }

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }

                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (mPositionOneSpinner.getVisibility() == View.VISIBLE &&
                            mPositionTwoSpinner.getVisibility() == View.VISIBLE &&
                            mPositionThreeSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFourSpinner.getVisibility() == View.VISIBLE &&
                            mPositionFiveSpinner.getVisibility() == View.VISIBLE)
                    {

                        mPositionOneValue = mPositionOneSpinner.getSelectedItemsAsString();
                        mPositionTwoValue = mPositionTwoSpinner.getSelectedItemsAsString();
                        mPositionThreeValue = mPositionThreeSpinner.getSelectedItemsAsString();
                        mPositionFourValue = mPositionFourSpinner.getSelectedItemsAsString();
                        mPositionFiveValue = mPositionFiveSpinner.getSelectedItemsAsString();

                        if (!TextUtils.isEmpty(mPositionOneValue)
                                && !TextUtils.isEmpty(mPositionTwoValue)
                                && !TextUtils.isEmpty(mPositionThreeValue)
                                && !TextUtils.isEmpty(mPositionFourValue)
                                && !TextUtils.isEmpty(mPositionFiveValue))
                        {
                            Band bandToInsert = new Band(
                                    mBandID,
                                    mBandName,
                                    mGenres,
                                    mNumberOfPositions,
                                    mPositionOneSpinner.getSelectedItemsAsString(),
                                    mPositionTwoSpinner.getSelectedItemsAsString(),
                                    mPositionThreeSpinner.getSelectedItemsAsString(),
                                    mPositionFourSpinner.getSelectedItemsAsString(),
                                    mPositionFiveSpinner.getSelectedItemsAsString(),
                                    mBandLocationLatLng);

                            mDatabase.child("Bands/" + mBandID).setValue(bandToInsert);
                            mDatabase.child("Bands/" + mBandID + "/bandCreator").setValue(mAuth.getCurrentUser().getUid());

                            if(mBandFromDatabase.getPositionOneMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue(mBandFromDatabase.getPositionOneMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionOneMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionTwoMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue(mBandFromDatabase.getPositionTwoMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionTwoMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionThreeMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionThreeMember").setValue(mBandFromDatabase.getPositionThreeMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionThreeMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionFourMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionFourMember").setValue(mBandFromDatabase.getPositionFourMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionFourMember").setValue("Vacant");
                            }

                            if(mBandFromDatabase.getPositionFiveMember() != null)
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionFiveMember").setValue(mBandFromDatabase.getPositionFiveMember());
                            }

                            else
                            {
                                mDatabase.child("Bands/" + mBandID + "/positionFiveMember").setValue("Vacant");
                            }

                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/inBand").setValue(true);
                            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandID);

                            if(!youtubeUrlEditText.getText().equals(null))
                            {
                                mDatabase.child("Bands/" + mBandID + "/youtubeUrl").setValue(youtubeUrlEditText.getText().toString());
                            }

                            // A dialog is then shown to alert the user that the changes have been made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Band Updated!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ReturnToHome();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                else
                {
                    Toast.makeText(getActivity(), "Please ensure you have completed all the required fields!", Toast.LENGTH_SHORT).show();
                }
            }
        })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // close the dialog
                    }
                }).show();
    }

    // This method
    private void DeleteBand()
    {
        // This dialog is created to confirm that the user wants to delete the band
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Band");
        builder.setMessage("Are you sure you wish to delete this band?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(mNumberOfPositions.equals("1"))
                {
                    String mPositionOneMember = mBandFromDatabase.getPositionOneMember();

                    mDatabase.child("Bands/" + mBandID).removeValue();

                    if(!mPositionOneMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionOneMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionOneMember + "/bandID").removeValue();
                    }
                }

                else if(mNumberOfPositions.equals("2"))
                {
                    String mPositionOneMember = mBandFromDatabase.getPositionOneMember();
                    String mPositionTwoMember = mBandFromDatabase.getPositionTwoMember();

                    mDatabase.child("Bands/" + mBandID).removeValue();

                    if(!mPositionOneMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionOneMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionOneMember + "/bandID").removeValue();
                    }

                    else if(!mPositionTwoMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionTwoMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionTwoMember + "/bandID").removeValue();
                    }
                }

                else if(mNumberOfPositions.equals("3"))
                {
                    String mPositionOneMember = mBandFromDatabase.getPositionOneMember();
                    String mPositionTwoMember = mBandFromDatabase.getPositionTwoMember();
                    String mPositionThreeMember = mBandFromDatabase.getPositionThreeMember();

                    mDatabase.child("Bands/" + mBandID).removeValue();

                    if(!mPositionOneMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionOneMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionOneMember + "/bandID").removeValue();
                    }

                    else if(!mPositionTwoMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionTwoMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionTwoMember + "/bandID").removeValue();
                    }

                    else if(!mPositionThreeMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionThreeMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionThreeMember + "/bandID").removeValue();
                    }
                }

                else if(mNumberOfPositions.equals("4"))
                {
                    String mPositionOneMember = mBandFromDatabase.getPositionOneMember();
                    String mPositionTwoMember = mBandFromDatabase.getPositionTwoMember();
                    String mPositionThreeMember = mBandFromDatabase.getPositionThreeMember();
                    String mPositionFourMember = mBandFromDatabase.getPositionFourMember();

                    mDatabase.child("Bands/" + mBandID).removeValue();

                    if(!mPositionOneMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionOneMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionOneMember + "/bandID").removeValue();
                    }

                    else if(!mPositionTwoMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionTwoMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionTwoMember + "/bandID").removeValue();
                    }

                    else if(!mPositionThreeMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionThreeMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionThreeMember + "/bandID").removeValue();
                    }

                    else if(!mPositionFourMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionFourMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionFourMember + "/bandID").removeValue();
                    }
                }

                else if(mNumberOfPositions.equals("5"))
                {
                    String mPositionOneMember = mBandFromDatabase.getPositionOneMember();
                    String mPositionTwoMember = mBandFromDatabase.getPositionTwoMember();
                    String mPositionThreeMember = mBandFromDatabase.getPositionThreeMember();
                    String mPositionFourMember = mBandFromDatabase.getPositionFourMember();
                    String mPositionFiveMember = mBandFromDatabase.getPositionFiveMember();

                    mDatabase.child("Bands/" + mBandID).removeValue();

                    if(!mPositionOneMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionOneMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionOneMember + "/bandID").removeValue();
                    }

                    else if(!mPositionTwoMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionTwoMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionTwoMember + "/bandID").removeValue();
                    }

                    else if(!mPositionThreeMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionThreeMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionThreeMember + "/bandID").removeValue();
                    }

                    else if(!mPositionFourMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionFourMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionFourMember + "/bandID").removeValue();
                    }

                    else if(!mPositionFiveMember.equals("Vacant"))
                    {
                        mDatabase.child("Users/" + mPositionFiveMember + "/inBand").setValue(false);
                        mDatabase.child("Users/" + mPositionFiveMember + "/bandID").removeValue();
                    }
                }

                // A dialog is then shown to alert the user that the changes have been made
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setMessage("Band Deleted!");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        ReturnToHome();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
            {
                public void onClick (DialogInterface dialog,int which)
                {
                    // close the dialog
                }
            }).
            show();
    }

    // If the youtube initialisation is successful load the URL from the text box if there is one
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored)
    {
        // Determines whether the player was restored from a saved state. If not cue the video
        if (!wasRestored)
        {
            youTubePlayer.cueVideo(parsedYouTubeURL);
        }
    }

    // If the youtube initialisation fails this is called. Usually due to not having youtube installed
    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult)
    {
        Toast.makeText(getActivity(), "The YouTube player can't be initialised! Please ensure you have the YouTube app installed.", Toast.LENGTH_LONG).show();
    }

    // Using some REGEX this trims the youtube url entered to just get the video id at the end
    private String ParseURL(CharSequence youtubeURL)
    {
        String videoIdPattern = "(?<=watch\\?v=|/videos/|embed/)[^#&?]*";

        Pattern compiledPattern = Pattern.compile(videoIdPattern);
        Matcher matcher = compiledPattern.matcher(youtubeURL);

        if (matcher.find())
        {
            return matcher.group();
        }

        // If the URL doesn't match this it means the url is probably a share link which is shortened
        // This block will determine this if it's the case
        else
        {
            try
            {
                String URL;
                String[] parsedURL;

                URL = youtubeURL.toString();
                parsedURL = URL.split("/");

                return parsedURL[3];
            }

            catch (ArrayIndexOutOfBoundsException e)
            {
                return null;
            }
        }
    }

    // This method initialises the player using the api key, relevant layout, fragment etc
    private void LoadYoutubePlayer()
    {
        // Initialise and setup the embedded youtube player
        YouTubePlayerFragment youtubePlayerFragment = new YouTubePlayerFragment();
        youtubePlayerFragment.initialize(getString(R.string.api_key), this);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.youtubeLayout, youtubePlayerFragment);
        fragmentTransaction.commit();
    }

    private void ReturnToHome()
    {
        getActivity().finish();
        getActivity().overridePendingTransition(0,0);

        Intent intent = new Intent(getActivity(), MusicianUserMainActivity.class);
        startActivity(intent);

        getFragmentManager().popBackStackImmediate();
    }
}
