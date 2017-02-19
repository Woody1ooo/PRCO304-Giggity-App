package com.liamd.giggity_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static android.R.id.toggle;
import static com.liamd.giggity_app.R.layout.activity_main;
import static com.liamd.giggity_app.R.layout.app_bar_main;
import static com.liamd.giggity_app.R.layout.nav_header_main;
import static com.liamd.giggity_app.R.menu.activity_main_drawer;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerLock
{
    // Declare visual components
    private ImageView navigationProfilePictureImageView;
    private TextView navigationProfileEmailTextView;
    private DrawerLayout drawer;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Variable to hold the currently logged in userID
    private String mLoggedInUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Sets home as the default selected navigation item
        navigationView.getMenu().getItem(0).setChecked(true);

        // Initialise visual components
        setTitle("Home");

        // Gets the currently logged in user and assigns the value to mLoggedInUserID
        FirebaseUser user = mAuth.getCurrentUser();
        mLoggedInUserID = user.getUid();

        // Load Home fragment by default
        setTitle("Home");
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment
                , "HomeFragment");
        fragmentTransaction.commit();

        // At the database reference "Users/%logged in user id%/hasCompletedSetup", a check is made
        // to see if the value is true or false.
        // If the user hasn't completed the account setup yet (i.e. hasCompletedSetup = false)
        // load the setup fragment on startup
        mDatabase.child("Users").child(mLoggedInUserID + "/hasCompletedSetup").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.getValue() == null || !((boolean) dataSnapshot.getValue()))
                {
                    setTitle("Initial Account Setup");
                    PreSetupFragment fragment = new PreSetupFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fragment
                            , "PreSetupFragment");
                    fragmentTransaction.commit();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Calls the method to populate the drawer with the user data
        NavigationDrawerUserData();
        return true;
    }


    // Method to determine the selected menu item
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

        if (id == R.id.nav_home)
        {
            setTitle("Home");
            HomeFragment fragment = new HomeFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "HomeFragment");
            fragmentTransaction.commit();
        }

        else if (id == R.id.nav_profile)
        {
            setTitle("My Musician Profile");
            MusicianProfileFragment fragment = new MusicianProfileFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianProfileFragment");
            fragmentTransaction.commit();
        }

        else if (id == R.id.nav_band_finder)
        {
            setTitle("Band Finder");
            BandFinderFragment fragment = new BandFinderFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "BandFinderFragment");
            fragmentTransaction.commit();
        }

        else if (id == R.id.nav_band_creator)
        {
            setTitle("Band Creator");
            BandCreatorFragment fragment = new BandCreatorFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "BandCreatorFragment");
            fragmentTransaction.commit();
        }

        else if (id == R.id.nav_requests)
        {
            setTitle("Band Requests");
            BandRequestsFragment fragment = new BandRequestsFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "BandRequestsFragment");
            fragmentTransaction.commit();
        }

        else if (id == R.id.nav_settings)
        {
            setTitle("Settings");
            SettingsFragment fragment = new SettingsFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "SettingsFragment");
            fragmentTransaction.commit();
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
        Uri photoURI;
        String userEmail;

        navigationProfilePictureImageView = (ImageView) findViewById(R.id.profileImageView);
        navigationProfileEmailTextView = (TextView) findViewById(R.id.userEmailTextView);

        photoURI = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        Picasso.with(this).load(photoURI).resize(220, 220).into(navigationProfilePictureImageView);

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        navigationProfileEmailTextView.setText(userEmail);
    }

    private void Logout()
    {
        // Will log the user out of Gmail or email/password login
        FirebaseAuth.getInstance().signOut();

        // Will log the user out of facebook
        LoginManager.getInstance().logOut();

        // Returns to the login activity

        finish();
        Intent returnToLoginActivity= new Intent(MainActivity.this, LoginActivity.class);
        returnToLoginActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(returnToLoginActivity);
    }

    // The implemented method from DrawerLock to set whether the drawer is locked or unlocked
    @Override
    public void setDrawerEnabled(boolean enabled)
    {
        int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawer.setDrawerLockMode(lockMode);
    }
}
