package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserBandRequestsInBandReceivedDetailsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, YouTubePlayer.OnInitializedListener
{
    // Declare visual components
    private CircleImageView mProfileImageView;
    private TextView mNameTextView;
    private TextView mGenresTextView;
    private TextView mInstrumentsTextView;
    private TextView mBaseLocationTextView;
    private TextView mLocationDistanceTextView;
    private TextView mYoutubeHeadingTextView;
    private Button mAcceptButton;
    private Button mRejectButton;
    private MapView mMapView;
    private GoogleMap mGoogleMap;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;
    private DataSnapshot mSnapshot;

    // Declare general variables
    private String mUserId;
    private String mBandId;
    private com.google.android.gms.maps.model.LatLng mUserConvertedLatLng;
    private String mYoutubeUrlEntered;
    private String mBandPosition;
    private String mRequestStatus;

    // Location variables
    private double mDistance;
    private Location mBandLocation;
    private double mBandLocationLat;
    private double mBandLocationLng;
    private Location mUserLocation;

    public MusicianUserBandRequestsInBandReceivedDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_requests_in_band_received_details, container, false);

        mProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.profileImage);
        mNameTextView = (TextView) fragmentView.findViewById(R.id.nameTextView);
        mGenresTextView = (TextView) fragmentView.findViewById(R.id.genreTextView);
        mInstrumentsTextView = (TextView) fragmentView.findViewById(R.id.instrumentsTextView);
        mBaseLocationTextView = (TextView) fragmentView.findViewById(R.id.locationDetailsTextView);
        mLocationDistanceTextView = (TextView) fragmentView.findViewById(R.id.locationDistanceTextView);
        mYoutubeHeadingTextView = (TextView) fragmentView.findViewById(R.id.youtubeUrlTextView);
        mAcceptButton = (Button) fragmentView.findViewById(R.id.acceptButton);
        mRejectButton = (Button) fragmentView.findViewById(R.id.rejectButton);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // Get variables from the previous fragment
        mUserId = getArguments().getString("UserID");
        mNameTextView.setText(getArguments().getString("UserName"));
        mBandId = getArguments().getString("BandID");

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.locationMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        // Initialise other variables required
        mBandLocation = new Location("");
        mUserLocation = new Location("");

        mAcceptButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Accept();
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Reject();
            }
        });

        // Set the fragment title
        getActivity().setTitle("Band Requests");

        // This value event listener gets the latest request status state so that a decision can never be overriden by two band users at the same time
        if(getActivity() != null)
        {
            mDatabase.child("MusicianSentBandRequests/" + mUserId + "/" + mBandId + "/requestStatus").addValueEventListener(new ValueEventListener()
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
        }

        return fragmentView;
    }

    // When the map is ready, call the SetupMap method
    @Override
    public void onMapReady(GoogleMap map)
    {
        mGoogleMap = map;

        // Once the map is ready, it can be set up using SetupMap()
        SetupMap();

        mGoogleMap.setOnInfoWindowClickListener(this);
    }

    private void SetupMap()
    {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mSnapshot = dataSnapshot;
                PopulateFields();

                int height = 125;
                int width = 125;
                BitmapDrawable bitMapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_pin);
                Bitmap b = bitMapDraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                // This places a marker at the users chosen location
                mGoogleMap.addMarker(new MarkerOptions().position(mUserConvertedLatLng).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                GetBandLocation();

                mLocationDistanceTextView.setText("Distance From Band: " + mDistance + "km");

                // This gets the position applied for from the snapshot
                mBandPosition = mSnapshot.child("MusicianSentBandRequests/" + mUserId + "/" + mBandId + "/bandPosition").getValue().toString();

                // This zooms the map in to a reasonable level (12) and centers it on the location provided
                float zoomLevel = 8;
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserConvertedLatLng, zoomLevel));
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void PopulateFields()
    {
        String urlStored;

        // This reference looks at the Firebase storage and works out whether the current user has an image
        mProfileImageReference.child("ProfileImages/" + mUserId + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            // If the user has an image this is loaded into the image view
            @Override
            public void onSuccess(Uri uri)
            {
                // The caching and memory features have been disabled to allow only the latest image to display
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mProfileImageReference.child("ProfileImages/" + mUserId + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mProfileImageView);
            }

            // If the user doesn't have an image the default image is loaded
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(500, 500).into(mProfileImageView);
            }
        });

        mGenresTextView.setText(mSnapshot.child("Users/" + mUserId + "/genres").getValue().toString());
        mInstrumentsTextView.setText(mSnapshot.child("Users/" + mUserId + "/instruments").getValue().toString());
        mBaseLocationTextView.setText(mSnapshot.child("Users/" + mUserId + "/homeAddress").getValue().toString());

        String userBaseLat = mSnapshot.child("Users/" + mUserId + "/homeLocation/latitude").getValue().toString();
        String userBaseLng = mSnapshot.child("Users/" + mUserId + "/homeLocation/longitude").getValue().toString();

        String latLng = userBaseLat + "," + userBaseLng;
        List<String> splitUserHomeLocation = Arrays.asList(latLng.split(","));

        double latitude = Double.parseDouble(splitUserHomeLocation.get(0));
        double longitude = Double.parseDouble(splitUserHomeLocation.get(1));

        mUserConvertedLatLng = new com.google.android.gms.maps.model.LatLng(latitude, longitude);

        // If the user already has a youtube url stored against their profile append this to the text box and parse this
        // to load the video player
        if(mSnapshot.child("Users/" + mUserId + "/youtubeUrl/").exists())
        {
            if (!mSnapshot.child("Users/" + mUserId + "/youtubeUrl/").getValue().equals(""))
            {
                urlStored = mSnapshot.child("Users/" + mUserId + "/youtubeUrl").getValue().toString();
                mYoutubeUrlEntered = ParseURL(urlStored);
                LoadYoutubePlayer();
            }

            else
            {
                mYoutubeHeadingTextView.setVisibility(View.GONE);
            }
        }

        else
        {
            mYoutubeHeadingTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {

    }

    // If the youtube initialisation is successful load the URL from the text box if there is one
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored)
    {
        // Determines whether the player was restored from a saved state. If not cue the video
        if (!wasRestored)
        {
            youTubePlayer.cueVideo(mYoutubeUrlEntered);
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

    // This method retrieves the location of the band and applies the attributes to a location object
    // The same is then done with the user location and then the Calculate Distance method is called
    private void GetBandLocation()
    {
        // Get the location of the gig from the previous fragment
        mBandLocationLat = mSnapshot.child("Bands/" + mBandId + "/baseLocation/latitude").getValue(Double.class);
        mBandLocationLng = mSnapshot.child("Bands/" + mBandId + "/baseLocation/longitude").getValue(Double.class);

        // Then set the data as parameters for the gig location object
        mBandLocation.setLatitude(mBandLocationLat);
        mBandLocation.setLongitude(mBandLocationLng);

        // Then set the data as parameters for the user location object
        mUserLocation.setLatitude(mUserConvertedLatLng.latitude);
        mUserLocation.setLongitude(mUserConvertedLatLng.longitude);

        // Calculate the distance between the provided location and the gig
        mDistance = CalculateDistance(mBandLocation, mUserLocation);
    }

    // This method takes the band location and the user's location and calculates the distance between the two
    private double CalculateDistance(Location bandLocation, Location userLocation)
    {
        double distance;

        // This calculates the distance between the passed band location and the user's current location
        distance = bandLocation.distanceTo(userLocation);

        // This converts the distance into km
        distance = distance / 1000;

        // This then rounds the distance to 2DP
        distance = Math.round(distance * 100D) / 100D;

        return distance;
    }

    private void Accept()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add User To Band");
        builder.setMessage("Are you sure you wish to add this user to your band?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // If a decision hasn't yet been made allow the user to confirm this request
                if(mRequestStatus.equals("Pending"))
                {
                    mDatabase.child("MusicianSentBandRequests/" + mUserId + "/" + mBandId + "/requestStatus").setValue("Accepted");
                    mDatabase.child("Users/" + mUserId + "/inBand").setValue(true);
                    mDatabase.child("Users/" + mUserId + "/bandID").setValue(mBandId);
                    mDatabase.child("Bands/" + mBandId + "/" + mBandPosition + "Member").setValue(mUserId);

                    // This posts a notification to the database to be picked up by the user who submitted the request
                    String notificationID;
                    String bandName;

                    // Generate a notification ID from the database
                    notificationID = mDatabase.push().getKey();

                    // Get the band's name
                    bandName = mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString();

                    Notification notification = new Notification(notificationID, bandName + " has accepted your request to join their band!", "MusicianSentBandRequestAccepted", mUserId);
                    mDatabase.child("Users/" + mUserId + "/notifications/" + notificationID + "/").setValue(notification);

                    // Get the current date time for the news items
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();

                    // This posts a news feed item
                    String newsFeedPushKey = mDatabase.child("NewsFeedItems/").push().getKey();
                    NewsFeedItem item = new NewsFeedItem(newsFeedPushKey,
                            mSnapshot.child("Users/" + mUserId + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + mUserId + "/lastName").getValue().toString(),
                            "has just joined " + mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString() + ".", mBandId, date);
                    item.setUserID(mUserId);
                    mDatabase.child("NewsFeedItems/" + newsFeedPushKey).setValue(item);

                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirmation");
                    builder.setMessage("User Added!");
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

    private void Reject()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Reject User From Band");
        builder.setMessage("Are you sure you wish to reject this user from your band?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // If a decision hasn't yet been made allow the user to reject this request
                if(mRequestStatus.equals("Pending"))
                {
                    mDatabase.child("MusicianSentBandRequests/" + mUserId + "/" + mBandId + "/requestStatus").setValue("Rejected");

                    // This posts a notification to the database to be picked up by the user who submitted the request
                    String notificationID;
                    String bandName;

                    // Generate a notification ID from the database
                    notificationID = mDatabase.push().getKey();

                    // Get the band's name
                    bandName = mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString();

                    Notification notification = new Notification(notificationID, bandName + " has rejected your request to join their band!", "MusicianSentBandRequestRejected", mUserId);
                    mDatabase.child("Users/" + mUserId + "/notifications/" + notificationID + "/").setValue(notification);

                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirmation");
                    builder.setMessage("User Rejected!");
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

    private void ReturnToHome()
    {
        getActivity().finish();
        getActivity().overridePendingTransition(0,0);

        Intent intent = new Intent(getActivity(), MusicianUserMainActivity.class);
        startActivity(intent);

        getFragmentManager().popBackStackImmediate();
    }
}
