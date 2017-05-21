package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;


/**
 * A simple {@link Fragment} subclass.
 */
public class FanUserGigDetailsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, YouTubePlayer.OnInitializedListener
{
    // Declare visual components
    private TextView mGigNameTextView;
    private CircleImageView mBandImageView;
    private TextView mBandNameTextView;
    private TextView mGigStartDateTextView;
    private TextView mGigEndDateTextView;
    private TextView mGigVenueTextView;
    private Button mGetTicketsButton;
    private MapView mMapView;
    private GoogleMap mGoogleMap;

    // Declare general variables
    private String mVenueId;
    private String mGigId;
    private String mBandId;
    private com.google.android.gms.maps.model.LatLng mVenueLocation;
    private String mGigStartDate;
    private String mGigEndDate;
    private String mParsedYouTubeURL;
    private String mYoutubeUrlEntered;
    private int mTicketQuantity;
    private int mTicketQuantityAvailable;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DataSnapshot mSnapshot;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    public FanUserGigDetailsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fan_user_fragment_gig_details, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // Initialise visual components
        mBandImageView = (CircleImageView) fragmentView.findViewById(R.id.bandProfileImage);
        mBandNameTextView = (TextView) fragmentView.findViewById(R.id.bandNameTextView);
        mGigNameTextView = (TextView) fragmentView.findViewById(R.id.gigNameTextView);
        mGigStartDateTextView = (TextView) fragmentView.findViewById(R.id.startDateTextView);
        mGigEndDateTextView = (TextView) fragmentView.findViewById(R.id.finishDateTextView);
        mGigVenueTextView = (TextView) fragmentView.findViewById(R.id.venueTextView);
        mGetTicketsButton = (Button) fragmentView.findViewById(R.id.getTicketsButton);

