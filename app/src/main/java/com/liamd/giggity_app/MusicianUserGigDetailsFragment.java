package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserGigDetailsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener
{
    // Declare visual components
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

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DataSnapshot mSnapshot;

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

        // Initialise visual components
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

    private void PopulateFields()
    {
        mGigVenueTextView.setText(mSnapshot.child("Venues/" + mVenueId + "/name").getValue().toString());

        mGigNameTextView.setText(getArguments().getString("GigTitle"));
        mGigStartDateTextView.setText(getArguments().getString("GigStartDate"));
        mGigEndDateTextView.setText(getArguments().getString("GigEndDate"));

        String userBaseLat = mSnapshot.child("Venues/" + mVenueId + "/venueLocation/latitude").getValue().toString();
        String userBaseLng = mSnapshot.child("Venues/" + mVenueId + "/venueLocation/longitude").getValue().toString();

        String latLng = userBaseLat + "," + userBaseLng;
        List<String> splitUserHomeLocation = Arrays.asList(latLng.split(","));

        double latitude = Double.parseDouble(splitUserHomeLocation.get(0));
        double longitude = Double.parseDouble(splitUserHomeLocation.get(1));

        mVenueLocation = new com.google.android.gms.maps.model.LatLng(latitude, longitude);
        mBandId = mSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").getValue().toString();
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
                // If the user has already submitted a request for this gig then inform them and cancel the request
                if(mSnapshot.child("MusicianSentGigRequests/" + mBandId + "/" + mGigId).exists())
                {
                    RequestExistsDialog();
                }

                else
                {
                    mDatabase.child("MusicianSentGigRequests/" + mBandId + "/" + mGigId).child("bandID").setValue(mBandId);
                    mDatabase.child("MusicianSentGigRequests/" + mBandId + "/" + mGigId).child("gigID").setValue(mGigId);
                    mDatabase.child("MusicianSentGigRequests/" + mBandId + "/" + mGigId).child("venueID").setValue(mVenueId);
                    mDatabase.child("MusicianSentGigRequests/" + mBandId + "/" + mGigId).child("requestStatus").setValue("Pending");
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
        // The user is then taken to the home fragment
        getActivity().setTitle("Home");
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frame, new MusicianUserHomeFragment(), "MusicianUserHomeFragment");
        ft.commit();
    }
}
