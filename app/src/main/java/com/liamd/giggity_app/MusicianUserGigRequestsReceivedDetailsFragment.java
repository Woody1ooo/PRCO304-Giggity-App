package com.liamd.giggity_app;


import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import me.everything.providers.android.calendar.CalendarProvider;

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
    private FirebaseAuth mAuth;

    // Declare general variables
    private String mGigId;
    private String mBandId;
    private String mVenueId;
    private String mGigStartDate;
    private String mGigEndDate;
    private com.google.android.gms.maps.model.LatLng mVenueLocation;
    private final static int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 1;
    private boolean hasPermission = true;

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

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

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
        String venueLat = mSnapshot.child("Venues/" + getArguments().getString("VenueID") + "/venueLocation/latitude").getValue().toString();
        String venueLng = mSnapshot.child("Venues/" + getArguments().getString("VenueID") + "/venueLocation/longitude").getValue().toString();

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
                mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + mGigId + "/gigID").setValue(mGigId);

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

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
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

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
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

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }
                }

                if(numberOfBandPositions.equals("5"))
                {
                    if(!mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionOneMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionTwoMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionThreeMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionFourMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }

                    if(!mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString().equals("Vacant"))
                    {
                        String bandMemberUserId = mSnapshot.child("Bands/" + mBandId + "/positionFiveMember").getValue().toString();

                        UserGigInformation info = new UserGigInformation();
                        info.setGigID(mGigId);
                        info.setCalendarEventID("Pending");

                        if(bandMemberUserId.equals(mAuth.getCurrentUser().getUid()))
                        {
                            info.setMemberConfirmedRequest("True");
                        }

                        else
                        {
                            info.setMemberConfirmedRequest("False");
                        }

                        mDatabase.child("UserGigInformation/" + bandMemberUserId + "/" + mGigId).setValue(info);
                    }
                }

                // A dialog is then shown to alert the user that the changes have been made
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setIcon(R.drawable.ic_event_available_black_24px);
                builder.setMessage("Request Accepted! Would you like to add this gig to your device calendar?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
                        {
                            requestPermissions(new String[]{android.Manifest.permission.WRITE_CALENDAR}, MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
                        }

                        if(hasPermission)
                        {
                            CalendarProvider provider = new CalendarProvider(getActivity());
                            List<me.everything.providers.android.calendar.Calendar> calendars = provider.getCalendars().getList();

                            // Insert Event
                            ContentResolver cr = getActivity().getContentResolver();
                            ContentValues values = new ContentValues();
                            TimeZone timeZone = TimeZone.getDefault();
                            values.put(CalendarContract.Events.DTSTART, Double.parseDouble(mSnapshot.child("Gigs/" + mGigId + "/startDate/time").getValue().toString()));
                            values.put(CalendarContract.Events.DTEND, Double.parseDouble(mSnapshot.child("Gigs/" + mGigId + "/endDate/time").getValue().toString()));
                            values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
                            values.put(CalendarContract.Events.TITLE, mGigNameTextView.getText().toString());
                            values.put(CalendarContract.Events.CALENDAR_ID, calendars.get(0).id);
                            values.put(CalendarContract.Events.EVENT_LOCATION, mVenueNameTextView.getText().toString());
                            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                            // get the event ID that is the last element in the Uri
                            final String eventID = uri.getLastPathSegment();

                            if(eventID != null)
                            {
                                // A dialog is then shown to alert the user that the changes have been made
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Confirmation");
                                builder.setIcon(R.drawable.ic_event_available_black_24px);
                                builder.setMessage("Calendar Event Added!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + mGigId + "/calendarEventID").setValue(eventID);
                                        mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + mGigId + "/gigID").setValue(mGigId);

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
                                builder.setTitle("Error");
                                builder.setIcon(R.drawable.ic_event_available_black_24px);
                                builder.setMessage("Calendar Event Could Not Be Added!");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + mGigId + "/gigID").setValue(mGigId);
                                        ReturnToRequests();
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + mGigId + "/calendarEventID").setValue("Null");
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

    // This method processes the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_CALENDAR)

            // If the permission has been accepted update hasPermission to reflect this
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.WRITE_CALENDAR) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                hasPermission = true;
            }

            // If the permission has been denied then display a message to that effect
            else
            {
                Toast.makeText(getActivity(), "If you wish use this feature," +
                        " please ensure you have given permission to access your device's calendar.", Toast.LENGTH_SHORT).show();

                hasPermission = false;
            }
    }

    private void ReturnToRequests()
    {
        // The user is then taken to the home fragment
        getFragmentManager().popBackStack();
    }
}
