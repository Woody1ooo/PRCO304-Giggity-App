package com.liamd.giggity_app;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class VenueUserProfileFragment extends Fragment
{
    // Declare visual components
    private CircleImageView venueImageView;
    private CircleImageView mainActivityImageView;
    private ImageButton changeImageButton;
    private ImageButton removeImageButton;
    private EditText venueNameEditText;
    private EditText emailEditText;
    private MultiSelectSpinner genreSpinner;
    private TextView chosenVenueTextView;
    private Button launchVenueFinderButton;
    private Button saveButton;
    private EditText mVenueCapacityEditText;
    private EditText mAgeEditText;
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
    private String mVenueId;
    private String mVenueName;
    private GoogleApiClient mGoogleApiClient;
    private int mAge = 0;
    private int mCapacity = 0;

    // This variable is the place information that is stored in the database.
    // This is required because when the location data is retrieved, the built-in
    // google maps latlng object doesn't have an empty constructor which is required
    // by firebase for retrieving data. This therefore stores the lat lng data in my
    // own LatLng class.
    private LatLng mVenueLatLng;

    // Declare activity result variables
    // These have differing values to differentiate them in the activity result method
    private int PLACE_PICKER_REQUEST = 1;
    private static int RESULT_LOAD_IMAGE = 0;

    public VenueUserProfileFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.venue_user_fragment_profile, container, false);

        // Initialise visual components
        venueImageView = (CircleImageView) fragmentView.findViewById(R.id.venueImage);
        mainActivityImageView = (CircleImageView) getActivity().findViewById(R.id.navDrawerImageView);
        changeImageButton = (ImageButton) fragmentView.findViewById(R.id.changeImageButton);
        removeImageButton = (ImageButton) fragmentView.findViewById(R.id.removeImageButton);
        venueNameEditText = (EditText) fragmentView.findViewById(R.id.venueNameEditText);
        emailEditText = (EditText) fragmentView.findViewById(R.id.emailEditText);
        genreSpinner = (MultiSelectSpinner) fragmentView.findViewById(R.id.genreSpinner);
        chosenVenueTextView = (TextView) fragmentView.findViewById(R.id.locationDetailsTextView);
        launchVenueFinderButton = (Button) fragmentView.findViewById(R.id.placeFinderButton);
        mVenueCapacityEditText = (EditText) fragmentView.findViewById(R.id.venueCapacityEditText);
        mAgeEditText = (EditText) fragmentView.findViewById(R.id.venueAgeEditText);
        saveButton = (Button) fragmentView.findViewById(R.id.saveButton);
        mProgressDialog = new ProgressDialog(getContext());

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

        genreSpinner.setItems(mGenreList);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // When the changeImageTextView is clicked the CheckForPermissions method is called
        changeImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CheckForPermissions();
            }
        });

        // When the removeImageTextView is clicked the CheckForPermissions method is called
        removeImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DeleteProfilePicture();
            }
        });

        // Pull the existing information from the database to populate the various fields
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String userStoredLat;
                String userStoredLng;

                mVenueId = dataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/venueID").getValue().toString();

                // This method loads the profile picture from the chosen login method
                LoadProfilePicture();

                String email = dataSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/email").getValue().toString();
                emailEditText.setText(email);

                mVenueName = dataSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString();
                venueNameEditText.setText(dataSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString());

                // This gets the venue capacity set against the venue and sets it as the value
                mVenueCapacityEditText.setText(dataSnapshot.child("Venues/" + mVenueId + "/capacity").getValue().toString());
                mCapacity = Integer.parseInt(mVenueCapacityEditText.getText().toString());

                // Set the minimum performer age components to those from the database
                mAgeEditText.setText(dataSnapshot.child("Venues/" + mVenueId + "/minimumPerformerAge").getValue().toString());
                mAge = Integer.parseInt(mAgeEditText.getText().toString());

                // This method populates the genre spinner with the genres the user
                // selected when setting up their account
                genreSpinner.setSelection(PopulateUserGenreData(dataSnapshot));

                String location = dataSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString();
                chosenVenueTextView.setText(location);

                userStoredLat = dataSnapshot.child("Venues/" + mVenueId
                        + "/venueLocation/latitude").getValue().toString();
                userStoredLng = dataSnapshot.child("Venues/" + mVenueId
                        + "/venueLocation/longitude").getValue().toString();

                String latLng = userStoredLat + "," + userStoredLng;
                List<String> splitUserHomeLocation = Arrays.asList(latLng.split(","));

                double latitude = Double.parseDouble(splitUserHomeLocation.get(0));
                double longitude = Double.parseDouble(splitUserHomeLocation.get(1));

                mVenueLatLng = new LatLng();
                mVenueLatLng.setLatitude(latitude);
                mVenueLatLng.setLongitude(longitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // If the location finder button is selected, call the LaunchPlacePicker to start
        // the place picker activity
        launchVenueFinderButton.setOnClickListener(new View.OnClickListener()
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

        mAgeEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                if(!mAgeEditText.getText().toString().equals(""))
                {
                    mAge = Integer.parseInt(mAgeEditText.getText().toString());
                }

                else
                {
                    mAge = 0;
                }
            }
        });

        mVenueCapacityEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                if(!mVenueCapacityEditText.getText().toString().equals(""))
                {
                    mCapacity = Integer.parseInt(mVenueCapacityEditText.getText().toString());
                }

                else
                {
                    mCapacity = 0;
                }
            }
        });

        // Set the fragment title
        getActivity().setTitle("My Venue Profile");

        return fragmentView;
    }

    // This method loads the user's profile picture from their chosen login method. This will need to be
    // changed to pull the users chosen pictures
    private void LoadProfilePicture()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext()).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).build();
        mGoogleApiClient.connect();

        // This reference looks at the Firebase storage and works out whether the current user has an image
        mProfileImageReference.child("VenueProfileImages/" + mVenueId + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            // If the venue has an image this is loaded into the image view
            @Override
            public void onSuccess(Uri uri)
            {
                // The caching and memory features have been disabled to allow only the latest image to display
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mProfileImageReference.child("VenueProfileImages/" + mVenueId + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(venueImageView);
            }

            // If the venue doesn't have an image then attempt to load the Google images
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                placePhotosAsync();
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
        builder.setTitle("Delete Venue Picture");
        builder.setIcon(R.drawable.ic_info_outline_black_24px);
        builder.setMessage("Are you sure you wish to delete your venue's picture?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // Displays the progress dialog
                mProgressDialog.setMessage("Deleting venue picture...");
                mProgressDialog.show();
                mProgressDialog.setCancelable(false);
                mProfileImageReference.child("VenueProfileImages/" + mVenueId + "/profileImage").delete();

                // Load the Google photos to replace the deleted image
                placePhotosAsync();

                // This updates the image on the navigation drawer as well
                ((DrawerProfilePictureUpdater) getActivity()).UpdateDrawerProfilePicture();

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
            builder.setTitle("Update Venue Picture");
            builder.setIcon(R.drawable.ic_info_outline_black_24px);
            builder.setMessage("Are you sure you wish to use this image for your venue picture?");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    // Displays the progress dialog
                    mProgressDialog.setMessage("Updating venue picture...");
                    mProgressDialog.show();
                    mProgressDialog.setCancelable(false);

                    // This gets the data from the intent and stores it in the selectedImage variable as well as uploading it to the firebase storage
                    final Uri selectedImage = data.getData();
                    StorageReference profileImagesRef = mProfileImageReference.child("VenueProfileImages/" + mVenueId + "/profileImage");

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
                            venueImageView.setImageDrawable(null);

                            // The caching and memory features have been disabled to allow only the latest image to display
                            StorageReference profileImagesRef = mProfileImageReference.child("VenueProfileImages/" + mVenueId + "/profileImage");
                            Glide.with(getContext()).using(new FirebaseImageLoader()).load
                                    (profileImagesRef).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(venueImageView);

                            ((DrawerProfilePictureUpdater) getActivity()).UpdateDrawerProfilePicture();

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

            mVenueLatLng = new LatLng();

            chosenVenueTextView.setText(place.getName() + " \n" + place.getAddress());
            venueNameEditText.setText(place.getName());

            mVenueName = place.getName().toString();

            latLngChosenHolder = place.getLatLng();

            double placeLat = latLngChosenHolder.latitude;
            double placeLng = latLngChosenHolder.longitude;

            mVenueLatLng.setLatitude(placeLat);
            mVenueLatLng.setLongitude(placeLng);
        }
    }

    // This method processes the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        {
            // If the permission has been accepted call the update profile picture method to access the storage
            if (permissions.length == 1 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED)
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

    // This method takes a snapshot of the database as a parameter and returns a
    // list of trimmed strings to populate the list of genres that the user selected
    // when they created their account
    private ArrayList<String> PopulateUserGenreData(DataSnapshot dataSnapshot)
    {
        // This takes the list of genres from the database that the user has selected
        // and adds them to a string
        String userPulledGenres = dataSnapshot.child("Venues/" + mVenueId
                + "/genre").getValue().toString();

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

    // Method to create a new instance of the place picker intent builder
    private void LaunchPlacePicker() throws GooglePlayServicesNotAvailableException,
            GooglePlayServicesRepairableException
    {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);

    }

    // This method is called when the user clicks the save button
    private void Save()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update Venue Preferences");
        builder.setIcon(R.drawable.ic_info_outline_black_24px);
        builder.setMessage("Are you sure you wish to update these fields?");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String genreList = genreSpinner.getSelectedItemsAsString();

                // If the venue name edit text is not empty then use this as the venue name
                if(!venueNameEditText.getText().toString().equals(""))
                {
                    mVenueName = venueNameEditText.getText().toString();
                }

                // When update is clicked the fields are checked to ensure none are blank
                if(genreSpinner.getSelectedItemsAsString().isEmpty())
                {
                    Toast.makeText(getActivity(), "Please ensure you have given a value for each field!", Toast.LENGTH_LONG).show();
                }

                // If the fields are correct then the relevant database nodes are updated
                else
                {
                    if(mCapacity > 0 && mAge > 0)
                    {
                        mDatabase.child("Venues/" + mVenueId).child("/capacity").setValue(mCapacity);
                        mDatabase.child("Venues/" + mVenueId).child("/genre").setValue(genreList);
                        mDatabase.child("Venues/" + mVenueId).child("/name").setValue(mVenueName);
                        mDatabase.child("Venues/" + mVenueId).child("/venueLocation").setValue(mVenueLatLng);
                        mDatabase.child("Venues/" + mVenueId).child("/minimumPerformerAge").setValue(mAge);

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

                    else
                    {
                        Toast.makeText(getActivity(), "Please ensure you have set your capacity and minimum performer age correctly!", Toast.LENGTH_LONG).show();
                    }
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

        Intent intent = new Intent(getActivity(), VenueUserMainActivity.class);
        startActivity(intent);

        getFragmentManager().popBackStackImmediate();
    }

    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback = new ResultCallback<PlacePhotoResult>()
    {
        @Override
        public void onResult(PlacePhotoResult placePhotoResult)
        {
            if (!placePhotoResult.getStatus().isSuccess())
            {
                return;
            }

            venueImageView.setImageBitmap(placePhotoResult.getBitmap());
        }
    };

    /**
     * Load a bitmap from the photos API asynchronously
     * by using buffers and result callbacks.
     */
    private void placePhotosAsync()
    {
        if(getActivity() != null)
        {
            final String placeId = mVenueId;
            Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeId)
                    .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>()
                    {
                        @Override
                        public void onResult(PlacePhotoMetadataResult photos)
                        {
                            ((DrawerProfilePictureUpdater) getActivity()).UpdateDrawerProfilePicture();

                            if (!photos.getStatus().isSuccess())
                            {
                                return;
                            }

                            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                            if (photoMetadataBuffer.getCount() > 0)
                            {
                                // Display the first bitmap in an ImageView in the size of the view
                                photoMetadataBuffer.get(0)
                                        .getScaledPhoto(mGoogleApiClient, venueImageView.getWidth(),
                                                venueImageView.getHeight())
                                        .setResultCallback(mDisplayPhotoResultCallback);
                            }

                            else
                            {
                                // This reference looks at the Firebase storage and works out whether the current venue has an image
                                mProfileImageReference.child("VenueProfileImages/" + mVenueId + "/profileImage")
                                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                                {
                                    // If the venue has an image this is loaded into the image view
                                    @Override
                                    public void onSuccess(Uri uri)
                                    {
                                        // The caching and memory features have been disabled to allow only the latest image to display
                                        Glide.with(getContext()).using(new FirebaseImageLoader()).load
                                                (mProfileImageReference.child("VenueProfileImages/" + mVenueId + "/profileImage"))
                                                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(venueImageView);
                                    }

                                    // If the venue doesn't have an image the default image is loaded
                                }).addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(500, 500).into(venueImageView);
                                    }
                                });
                            }
                            photoMetadataBuffer.release();
                        }
                    });
        }
    }
}
