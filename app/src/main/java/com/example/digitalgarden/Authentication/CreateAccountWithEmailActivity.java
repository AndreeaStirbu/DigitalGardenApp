package com.example.digitalgarden.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalgarden.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Create Firebase Account.
 */
public class CreateAccountWithEmailActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private TextInputLayout mInputName, mInputEmail, mInputPass;
    private String mName, email, mPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_with_email);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialise UI
        Button create_account = findViewById(R.id.create_account);
        Button signIn = findViewById(R.id.sign_in);
        mInputName = findViewById(R.id.name_editBox);
        mInputEmail = findViewById(R.id.email_editBox);
        mInputPass = findViewById(R.id.password_editBox);

        // Set Errors for Input Texts
        mInputName.setErrorTextAppearance(R.style.error_appearance);
        mInputEmail.setErrorTextAppearance(R.style.error_appearance);
        mInputPass.setErrorTextAppearance(R.style.error_appearance);

        // Add onClick listener to buttons
        create_account.setOnClickListener(this);
        signIn.setOnClickListener(this);
    }

    /**
     * {@inheritDoc}
     * @param v The view that has been clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_account:
                if(inputValidation()) {
                    if(inputValidation()) { createAccount(mName, email, mPass); }
                }
                break;
            case R.id.sign_in:
                Intent signIn = new Intent(CreateAccountWithEmailActivity.this, SignInWithEmail.class);
                signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                CreateAccountWithEmailActivity.this.startActivity(signIn);
                break;
        }
    }

    /**
     * Create user
     * @param name The user's name
     * @param email The user's email
     * @param password The user's password
     */
    private void createAccount(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Create user
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates);

                            // Navigate to Sign in Activity
                            Intent login = new Intent(CreateAccountWithEmailActivity.this, SignInWithEmail.class);
                            CreateAccountWithEmailActivity.this.startActivity(login);
                            Toast.makeText(CreateAccountWithEmailActivity.this, "Registration succeeded.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CreateAccountWithEmailActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Validate the user input
     * @return If input is valid
     */
    private Boolean inputValidation() {
        Boolean valid = true;

        mName = mInputName.getEditText().getText().toString().trim();
        email = mInputEmail.getEditText().getText().toString().trim();
        mPass = mInputPass.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(mName)) {
            mInputName.setError("Please enter Name");
            mInputName.requestFocus();
            valid = false;
        } else {
            mInputName.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            mInputEmail.setError("Please enter Email");
            mInputEmail.requestFocus();
            valid = false;
        }

        if (TextUtils.isEmpty(mPass)) {
            mInputPass.setError("Please enter Password");
            mInputPass.requestFocus();
            valid = false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mInputEmail.setError("Please enter a valid email");
            mInputEmail.requestFocus();
            valid = false;
        } else

        if (mPass.length() < 6) {
            mInputPass.setError("Password must contain at least 6 characters");
            mInputPass.requestFocus();
            valid = false;
        }

        if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !TextUtils.isEmpty(email)) {
            mInputEmail.setError(null);
        }

        if(!TextUtils.isEmpty(mPass) && mPass.length() >= 6) {
            mInputPass.setError(null);
        }

        return valid;
    }
}
