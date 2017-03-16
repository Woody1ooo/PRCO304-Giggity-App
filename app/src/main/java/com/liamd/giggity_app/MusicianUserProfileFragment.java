package com.liamd.giggity_app;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicianUserProfileFragment extends Fragment
{
    // Declare visual components
    private CircleImageView profileImageView;
    private TextView changeImageTextView;
    private TextView removeImageTextView;
    private ProgressDialog mProgressDialog;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mProfileImageReference;
    private UploadTask mUploadTask;

    // Declare general variables
    private static int RESULT_LOAD_IMAGE = 1;
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    public MusicianUserProfileFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.musician_user_fragment_profile, container, false);

        // Initialise visual components
        profileImageView = (CircleImageView) fragmentView.findViewById(R.id.profile_image);
        changeImageTextView = (TextView) fragmentView.findViewById(R.id.changeImageTextView);
        removeImageTextView = (TextView) fragmentView.findViewById(R.id.removeImageTextView);
        mProgressDialog = new ProgressDialog(getContext());

        // Add the images to the text views
        changeImageTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_insert_photo_black_18dp, 0, 0, 0);
        removeImageTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete_black_18dp, 0, 0, 0);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creates a reference to the storage element of firebase
        mStorage = FirebaseStorage.getInstance();
        mProfileImageReference = mStorage.getReference("ProfileImages/");

        // This method loads the profile picture from the chosen login method
        LoadProfilePicture();

        // When the changeImageTextView is clicked the CheckForPermissions method is called
        changeImageTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CheckForPermissions();
            }
        });

        // When the removeImageTextView is clicked the CheckForPermissions method is called
        removeImageTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DeleteProfilePicture();
            }
        });

        return fragmentView;
    }

    // This method loads the user's profile picture from their chosen login method. This will need to be
    // changed to pull the users chosen pictures
    private void LoadProfilePicture()
    {
        Uri photoURI;
        photoURI = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

        if (!photoURI.equals(null))
        {
            Picasso.with(getContext()).load(photoURI).resize(350, 350).into(profileImageView);
        }
    }

    // This method checks to see whether the user has allowed the app to access the storage
    // If the permission is granted call the UpdateProfilePicture method
    private void CheckForPermissions()
    {
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        else
        {
            UpdateProfilePicture();
        }
    }

    // This method starts an intent to open the user's storage to select a photo
    private void UpdateProfilePicture()
    {
        Intent selectImage = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(selectImage, RESULT_LOAD_IMAGE);
    }

    private void DeleteProfilePicture()
    {
        // This dialog is created to confirm that the users want to edit the fields
        // they have chosen
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Profile Picture");
        builder.setMessage("Are you sure you wish to delete your profile picture?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // Displays the progress dialog
                mProgressDialog.setMessage("Deleting profile picture...");
                mProgressDialog.show();
                mProfileImageReference.child(mAuth.getCurrentUser().getUid() + "/").delete();
                Picasso.with(getContext()).load(R.drawable.com_facebook_profile_picture_blank_portrait).resize(350, 350).into(profileImageView);
                mProgressDialog.hide();
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

    // When the image has been selected upload the image to the firebase URL specified by mProfileImageReference
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data)
        {
            // This dialog is created to confirm that the users want to edit the fields
            // they have chosen
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Update Profile Picture");
            builder.setMessage("Are you sure you wish to use this image for your profile picture?");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    final Uri selectedImage = data.getData();
                    mUploadTask = mProfileImageReference.child(mAuth.getCurrentUser().getUid() + "/").putFile(selectedImage);

                    mUploadTask.addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                        }
                    });

                    // If the upload is successful
                    mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            profileImageView.setImageResource(0);
                            Glide.with(getContext()).using(new FirebaseImageLoader()).load
                                    (mProfileImageReference.child(mAuth.getCurrentUser().getUid() + "/" + selectedImage.getLastPathSegment())).override(350, 350).into(profileImageView);
                        }
                    });
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
    }

    // This method processes the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
        {
            // If the permission has been accepted call the update profile picture method to access the storage
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                UpdateProfilePicture();
            }

            // If the permission has been denied then display a message to that effect
            else
            {
                Toast.makeText(getActivity(), "If you wish to change your profile image," +
                        " please ensure you have given permission to access your storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
