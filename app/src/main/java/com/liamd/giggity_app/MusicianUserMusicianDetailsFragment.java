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
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserMusicianDetailsFragment extends Fragment implements OnMapReadyCallback, YouTubePlayer.OnInitializedListener
{
    // Declare visual components
    private CircleImageView mMusicianImageView;
    private TextView mMusicianNameTextView;
    private TextView mMusicianGenresTextView;
    private TextView mMusicianInstrumentsTextView;
    private TextView mYoutubeShowcaseHeadingTextView;
    private TextView mMusicianDistanceTextView;
    private FrameLayout mYouTubeFrame;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Button mInviteButton;

    // Declare Firebase specific variables
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;
    private DataSnapshot mSnapshot;

    // Declare variables passed from the previous fragment
    private String mUserId;
    private String mUserName;
    private String mUserGenres;
    private String mUserInstruments;
    private String mBandId;
    private String mBandPosition;
    private double mDistance;
    private double mMusicianLocationLat;
    private double mMusicianLocationLng;

    // Declare general variables
    private String mYoutubeURL;

    public MusicianUserMusicianDetailsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_musician_details, container, false);

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // Initialise visual components
        mMusicianImageView = (CircleImageView) fragmentView.findViewById(R.id.musicianImageView);
        mMusicianNameTextView = (TextView) fragmentView.findViewById(R.id.musicianNameTextView);
        mMusicianGenresTextView = (TextView) fragmentView.findViewById(R.id.musicianGenresTextView);
        mMusicianInstrumentsTextView = (TextView) fragmentView.findViewById(R.id.musicianInstrumentsTextView);
        mYoutubeShowcaseHeadingTextView = (TextView) fragmentView.findViewById(R.id.youtubeHeadingTextView);
        mMusicianDistanceTextView = (TextView) fragmentView.findViewById(R.id.musicianDistanceTextView);
        mYouTubeFrame = (FrameLayout) fragmentView.findViewById(R.id.youtube_layout);
        mInviteButton = (Button) fragmentView.findViewById(R.id.inviteButton);

        // Retrieve values from the previous fragment
        mUserId = getArguments().getString("UserID");
        mUserName = getArguments().getString("UserName");
        mUserGenres = getArguments().getString("UserGenres");
        mUserInstruments = getArguments().getString("UserInstruments");
        mBandId = getArguments().getString("BandId");
        mBandPosition = getArguments().getString("BandPosition");
        mDistance = getArguments().getDouble("Distance");
        mMusicianLocationLat = getArguments().getDouble("Lat");
        mMusicianLocationLng = getArguments().getDouble("Lng");

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.musicianMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        mInviteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Request();
            }
        });

        // Set the fragment title
        getActivity().setTitle("Musician Finder");

        return fragmentView;
    }

    // When the map is ready, call the SetupMap method
    @Override
    public void onMapReady(GoogleMap map)
    {
        mGoogleMap = map;

        SetupMap();
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

                com.google.android.gms.maps.model.LatLng musicianLocation = new LatLng(mMusicianLocationLat, mMusicianLocationLng);

                // This places a marker at the users chosen location
                int height = 125;
                int width = 125;
                BitmapDrawable bitMapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_pin);
                Bitmap b = bitMapDraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                // This places a marker at the users chosen location
                mGoogleMap.addMarker(new MarkerOptions().position(musicianLocation).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                // This zooms the map in to a reasonable level (12) and centers it on the location provided
                float zoomLevel = 8;
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(musicianLocation, zoomLevel));
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void PopulateFields()
    {
        mMusicianNameTextView.setText(mUserName);
        mMusicianGenresTextView.setText(mUserGenres);
        mMusicianInstrumentsTextView.setText(mUserInstruments);
        mMusicianDistanceTextView.setText(mDistance + "km from your band's base location");

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
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mMusicianImageView);
            }

            // If the user doesn't have an image the default image is loaded
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(500, 500).into(mMusicianImageView);
            }
        });

        // If the user already has a youtube url stored against their profile append this to the text box and parse this
        // to load the video player
        if(mSnapshot.child("Users/" + mUserId + "/youtubeUrl/").exists())
        {
            if (!mSnapshot.child("Users/" + mUserId + "/youtubeUrl/").getValue().equals(""))
            {
                mYoutubeURL = mSnapshot.child("Users/" + mUserId + "/youtubeUrl/").getValue().toString();
                mYoutubeURL = ParseURL(mYoutubeURL);
                LoadYoutubePlayer();
            }

            else
            {
                mYoutubeShowcaseHeadingTextView.setVisibility(View.GONE);
                mYouTubeFrame.setVisibility(View.GONE);
            }
        }

        else
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
        fragmentTransaction.replace(R.id.youtube_layout, youtubePlayerFragment);
        fragmentTransaction.commit();
    }

    private void Request()
    {
        // This dialog is created to confirm that the band wants to send a request to the user chosen
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Invite Musician");
        builder.setMessage("Are you sure you wish to offer this musician a place in your band?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(mSnapshot.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).exists())
                {
                    RequestExistsDialog();
                }

                else
                {
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).child("bandID").setValue(mBandId);
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).child("userID").setValue(mUserId);
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).child("requestStatus").setValue("Pending");
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).child("bandPosition").setValue(mBandPosition);
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).child("userName").setValue(mUserName);
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).child("userInstruments").setValue(mUserInstruments);
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).child("bandName").setValue(mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString());
                    mDatabase.child("BandSentMusicianRequests/" + mBandId + "/" + mUserId).child("positionInstruments").setValue(getArguments().getString("PositionInstruments"));

                    // This posts a notification to the database to be picked up by the user who submitted the request
                    String notificationID;

                    // Generate a notification ID from the database
                    notificationID = mDatabase.push().getKey();

                    Notification notification = new Notification(notificationID, mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString() + " has invited you to join their band!", "BandSentMusicianRequestPending", mUserId);
                    mDatabase.child("Users/" + mUserId + "/notifications/" + notificationID + "/").setValue(notification);

                    ConfirmDialog();
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

    private void ConfirmDialog()
    {
        // A dialog is then shown to alert the user that the request has been sent
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirmation");
        builder.setMessage("Request Submitted! Please check your sent requests to view the status.");
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

    private void RequestExistsDialog()
    {
        // A dialog is then shown to alert the user that they have already submitted a request to this user
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Alert");
        builder.setMessage("Request Rejected! You have already sent a request to this musician.");
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

    private void ReturnToHome()
    {
        // The user is then taken to the home fragment
        getActivity().finish();
        getActivity().overridePendingTransition(0,0);

        Intent intent = new Intent(getActivity(), MusicianUserMainActivity.class);
        startActivity(intent);

        getFragmentManager().popBackStackImmediate();
    }
}
