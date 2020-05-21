package com.example.digitalgarden.Authentication;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalgarden.DashboardActivity;
import com.example.digitalgarden.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Activity that provides the user different sign in/up options
 */
public class SignOptionsActivity extends AppCompatActivity implements View.OnClickListener {

    private int RC_SIGN_IN = 0;
    protected Button mGoToSignIn;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_options);

        SignInButton mSignInButton = findViewById(R.id.signInGoogle);
        mSignInButton.setSize(SignInButton.SIZE_STANDARD);

        Button mSignUpEmail = findViewById(R.id.signUpEmail);
        mGoToSignIn = findViewById(R.id.signInEmail);

        mSignInButton.setOnClickListener(this);
        mSignUpEmail.setOnClickListener(this);
        mGoToSignIn.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * {@inheritDoc}
     * @param v The view that has been clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signInGoogle :
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.signUpEmail:
                Intent create_email_account = new Intent(SignOptionsActivity.this, CreateAccountWithEmailActivity.class);
                SignOptionsActivity.this.startActivity(create_email_account);
                break;
            case R.id.signInEmail:
                Intent signIn = new Intent(SignOptionsActivity.this, SignInWithEmail.class);
                SignOptionsActivity.this.startActivity(signIn);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * Sign in with Google
     * @param completedTask Task has been completed
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Intent dashboard = new Intent(SignOptionsActivity.this, DashboardActivity.class);
        dashboard.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        SignOptionsActivity.this.startActivity(dashboard);
        Toast.makeText(getApplicationContext(),"Log in successfully", Toast.LENGTH_SHORT).show();
    }
}
