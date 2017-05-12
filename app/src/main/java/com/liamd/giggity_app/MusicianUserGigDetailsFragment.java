package com.liamd.giggity_app;


import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.*;
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
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigDetailsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener
{
    // Declare visual components
    private CircleImageView mVenueImage;
    private TextView mGigNameTextView;
    private TextView mGigStartDateTextView;
    private TextView mGigEndDateTextView;
    private TextView mGigVenueTextView;
    private Button mApplyButton;
    private MapView mMapView;
    private GoogleMap mGoogleMap;

    // Declare general variables
    private String mVenueId;
    private String mBandId;
    private String mGigId;
    private com.google.android.gms.maps.model.LatLng mVenueLocation;
    private String mGigStartDate;
    private String mGigEndDate;
    private GoogleApiClient mGoogleApiClient;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DataSnapshot mSnapshot;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    public MusicianUserGigDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_gig_details, container, false);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // Initialise visual components
        mVenueImage = (CircleImageView) fragmentView.findViewById(R.id.venueImage);
        mGigNameTextView = (TextView) fragmentView.findViewById(R.id.gigNameTextView);
        mGigStartDateTextView = (TextView) fragmentView.findViewById(R.id.startDateTextView);
        mGigEndDateTextView = (TextView) fragmentView.findViewById(R.id.finishDateTextView);
        mGigVenueTextView = (TextView) fragmentView.findViewById(R.id.venueTextView);
        mApplyButton = (Button) fragmentView.findViewById(R.id.applyButton);

        // Retrieve variables from the previous fragment
        mVenueId = getArguments().getString("GigVenueID");
        mGigId = getArguments().getString("GigID");

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.venueMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        mApplyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Apply();
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
                float zoomLevel = 15;
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
        mBandId = mSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").getValue().toString();

        mGoogleApiClient = new GoogleApiClient.Builder(getContext()).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).build();
        mGoogleApiClient.connect();

        // This calls the method to load the photos, though it doesn't work at the moment...
        placePhotosAsync();
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {

    }

    private void Apply()
    {
        // This dialog is created to confirm that the users want to edit the fields
        // they have chosen
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Apply For Gig");
        builder.setMessage("Are you sure you wish to apply for this gig?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                boolean canCreateRequest = true;

                // If the user has already submitted a request for this gig then inform them and cancel the request
                if(mSnapshot.child("BandSentGigRequests/" + mBandId + "/" + mGigId).exists())
                {
                    RequestExistsDialog();
                }

                else
                {
                    int gigAgeRestriction = Integer.parseInt(mSnapshot.child("Venues/" + mVenueId + "/minimumPerformerAge").getValue().toString());
                    String numberOfBandPositions = mSnapshot.child("Bands/" + mBandId + "/numberOfPositions").getValue().toString();

                    if(numberOfBandPositions.equals("1"))
                    {
                        String positionOneUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                        if(Integer.parseInt(mSnapshot.child("Users/" + positionOneUserId + "/age").getValue().toString()) < gigAgeRestriction)
                        {
                            canCreateRequest = false;

                            // A dialog is then shown to alert the user that the request cannot be made
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Error!");
                            builder.setMessage("You cannot apply for this gig as one or more of your band members are under the age restriction placed on this gig!");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {

                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        }
                    }

                    else if(numberOfBandPositions.equals("2"))
                    {
                       String positionOneUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                       String positionTwoUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                        if(!positionOneUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionOneUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the request cannot be made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {

                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionTwoUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionTwoUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the request cannot be made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }
                    }

                    else if(numberOfBandPositions.equals("3"))
                    {
                        String positionOneUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                        String positionTwoUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();
                        String positionThreeUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                        if(!positionOneUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionOneUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the request cannot be made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {

                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionTwoUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionTwoUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the request cannot be made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionThreeUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionThreeUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the request cannot be made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }
                    }

                    else if(numberOfBandPositions.equals("4"))
                    {
                        String positionOneUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                        String positionTwoUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();
                        String positionThreeUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();
                        String positionFourUserId = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                        if(!positionOneUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionOneUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the request cannot be made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {

                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionTwoUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionTwoUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the request cannot be made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionThreeUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionThreeUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionFourUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionFourUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }
                    }

                    else if(numberOfBandPositions.equals("5"))
                    {
                        String positionOneUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                        String positionTwoUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();
                        String positionThreeUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();
                        String positionFourUserId = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();
                        String positionFiveUserId = mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString();

                        if(!positionOneUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionOneUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {

                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionTwoUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionTwoUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionThreeUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionThreeUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionFourUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionFourUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }

                        else if(!positionFiveUserId.equals("Vacant"))
                        {
                            if(Integer.parseInt(mSnapshot.child("Users/" + positionFiveUserId + "/age").getValue().toString()) < gigAgeRestriction)
                            {
                                canCreateRequest = false;

                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error!");
                                builder.setMessage("You cannot apply for this as one or more of your band members are under the age restriction placed by this gig!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }
                    }

                    if(canCreateRequest)
                    {
                        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).child("bandID").setValue(mBandId);
                        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).child("bandName").setValue(mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString());
                        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).child("venueName").setValue(mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString());
                        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).child("gigStartDate").setValue(mSnapshot.child("Gigs/" + mGigId + "/startDate").getValue());
                        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).child("gigEndDate").setValue(mSnapshot.child("Gigs/" + mGigId + "/endDate").getValue());
                        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).child("gigName").setValue(mSnapshot.child("Gigs/" + mGigId + "/title").getValue());
                        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).child("gigID").setValue(mGigId);
                        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).child("venueID").setValue(mVenueId);
                        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).child("requestStatus").setValue("Pending");

                        // This posts a notification to the database to be picked up by the user who submitted the request
                        String notificationID;
                        String bandName;

                        // Generate a notification ID from the database
                        notificationID = mDatabase.push().getKey();

                        // Get the band's name
                        bandName = mSnapshot.child("Bands/" + mBandId + "/name").getValue().toString();

                        Notification notification = new Notification(notificationID, bandName + " has requested to play at " + mGigNameTextView.getText().toString() + "!", "BandSentGigRequestPending");
                        mDatabase.child("Users/" + mSnapshot.child("Venues/" + mVenueId + "/userID").getValue().toString() + "/notifications/" + notificationID + "/").setValue(notification);

                        ConfirmDialog();
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
        builder.setMessage("Application Rejected! You already have a request for this gig.");
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
        getActivity().finish();
        getActivity().overridePendingTransition(0,0);

        Intent intent = new Intent(getActivity(), MusicianUserMainActivity.class);
        startActivity(intent);

        getFragmentManager().popBackStackImmediate();
    }

    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>()
    {
        @Override
        public void onResult(PlacePhotoResult placePhotoResult)
        {
            if (!placePhotoResult.getStatus().isSuccess())
            {
                return;
            }

            mVenueImage.setImageBitmap(placePhotoResult.getBitmap());
        }
    };

    /**
     * Load a bitmap from the photos API asynchronously
     * by using buffers and result callbacks.
     */
    private void placePhotosAsync()
    {
        final String placeId = mVenueId; // Australian Cruise Group
        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>()
                {
                    @Override
                    public void onResult(PlacePhotoMetadataResult photos)
                    {
                        if (!photos.getStatus().isSuccess())
                        {
                            return;
                        }

                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                        if (photoMetadataBuffer.getCount() > 0)
                        {
                            // Display the first bitmap in an ImageView in the size of the view
                            photoMetadataBuffer.get(0)
                                    .getScaledPhoto(mGoogleApiClient, mVenueImage.getWidth(),
                                            mVenueImage.getHeight())
                                    .setResultCallback(mDisplayPhotoResultCallback);
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }
}
