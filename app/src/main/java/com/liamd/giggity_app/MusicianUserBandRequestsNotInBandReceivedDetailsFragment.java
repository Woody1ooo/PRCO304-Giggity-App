package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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
public class MusicianUserBandRequestsNotInBandReceivedDetailsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, YouTubePlayer.OnInitializedListener
{
    // Declare visual components
    private CircleImageView mProfileImageView;
    private TextView mNameTextView;
    private TextView mGenresTextView;
    private TextView mInstrumentsTextView;
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
    private String mBandId;
    private com.google.android.gms.maps.model.LatLng mBandConvertedLatLng;
    private String mYoutubeUrlEntered;
    private String mBandPosition;

    // Location variables
    private double mDistance;
    private double mUserLocationLat;
    private double mUserLocationLng;
    private Location mUserLocation;
    private Location mBandLocation;

    public MusicianUserBandRequestsNotInBandReceivedDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_band_requests_not_in_band_received_details, container, false);

        mProfileImageView = (CircleImageView) fragmentView.findViewById(R.id.profileImage);
        mNameTextView = (TextView) fragmentView.findViewById(R.id.nameTextView);
        mGenresTextView = (TextView) fragmentView.findViewById(R.id.genreTextView);
        mInstrumentsTextView = (TextView) fragmentView.findViewById(R.id.instrumentsTextView);
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
        mNameTextView.setText(getArguments().getString("BandName"));
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

        return fragmentView;
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {

    }

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
                mGoogleMap.addMarker(new MarkerOptions().position(mBandConvertedLatLng).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                GetUserLocation();

                mLocationDistanceTextView.setText("Distance From Band: " + mDistance + "km");

                // This gets the position applied for from the snapshot
                mBandPosition = mSnapshot.child("BandSentMusicianRequests/" + mBandId + "/" + mAuth.getCurrentUser().getUid() + "/bandPosition").getValue().toString();

                // This zooms the map in to a reasonable level (12) and centers it on the location provided
                float zoomLevel = 8;
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mBandConvertedLatLng, zoomLevel));
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

        // This reference looks at the Firebase storage and works out whether the current band has an image
        mProfileImageReference.child("BandProfileImages/" + mBandId + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            // If the band has an image this is loaded into the image view
            @Override
            public void onSuccess(Uri uri)
            {
                // The caching and memory features have been disabled to allow only the latest image to display
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mProfileImageReference.child("BandProfileImages/" + mBandId + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mProfileImageView);
            }
        });

        mGenresTextView.setText(mSnapshot.child("Bands/" + mBandId + "/genres").getValue().toString());
        mInstrumentsTextView.setText(getArguments().getString("PositionInstruments"));

        String bandBaseLat = mSnapshot.child("Bands/" + mBandId + "/baseLocation/latitude").getValue().toString();
        String bandBaseLng = mSnapshot.child("Bands/" + mBandId + "/baseLocation/longitude").getValue().toString();

        String latLng = bandBaseLat + "," + bandBaseLng;
        List<String> splitBandBaseLocation = Arrays.asList(latLng.split(","));

        double latitude = Double.parseDouble(splitBandBaseLocation.get(0));
        double longitude = Double.parseDouble(splitBandBaseLocation.get(1));

        mBandConvertedLatLng = new com.google.android.gms.maps.model.LatLng(latitude, longitude);

        // If the user already has a youtube url stored against their profile append this to the text box and parse this
        // to load the video player
        if(mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").exists())
        {
            if (!mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").getValue().equals(""))
            {
                urlStored = mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").getValue().toString();
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
    private void GetUserLocation()
    {
        // Get the location of the user from the database
        mUserLocationLat = mSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/homeLocation/latitude").getValue(Double.class);
        mUserLocationLng = mSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/homeLocation/longitude").getValue(Double.class);

        // Then set the data as parameters for the gig location object
        mUserLocation.setLatitude(mUserLocationLat);
        mUserLocation.setLongitude(mUserLocationLng);

        // Then set the data as parameters for the user location object
        mBandLocation.setLatitude(mBandConvertedLatLng.latitude);
        mBandLocation.setLongitude(mBandConvertedLatLng.longitude);

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
        builder.setTitle("Accept Band Request");
        builder.setMessage("Are you sure you wish to join this band?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(mSnapshot.child("BandSentMusicianRequests/" + mBandId + "/" + mAuth.getCurrentUser().getUid() + "/requestStatus").getValue().toString().equals("Pending"))
                {
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mAuth.getCurrentUser().getUid() + "/requestStatus").setValue("Accepted");
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/inBand").setValue(true);
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandId);
                    mDatabase.child("Bands/" + mBandId + "/" + mBandPosition + "Member").setValue(mAuth.getCurrentUser().getUid());

                    // Get the current date time for the news items
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();

                    // This posts a news feed item
                    String newsFeedPushKey = mDatabase.child("NewsFeedItems/").push().getKey();
                    NewsFeedItem item = new NewsFeedItem(newsFeedPushKey,
                            mSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/lastName").getValue().toString(),
                            "has just joined " + mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString() + ".", mBandId, date);
                    item.setUserID(mAuth.getCurrentUser().getUid());
                    mDatabase.child("NewsFeedItems/" + newsFeedPushKey).setValue(item);

                    // Check how many people in the band need notifications sent
                    if (mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString().equals("2"))
                    {
                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // Check how many people in the band need notifications sent
                    else if (mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString().equals("3"))
                    {
                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // Check how many people in the band need notifications sent
                    else if (mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString().equals("4"))
                    {
                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // Check how many people in the band need notifications sent
                    else if (mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString().equals("5"))
                    {
                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has accepted a request at your band!", "BandSentMusicianRequestAccepted", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirmation");
                    builder.setMessage("Band Joined!");
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

    private void Reject()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Reject Band Request");
        builder.setMessage("Are you sure you wish to reject this band's offer?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(mSnapshot.child("BandSentMusicianRequests/" + mBandId + "/" + mAuth.getCurrentUser().getUid() + "/requestStatus").getValue().toString().equals("Pending"))
                {
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mAuth.getCurrentUser().getUid() + "/requestStatus").setValue("Rejected");
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/inBand").setValue(true);
                    mDatabase.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").setValue(mBandId);
                    mDatabase.child("Bands/" + mBandId + "/" + mBandPosition + "Member").setValue(mAuth.getCurrentUser().getUid());

                    // Check how many people in the band need notifications sent
                    if (mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString().equals("2"))
                    {
                        // Check the position isn't vacant
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // Check how many people in the band need notifications sent
                    else if (mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString().equals("3"))
                    {
                        // Check the position isn't vacant
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // Check how many people in the band need notifications sent
                    else if (mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString().equals("4"))
                    {
                        // Check the position isn't vacant
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // Check how many people in the band need notifications sent
                    else if (mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString().equals("5"))
                    {
                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        // Check the position isn't vacant
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserID;
                            String notificationID;
                            String bandMemberName;

                            // Get the band members user ID
                            bandMemberUserID = mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString();

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            // Get the band members name using the ID
                            bandMemberName = mSnapshot.child("Users/" + bandMemberUserID + "/firstName").getValue().toString() + " " +
                                    mSnapshot.child("Users/" + bandMemberUserID + "/lastName").getValue().toString();

                            Notification notification = new Notification(notificationID, bandMemberName + " has rejected a request at your band!", "BandSentMusicianRequestRejected", bandMemberUserID);

                            mDatabase.child("Users/" + bandMemberUserID + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirmation");
                    builder.setMessage("Offer Rejected!");
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
