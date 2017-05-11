package com.liamd.giggity_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.login.LoginManager;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;

import static com.liamd.giggity_app.R.layout.musician_user_activity_main;

public class MusicianUserMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerProfilePictureUpdater
{
    // Declare visual components
    private CircleImageView profileImageView;
    private TextView navigationProfileEmailTextView;
    private DrawerLayout drawer;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;
    private DataSnapshot mSnapshot;

    // Declare general variables
    private String mUserEmail;
    private final static int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 1;
    private boolean hasPermission;
    private ArrayList<UserGigInformation> mListOfUserGigInformation = new ArrayList<>();
    private String mGigId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(musician_user_activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // When the app is loaded this service is started
        startService(new Intent(this, NotificationService.class));

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference();

        if(savedInstanceState == null)
        {
            // Hides the manager item by default
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_band_manager).setVisible(false);
            menu.findItem(R.id.nav_band_members).setVisible(false);
            menu.findItem(R.id.nav_requests_band).setVisible(false);
        }

        // If the network is unavailable display the dialog to prevent unauthorised navigation drawer selections
        if(!IsNetworkAvailable())
        {
            DisplayNetworkAlertDialog();
        }

        else
        {
            // At the database reference "Users/%logged in user id%/hasCompletedSetup", a check is made
            // to see if the value is true or false.
            // If the user hasn't completed the account setup yet (i.e. hasCompletedSetup = false)
            // load the setup activity on startup
            mDatabase.child("Users/").addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(mAuth.getCurrentUser() != null)
                    {
                        // Check if the node exists then determine whether they are in a band already to show/hide items
                        if(dataSnapshot.child(mAuth.getCurrentUser().getUid() + "/inBand").exists())
                        {
                            if(dataSnapshot.child(mAuth.getCurrentUser().getUid() + "/inBand").getValue().equals(true))
                            {
                                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                                Menu menu = navigationView.getMenu();
                                menu.findItem(R.id.nav_my_gigs).setVisible(true);
                                menu.findItem(R.id.nav_requests_gig).setVisible(true);
                                menu.findItem(R.id.nav_band_manager).setVisible(true);
                                menu.findItem(R.id.nav_band_members).setVisible(true);
                                menu.findItem(R.id.nav_requests_band).setVisible(true);
                                menu.findItem(R.id.nav_band_creator).setVisible(false);
                                menu.findItem(R.id.nav_band_finder).setVisible(false);
                                menu.findItem(R.id.nav_requests_musician).setVisible(false);
                            }

                            else
                            {
                                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                                Menu menu = navigationView.getMenu();
                                menu.findItem(R.id.nav_my_gigs).setVisible(false);
                                menu.findItem(R.id.nav_requests_gig).setVisible(false);
                                menu.findItem(R.id.nav_band_manager).setVisible(false);
                                menu.findItem(R.id.nav_band_members).setVisible(false);
                                menu.findItem(R.id.nav_requests_band).setVisible(false);
                                menu.findItem(R.id.nav_band_creator).setVisible(true);
                                menu.findItem(R.id.nav_band_finder).setVisible(true);
                                menu.findItem(R.id.nav_requests_musician).setVisible(true);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

        mDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(mAuth.getCurrentUser() != null && dataSnapshot.child("UserGigInformation/" + mAuth.getCurrentUser().getUid()).exists())
                {
                    Iterable<DataSnapshot> children = dataSnapshot.child("UserGigInformation/" + mAuth.getCurrentUser().getUid()).getChildren();
                    for (DataSnapshot child : children)
                    {
                        UserGigInformation userGigInformation;
                        userGigInformation = child.getValue(UserGigInformation.class);
                        if(userGigInformation.getCalendarEventID() != null
                                && userGigInformation.getMemberConfirmedRequest() != null
                                && userGigInformation.getCalendarEventID().equals("Pending")
                                && userGigInformation.getMemberConfirmedRequest().equals("False"))
                        {
                            mListOfUserGigInformation.add(userGigInformation);
                        }

                        else if(userGigInformation.getGigID().equals("BandCancelled"))
                        {
                            RemoveEventsFromCalendar(userGigInformation, child.getKey());
                        }
                    }
                }

                if(mListOfUserGigInformation.size() > 0)
                {
                    mSnapshot = dataSnapshot;
                    AddEventsToCalendar(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });


        // If this is the first time the activity is opened load the home fragment by default
        if(savedInstanceState == null)
        {
            // Sets home as the default selected navigation item
            navigationView.getMenu().getItem(0).setChecked(true);

            setTitle("Home");
            MusicianUserHomeFragment fragment = new MusicianUserHomeFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserHomeFragment");
            fragmentTransaction.commit();
        }

        String fragmentToOpen = getIntent().getStringExtra("FragmentToOpenExtra");

        if(fragmentToOpen != null)
        {
            if(fragmentToOpen.equals("MusicianUserBandRequestsInBandFragment"))
            {
                // This ensures that whenever the back button is pressed there is never a blank home screen shown
                setTitle("Band Requests");
                MusicianUserBandRequestsInBandFragment fragment = new MusicianUserBandRequestsInBandFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment
                        , "MusicianUserBandRequestsInBandFragment");
                fragmentTransaction.commit();
            }

            else if(fragmentToOpen.equals("MusicianUserBandRequestsNotInBandFragment"))
            {
                // This ensures that whenever the back button is pressed there is never a blank home screen shown
                setTitle("Band Requests");
                MusicianUserBandRequestsNotInBandFragment fragment = new MusicianUserBandRequestsNotInBandFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment
                        , "MusicianUserBandRequestsNotInBandFragment");
                fragmentTransaction.commit();
            }

            else if(fragmentToOpen.equals("MusicianUserGigRequestsFragment"))
            {
                // This ensures that whenever the back button is pressed there is never a blank home screen shown
                setTitle("Gig Requests");
                MusicianUserGigRequestsFragment fragment = new MusicianUserGigRequestsFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment
                        , "MusicianUserGigRequestsFragment");
                fragmentTransaction.commit();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }

        else
        {
            super.onBackPressed();
        }

        // If the back button is pressed when the home fragment is visible the navigation view and title are set to this to prevent the wrong navigation options/title from displaying
        MusicianUserHomeFragment homeFragment = (MusicianUserHomeFragment) getFragmentManager().findFragmentByTag("MusicianUserHomeFragment");
        if(homeFragment != null && homeFragment.isVisible())
        {
            final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.getMenu().getItem(0).setChecked(true);
            setTitle("Home");
        }

        // This ensures that home is the fragment that should be returned to
        if(getFragmentManager().getBackStackEntryCount() == 0)
        {
            // This ensures that whenever the back button is pressed there is never a blank home screen shown
            setTitle("Home");
            MusicianUserHomeFragment fragment = new MusicianUserHomeFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserHomeFragment");
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the musician_user_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.musician_user_main, menu);

        // Calls the method to populate the drawer with the user data
        NavigationDrawerUserData();

        return true;
    }


    // Method to determine the selected musician_user_menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // If sign out is selected, the user is signed out and the StartLoginActivity method
            // is called
            case R.id.action_settings:
                Logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Each time a navigation item is selected this clears the users previous path as they are now entering a different section
        if (id == R.id.nav_home)
        {
            //ClearBackStack(this);

            getFragmentManager().popBackStackImmediate();

            setTitle("Home");
            MusicianUserHomeFragment fragment = new MusicianUserHomeFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserHomeFragment");
            fragmentTransaction.commit();
        }

        else if (id == R.id.nav_profile)
        {
            ClearBackStack(this);
            //getFragmentManager().popBackStackImmediate();

            setTitle("My Musician Profile");
            MusicianUserProfileFragment fragment = new MusicianUserProfileFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserProfileFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_gig_finder)
        {
            ClearBackStack(this);
            //getFragmentManager().popBackStackImmediate();

            setTitle("Gig Finder");
            MusicianUserGigFinderFragment fragment = new MusicianUserGigFinderFragment();
            Bundle arguments = new Bundle();

            // This variable determines the path the app takes as it looks at the type of account the user holds
            arguments.putString("UserType", "Musician");
            fragment.setArguments(arguments);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserGigFinderFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_my_gigs)
        {
            ClearBackStack(this);
            //getFragmentManager().popBackStackImmediate();

            setTitle("My Gigs");
            MusicianUserViewGigsFragment fragment = new MusicianUserViewGigsFragment();
            Bundle arguments = new Bundle();

            // This variable determines the path the app takes as it looks at the type of account the user holds
            arguments.putString("UserType", "Musician");
            fragment.setArguments(arguments);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserViewGigsFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_requests_gig)
        {
            //ClearBackStack(this);
            getFragmentManager().popBackStackImmediate();

            setTitle("Gig Requests");
            MusicianUserGigRequestsFragment fragment = new MusicianUserGigRequestsFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserGigRequestsFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_band_finder)
        {
            //ClearBackStack(this);
            getFragmentManager().popBackStackImmediate();

            setTitle("Band Finder");
            MusicianUserBandFinderFragment fragment = new MusicianUserBandFinderFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserBandFinderFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_band_creator)
        {
            //ClearBackStack(this);
            getFragmentManager().popBackStackImmediate();

            setTitle("Band Creator");
            MusicianUserBandCreatorFragment fragment = new MusicianUserBandCreatorFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserBandCreatorFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_requests_musician)
        {
            //ClearBackStack(this);
            getFragmentManager().popBackStackImmediate();

            setTitle("Band Requests");
            MusicianUserBandRequestsNotInBandFragment fragment = new MusicianUserBandRequestsNotInBandFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserBandRequestsNotInBandFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_requests_band)
        {
            //ClearBackStack(this);
            getFragmentManager().popBackStackImmediate();

            setTitle("Band Requests");
            MusicianUserBandRequestsInBandFragment fragment = new MusicianUserBandRequestsInBandFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserBandRequestsInBandFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_band_members)
        {
            //ClearBackStack(this);
            getFragmentManager().popBackStackImmediate();

            setTitle("Band Members");
            MusicianUserBandMembersFragment fragment = new MusicianUserBandMembersFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserBandMembersFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_settings)
        {
            //ClearBackStack(this);
            getFragmentManager().popBackStackImmediate();

            setTitle("Settings");
            SettingsFragment fragment = new SettingsFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "SettingsFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_band_manager)
        {
            //ClearBackStack(this);
            getFragmentManager().popBackStackImmediate();

            setTitle("Band Manager");
            MusicianUserBandManagementFragment fragment = new MusicianUserBandManagementFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserBandManagementFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_logout)
        {
            Logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Populates the user specific data in the navigation drawer (profile image and email)
    // The image view needs to be initialised here as onCreate doesn't draw the drawer
    private void NavigationDrawerUserData()
    {
        // Initialise visual components
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        profileImageView = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.headerProfileImage);
        navigationProfileEmailTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userEmailTextView);

        mProfileImageReference.child("ProfileImages/" + mAuth.getCurrentUser().getUid() + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load
                        (mProfileImageReference.child("ProfileImages/" + mAuth.getCurrentUser().getUid() + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(220, 220).into(profileImageView);
            }

        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Picasso.with(getApplicationContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(220, 220).into(profileImageView);
            }
        });

        mUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        navigationProfileEmailTextView.setText(mUserEmail);
    }

    private void Logout()
    {
        // Stop the notification service when the user logs out
        stopService(new Intent(this.getBaseContext(), NotificationService.class));

        // Will log the user out of Gmail or email/password login
        FirebaseAuth.getInstance().signOut();

        // Will log the user out of facebook
        LoginManager.getInstance().logOut();

        finish();

        if(!isFinishing())
        {
            finish();
        }

        // Returns to the login activity
        Intent returnToLoginActivity= new Intent(MusicianUserMainActivity.this, LoginActivity.class);
        returnToLoginActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(returnToLoginActivity);
    }

    // This method processes the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_CALENDAR)

            // If the permission has been accepted update hasPermission to reflect this
            if (permissions.length == 1 &&
                    permissions[0].equals(android.Manifest.permission.WRITE_CALENDAR) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                hasPermission = true;

                AddEventsToCalendar(mSnapshot);
            }

            // If the permission has been denied then display a message to that effect
            else
            {
                Toast.makeText(getApplicationContext(), "If you wish use this feature," +
                        " please ensure you have given permission to access your device's calendar.", Toast.LENGTH_SHORT).show();

                hasPermission = false;
            }
    }

