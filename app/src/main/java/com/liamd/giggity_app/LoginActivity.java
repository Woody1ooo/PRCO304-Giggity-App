package com.liamd.giggity_app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

public class LoginActivity extends AppCompatActivity
{
    //Declare visual components
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private ProgressDialog mProgressDialog;

    //Declare Firebase specific variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get a reference to the database
        mAuth = FirebaseAuth.getInstance();

        //Initialise visual components
        setTitle("Giggity");
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        mLoginButton = (Button) findViewById(R.id.loginButton);
        mProgressDialog = new ProgressDialog(this);

        //Check whether the user is already logged in
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null)
                {
                    //User is already signed in
                    //Insert intent here to automatically take the user to the homepage...
                }
            }
        };

        //Login button onClickListener calls the SignIn() method
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

    //Method called when the login button is clicked
    private void SignIn()
    {
        String emailValue = mEmailEditText.getText().toString();
        String passwordValue = mPasswordEditText.getText().toString();

        //Checks if either the email or password fields are empty
        if(TextUtils.isEmpty(emailValue) || TextUtils.isEmpty(passwordValue))
        {
            Toast.makeText(LoginActivity.this, "Please enter a value for email and password!",
                    Toast.LENGTH_LONG).show();
        }

        else
        {
            //Displays the progress dialog
            mProgressDialog.setMessage("Signing In...");
            mProgressDialog.show();

            //Method takes the value of the email and password variables and attempts to login
            mAuth.signInWithEmailAndPassword(emailValue, passwordValue).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    //If the task errors, a toast is displayed to that effect
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
}
