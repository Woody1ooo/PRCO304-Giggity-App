package com.liamd.giggity_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{
    // Declare visual components
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private ProgressDialog mProgressDialog;

    // Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // Declare Google specific variables
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Creates a reference to Firebase
        mAuth = FirebaseAuth.getInstance();

        // Check whether the user is already logged in
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null)
                {
                    // User is already signed in, therefore go straight to the homepage
                    LoadHomeActivity();
                }
            }
        };

        // Initialise visual components
        setTitle("Giggity");
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mLoginButton = (Button) findViewById(R.id.loginButton);
        mProgressDialog = new ProgressDialog(this);

        // Gmail sign in button listener
        findViewById(R.id.googleButton).setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        // Login button onClickListener calls the SignIn() method
        mLoginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SignIn();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // Method called when the traditional login button is clicked
    private void SignIn()
    {
        String emailValue = mEmailEditText.getText().toString();
        String passwordValue = mPasswordEditText.getText().toString();

        // Checks if either the email or password fields are empty
        if(TextUtils.isEmpty(emailValue) || TextUtils.isEmpty(passwordValue))
        {
            Toast.makeText(LoginActivity.this, "Please enter a value for email and password!",
                    Toast.LENGTH_LONG).show();
        }

        else
        {
            // Displays the progress dialog
            mProgressDialog.setMessage("Signing In...");
            mProgressDialog.show();

            // Method takes the value of the email and password variables and attempts to login
            mAuth.signInWithEmailAndPassword(emailValue, passwordValue).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    // If the task errors, a toast is displayed to that effect
                    if(!task.isSuccessful())
                    {
                        mProgressDialog.hide();
                        Toast.makeText(LoginActivity.this,
                                "Sign In Error! Please ensure your username/password are correct",
                                Toast.LENGTH_LONG).show();
                    }

                    else
                    {
                        mProgressDialog.hide();
                        Toast.makeText(LoginActivity.this, "login success!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    // onClick listener for the Gmail login calls the SignInWithGmail method
    @Override
    public void onClick(View view)
    {
        int i = view.getId();
        if(i == R.id.googleButton)
        {
            SignInWithGmail();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(LoginActivity.this, "Connection Failed! Please try again later",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess())
            {
                // Google sign in was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }

            // If the Gmail sign in wasn't successful, a toast is displayed to that effect
            else
            {
                Toast.makeText(LoginActivity.this, "Gmail login failed! Please ensure you have a" +
                        "Gmail account associated with your android device.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Once the Gmail sign in has been successful, this method is called to sign in to Firebase
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        // If sign in to Firebase is successful, a toast displays to that effect,
                        // and the home activity is loaded.
                        // If sign in to Firebase fails, a toast displays to that effect
                        if(task.isSuccessful())
                        {
                            Toast.makeText(LoginActivity.this, "Gmail sign in successful!",
                                    Toast.LENGTH_SHORT).show();
                            LoadHomeActivity();
                        }

                        else
                        {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method which starts the Gmail sign in intent
    private void SignInWithGmail()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Method to load the home activity. Only called if the user is signed in.
    private void LoadHomeActivity()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
