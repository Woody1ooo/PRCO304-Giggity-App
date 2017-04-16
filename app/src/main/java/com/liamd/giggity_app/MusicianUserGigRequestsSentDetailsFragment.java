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
import com.google.android.gms.maps.model.Marker;
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
public class MusicianUserGigRequestsSentDetailsFragment extends Fragment implements OnMapReadyCallback
{
    // Declare visual components
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

    // Declare general variables
    private String mGigId;
    private String mBandId;
    private String mGigStartDate;
    private String mGigEndDate;
    private com.google.android.gms.maps.model.LatLng mVenueLocation;


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
        mGigNameTextView.setText(getArguments().getString("GigName"));
        mVenueNameTextView.setText(getArguments().getString("VenueName"));
        mGigStartDate = getArguments().getString("GigStartDate");
        mGigEndDate = getArguments().getString("GigEndDate");
        mRequestStatusTextView.setText(getArguments().getString("RequestStatus"));

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
