package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;


/**
 * A simple {@link Fragment} subclass.
 */
public class VenueUserBandDetailsFragment extends Fragment implements OnMapReadyCallback, YouTubePlayer.OnInitializedListener
{
    // Declare general variables
    private String mBandId;
    private String mBandName;
    private String mBandGenres;
    private Double mBandDistance;
    private Double mBandLocationLat;
    private Double mBandLocationLng;
    private Double mVenueLocationLat;
    private Double mVenueLocationLng;
    private LatLng mBandLocation;
    private LatLng mVenueLocation;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private String mYoutubeURL;
    private String mVenueId;
    private String mGigId;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;
    private DataSnapshot mSnapshot;

    // Declare visual components
    private CircleImageView mBandImageView;
    private TextView mBandNameTextView;
    private TextView mBandGenresTextView;
    private TextView mBandDistanceTextView;
    private TextView mYoutubeHeadingTextView;
    private Button mInviteButton;

    public VenueUserBandDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.venue_user_fragment_band_details, container, false);

        // Initialise visual components
        mBandImageView = (CircleImageView) fragmentView.findViewById(R.id.bandImageView);
        mBandNameTextView = (TextView) fragmentView.findViewById(R.id.bandNameTextView);
        mBandGenresTextView = (TextView) fragmentView.findViewById(R.id.bandGenresTextView);
        mBandDistanceTextView = (TextView) fragmentView.findViewById(R.id.bandDistanceTextView);
        mYoutubeHeadingTextView = (TextView) fragmentView.findViewById(R.id.youtubeHeadingTextView);
        mInviteButton = (Button) fragmentView.findViewById(R.id.inviteButton);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.bandMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        mInviteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Invite();
            }
        });

        return fragmentView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;

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

                // This places a marker at the users chosen location
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(mBandLocation));

                mBandDistanceTextView.setText("Distance From Band: " + mBandDistance + "km");

                // This zooms the map in to a reasonable level (12) and centers it on the location provided
                float zoomLevel = 15;
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mBandLocation, zoomLevel));
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

        mBandId = getArguments().getString("BandID");
        mBandName = getArguments().getString("BandName");
        mBandGenres = getArguments().getString("BandGenres");
        mBandDistance = getArguments().getDouble("BandDistance");
        mBandLocationLat = getArguments().getDouble("BandLocationLat");
        mBandLocationLng = getArguments().getDouble("BandLocationLng");
        mVenueLocationLat = getArguments().getDouble("VenueLocationLat");
        mVenueLocationLng = getArguments().getDouble("VenueLocationLng");
        mGigId = getArguments().getString("GigId");
        mVenueId = mSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/venueID").getValue().toString();

        mBandLocation = new LatLng(mBandLocationLat, mBandLocationLng);
        mVenueLocation = new LatLng(mVenueLocationLat, mVenueLocationLng);

        mBandNameTextView.setText(mBandName);
        mBandGenresTextView.setText(mBandGenres);
        mBandDistanceTextView.setText("Distance From Band: " + mBandDistance + "km");

        // This reference looks at the Firebase storage and works out whether the current user has an image
        mProfileImageReference.child("BandProfileImages/" + mBandId + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            // If the user has an image this is loaded into the image view
            @Override
            public void onSuccess(Uri uri)
            {
                // The caching and memory features have been disabled to allow only the latest image to display
                Glide.with(getContext()).using(new FirebaseImageLoader()).load
                        (mProfileImageReference.child("BandProfileImages/" + mBandId + "/profileImage"))
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

        // If the user already has a youtube url stored against their profile append this to the text box and parse this
        // to load the video player
        if(mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").exists())
        {
            if (!mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").getValue().equals(""))
            {
                urlStored = mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").getValue().toString();
                mYoutubeURL = ParseURL(urlStored);
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

    private void Invite()
    {
        // This dialog is created to confirm that the users want to edit the fields
        // they have chosen
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Invite to gig");
        builder.setMessage("Are you sure you wish to invite this band to play at your gig?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // If the user has already submitted a request for this band then inform them and cancel the request
                if(mSnapshot.child("VenueSentGigRequests/" + mVenueId + "/" + mGigId + "/" + mBandId).exists())
                {
                    RequestExistsDialog();
                }

                else
                {
                    mDatabase.child("VenueSentGigRequests/" + mVenueId + "/" + mGigId + "/" + mBandId).child("bandID").setValue(mBandId);
                    mDatabase.child("VenueSentGigRequests/" + mVenueId + "/" + mGigId + "/" + mBandId).child("gigID").setValue(mGigId);
                    mDatabase.child("VenueSentGigRequests/" + mVenueId + "/" + mGigId + "/" + mBandId).child("venueID").setValue(mVenueId);
                    mDatabase.child("VenueSentGigRequests/" + mVenueId + "/" + mGigId + "/" + mBandId).child("requestStatus").setValue("Pending");
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
        // A dialog is then shown to alert the user that the changes have been made
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirmation");
        builder.setMessage("Application Submitted! Please check your sent requests to view the status.");
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
        // A dialog is then shown to alert the user that they have already submitted a request to this band
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Alert");
        builder.setMessage("Application Rejected! You have already sent a request to this band.");
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
        getActivity().setTitle("Home");
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frame, new MusicianUserHomeFragment(), "MusicianUserHomeFragment");
        ft.commit();
    }
}
