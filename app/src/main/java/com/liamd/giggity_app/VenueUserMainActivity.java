package com.liamd.giggity_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class VenueUserMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerProfilePictureUpdater
{
    // Declare visual components
    private CircleImageView mProfileImageView;
    private TextView mNavigationProfileEmailTextView;

    // Declare firebase variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mVenueImageReference;

    // Declare general variables
    private String mVenueId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_user_activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // If the network is unavailable display the dialog to prevent unauthorised navigation drawer selections
        if(!IsNetworkAvailable())
        {
            DisplayNetworkAlertDialog();
        }

        else
        {
            // Data snapshot to retrieve the venue ID
            mDatabase.child("Users/" + mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    mVenueId = dataSnapshot.child("venueID").getValue().toString();
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mVenueImageReference = mStorage.getReference();

        // When the app is loaded this service is started
        startService(new Intent(this, NotificationService.class));

        // Initialise visual components
        setTitle("Home");

        if(savedInstanceState == null)
        {
            // Sets home as the default selected navigation item
            navigationView.getMenu().getItem(0).setChecked(true);

            // Load Home fragment by default
            setTitle("Home");
            VenueUserHomeFragment fragment = new VenueUserHomeFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "VenueUserHomeFragment");
            fragmentTransaction.commit();
        }

        String fragmentToOpen = getIntent().getStringExtra("FragmentToOpenExtra");

        if(fragmentToOpen != null)
        {
            if(fragmentToOpen.equals("VenueUserGigRequestsFragment"))
            {
                // This ensures that whenever the back button is pressed there is never a blank home screen shown
                setTitle("Gig Requests");
                VenueUserGigRequestsFragment fragment = new VenueUserGigRequestsFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment
                        , "VenueUserGigRequestsFragment");
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
        VenueUserHomeFragment homeFragment = (VenueUserHomeFragment) getFragmentManager().findFragmentByTag("VenueUserHomeFragment");
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
            VenueUserHomeFragment fragment = new VenueUserHomeFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "VenueUserHomeFragment");
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the musician_user_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.venue_user_main, menu);

        // Calls the method to populate the drawer with the user data
        NavigationDrawerUserData();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // If sign out is selected, the user is signed out and the StartLoginActivity method
            // is called
            case R.id.action_logout:
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

        if (id == R.id.nav_venue_home)
        {
            //ClearBackStack(this);

            getFragmentManager().popBackStackImmediate();

            setTitle("Home");
            VenueUserHomeFragment fragment = new VenueUserHomeFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "VenueUserHomeFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_profile)
        {
            //ClearBackStack(this);

            getFragmentManager().popBackStackImmediate();

            setTitle("My Venue Profile");
            VenueUserProfileFragment fragment = new VenueUserProfileFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "VenueUserProfileFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if (id == R.id.nav_create_gig)
        {
            //ClearBackStack(this);

            getFragmentManager().popBackStackImmediate();

            setTitle("Create a Gig");
            VenueUserCreateGigFragment fragment = new VenueUserCreateGigFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserHomeFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if(id == R.id.nav_my_gigs)
        {
            //ClearBackStack(this);

            getFragmentManager().popBackStackImmediate();

            setTitle("My Gigs");
            VenueUserViewGigsFragment fragment = new VenueUserViewGigsFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment, "VenueUserViewGigsFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if(id == R.id.nav_gig_requests)
        {
            //ClearBackStack(this);

            getFragmentManager().popBackStackImmediate();

            setTitle("Gig Requests");
            VenueUserGigRequestsFragment fragment = new VenueUserGigRequestsFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment, "VenueUserGigRequestsFragment")
                    .addToBackStack(null)
                    .commit();
        }

        else if(id == R.id.nav_ticket_scanner)
        {
            getFragmentManager().popBackStackImmediate();

            setTitle("Ticket Scanner");
            VenueUserTicketScannerFragment fragment = new VenueUserTicketScannerFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment, "VenueUserTicketScannerFragment")
                    .addToBackStack(null)
                    .commit();
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
        mProfileImageView = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.navDrawerImageView);
        mNavigationProfileEmailTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userEmailTextView);

        mVenueImageReference.child("VenueProfileImages/" + mVenueId + "/profileImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load
                        (mVenueImageReference.child("VenueProfileImages/" + mVenueId + "/profileImage"))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).override(220, 220).into(mProfileImageView);
            }

        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Picasso.with(getApplicationContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(220, 220).into(mProfileImageView);
            }
        });

        mNavigationProfileEmailTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    @Override
    public void UpdateDrawerProfilePicture()
    {
        NavigationDrawerUserData();
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
        Intent returnToLoginActivity= new Intent(VenueUserMainActivity.this, LoginActivity.class);
        returnToLoginActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(returnToLoginActivity);
    }

    // This clears the back stack of fragments and adds a home fragment
    private static void ClearBackStack(Activity activity)
    {
        FragmentManager fragmentManager = activity.getFragmentManager();
        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i)
        {
            fragmentManager.popBackStack();
        }

        VenueUserHomeFragment fragment = new VenueUserHomeFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment
                , "VenueUserHomeFragment");
        fragmentTransaction.commit();
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(VenueUserMainActivity.this);
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
