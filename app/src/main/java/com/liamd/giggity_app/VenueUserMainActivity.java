package com.liamd.giggity_app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
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
}
