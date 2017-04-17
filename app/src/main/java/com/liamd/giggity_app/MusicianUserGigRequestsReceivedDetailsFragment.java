package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigRequestsReceivedDetailsFragment extends Fragment implements OnMapReadyCallback
{

    // Declare visual components
    private TextView mGigNameTextView;
    private TextView mVenueNameTextView;
    private TextView mStartDateTextView;
    private TextView mEndDateTextView;
    private Button mAcceptRequestButton;
    private Button mRejectRequestButton;
    private MapView mMapView;
    private GoogleMap mGoogleMap;

    // Declare firebase variables
    private DatabaseReference mDatabase;
    private DataSnapshot mSnapshot;

    // Declare general variables
    private String mGigId;
    private String mBandId;
    private String mVenueId;
    private String mGigStartDate;
    private String mGigEndDate;
    private com.google.android.gms.maps.model.LatLng mVenueLocation;

    public MusicianUserGigRequestsReceivedDetailsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_gig_requests_received_details, container, false);

        // Initialise visual components
        mGigNameTextView = (TextView) fragmentView.findViewById(R.id.gigNameTextView);
        mVenueNameTextView = (TextView) fragmentView.findViewById(R.id.venueNameTextView);
        mStartDateTextView = (TextView) fragmentView.findViewById(R.id.gigStartDateTextView);
        mEndDateTextView = (TextView) fragmentView.findViewById(R.id.gigEndDateTextView);
        mAcceptRequestButton = (Button) fragmentView.findViewById(R.id.acceptButton);
        mRejectRequestButton = (Button) fragmentView.findViewById(R.id.rejectButton);

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.venueMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAcceptRequestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Accept();
            }
        });

        mRejectRequestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Reject();
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

                // This places a marker at the users chosen location
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(mVenueLocation)
                        .icon(BitmapDescriptorFactory.defaultMarker(HUE_RED)));

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
    }

    private void Accept()
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
                mDatabase.child("VenueSentGigRequests/" + mVenueId + "/" + mGigId + "/" + mBandId + "/requestStatus").setValue("Accepted");
                mDatabase.child("Gigs/" + mGigId + "/bookedAct").setValue(mBandId);

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
        builder.setTitle("Reject Gig Request");
        builder.setMessage("Are you sure you wish to reject this gig request?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                mDatabase.child("VenueSentGigRequests/" + mVenueId + "/" + mGigId + "/" + mBandId + "/requestStatus").setValue("Rejected");

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
