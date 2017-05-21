package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import me.everything.providers.android.calendar.CalendarProvider;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserGigRequestsReceivedDetailsFragment extends Fragment implements OnMapReadyCallback, YouTubePlayer.OnInitializedListener
{
    // Declare visual components
    private CircleImageView mBandImageView;
    private TextView mGigNameTextView;
    private TextView mGigStartDateTextView;
    private TextView mGigEndDateTextView;
    private TextView mBandNameTextView;
    private TextView mBandGenresTextView;
    private TextView mYoutubeShowcaseHeadingTextView;
    private TextView mBandDistanceTextView;
    private FrameLayout mYouTubeFrame;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private TextView mRequestStatusTextView;
    private Button mAcceptButton;
    private Button mRejectButton;

    // Declare general variables
    private String mBandId;
    private String mGigId;
    private String mVenueId;
    private String mGigName;
    private String mStartDate;
    private String mFinishDate;
    private com.google.android.gms.maps.model.LatLng mBandLatLng;
    private Location mBandLocation;
    private Location mGigLocation;
    private String mRequestStatus;
    private String mYoutubeURL;

    // Declare Firebase specific variables
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mBandImageReference;
    private DataSnapshot mSnapshot;

    public VenueUserGigRequestsReceivedDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.venue_user_fragment_gig_requests_received_details, container, false);

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mBandImageReference = mStorage.getReference();

        // Initialise visual components
        mGigNameTextView = (TextView) fragmentView.findViewById(R.id.gigNameTextView);
        mGigStartDateTextView = (TextView) fragmentView.findViewById(R.id.gigStartDateTextView);
        mGigEndDateTextView = (TextView) fragmentView.findViewById(R.id.gigFinishDateTextView);
        mBandImageView = (CircleImageView) fragmentView.findViewById(R.id.bandImageView);
        mBandNameTextView = (TextView) fragmentView.findViewById(R.id.bandNameTextView);
        mBandGenresTextView = (TextView) fragmentView.findViewById(R.id.bandGenresTextView);
        mBandDistanceTextView = (TextView) fragmentView.findViewById(R.id.bandDistanceTextView);
        mRequestStatusTextView = (TextView) fragmentView.findViewById(R.id.requestStatusTextView);
        mYoutubeShowcaseHeadingTextView = (TextView) fragmentView.findViewById(R.id.youtubeHeadingTextView);
        mYouTubeFrame = (FrameLayout) fragmentView.findViewById(R.id.youtube_layout);
        mAcceptButton = (Button) fragmentView.findViewById(R.id.acceptButton);
        mRejectButton = (Button) fragmentView.findViewById(R.id.rejectButton);

        // Initialise general variables
        mBandLocation = new Location("");
        mGigLocation = new Location("");

        // Retrieve values from the previous fragment
        mBandId = getArguments().getString("BandID");
        mGigId = getArguments().getString("GigID");
        mVenueId = getArguments().getString("VenueID");
        mGigName = getArguments().getString("GigName");
        mStartDate = getArguments().getString("GigStartDate");
        mFinishDate = getArguments().getString("GigEndDate");
        mRequestStatus = getArguments().getString("RequestStatus");

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.bandMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        mAcceptButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AcceptRequest();
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                RejectRequest();
            }
        });

        // Set the fragment title
        getActivity().setTitle("Gig Requests");

        return fragmentView;
    }

    // When the map is ready, call the SetupMap method
    @Override
    public void onMapReady(GoogleMap map)
    {
        mGoogleMap = map;

        SetupMap();
    }

    // This method sets up any location related variables
    private void SetupMap()
    {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mSnapshot = dataSnapshot;
                PopulateFields();

                String bandBaseLat = mSnapshot.child("Bands/" + mBandId + "/baseLocation/latitude").getValue().toString();
                String bandBaseLng = mSnapshot.child("Bands/" + mBandId + "/baseLocation/longitude").getValue().toString();

                String latLng = bandBaseLat + "," + bandBaseLng;
                List<String> splitBandBaseLocation = Arrays.asList(latLng.split(","));

                double latitude = Double.parseDouble(splitBandBaseLocation.get(0));
                double longitude = Double.parseDouble(splitBandBaseLocation.get(1));

                mBandLatLng = new com.google.android.gms.maps.model.LatLng(latitude, longitude);

                com.google.android.gms.maps.model.LatLng bandLocation = new com.google.android.gms.maps.model.LatLng(mBandLatLng.latitude, mBandLatLng.longitude);

                int height = 125;
                int width = 125;
                BitmapDrawable bitMapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_pin);
                Bitmap b = bitMapDraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                // This places a marker at the users chosen location
                mGoogleMap.addMarker(new MarkerOptions().position(bandLocation).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                // This zooms the map in to a reasonable level (12) and centers it on the location provided
                float zoomLevel = 8;
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bandLocation, zoomLevel));

                // These values are set so they can be compared with the gig location vales to calculate the distance
                mBandLocation.setLatitude(latitude);
                mBandLocation.setLongitude(longitude);

                // This block gets the venue location so the distance can be compared
                String gigBaseLat = mSnapshot.child("Venues/" + mVenueId + "/venueLocation/latitude").getValue().toString();
                String gigBaseLng = mSnapshot.child("Venues/" + mVenueId + "/venueLocation/longitude").getValue().toString();

                latLng = gigBaseLat + "," + gigBaseLng;
                List<String> splitGigBaseLocation = Arrays.asList(latLng.split(","));

                latitude = Double.parseDouble(splitGigBaseLocation.get(0));
                longitude = Double.parseDouble(splitGigBaseLocation.get(1));

                mGigLocation.setLongitude(longitude);
                mGigLocation.setLatitude(latitude);

                mBandDistanceTextView.setText(CalculateDistance(mGigLocation, mBandLocation) + "km from your venue's location");
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    // This method populates the visual components with data
    private void PopulateFields()
    {
        mGigNameTextView.setText(mGigName);

        // This takes the start and end dates and reformats them to look more visually appealing
        String formattedStartDateSectionOne = mStartDate.split(" ")[0];
        String formattedStartDateSectionTwo = mStartDate.split(" ")[1];
        String formattedStartDateSectionThree = mStartDate.split(" ")[2];
        String formattedStartDateSectionFour = mStartDate.split(" ")[3];

        String formattedFinishDateSectionOne = mFinishDate.split(" ")[0];
        String formattedFinishDateSectionTwo = mFinishDate.split(" ")[1];
        String formattedFinishDateSectionThree = mFinishDate.split(" ")[2];
        String formattedFinishDateSectionFour = mFinishDate.split(" ")[3];

        mGigStartDateTextView.setText(formattedStartDateSectionOne + " " + formattedStartDateSectionTwo + " " + formattedStartDateSectionThree + " " + formattedStartDateSectionFour);
        mGigEndDateTextView.setText(formattedFinishDateSectionOne + " " + formattedFinishDateSectionTwo + " " + formattedFinishDateSectionThree + " " + formattedFinishDateSectionFour);

        mBandNameTextView.setText(mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString());
        mBandGenresTextView.setText(mSnapshot.child("Bands/" + mBandId + "/genres").getValue().toString());

        mRequestStatusTextView.setText(mRequestStatus);

        // Depending on the status the colour is updated
        if (mRequestStatus.equals("Pending"))
        {
            mRequestStatusTextView.setTextColor(Color.parseColor("#ff6100"));
        } else if (mRequestStatus.equals("Rejected"))
        {
            mRequestStatusTextView.setTextColor(Color.RED);
        } else if (mRequestStatus.equals("Accepted"))
        {
            mRequestStatusTextView.setTextColor(Color.GREEN);
        }

        if(getActivity() != null)
        {
            // This reference looks at the Firebase storage and works out whether the current user has an image
            mBandImageReference.child("BandProfileImages/" + mBandId + "/profileImage")
                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                // If the user has an image this is loaded into the image view
                @Override
                public void onSuccess(Uri uri)
                {
                    // The caching and memory features have been disabled to allow only the latest image to display
                    Glide.with(getContext()).using(new FirebaseImageLoader()).load
                            (mBandImageReference.child("BandProfileImages/" + mBandId + "/profileImage"))
                            .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mBandImageView);
                }

                // If the user doesn't have an image the default image is loaded
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                }
            });
        }

        // If the user already has a youtube url stored against their profile append this to the text box and parse this
        // to load the video player
        if (mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").exists())
        {
            if (!mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").getValue().equals(""))
            {
                mYoutubeURL = mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").getValue().toString();
                mYoutubeURL = ParseURL(mYoutubeURL);
                LoadYoutubePlayer();
            } else
            {
                mYoutubeShowcaseHeadingTextView.setVisibility(View.GONE);
                mYouTubeFrame.setVisibility(View.GONE);
            }
        } else
        {
            mYoutubeShowcaseHeadingTextView.setVisibility(View.GONE);
            mYouTubeFrame.setVisibility(View.GONE);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored)
    {
        // Determines whether the player was restored from a saved state. If not cue the video
        if (!wasRestored)
        {
            youTubePlayer.cueVideo(mYoutubeURL);
        }
    }

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

    // This method takes the band location and the gig's location and calculates the distance between the two
    private double CalculateDistance(Location gigLocation, Location bandLocation)
    {
        double distance;

        // This calculates the distance between the passed band location and the musician's location
        distance = bandLocation.distanceTo(gigLocation);

        // This converts the distance into km
        distance = distance / 1000;

        // This then rounds the distance to 2DP
        distance = Math.round(distance * 100D) / 100D;

        return distance;
    }

    private void AcceptRequest()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Accept Gig Request");
        builder.setMessage("Are you sure you wish to accept this gig request?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(mRequestStatus.equals("Pending"))
                {
                    mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId + "/requestStatus").setValue("Accepted");
                    mDatabase.child("Gigs/" + mGigId + "/bookedAct").setValue(mBandId);

                    // Get the current date time for the news items
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();

                    // This posts a news feed item
                    String newsFeedPushKey = mDatabase.child("NewsFeedItems/").push().getKey();
                    NewsFeedItem item = new NewsFeedItem(newsFeedPushKey, mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString() + " ",
                            "are now booked to play a gig at " + mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + "!", mBandId, date);
                    mDatabase.child("NewsFeedItems/" + newsFeedPushKey).setValue(item);

                    // Depending on the number of positions in the band and which of those are vacant, an entry for each member is entered into the database
                    // to then be picked up when they login to find out whether they want to add it to their calendar
                    String numberOfBandPositions = mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString();

                    if(numberOfBandPositions.equals("2"))
                    {
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    if(numberOfBandPositions.equals("3"))
                    {
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    if(numberOfBandPositions.equals("4"))
                    {
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    if(numberOfBandPositions.equals("5"))
                    {
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if (!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if (!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if (!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if (!mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has accepted your request to play their gig!", "BandSentGigRequestAccepted", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirmation");
                    builder.setMessage("Request Accepted!");
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

    // This removes the request from the database
    private void RejectRequest()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Reject Gig Request");
        builder.setMessage("Are you sure you wish to reject this gig request?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(mRequestStatus.equals("Pending"))
                {
                    mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId + "/requestStatus").setValue("Rejected");

                    // Depending on the number of positions in the band and which of those are vacant, an entry for each member is entered into the database
                    // to then be picked up when they login to find out whether they want to add it to their calendar
                    String numberOfBandPositions = mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString();

                    if(numberOfBandPositions.equals("2"))
                    {
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    if(numberOfBandPositions.equals("3"))
                    {
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    if(numberOfBandPositions.equals("4"))
                    {
                        if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if(!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    if(numberOfBandPositions.equals("5"))
                    {
                        if (!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if (!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if (!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if (!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }

                        if (!mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString().equals("Vacant"))
                        {
                            String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString();

                            UserGigInformation info = new UserGigInformation();
                            info.setGigID(mGigId);
                            info.setCalendarEventID("Pending");
                            info.setMemberConfirmedRequest("False");

                            mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);

                            String notificationID;

                            // Generate a notification ID from the database
                            notificationID = mDatabase.push().getKey();

                            Notification notification = new Notification(notificationID, mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString() + " has rejected your request to play their gig!", "BandSentGigRequestRejected", bandMemberUserId);

                            mDatabase.child("Users/" + bandMemberUserId + "/notifications/" + notificationID + "/").setValue(notification);
                        }
                    }

                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirmation");
                    builder.setMessage("Request Rejected!");
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