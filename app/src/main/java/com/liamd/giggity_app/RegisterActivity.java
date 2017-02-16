package com.liamd.giggity_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity
{
    // Declare visual components
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmEditText;
    private Button mRegisterButton;
    private ProgressDialog mProgressDialog;
    private String mLoggedInUserID;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Creates a reference to Firebase's authentication
        mAuth = FirebaseAuth.getInstance();

        // Creates a reference to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise visual components
        setTitle("Register");
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mPasswordConfirmEditText = (EditText) findViewById(R.id.passwordConfirmEditText);
        mRegisterButton = (Button) findViewById(R.id.registerButton);
        mProgressDialog = new ProgressDialog(this);

        mRegisterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Register();
            }
        });
    }

    // Method to register a new user via email + password authentication
    // As well as creating a user in the authentication section of Firebase
    // this also inserts a record into the database
    private void Register()
    {
        // Checks if either the email, password, or password confirm fields are empty
        if(TextUtils.isEmpty(mEmailEditText.getText()) || TextUtils.isEmpty(mPasswordEditText.getText())
                || TextUtils.isEmpty(mPasswordConfirmEditText.getText()))
        {
            Toast.makeText(RegisterActivity.this, "Please enter a value in each of the fields!",
                    Toast.LENGTH_LONG).show();
        }

        else
        {
            // A check to make sure the password and confirmed password fields match
            if(mPasswordEditText.getText().toString().equals(mPasswordConfirmEditText.getText().toString()))
            {
                // A new user object is created to be inserted into the database
                final User newUser = new User();
                newUser.setEmail(mEmailEditText.getText().toString());
                newUser.setPassword(mPasswordEditText.getText().toString());

                mProgressDialog.setMessage("Creating User...");
                mProgressDialog.show();

                // This creates the record in Firebase's authentication
                mAuth.createUserWithEmailAndPassword(newUser.getEmail(),
                        newUser.getPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        // If the user is successfully added, the user is stored under the "Users" node
                        // in the database, with the user ID as the key.
                        if(task.isSuccessful())
                        {
                            FirebaseUser user = mAuth.getCurrentUser();
                            mLoggedInUserID = user.getUid();
                            newUser.setUserID(mLoggedInUserID);

                            // This field determines whether the user has chosen their account type yet
                            newUser.setHasCompletedSetup(false);

                            // The password is set to null before being added to the database,
                            // as for security reasons this shouldn't be stored in plain sight.
                            newUser.setPassword(null);

                            // Because after registration, the user is immediately logged in,
                            // we can get their user ID and use this as the key.
                            mDatabase.child("Users").child(mLoggedInUserID).setValue(newUser);
                            mProgressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "User successfully created!",
                                    Toast.LENGTH_SHORT).show();



                            // Calls the LoadMainActivity method
                            LoadMainActivity();
                        }

                        else
                        {
                            mProgressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Registration failed! Please note" +
                                            " that if you have previously registered using a different method," +
                                            " you will need to use this instead.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            // If the value of password and confirm password are not equal
            // Display a toast to that effect
            else
            {
                Toast.makeText(RegisterActivity.this, "Passwords do not match!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to load the home activity. Only called if the user is signed in.
    private void LoadMainActivity()
    {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
