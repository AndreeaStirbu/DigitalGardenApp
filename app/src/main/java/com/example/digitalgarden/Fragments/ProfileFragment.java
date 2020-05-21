package com.example.digitalgarden.Fragments;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.digitalgarden.DashboardActivity;
import com.example.digitalgarden.Fragments.PhotoFragment;
import com.example.digitalgarden.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.bumptech.glide.Glide;
import static android.view.View.GONE;
import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Fragment that is holding and updating the account details
 */
public class ProfileFragment extends PhotoFragment implements View.OnClickListener {
    // Firebase variables
    private FirebaseUser mF_acc;

    // Google variables
    private GoogleSignInAccount mG_acc;

    // UI variables
    private ImageView mImgView;
    private TextView mProfileName, mProfileEmail;
    private String mName, mEmail;
    private ImageButton mBtnChangeName, mBtnChangeEmail, mBtnChangePass;
    private LinearLayout mChangeNameLayout, mChangeEmailLayout, mChangePassLayout;
    private TextInputLayout mEtNameChange, mEtEmailChange, mEtPassChangeOld, mEtPassChangeNew, mEtPassForEmailChange;

    private SwipeRefreshLayout mSwipeRefresh;
    private DashboardActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get the parent mActivity
        mActivity = (DashboardActivity) getActivity();

        // Initialise Google/Firebase accounts
        mG_acc = GoogleSignIn.getLastSignedInAccount(getContext());
        mF_acc = FirebaseAuth.getInstance().getCurrentUser();

        // Initialise UI
        fetchUI(view);

