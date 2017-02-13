package com.liamd.giggity_app;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity
{
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Test method to display the users photo from their login method
        // Picasso could be used to make circular images...
        Uri photoURI = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        imageView = (ImageView) findViewById(R.id.imageView);
        Picasso.with(this).load(photoURI).resize(300, 300).into(imageView);
    }

    // Method to initialise and inflate the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Method to determine the selected menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // If sign out is selected, the user is signed out and the StartLoginActivity method
            // is called

            case R.id.signOutButton:

                // Will sign the user out of Gmail or email/password login
                FirebaseAuth.getInstance().signOut();

                // Will sign the user out of facebook
                LoginManager.getInstance().logOut();

                // Returns to the login activity

                finish();
                Intent returnToLoginActivity= new Intent(HomeActivity.this, LoginActivity.class);
                returnToLoginActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(returnToLoginActivity);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
