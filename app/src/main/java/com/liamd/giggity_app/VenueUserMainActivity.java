package com.liamd.giggity_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
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
import com.google.android.gms.ads.formats.NativeAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class VenueUserMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    // Declare visual components
    private CircleImageView circleImageView;
    private TextView navigationProfileEmailTextView;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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

        // Sets home as the default selected navigation item
        navigationView.getMenu().getItem(0).setChecked(true);

        // Initialise visual components
        setTitle("Venue User Home");

        // Load Home fragment by default
        setTitle("Home");
        VenueUserHomeFragment fragment = new VenueUserHomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment
                , "VenueUserHomeFragment");
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
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
            setTitle("Home");
            VenueUserHomeFragment fragment = new VenueUserHomeFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "VenueUserHomeFragment");
            fragmentTransaction.commit();
        }

        else if (id == R.id.nav_create_gig)
        {
            setTitle("Create a Gig");
            VenueUserCreateGigFragment fragment = new VenueUserCreateGigFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment
                    , "MusicianUserHomeFragment");
            fragmentTransaction.commit();
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

        circleImageView = (CircleImageView) findViewById(R.id.profile_image);
        navigationProfileEmailTextView = (TextView) findViewById(R.id.userEmailTextView);

        photoURI = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        Picasso.with(this).load(photoURI).resize(220, 220).into(circleImageView);

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
        Intent returnToLoginActivity= new Intent(VenueUserMainActivity.this, LoginActivity.class);
        returnToLoginActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(returnToLoginActivity);
    }
}
