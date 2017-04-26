package com.liamd.giggity_app;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Arrays;
import java.util.List;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserViewGigsDetailsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener
{
    // Declare visual components
    private TextView mGigNameTextView;
    private TextView mGigStartDateTextView;
    private TextView mGigEndDateTextView;
    private TextView mGigVenueTextView;
    private Button mCancelButton;
    private Button mViewTicketButton;
    private MapView mMapView;
    private GoogleMap mGoogleMap;

    // Declare general variables
    private String mVenueId;
    private String mBandId;
    private String mGigId;
    private com.google.android.gms.maps.model.LatLng mVenueLocation;
    private String mGigStartDate;
    private String mGigEndDate;
    private boolean mIsFanAccount;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DataSnapshot mSnapshot;

    public MusicianUserViewGigsDetailsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_view_gigs_details, container, false);

        if(getArguments().getString("UserType").equals("Fan"))
        {
            mIsFanAccount = true;
        }

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        mGigNameTextView = (TextView) fragmentView.findViewById(R.id.gigNameTextView);
        mGigStartDateTextView = (TextView) fragmentView.findViewById(R.id.startDateTextView);
        mGigEndDateTextView = (TextView) fragmentView.findViewById(R.id.finishDateTextView);
        mGigVenueTextView = (TextView) fragmentView.findViewById(R.id.venueTextView);
        mCancelButton = (Button) fragmentView.findViewById(R.id.cancelButton);
        mViewTicketButton = (Button) fragmentView.findViewById(R.id.viewTicketButton);

        if(!mIsFanAccount)
        {
            mViewTicketButton.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.VISIBLE);
        }

        else
        {
            mViewTicketButton.setVisibility(View.VISIBLE);
            mCancelButton.setVisibility(View.GONE);
        }

        // Retrieve variables from the previous fragment
        mVenueId = getArguments().getString("GigVenueID");
        mGigId = getArguments().getString("GigID");

        // Initialise the map
        mMapView = (MapView) fragmentView.findViewById(R.id.venueMapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        mCancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Cancel();
            }
        });

        mViewTicketButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ViewTicket();
            }
        });

        // Set the fragment title
        getActivity().setTitle("My Gigs");

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

        if(!mIsFanAccount)
        {
            mBandId = mSnapshot.child("Users/" + mAuth.getCurrentUser().getUid() + "/bandID").getValue().toString();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {

    }

    private void Cancel()
    {
        // This dialog is created to confirm that the users want to edit the fields
        // they have chosen
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cancel Gig Position");
        builder.setMessage("Are you sure you wish to cancel your band's position at this gig?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String eventId = mSnapshot.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + mGigId + "/calendarEventID").getValue().toString();

                // If the event id exists then it should be deleted from the user's calendar
                if(!eventId.equals("Pending") && !eventId.equals("Null"))
                {
                    Uri deleteUri;
                    deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(eventId));
                    int rows = getActivity().getContentResolver().delete(deleteUri, null, null);

                    // If no rows were removed then this means there was an error
                    if(rows <= 0)
                    {
                        // A dialog is then shown to alert the user that the changes have been made
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Error!");
                        builder.setMessage("Calendar event could not be deleted! To correct this problem, you will need to delete this event manually.");
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

                // Remove this entry in user gig information once the calendar event has been handled
                mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + mGigId).removeValue();
                mDatabase.child("Gigs/" + mGigId + "/bookedAct").setValue("Vacant");

                String positionOneMember = "Vacant";
                String positionTwoMember = "Vacant";
                String positionThreeMember = "Vacant";
                String positionFourMember = "Vacant";
                String positionFiveMember = "Vacant";

                if(mSnapshot.child("Bands/" + mBandId + "/positionOneMember").exists())
                {
                    positionOneMember = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();
                }

                if(mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").exists())
                {
                    positionTwoMember = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();
                }

                if(mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").exists())
                {
                    positionThreeMember = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();
                }

                if(mSnapshot.child("Bands/" + mBandId + "/positionFourMember").exists())
                {
                    positionFourMember = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();
                }

                if(mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").exists())
                {
                    positionFiveMember = mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString();
                }


                if(!positionOneMember.equals("Vacant") && !positionOneMember.equals(mAuth.getCurrentUser().getUid()))
                {
                    mDatabase.child("UserGigInformation/" + positionOneMember + "/" + mGigId + "/gigID").setValue("BandCancelled");
                }

                if(!positionTwoMember.equals("Vacant") && !positionTwoMember.equals(mAuth.getCurrentUser().getUid()))
                {
                    mDatabase.child("UserGigInformation/" + positionTwoMember + "/" + mGigId + "/gigID").setValue("BandCancelled");
                }

                if(!positionThreeMember.equals("Vacant") && !positionThreeMember.equals(mAuth.getCurrentUser().getUid()))
                {
                    mDatabase.child("UserGigInformation/" + positionThreeMember + "/" + mGigId + "/gigID").setValue("BandCancelled");
                }

                if(!positionFourMember.equals("Vacant") && !positionFourMember.equals(mAuth.getCurrentUser().getUid()))
                {
                    mDatabase.child("UserGigInformation/" + positionFourMember + "/" + mGigId + "/gigID").setValue("BandCancelled");
                }

                if(!positionFiveMember.equals("Vacant") && !positionFiveMember.equals(mAuth.getCurrentUser().getUid()))
                {
                    mDatabase.child("UserGigInformation/" + positionFiveMember + "/" + mGigId + "/gigID").setValue("BandCancelled");
                }

                ConfirmDialog();
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

    private void ViewTicket()
    {
        final Dialog viewTicketDialog = new Dialog(getActivity());
        viewTicketDialog.setContentView(R.layout.ticket_display_dialog_layout);

        String ticketId = mSnapshot.child("Tickets/" + mGigId + "/" + mAuth.getCurrentUser().getUid() + "/ticketId").getValue().toString();
        int admissionQuantity = Integer.parseInt(mSnapshot.child("Tickets/" + mGigId + "/" + mAuth.getCurrentUser().getUid() + "/admissionQuantity").getValue().toString());

        // Initialise dialog visual components
        ImageView mTicketImageView = (ImageView) viewTicketDialog.findViewById(R.id.ticketImageView);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try
        {
            BitMatrix bitMatrix = multiFormatWriter.encode(ticketId + "\n" + admissionQuantity, BarcodeFormat.QR_CODE, 750, 750);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            mTicketImageView.setImageBitmap(bitmap);
        }

        catch (WriterException e)
        {
            e.printStackTrace();
        }

        viewTicketDialog.show();
    }

    private void ConfirmDialog()
    {
        // A dialog is then shown to alert the user that the changes have been made
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirmation");
        builder.setMessage("Gig Position Cancelled!");
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
}
