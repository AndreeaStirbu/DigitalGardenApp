package com.example.digitalgarden.Authentication;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalgarden.DashboardActivity;
import com.example.digitalgarden.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Activity that is handling Firebase Authentication
 */
public class SignInWithEmail extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private TextInputLayout mInputEmail, mInputPass;
    private String mEmail, mPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_with_email);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialise UI
        mInputEmail = findViewById(R.id.email_sign_in);
        mInputPass = findViewById(R.id.password_sign_in);
        mInputEmail.setErrorTextAppearance(R.style.error_appearance);
        mInputPass.setErrorTextAppearance(R.style.error_appearance);

        Button mSign_in = findViewById(R.id.sign_into_account);
        Button mSign_up = findViewById(R.id.sign_up);
        mSign_in.setOnClickListener(this);
        mSign_up.setOnClickListener(this);
    }

    /**
     * {@inheritDoc}
     * @param v The view that has been clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_into_account:
                // If the user introduced valid input, sign in into the account
                if(inputValidation()) {
                    signIn(mEmail, mPass);
                }
                break;
            case R.id.sign_up:
                // If the user wants to sign up, redirect to Sign up screen
                Intent signUp = new Intent(SignInWithEmail.this, CreateAccountWithEmailActivity.class);
                SignInWithEmail.this.startActivity(signUp);
                break;
        }
    }

    private void signIn(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent dashboard = new Intent(SignInWithEmail.this, DashboardActivity.class);
                            dashboard.putExtra("afterSignIn", true);
                            dashboard.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            SignInWithEmail.this.startActivity(dashboard);
                            Toast.makeText(SignInWithEmail.this, "Authentication succeeded.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Show error from Firebase
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            switch (errorCode) {
                                case "ERROR_INVALID_CREDENTIAL":
                                    Toast.makeText(SignInWithEmail.this, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_WRONG_PASSWORD":
                                    Toast.makeText(SignInWithEmail.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                                    mInputPass.setError("password is incorrect ");
                                    mInputPass.requestFocus();
                                    mInputPass.getEditText().setText("");
                                    break;

                                case "ERROR_USER_MISMATCH":
                                    Toast.makeText(SignInWithEmail.this, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                    Toast.makeText(SignInWithEmail.this, "An account already exists with the same mEmail address but different sign-in credentials. Sign in using a provider associated with this mEmail address.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                    Toast.makeText(SignInWithEmail.this, "The mEmail address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                                    mInputEmail.setError("The mEmail address is already in use by another account.");
                                    mInputEmail.requestFocus();
                                    break;

                                case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                    Toast.makeText(SignInWithEmail.this, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_USER_TOKEN_EXPIRED":
                                    Toast.makeText(SignInWithEmail.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_USER_NOT_FOUND":
                                    Toast.makeText(SignInWithEmail.this, "There is no user record corresponding to this identifier. The user may have been deleted.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_INVALID_USER_TOKEN":
                                    Toast.makeText(SignInWithEmail.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    }
                });
    }

    /**
     * Validate the user input
     * @return If input is valid
     */
    private Boolean inputValidation() {
        boolean valid = true;

        mEmail = mInputEmail.getEditText().getText().toString().trim();
        mPass = mInputPass.getEditText().getText().toString().trim();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            mInputEmail.setError("Please enter a valid mEmail");
            mInputEmail.requestFocus();
            valid = false;
        }

        if (mPass.length() < 6) {
            mInputPass.setError("Password must contain at least 6 characters");
            mInputPass.requestFocus();
            valid = false;
        }

        if (TextUtils.isEmpty(mEmail)) {
            mInputEmail.setError("Please enter Email");
            mInputEmail.requestFocus();
            valid = false;
        }

        if (TextUtils.isEmpty(mPass)) {
            mInputPass.setError("Please enter Password");
            mInputPass.requestFocus();
            valid = false;
        }

        if(android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail).matches() && !TextUtils.isEmpty(mEmail)) {
            mInputEmail.setError(null);
        }

        if(!TextUtils.isEmpty(mPass) && mPass.length() >= 6) {
            mInputPass.setError(null);
        }

        return valid;
    }
}
