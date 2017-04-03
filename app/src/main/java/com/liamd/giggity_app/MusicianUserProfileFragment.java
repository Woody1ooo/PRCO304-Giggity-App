package com.liamd.giggity_app;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserProfileFragment extends Fragment implements YouTubePlayer.OnInitializedListener
{
    // Declare visual components
    private CircleImageView profileImageView;
    private CircleImageView mainActivityImageView;
    private TextView changeImageTextView;
    private TextView removeImageTextView;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private MultiSelectSpinner genreSpinner;
    private MultiSelectSpinner instrumentSpinner;
    private TextView chosenLocationTextView;
    private Button launchHomeFinderButton;
    private EditText youtubeUrlEditText;
    private Button checkUrlButton;
    private Button saveButton;
    private ProgressDialog mProgressDialog;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;
    private UploadTask mUploadTask;

    // Declare general variables
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private List<String> mGenreList;
    private List<String> mInstrumentList;
    private String youtubeUrlEntered;

    // Declare musician user data variables required

    // This variable is the place information that is stored in the database.
    // This is required because when the location data is retrieved, the built-in
    // google maps latlng object doesn't have an empty constructor which is required
    // by firebase for retrieving data. This therefore stores the lat lng data in my
    // own LatLng class.
    private LatLng mMusicianUserLatLng;

    // Declare activity result variables
    // These have differing values to differentiate them in the activity result method
    private int PLACE_PICKER_REQUEST = 1;
    private static int RESULT_LOAD_IMAGE = 0;

    public MusicianUserProfileFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_profile, container, false);

        // Initialise visual components
        profileImageView = (CircleImageView) fragmentView.findViewById(R.id.profile_image);
        mainActivityImageView = (CircleImageView) getActivity().findViewById(R.id.headerProfileImage);
        changeImageTextView = (TextView) fragmentView.findViewById(R.id.changeImageTextView);
        removeImageTextView = (TextView) fragmentView.findViewById(R.id.removeImageTextView);
        firstNameEditText = (EditText) fragmentView.findViewById(R.id.firstNameEditText);
        lastNameEditText = (EditText) fragmentView.findViewById(R.id.lastNameEditText);
        emailEditText = (EditText) fragmentView.findViewById(R.id.emailEditText);
        genreSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);
        instrumentSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.instrumentSpinner);
        chosenLocationTextView = (TextView) fragmentView.findViewById(R.id.locationDetailsTextView);
        launchHomeFinderButton = (Button) fragmentView.findViewById(R.id.placeFinderButton);
        youtubeUrlEditText = (EditText) fragmentView.findViewById(R.id.youtubeUrlEditText);
        checkUrlButton = (Button) fragmentView.findViewById(R.id.checkUrlButton);
        saveButton = (Button) fragmentView.findViewById(R.id.saveButton);
        mProgressDialog = new ProgressDialog(getContext());

        // Add items to the genre list, and set the spinner to use these
        mGenreList = new ArrayList<>();
        mGenreList.add("Classic Rock");
        mGenreList.add("Alternative Rock");
        mGenreList.add("Blues");
        mGenreList.add("Indie");
        mGenreList.add("Metal");
        mGenreList.add("Pop");
        mGenreList.add("Classical");
        mGenreList.add("Jazz");
        mGenreList.add("Acoustic");

        genreSpinner.setItems(mGenreList);

        // Add items to the instrument list, and set the spinner to use these
        mInstrumentList = new ArrayList<>();
        mInstrumentList.add("Lead Vocals");
        mInstrumentList.add("Backing Vocals");
        mInstrumentList.add("Lead Guitar");
        mInstrumentList.add("Rhythm Guitar");
        mInstrumentList.add("Acoustic Guitar");
        mInstrumentList.add("Bass Guitar");
        mInstrumentList.add("Drums");
        mInstrumentList.add("Keyboards");
        mInstrumentList.add("Piano");

        instrumentSpinner.setItems(mInstrumentList);

        // Add the images to the text views
        changeImageTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_insert_photo_black_18dp, 0, 0, 0);
        removeImageTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete_black_18dp, 0, 0, 0);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // This method loads the profile picture from the chosen login method
        LoadProfilePicture();

        // When the changeImageTextView is clicked the CheckForPermissions method is called
        changeImageTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CheckForPermissions();
            }
        });

        // When the removeImageTextView is clicked the CheckForPermissions method is called
        removeImageTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DeleteProfilePicture();
            }
        });

        // Pull the existing information from the database to populate the various fields
        mDatabase.child("Users/").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String urlStored;
                String userStoredLat;
                String userStoredLng;

                String firstName = dataSnapshot.child(mAuth.getCurrentUser().getUid() + "/firstName").getValue().toString();
                firstNameEditText.setText(firstName);

                String lastName = dataSnapshot.child(mAuth.getCurrentUser().getUid() + "/lastName").getValue().toString();
                lastNameEditText.setText(lastName);

                String email = dataSnapshot.child(mAuth.getCurrentUser().getUid() + "/email").getValue().toString();
                emailEditText.setText(email);

                // This method populates the genre spinner with the genres the user
                // selected when setting up their account
                genreSpinner.setSelection(PopulateUserGenreData(dataSnapshot));

                // This method populates the instrument spinner with the instruments the user
                // selected when setting up their account
                instrumentSpinner.setSelection(PopulateUserInstrumentData(dataSnapshot));

                String location = dataSnapshot.child(mAuth.getCurrentUser().getUid() + "/homeAddress").getValue().toString();
                chosenLocationTextView.setText(location);

                userStoredLat = dataSnapshot.child(mAuth.getCurrentUser().getUid()
                        + "/homeLocation/latitude").getValue().toString();
                userStoredLng = dataSnapshot.child(mAuth.getCurrentUser().getUid()
                        + "/homeLocation/longitude").getValue().toString();

                String latLng = userStoredLat + "," + userStoredLng;
                List<String> splitUserHomeLocation = Arrays.asList(latLng.split(","));

                double latitude = Double.parseDouble(splitUserHomeLocation.get(0));
                double longitude = Double.parseDouble(splitUserHomeLocation.get(1));

                mMusicianUserLatLng = new LatLng();
                mMusicianUserLatLng.setLatitude(latitude);
                mMusicianUserLatLng.setLongitude(longitude);

                // If the user already has a youtube url stored against their profile append this to the text box and parse this
                // to load the video player
                if (dataSnapshot.child(mAuth.getCurrentUser().getUid() + "/youtubeUrl/").exists())
                {
                    urlStored = dataSnapshot.child(mAuth.getCurrentUser().getUid() + "/youtubeUrl").getValue().toString();
                    youtubeUrlEditText.setText(urlStored);

                    youtubeUrlEntered = ParseURL(youtubeUrlEditText.getText());
                    LoadYoutubePlayer();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        checkUrlButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!TextUtils.isEmpty(youtubeUrlEditText.getText()))
                {
                    youtubeUrlEntered = ParseURL(youtubeUrlEditText.getText());
                    LoadYoutubePlayer();
                }
            }
        });

        // If the location finder button is selected, call the LaunchPlacePicker to start
        // the place picker activity
        launchHomeFinderButton.setOnClickListener(new View.OnClickListener()
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
                } catch (GooglePlayServicesNotAvailableException e)
                {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e)
                {
                    e.printStackTrace();
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Save();
            }
        });

        return fragmentView;
    }

    // This method loads the user's profile picture from their chosen login method. This will need to be
    // changed to pull the users chosen pictures
    private void LoadProfilePicture()
    {
        // This reference looks at the Firebase storage and works out whether the current user has an image
        mProfileImageReference.child("ProfileImages/" + mAuth.getCurrentUser().getUid() + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            // If the user has an image this is loaded into the image view
            @Override
            public void onSuccess(Uri uri)
            {
                // The caching and memory features have been disabled to allow only the latest image to display
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mProfileImageReference.child("ProfileImages/" + mAuth.getCurrentUser().getUid() + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(profileImageView);
            }

            // If the user doesn't have an image the default image is loaded
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(350, 350).into(profileImageView);
            }
        });
    }

    // This method checks to see whether the user has allowed the app to access the storage
    // If the permission is granted call the UpdateProfilePicture method
    private void CheckForPermissions()
    {
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else
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
        builder.setTitle("Delete Profile Picture");
        builder.setMessage("Are you sure you wish to delete your profile picture?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // Displays the progress dialog
                mProgressDialog.setMessage("Deleting profile picture...");
                mProgressDialog.show();
                mProfileImageReference.child("ProfileImages/" + mAuth.getCurrentUser().getUid() + "/profileImage").delete();
                Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(350, 350).into(profileImageView);

                // This updates the image on the navigation drawer as well
                Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(220, 220).into(mainActivityImageView);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        mProgressDialog.hide();

        // When the image has been selected upload the image to the firebase URL specified by mProfileImageReference
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data)
        {
            // This dialog is created to confirm that the user wants to edit their picture
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Update Profile Picture");
            builder.setMessage("Are you sure you wish to use this image for your profile picture?");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    // Displays the progress dialog
                    mProgressDialog.setMessage("Updating profile picture...");
                    mProgressDialog.show();

                    // This gets the data from the intent and stores it in the selectedImage variable as well as uploading it to the firebase storage
                    final Uri selectedImage = data.getData();
                    StorageReference profileImagesRef = mProfileImageReference.child("ProfileImages/" + mAuth.getCurrentUser().getUid() + "/profileImage");

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
                            profileImageView.setImageDrawable(null);

                            // The caching and memory features have been disabled to allow only the latest image to display
                            StorageReference profileImagesRef = mProfileImageReference.child("ProfileImages/" + mAuth.getCurrentUser().getUid() + "/profileImage");
                            Glide.with(getContext()).using(new FirebaseImageLoader()).load
                                    (profileImagesRef).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(350, 350).into(profileImageView);

                            // This updates the image on the navigation drawer as well
                            Glide.with(getContext()).using(new FirebaseImageLoader()).load
                                    (profileImagesRef).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(220, 220).into(mainActivityImageView);

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

        // If the request is for the place picker (i.e. if it matches the request code)
        else if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK && null != data)
        {
            com.google.android.gms.maps.model.LatLng latLngChosenHolder;

            // Store the relevant location data in the variables and display the address of the location
            // because otherwise it will just display a latlng
            mProgressDialog.hide();
            Place place = PlacePicker.getPlace(data, getActivity());

            mMusicianUserLatLng = new LatLng();

            chosenLocationTextView.setText(place.getAddress());
            latLngChosenHolder = place.getLatLng();

            double placeLat = latLngChosenHolder.latitude;
            double placeLng = latLngChosenHolder.longitude;

            mMusicianUserLatLng.setLatitude(placeLat);
            mMusicianUserLatLng.setLongitude(placeLng);
        }
    }

    // This method processes the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        {
            // If the permission has been accepted call the update profile picture method to access the storage
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                UpdateProfilePicture();
            }

            // If the permission has been denied then display a message to that effect
            else
            {
                Toast.makeText(getActivity(), "If you wish to change your profile image," +
                        " please ensure you have given permission to access your storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // If the youtube initialisation is successful load the URL from the text box if there is one
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored)
    {
        // Determines whether the player was restored from a saved state. If not cue the video
        if (!wasRestored)
        {
            youTubePlayer.cueVideo(youtubeUrlEntered);
        }
    }

    // If the youtube initialisation fails this is called. Usually due to not having youtube installed
    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult)
    {
        Toast.makeText(getActivity(), "The YouTube player can't be initialised! Please ensure you have the YouTube app installed.", Toast.LENGTH_LONG).show();
    }

    // This method takes a snapshot of the database as a parameter and returns a
    // list of trimmed strings to populate the list of genres that the user selected
    // when they created their account
    private ArrayList<String> PopulateUserGenreData(DataSnapshot dataSnapshot)
    {
        // This takes the list of genres from the database that the user has selected
        // and adds them to a string
        String userPulledGenres = dataSnapshot.child(mAuth.getCurrentUser().getUid()
                + "/genres").getValue().toString();

        // This then splits this string into an array of strings, each separated by
        // a comma
        List<String> splitUserPulledGenres = Arrays.asList(userPulledGenres.split(","));

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

    // This method takes a snapshot of the database as a parameter and returns a
    // list of trimmed strings to populate the list of instruments that the user selected
    // when they created their account
    private ArrayList<String> PopulateUserInstrumentData(DataSnapshot dataSnapshot)
    {
        // This takes the list of instruments from the database that the user has selected
        // and adds them to a string
        String userPulledInstruments = dataSnapshot.child(mAuth.getCurrentUser().getUid()
                + "/instruments").getValue().toString();

        // This then splits this string into an array of strings, each separated by
        // a comma
        List<String> splitUserPulledInstruments = Arrays.asList(userPulledInstruments.split(","));

        // For the select list to understand this, they need any leading or trailing
        // spaces to be removed
        ArrayList<String> splitUserPulledInstrumentsFormatted = new ArrayList<>();

        // The string array is then iterated through and added to a separate string
        // array and passed to the spinner.
        for (int i = 0; i < splitUserPulledInstruments.size(); i++)
        {
            String formattedGenreStringToAdd;

            formattedGenreStringToAdd = splitUserPulledInstruments.get(i).trim();

            splitUserPulledInstrumentsFormatted.add(formattedGenreStringToAdd);
        }

        return splitUserPulledInstrumentsFormatted;
    }

    // Using some REGEX this trims the youtube url entered to just get the video id at the end
    private String ParseURL(CharSequence youtubeURL)
    {
        String videoIdPattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

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
            String URL;
            String[] parsedURL;

            URL = youtubeURL.toString();
            parsedURL = URL.split("/");

            return parsedURL[3];
        }
    }

    // Method to create a new instance of the place picker intent builder
    private void LaunchPlacePicker() throws GooglePlayServicesNotAvailableException,
            GooglePlayServicesRepairableException
    {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);

    }

    // This method initialises the player using the api key, relevant layout, fragment etc
    private void LoadYoutubePlayer()
    {
        // Initialise and setup the embedded youtube player
        YouTubePlayerFragment youtubePlayerFragment = new YouTubePlayerFragment();
        youtubePlayerFragment.initialize(getString(R.string.api_key), this);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.youtube_layout, youtubePlayerFragment);
        fragmentTransaction.commit();
    }

    // This method is called when the user clicks the save button
    private void Save()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update Profile Preferences");
        builder.setMessage("Are you sure you wish to update these fields?");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String genreList = genreSpinner.getSelectedItemsAsString();
                String instrumentList = instrumentSpinner.getSelectedItemsAsString();
                String address = chosenLocationTextView.getText().toString();
                String youtubeUrl = youtubeUrlEditText.getText().toString();

                // When update is clicked the fields are checked to ensure none are blank
                if (TextUtils.isEmpty(firstNameEditText.getText())
                        || TextUtils.isEmpty(lastNameEditText.getText())
                        || TextUtils.isEmpty(chosenLocationTextView.getText())
                        || genreSpinner.getSelectedItemsAsString().isEmpty()
                        || instrumentSpinner.getSelectedItemsAsString().isEmpty())
                {
                    Toast.makeText(getActivity(), "Please ensure you have given a value for each field! Please note that the Youtube URL is optional.", Toast.LENGTH_LONG).show();
                }

                // If the fields are correct then the relevant database nodes are updated
                else
                {
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).child("firstName").setValue(firstName);
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).child("lastName").setValue(lastName);
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).child("genres").setValue(genreList);
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).child("instruments").setValue(instrumentList);
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).child("homeAddress").setValue(address);
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).child("homeLocation").setValue(mMusicianUserLatLng);
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).child("youtubeUrl").setValue(youtubeUrl);

                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirmation");
                    builder.setMessage("Fields Updated!");
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

    private void ReturnToHome()
    {
        getActivity().finish();
        getActivity().overridePendingTransition(0,0);

        Intent intent = new Intent(getActivity(), MusicianUserMainActivity.class);
        startActivity(intent);

        getFragmentManager().popBackStackImmediate();

        // The user is then taken to the home fragment
        //getActivity().setTitle("Home");
        //final FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.replace(R.id.frame, new MusicianUserHomeFragment(), "MusicianUserHomeFragment");
        //ft.commit();
    }
}