        // Retrieve variables from the previous fragment
        mVenueId = getArguments().getString("GigVenueID");
        mGigId = getArguments().getString("GigID");

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.venueMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        mGetTicketsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DisplayTicketSelectionDialog();
            }
        });

        // Set the fragment title
        getActivity().setTitle("Gig Finder");

        return fragmentView;
    }

    // When the map is ready, call the SetupMap method
    @Override
    public void onMapReady(GoogleMap map)
    {
        mGoogleMap = map;

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
                mGoogleMap.addMarker(new MarkerOptions().position(mVenueLocation).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                // This zooms the map in to a reasonable level (12) and centers it on the location provided
                float zoomLevel = 8;
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mVenueLocation, zoomLevel));
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void PopulateFields()
    {
        mGigVenueTextView.setText(mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString());

        mGigNameTextView.setText(getArguments().getString("GigTitle"));
        mGigStartDate = getArguments().getString("GigStartDate");
        mGigEndDate = getArguments().getString("GigEndDate");
        mBandId = mSnapshot.child("Gigs/" + mGigId + "/bookedAct").getValue().toString();

        // If the band already has a youtube url stored against their profile append this to the text box and parse this
        // to load the video player
        if (mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").exists())
        {
            mYoutubeUrlEntered = mSnapshot.child("Bands/" + mBandId + "/youtubeUrl/").getValue().toString();

            mParsedYouTubeURL = ParseURL(mYoutubeUrlEntered);

            if(mParsedYouTubeURL != null)
            {
                LoadYoutubePlayer();
            }
        }

        mBandNameTextView.setText(mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString());

        // This takes the start and end dates and reformats them to look more visually appealing
        String formattedStartDateSectionOne = mGigStartDate.split(" ")[0];
        String formattedStartDateSectionTwo = mGigStartDate.split(" ")[1];
        String formattedStartDateSectionThree = mGigStartDate.split(" ")[2];
        String formattedStartDateSectionFour = mGigStartDate.split(" ")[3];

        String formattedFinishDateSectionOne = mGigEndDate.split(" ")[0];
        String formattedFinishDateSectionTwo = mGigEndDate.split(" ")[1];
        String formattedFinishDateSectionThree = mGigEndDate.split(" ")[2];
        String formattedFinishDateSectionFour = mGigEndDate.split(" ")[3];

        mGigStartDateTextView.setText(formattedStartDateSectionOne + " " + formattedStartDateSectionTwo + " " + formattedStartDateSectionThree + " " + formattedStartDateSectionFour);
        mGigEndDateTextView.setText(formattedFinishDateSectionOne + " " + formattedFinishDateSectionTwo + " " + formattedFinishDateSectionThree + " " + formattedFinishDateSectionFour);

        String userBaseLat = mSnapshot.child("Venues/" + mVenueId + "/venueLocation/latitude").getValue().toString();
        String userBaseLng = mSnapshot.child("Venues/" + mVenueId + "/venueLocation/longitude").getValue().toString();

        String latLng = userBaseLat + "," + userBaseLng;
        List<String> splitUserHomeLocation = Arrays.asList(latLng.split(","));

        double latitude = Double.parseDouble(splitUserHomeLocation.get(0));
        double longitude = Double.parseDouble(splitUserHomeLocation.get(1));

        mVenueLocation = new com.google.android.gms.maps.model.LatLng(latitude, longitude);

        LoadProfilePicture();
    }

    // This method loads the band's profile picture
    private void LoadProfilePicture()
    {
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
    }

    private void DisplayTicketSelectionDialog()
    {
        final Dialog ticketPickerDialog = new Dialog(getActivity());
        ticketPickerDialog.setTitle("Ticket Picker");
        ticketPickerDialog.setContentView(R.layout.ticket_purchase_dialog_layout);

        // This pulls the ticket cost and quantity set by the venue owner into the cost field
        final int ticketCost = Integer.parseInt(mSnapshot.child("Gigs/" + mGigId + "/ticketCost").getValue().toString());
        mTicketQuantityAvailable = Integer.parseInt(mSnapshot.child("Gigs/" + mGigId + "/ticketQuantity").getValue().toString());

        // Initialise dialog visual components
        TextView mGigNameTextView = (TextView) ticketPickerDialog.findViewById(R.id.gigNameTextView);
        Spinner mTicketQuantitySpinner = (Spinner) ticketPickerDialog.findViewById(R.id.ticketQuantitySpinner);
        final TextView mCostTotalTextView = (TextView) ticketPickerDialog.findViewById(R.id.costTotalTextView);
        Button mPurchaseButton = (Button) ticketPickerDialog.findViewById(R.id.purchaseButton);

        mGigNameTextView.setText(getArguments().getString("GigTitle"));

        mTicketQuantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                if(position == 0)
                {
                    mCostTotalTextView.setText("£" + ticketCost + ".00");
                    mTicketQuantity = 1;
                }

                else if(position == 1)
                {
                    mCostTotalTextView.setText("£" + ticketCost * 2 + ".00");
                    mTicketQuantity = 2;
                }

                else if(position == 2)
                {
                    mCostTotalTextView.setText("£" + ticketCost * 3 + ".00");
                    mTicketQuantity = 3;
                }

                else if(position == 3)
                {
                    mCostTotalTextView.setText("£" + ticketCost * 4 + ".00");
                    mTicketQuantity = 4;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        mPurchaseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // This dialog is created to confirm that the users want to purchase the tickets
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirm Purchase");
                builder.setMessage("Are you sure you wish to purchase these tickets?");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();

                        int gigAgeRestriction = Integer.parseInt(mSnapshot.child("Gigs/" + mGigId + "/ageRestriction").getValue().toString());

                        if(Integer.parseInt(mSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/age").getValue().toString()) >= gigAgeRestriction)
                        {
                            // If the user has selected more tickets than there are available then a message is displayed
                            if(mTicketQuantityAvailable < mTicketQuantity)
                            {
                                // This dialog is created to confirm that the users want to purchase the tickets
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You've selected more tickets than there are available! Please amend your order.");
                                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {

                                    }
                                });
                                builder.show();
                            }

                            else
                            {
                                // This creates a ticket object and posts it to the database under the generated push key
                                String ticketId = mDatabase.child("Tickets").push().getKey();
                                Ticket ticket = new Ticket(ticketId, mTicketQuantity, mGigId, "Valid");
                                mDatabase.child("Tickets/" + mGigId + "/" + mAuth.getCurrentUser().getUid() + "/").setValue(ticket);
                                mDatabase.child("Gigs/" + mGigId + "/ticketQuantity").setValue(mTicketQuantityAvailable - mTicketQuantity);

                                // This dialog is created to confirm that the users want to purchase the tickets
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Confirmation");
                                builder.setMessage("Tickets Purchased! To access your ticket please navigate to 'My Gigs' > View Ticket'.");
                                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        dialogInterface.dismiss();

                                        getActivity().finish();
                                        getActivity().overridePendingTransition(0,0);

                                        Intent intent = new Intent(getActivity(), FanUserMainActivity.class);
                                        startActivity(intent);

                                        getFragmentManager().popBackStackImmediate();
                                    }
                                });
                                builder.show();
                                builder.setCancelable(false);
                            }
                        }

                        else
                        {
                            // This dialog is displayed to explain that the user cannot purchase the tickets
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Error!");
                            builder.setMessage("You cannot purchase these tickets as you are under the age restriction set by the venue!");
                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {

                                }
                            });
                            builder.show();
                            builder.setCancelable(false);
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
        });
        ticketPickerDialog.show();
    }

    // If the youtube initialisation is successful load the URL from the text box if there is one
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored)
    {
        // Determines whether the player was restored from a saved state. If not cue the video
        if (!wasRestored)
        {
            youTubePlayer.cueVideo(mParsedYouTubeURL);
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

            if(parsedURL.length >= 3)
            {
                return parsedURL[3];
            }

            else
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

    @Override
    public void onInfoWindowClick(Marker marker)
    {

    }
}
