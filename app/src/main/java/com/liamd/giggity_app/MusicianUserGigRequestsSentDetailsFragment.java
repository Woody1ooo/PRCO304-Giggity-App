package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigRequestsSentDetailsFragment extends Fragment implements OnMapReadyCallback
{
    // Declare visual components
    private CircleImageView mVenueImage;
    private TextView mGigNameTextView;
    private TextView mVenueNameTextView;
    private TextView mStartDateTextView;
    private TextView mEndDateTextView;
    private TextView mRequestStatusTextView;
    private Button mCancelRequestButton;
    private MapView mMapView;
    private GoogleMap mGoogleMap;

    // Declare firebase variables
    private DatabaseReference mDatabase;
    private DataSnapshot mSnapshot;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;

    // Declare general variables
    private String mGigId;
    private String mVenueId;
    private String mBandId;
    private String mGigStartDate;
    private String mGigEndDate;
    private com.google.android.gms.maps.model.LatLng mVenueLocation;
    private String mRequestStatus;
    private GoogleApiClient mGoogleApiClient;

    public MusicianUserGigRequestsSentDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_gig_requests_sent_details, container, false);

        // Initialise visual components
        mVenueImage = (CircleImageView) fragmentView.findViewById(R.id.venueImage);
        mGigNameTextView = (TextView) fragmentView.findViewById(R.id.gigNameTextView);
        mVenueNameTextView = (TextView) fragmentView.findViewById(R.id.venueNameTextView);
        mStartDateTextView = (TextView) fragmentView.findViewById(R.id.gigStartDateTextView);
        mEndDateTextView = (TextView) fragmentView.findViewById(R.id.gigEndDateTextView);
        mRequestStatusTextView = (TextView) fragmentView.findViewById(R.id.requestStatusTextView);
        mCancelRequestButton = (Button) fragmentView.findViewById(R.id.cancelRequestButton);

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.venueMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        // When the cancel button is clicked this fires the cancel request method to delete this request from the database
        mCancelRequestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CancelRequest();
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

    private void SetupMap()
    {
        mDatabase.child("Venues/").addListenerForSingleValueEvent(new ValueEventListener()
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

    // This method populates the visual components with the relevant data
    private void PopulateFields()
    {
        // Retrieve data from previous fragment and display it where required
        mGigId = getArguments().getString("GigID");
        mBandId = getArguments().getString("BandID");
        mVenueId = getArguments().getString("VenueID");
        mGigNameTextView.setText(getArguments().getString("GigName"));
        mVenueNameTextView.setText(getArguments().getString("VenueName"));
        mGigStartDate = getArguments().getString("GigStartDate");
        mGigEndDate = getArguments().getString("GigEndDate");
        mRequestStatusTextView.setText(getArguments().getString("RequestStatus"));

        mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId + "/requestStatus").addValueEventListener(new ValueEventListener()
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

        // These blocks set the colour of the text depending on the status
        if(mRequestStatusTextView.getText().equals("Pending"))
        {
            mRequestStatusTextView.setTextColor(Color.parseColor("#ff6100"));
        }

        else if(mRequestStatusTextView.getText().equals("Accepted"))
        {
            mRequestStatusTextView.setTextColor(Color.GREEN);
        }

        else if(mRequestStatusTextView.getText().equals("Rejected"))
        {
            mRequestStatusTextView.setTextColor(Color.RED);
        }

        // This takes the start and end dates and reformats them to look more visually appealing
        String formattedStartDateSectionOne = mGigStartDate.split(" ")[0];
        String formattedStartDateSectionTwo = mGigStartDate.split(" ")[1];
        String formattedStartDateSectionThree = mGigStartDate.split(" ")[2];
        String formattedStartDateSectionFour = mGigStartDate.split(" ")[3];

        String formattedFinishDateSectionOne = mGigEndDate.split(" ")[0];
        String formattedFinishDateSectionTwo = mGigEndDate.split(" ")[1];
        String formattedFinishDateSectionThree = mGigEndDate.split(" ")[2];
        String formattedFinishDateSectionFour = mGigEndDate.split(" ")[3];

        mStartDateTextView.setText(formattedStartDateSectionOne + " " + formattedStartDateSectionTwo + " " + formattedStartDateSectionThree + " " + formattedStartDateSectionFour);
        mEndDateTextView.setText(formattedFinishDateSectionOne + " " + formattedFinishDateSectionTwo + " " + formattedFinishDateSectionThree + " " + formattedFinishDateSectionFour);

        // This block then retrieves the lat lng from the database and assigns it to the mVenueLocation variable
        String venueLat = mSnapshot.child(getArguments().getString("VenueID") + "/venueLocation/latitude").getValue().toString();
        String venueLng = mSnapshot.child(getArguments().getString("VenueID") + "/venueLocation/longitude").getValue().toString();

        String latLng = venueLat + "," + venueLng;
        List<String> splitUserHomeLocation = Arrays.asList(latLng.split(","));

        double latitude = Double.parseDouble(splitUserHomeLocation.get(0));
        double longitude = Double.parseDouble(splitUserHomeLocation.get(1));

        mVenueLocation = new com.google.android.gms.maps.model.LatLng(latitude, longitude);

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
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mVenueImage);
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

    private void CancelRequest()
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cancel Gig Request");
        builder.setMessage("Are you sure you wish to withdraw your request for your band to play this gig?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(mRequestStatus.equals("Pending"))
                {
                    mDatabase.child("BandSentGigRequests/" + mBandId + "/" + mGigId).removeValue();

                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Confirmation");
                    builder.setMessage("Request Deleted!");
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

    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback = new ResultCallback<PlacePhotoResult>()
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
        final String placeId = mVenueId;
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
                                            .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(500, 500).into(mVenueImage);
                                }

                                // If the venue doesn't have an image the default image is loaded
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(500, 500).into(mVenueImage);
                                }
                            });
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }
}