        return view;
    }


    /**
     * {@inheritDoc}
     * @param v The view that has been clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOpenGallery:
                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                    }
                } else {
                    openGallery();
                }
                break;
            case R.id.btnOpenCamera:
                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED){
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                    }
                } else {
                    openCamera();
                    //photoUri = openCamera(photoUri);
                }
                break;
            case R.id.btn_change_name:
                mBtnChangeName.setVisibility(View.INVISIBLE);
                mChangeNameLayout.setVisibility(View.VISIBLE);
                mBtnChangeEmail.setVisibility(View.INVISIBLE);
                mBtnChangePass.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_change_email:
                mBtnChangeEmail.setVisibility(GONE);
                mBtnChangeName.setVisibility(View.GONE);
                mBtnChangePass.setVisibility(View.GONE);
                mChangeNameLayout.setVisibility(View.GONE);
                mChangePassLayout.setVisibility(GONE);
                mChangeEmailLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_change_password:
                mBtnChangePass.setVisibility(GONE);
                mChangePassLayout.setVisibility(View.VISIBLE);
                mBtnChangeName.setVisibility(View.INVISIBLE);
                mBtnChangeEmail.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_cancel_name:
                mChangeNameLayout.setVisibility(GONE);
                makeActionButtonsVisible();
                break;
            case R.id.btn_cancel_email:
                mChangeEmailLayout.setVisibility(GONE);
                makeActionButtonsVisible();
                break;
            case R.id.btn_cancel_pass:
                mChangePassLayout.setVisibility(GONE);
                makeActionButtonsVisible();
                break;
            case R.id.btn_save_name:
                saveName();
                mChangeNameLayout.setVisibility(GONE);
                makeActionButtonsVisible();
                break;
            case R.id.btn_save_email:
                saveEmail();
                mChangeEmailLayout.setVisibility(GONE);
                makeActionButtonsVisible();
                break;
            case R.id.btn_save_pass:
                savePassword();
                mChangePassLayout.setVisibility(GONE);
                makeActionButtonsVisible();
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setUriGallery() {
        //Set Circular Image with Glide
        Glide.with(this)
                .load(imgUri)
                .centerCrop()
                .dontAnimate()
                .into(mImgView);
        savePicture(imgUri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setUriCamera() {
        // Set Circular Image with Glide
        try
        {
            Glide.with(this)
                    .load(imgUri)
                    .centerCrop()
                    .dontAnimate()
                    .into(mImgView);
            savePicture(imgUri);
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to load", e);
        }
    }


    // Initialise UI
    /**
     * Initialise all the views in the fragment and make extra configurations where needed.
     * @param view The inflated layout of the fragment
     */
    private void fetchUI(View view) {
        ImageButton btnOpenGallery = view.findViewById(R.id.btnOpenGallery);
        ImageButton btnOpenCamera = view.findViewById(R.id.btnOpenCamera);
        mProfileName = view.findViewById(R.id.profile_name);
        mProfileEmail = view.findViewById(R.id.profile_email);
        mImgView = view.findViewById(R.id.profile_picture);
        mBtnChangeName = view.findViewById(R.id.btn_change_name);
        mChangeNameLayout = view.findViewById(R.id.change_name_layout);
        mEtNameChange = view.findViewById(R.id.et_change_name);
        Button btnNameCancel = view.findViewById(R.id.btn_cancel_name);
        Button btnNameSave = view.findViewById(R.id.btn_save_name);
        mEtNameChange.setErrorTextAppearance(R.style.error_appearance);

        mBtnChangeEmail = view.findViewById(R.id.btn_change_email);
        mChangeEmailLayout = view.findViewById(R.id.change_email_layout);
        mEtPassForEmailChange = view.findViewById(R.id.password_for_email_change);
        mEtEmailChange = view.findViewById(R.id.et_change_email);
        Button btnEmailCancel = view.findViewById(R.id.btn_cancel_email);
        Button btnEmailSave = view.findViewById(R.id.btn_save_email);
        mEtPassForEmailChange.setErrorTextAppearance(R.style.error_appearance);

        mBtnChangePass = view.findViewById(R.id.btn_change_password);
        mChangePassLayout = view.findViewById(R.id.change_password_layout);
        mEtPassChangeOld = view.findViewById(R.id.et_change_old_password);
        mEtPassChangeNew = view.findViewById(R.id.et_change_new_password);
        Button btnPassCancel = view.findViewById(R.id.btn_cancel_pass);
        Button btnPassSave = view.findViewById(R.id.btn_save_pass);
        mEtPassChangeOld.setErrorTextAppearance(R.style.error_appearance);
        mEtPassChangeNew.setErrorTextAppearance(R.style.error_appearance);
        mSwipeRefresh = view.findViewById(R.id.refreshProfile);

        mSwipeRefresh.setOnRefreshListener(refreshListener);
        btnOpenGallery.setOnClickListener(this);
        btnOpenCamera.setOnClickListener(this);

        mBtnChangeName.setOnClickListener(this);
        mBtnChangeEmail.setOnClickListener(this);
        mBtnChangePass.setOnClickListener(this);

        btnNameCancel.setOnClickListener(this);
        btnEmailCancel.setOnClickListener(this);
        btnPassCancel.setOnClickListener(this);

        btnNameSave.setOnClickListener(this);
        btnEmailSave.setOnClickListener(this);
        btnPassSave.setOnClickListener(this);

        mEtNameChange.getEditText().setText(mName);
        mEtEmailChange.getEditText().setText(mEmail);

        initialiseUI();
    }

    /**
     * Populate UI account information
     */
    private void initialiseUI() {
        getAccountDetails();

        mProfileName.setText(mName);
        mProfileEmail.setText(mEmail);
        if(imgUri != null) {
            Glide.with(this)
                    .load(imgUri)
                    .centerCrop()
                    .dontAnimate()
                    .into(mImgView);
        }
    }

    /**
     * Retrieves Name, Email, PhotoUri for Firebase and Google account
     */
    private void getAccountDetails() {
        if(mG_acc != null) {
            mName = mG_acc.getDisplayName();
            mEmail = mG_acc.getEmail();
            imgUri = mG_acc.getPhotoUrl();
        }

        if(mF_acc != null) {
            mName = mF_acc.getDisplayName();
            mEmail = mF_acc.getEmail();
            imgUri = mF_acc.getPhotoUrl();
        }
    }

    /**
     * Hiding all edit buttons when editing name | email | password
     */
    private void makeActionButtonsVisible() {
        mBtnChangeName.setVisibility(View.VISIBLE);
        mBtnChangeEmail.setVisibility(View.VISIBLE);
        mBtnChangePass.setVisibility(View.VISIBLE);
    }

    // Refresh
    /**
     *  Callback to refresh the UI
     */
    private void dispatchRefresh(){
        mSwipeRefresh.setRefreshing(true);
        initialiseUI();
        mSwipeRefresh.setRefreshing(false);
    }

    /**
     * Bind the callback dispatchRefresh to the refresh listener
     */
    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            dispatchRefresh();
        }
    };


    // Save Account Details
    /**
     * Update the Display Name of the Firebase Account
     */
    private void saveName(){
        String name = mEtNameChange.getEditText().getText().toString();
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            mF_acc.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Name has been updated", Toast.LENGTH_SHORT).show();
                                dispatchRefresh();
                                mActivity.initialiseDrawerData();
                            }
                        }
                    });
    }

    /**
     * Update the  Email of the Firebase Account
     */
    private void saveEmail() {
        final String newEmail = mEtEmailChange.getEditText().getText().toString();
        String password = mEtPassForEmailChange.getEditText().getText().toString();

        if (inputValidation(newEmail, mEtEmailChange) && inputValidation(password, mEtPassForEmailChange)) {
            AuthCredential credential = EmailAuthProvider.getCredential(mF_acc.getEmail(), password);
            mF_acc.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                mF_acc.updateEmail(newEmail)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), "User mEmail address updated.", Toast.LENGTH_SHORT).show();
                                                    dispatchRefresh();
                                                    mActivity.initialiseDrawerData();
                                                } else {
                                                    Toast.makeText(getActivity(), "Cannot update user mEmail", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                switch (errorCode) {
                                    case "ERROR_INVALID_EMAIL":
                                        Toast.makeText(getActivity(), "The mEmail address is badly formatted.", Toast.LENGTH_LONG).show();
                                        mEtEmailChange.setError("The mEmail address is badly formatted.");
                                        mEtEmailChange.requestFocus();
                                        break;

                                    case "ERROR_WRONG_PASSWORD":
                                        Toast.makeText(getActivity(), "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                                        mEtPassForEmailChange.setError("password is incorrect ");
                                        mEtPassForEmailChange.requestFocus();
                                        mEtPassForEmailChange.getEditText().setText("");
                                        break;

                                    case "ERROR_USER_MISMATCH":
                                        Toast.makeText(getActivity(), "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                                        break;
                                }
                                Toast.makeText(getActivity(), "Verification failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Update the password of the Firebase Account
     */
    private void savePassword(){
        String oldPass = mEtPassChangeOld.getEditText().getText().toString();
        final String newPass = mEtPassChangeNew.getEditText().getText().toString();

        if(inputValidation(oldPass, mEtPassChangeOld) && inputValidation(newPass, mEtPassChangeNew)) {
            AuthCredential credential = EmailAuthProvider.getCredential(mF_acc.getEmail(), oldPass);
            mF_acc.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mF_acc.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Error password not updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                String errorCode;
                                if(task.getException() instanceof  FirebaseAuthException)
                                {
                                    errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                    switch (errorCode) {
                                        case "ERROR_WRONG_PASSWORD":
                                            Toast.makeText(getActivity(), "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                                            mEtPassChangeOld.setError("password is incorrect ");
                                            mEtPassChangeOld.requestFocus();
                                            mEtPassChangeOld.getEditText().setText("");
                                            break;
                                    }
                                } else{
                                    errorCode="";
                                }
                                Toast.makeText(getActivity(), "Verification failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Update the picture of the Firebase Account
     * @param photoUri The Uri of the profile picture
     */
    private void savePicture(Uri photoUri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUri)
                .build();

        mF_acc.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mActivity.initialiseDrawerData();
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }

    /**
     * Validate the user input
     * @return If input is valid
     */
    private boolean inputValidation(String input, TextInputLayout inputControl) {
        boolean valid = true;

        if(TextUtils.isEmpty(input)) {
            inputControl.setError("Please enter a value");
            inputControl.requestFocus();
            valid = false;
        } else if(valid){
            inputControl.setError(null);
        }

        return valid;
    }
}
