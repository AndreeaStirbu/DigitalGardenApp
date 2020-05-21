package com.example.digitalgarden.Authentication;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.digitalgarden.DashboardActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Activity skips Sign in screen if the user is already logged in, or directs the user to the Sign in screen if not logged in already
 */
public class StartingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInAccount google_account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser firebase_account = FirebaseAuth.getInstance().getCurrentUser();

        if(google_account != null || firebase_account != null) {
            // Navigate to Dashboard Activity
            Intent dashboard = new Intent(StartingActivity.this, DashboardActivity.class);
            dashboard.putExtra("afterSignIn", false);
            StartingActivity.this.startActivity(dashboard);
            finish();
        } else {
            // Navigate to Sign In Activity
            Intent login = new Intent(StartingActivity.this, SignOptionsActivity.class);
            StartingActivity.this.startActivity(login);
        }
    }
}