    // This clears the back stack of fragments and adds a home fragment
    private static void ClearBackStack(Activity activity)
    {
        FragmentManager fragmentManager = activity.getFragmentManager();
        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i)
        {
            fragmentManager.popBackStack();
        }

        MusicianUserHomeFragment fragment = new MusicianUserHomeFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment
                , "MusicianUserHomeFragment");
        fragmentTransaction.commit();
    }

    @Override
    public void UpdateDrawerProfilePicture()
    {
        NavigationDrawerUserData();
    }

    private void RemoveEventsFromCalendar(UserGigInformation cancelledGig, String gigId)
    {
        // Remove Event
        if(cancelledGig.getGigID().equals("BandCancelled"))
        {
            if (ActivityCompat.checkSelfPermission(MusicianUserMainActivity.this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_CALENDAR}, MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
            }

            if (hasPermission)
            {
                Uri deleteUri;
                deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(cancelledGig.getCalendarEventID()));
                int rowsRemoved = this.getContentResolver().delete(deleteUri, null, null);

                // The database is then updated to remove the gig entry
                mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + gigId).removeValue();

                if(rowsRemoved > 0)
                {
                    // A dialog is then shown to alert the user that the changes have been made
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MusicianUserMainActivity.this);
                    builder.setTitle("Alert");
                    builder.setMessage("Your band has pulled out of a gig you were scheduled to play! We have therefore removed this event from your devices calendar for you.");
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
        }
    }

    private void AddEventsToCalendar(final DataSnapshot dataSnapshot)
    {
        // This dialog is created to confirm that the user wants to edit the chosen fields
        final AlertDialog.Builder builder = new AlertDialog.Builder(MusicianUserMainActivity.this);
        builder.setTitle("Gig Request Accepted");

        if(mListOfUserGigInformation.size() > 1)
        {
            builder.setMessage("It looks as though a member of your band has accepted multiple gig requests! Would you like these gigs to be added to your device calendar?");
        }
        else
        {
            builder.setMessage("It looks as though a member of your band has accepted a gig request! Would you like this gig to be added to your device calendar?");
        }

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if (ActivityCompat.checkSelfPermission(MusicianUserMainActivity.this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_CALENDAR}, MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
                }

                if (hasPermission)
                {
                    for (UserGigInformation element : mListOfUserGigInformation)
                    {
                        final UserGigInformation info = new UserGigInformation();
                        info.setCalendarEventID(element.getCalendarEventID());
                        info.setGigID(element.getGigID());

                        CalendarProvider provider = new CalendarProvider(MusicianUserMainActivity.this);
                        List<Calendar> calendars = provider.getCalendars().getList();

                        mGigId = info.getGigID();
                        String venueId = dataSnapshot.child("Gigs/" + mGigId + "/venueID").getValue().toString();

                        // Insert Event
                        ContentResolver cr = MusicianUserMainActivity.this.getContentResolver();
                        ContentValues values = new ContentValues();
                        TimeZone timeZone = TimeZone.getDefault();
                        values.put(CalendarContract.Events.DTSTART, Double.parseDouble(dataSnapshot.child("Gigs/" + mGigId + "/startDate/time").getValue().toString()));
                        values.put(CalendarContract.Events.DTEND, Double.parseDouble(dataSnapshot.child("Gigs/" + mGigId + "/endDate/time").getValue().toString()));
                        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
                        values.put(CalendarContract.Events.TITLE, dataSnapshot.child("Gigs/" + mGigId + "/title").getValue().toString());
                        if(calendars.size() <= 0)
                        {
                            Toast.makeText(MusicianUserMainActivity.this, "Please ensure you have at least one device calendar setup!", Toast.LENGTH_SHORT).show();
                        }

                        else
                        {
                            values.put(CalendarContract.Events.CALENDAR_ID, calendars.get(0).id);
                            values.put(CalendarContract.Events.EVENT_LOCATION, dataSnapshot.child("Venues/" + venueId + "/name").getValue().toString());
                            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                            // get the event ID that is the last element in the Uri
                            final String eventID = uri.getLastPathSegment();

                            // The database is then updated with the calendar event ID
                            mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + mGigId + "/calendarEventID").setValue(eventID);
                        }
                    }

                    // The list is then cleared to prevent duplicates
                    mListOfUserGigInformation.clear();

                    dialogInterface.dismiss();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                for (UserGigInformation element : mListOfUserGigInformation)
                {
                    final UserGigInformation info = new UserGigInformation();
                    info.setCalendarEventID(element.getCalendarEventID());
                    info.setGigID(element.getGigID());

                    // If this is declined the field is still updated to prevent asking the user again in future
                    mDatabase.child("UserGigInformation/" + mAuth.getCurrentUser().getUid() + "/" + info.getGigID() + "/calendarEventID").setValue("Null");
                }

                // The list is then cleared to prevent duplicates
                mListOfUserGigInformation.clear();
                dialogInterface.dismiss();
            }
        });

        if(getApplicationContext() != null)
        {
            //builder.show();
            //builder.setCancelable(false);
        }
    }

    // This method checks whether an internet connection is present
    private boolean IsNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // If there is no network connection an undismissable dialog is displayed as at least one database read is required to check which navigation options to display
    // Once the connection is restored the user can continue
    private void DisplayNetworkAlertDialog()
    {
        // A dialog is then shown to alert the user that the changes have been made
        final AlertDialog.Builder builder = new AlertDialog.Builder(MusicianUserMainActivity.this);
        builder.setTitle("Error!");
        builder.setMessage("It seems you've lost your internet connection! Some parts of Giggity require this to function correctly. When your connection is restored you will be able to continue.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(!IsNetworkAvailable())
                {
                    DisplayNetworkAlertDialog();
                }

                else
                {
                    dialogInterface.dismiss();
                    finish();
                    startActivity(getIntent());
                }
            }
        });
        builder.show();
        builder.setCancelable(false);
    }
}
