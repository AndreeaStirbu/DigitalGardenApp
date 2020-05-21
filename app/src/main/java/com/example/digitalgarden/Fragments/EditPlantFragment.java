package com.example.digitalgarden.Fragments;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.digitalgarden.DashboardActivity;
import com.example.digitalgarden.Model.Plant;
import com.example.digitalgarden.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.util.Calendar;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Fragment that is updating the details of a Plant object and commits them to Firebase.
 */
public class EditPlantFragment extends PhotoFragment implements View.OnClickListener {
    public Plant plant;
    private DashboardActivity mActivity;
    private int mAlarmCounter;
    private boolean mNotificationsAllowed;
    private boolean mImportantInfoUpdated;

    // Firebase variables
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;

    // UI variables
    private AutoCompleteTextView mSoilDropDown, mLightDropDown;
    private SeekBar mSeekbar;
    private TextView mShowSeekbarProgress;
    private TextInputLayout mPlantName, mPlantSpecie, mLastDays, mTempStart, mTempEnd;
    private TextInputEditText mNotes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_edit_plant, container, false);

        // Get the parent mActivity
        mActivity = (DashboardActivity) getActivity();

        //Initialise Firebase variables
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Plant");
        mStorageRef = FirebaseStorage.getInstance().getReference("Thumbnails");

        // Initialise variable for boolean that checks if notification are on or off
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("NotificationsOnOff", 0);
        mNotificationsAllowed = sharedPreferences.getBoolean("switchValue", true);

        // Initialise all the views withing the form
        fetchUI(rootView);

        return rootView;
    }

    /**
     * {@inheritDoc}
     * Hide the Toolbar when opening this fragment.
     */
    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    /**
     * {@inheritDoc}
     * Display the Toolbar when closing this fragment.
     */
    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    /**
     * {@inheritDoc}
     * @param v The view that has been clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_btnOpenGallery:
                // Ask for permission to access the external storage
                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(mActivity,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                STORAGE_PERMISSION_CODE);
                    }
                } else {
                    // If permissions are granted, open the Gallery
                    openGallery();
                }
                break;
            case R.id.edit_btnOpenCamera:
                // Ask for permission to open the camera
                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED){
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                            Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(mActivity,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_PERMISSION_CODE);
                    }
                } else {
                    // If permissions are granted, open the camera
                    openCamera();
                }
            case R.id.btnUpdate:
                if(inputValidation()) editPlant();
        }
    }

    // Initialise Form
    /**
     * Initialise all the views in the fragment and make
     * extra configurations where needed.
     * @param rootView The inflated layout of the fragment
     */
    private void fetchUI(View rootView) {
        Button btnUpdate = rootView.findViewById(R.id.btnUpdate);
        ImageButton btnOpenGallery = rootView.findViewById(R.id.edit_btnOpenGallery);
        ImageButton btnOpenCamera = rootView.findViewById(R.id.edit_btnOpenCamera);
        TextInputEditText lastDays_et = rootView.findViewById(R.id.edit_last_water);
        TextInputEditText tempStart_et = rootView.findViewById(R.id.edit_temperature_start);
        TextInputEditText tempEnd_et = rootView.findViewById(R.id.edit_temperature_end);

        mPlantName = rootView.findViewById(R.id.edit_plant_name);
        mPlantSpecie = rootView.findViewById(R.id.edit_plant_specie);
        mLastDays = rootView.findViewById(R.id.last_water);
        mTempStart = rootView.findViewById(R.id.temp_start);
        mTempEnd = rootView.findViewById(R.id.temp_end);
        mNotes = rootView.findViewById(R.id.notes_editText);
        mSeekbar = rootView.findViewById(R.id.edit_waterFrequency);
        mShowSeekbarProgress = rootView.findViewById(R.id.edit_SeekbarProgress);
        imgView = rootView.findViewById(R.id.edit_imgView);
        mSoilDropDown = rootView.findViewById(R.id.edit_soil_moisture);
        mLightDropDown = rootView.findViewById(R.id.edit_light_level);

        // Initialise the UI with data
        initialiseUI();

        // Set input type of edit texts
        lastDays_et.setInputType(InputType.TYPE_CLASS_NUMBER);
        tempStart_et.setInputType(InputType.TYPE_CLASS_NUMBER);
        tempEnd_et.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Set onClick listeners for buttons
        btnOpenGallery.setOnClickListener(this);
        btnOpenCamera.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mShowSeekbarProgress.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    /**
     * Populate UI in the form with plant's details
     */
    private void initialiseUI() {
        mSoilDropDown.setText(plant.getSoilType());
        mLightDropDown.setText(plant.getLightLevel());

        ArrayAdapter<String> soilAdapter =
                new ArrayAdapter<>(
                        getContext(),
                        R.layout.dropdown_menu_popup_item,
                        SOIL_TYPES);

        mSoilDropDown.setInputType(InputType.TYPE_NULL);
        mSoilDropDown.setAdapter(soilAdapter);

        ArrayAdapter<String> lightAdapter =
                new ArrayAdapter<>(
                        getContext(),
                        R.layout.dropdown_menu_popup_item,
                        LIGHT_LEVEL);

        mLightDropDown.setInputType(InputType.TYPE_NULL);
        mLightDropDown.setAdapter(lightAdapter);

        mPlantName.getEditText().setText(plant.getPlantName());
        mPlantSpecie.getEditText().setText(plant.getPlantSpecie());
        mLastDays.getEditText().setText(String.valueOf(plant.getLastWatering()));
        mTempStart.getEditText().setText(String.valueOf(plant.getMinTemp()));
        mTempEnd.getEditText().setText(String.valueOf(plant.getMaxTemp()));
        mNotes.setText(plant.getNotes());
        mSeekbar.setProgress(plant.getWateringFrequency());
        mShowSeekbarProgress.setText(String.valueOf(mSeekbar.getProgress()));
        Picasso.with(getContext()).load(plant.getImageSrc())
                .resize(1000, 1000)
                .centerInside()
                .into(imgView);
    }


    // Update Plant

    /**
     * When pressing the upload button to commit the changes, a Dialog will be prompted and make
     * the user confirm that he wants to make the changes.
     * If the users accepts, updatePlant() is called.
     */
    private void editPlant() {
        new AlertDialog.Builder(getContext())
                .setTitle("Are you sure you want to change " + plant.getPlantName()+"'s details?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updatePlant();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * Check if the user wants to update the Plant's image and update the image and details in the
     * Firebase Storage/Database
     */
    private void updatePlant() {
        // Get the current image source of the Plant Image
        String imgSrc = plant.getImageSrc();

        // See if any changes that will require re-calculation of Next Notification/Watering date have been made
        if(((plant.getLastWatering() != Integer.parseInt(mLastDays.getEditText().getText().toString()))
                || (plant.getWateringFrequency() != mSeekbar.getProgress()))) {
            mImportantInfoUpdated = true;
        } else {
            mImportantInfoUpdated = false;
        }

        // If notifications are on and important changes have been made then cancel the current alarm
        // as another one will be created
        if (mNotificationsAllowed && mImportantInfoUpdated)
        {
            mActivity.cancelAlarm(plant);
        }

        // If the user wants to change the picture of the plant
        if(imgUri != null && imgUri.toString() != imgSrc) {
            final StorageReference Ref = mStorageRef.child(System.currentTimeMillis() + "." + getExtension(imgUri));
            Ref.putFile(imgUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // If the new image has been uploaded to the storage, commit the plant's details to the database
                            Ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    plant.setImageSrc(uri.toString());
                                    updateDatabaseObject(true);

                                    // If notifications are on and the plant has a new Next Notification Date
                                    if(mNotificationsAllowed && mImportantInfoUpdated) {
                                        // Get the current Alarm Code
                                        SharedPreferences countPref = getActivity().getSharedPreferences("AlarmCounter", Context.MODE_PRIVATE);
                                        mAlarmCounter = countPref.getInt("mAlarmCounter", 0);
                                        plant.setNotificationCode(mAlarmCounter);

                                        // Create a new alarm
                                        mActivity.createAlarm(plant);

                                        // Increase the Alarm Code
                                        mAlarmCounter++;
                                        SharedPreferences.Editor editor = countPref.edit();
                                        editor.putInt("mAlarmCounter", mAlarmCounter);
                                        editor.apply();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(mActivity, "Image could not be added to the Storage", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Delete the previous picture from the Storage
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(imgSrc);
            storageReference.delete();
        }
        else {
            updateDatabaseObject(false);
        }
    }

    /**
     * Update the Plant's details in the Firebase Database
     * @param isNewImage boolean stating the user wants to change the plant's image
     */
    private void updateDatabaseObject(final boolean isNewImage) {
        plant.setPlantName(mPlantName.getEditText().getText().toString());
        plant.setPlantSpecie(mPlantSpecie.getEditText().getText().toString());
        plant.setLastWatering(Integer.parseInt(mLastDays.getEditText().getText().toString()));
        plant.setWateringFrequency(mSeekbar.getProgress());
        plant.setSoilType(mSoilDropDown.getText().toString());
        plant.setLightLevel(mLightDropDown.getText().toString());
        plant.setMinTemp(Integer.parseInt(mTempStart.getEditText().getText().toString()));
        plant.setMaxTemp(Integer.parseInt(mTempEnd.getEditText().getText().toString()));
        plant.setNotes(mNotes.getText().toString());

        // If notifications are on and important changes have been made, calculate the new Next Notification Date and
        // store the current Alarm Code in the database
        if(mNotificationsAllowed && mImportantInfoUpdated)
        {
            plant.setNextNotificationDate(calculateNextNotificationDate(plant.getWateringFrequency(), plant.getLastWatering()));
            SharedPreferences countPref = getActivity().getSharedPreferences("AlarmCounter", Context.MODE_PRIVATE);
            mAlarmCounter = countPref.getInt("mAlarmCounter", 0);
            plant.setNotificationCode(mAlarmCounter);
        } else if(mImportantInfoUpdated) {
            // If notifications are not on, only calculate the Next Notification/Watering date
            plant.setNextNotificationDate(calculateNextNotificationDate(plant.getWateringFrequency(), plant.getLastWatering()));
        }

        mDatabaseRef.child(plant.getPlantId()).setValue(plant).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(mActivity, "Plant updated", Toast.LENGTH_SHORT).show();
                // If the user is not updating the plant's image, the plant has been updated, bu we still need to create an alarm
                if(!isNewImage) {
                    callbackOnSuccess();
                }
                else {
                    // If the user updated an image, at this point the picture has been committed to Storage and an alarm has been
                    // created. The only thing left is navigating to the previous fragment.
                    mActivity.onBackPressed();
                }
            }
        });
    }

    /**
     * Create a new alarm and navigate to the previous fragment.
     * This is called if the user did not update the plant's image
     */
    private void callbackOnSuccess() {
        if(mNotificationsAllowed && mImportantInfoUpdated) {
            SharedPreferences countPref = getActivity().getSharedPreferences("AlarmCounter", Context.MODE_PRIVATE);
            mAlarmCounter = countPref.getInt("mAlarmCounter", 0);
            plant.setNotificationCode(mAlarmCounter);

            mActivity.createAlarm(plant);

            mAlarmCounter++;
            SharedPreferences.Editor editor = countPref.edit();
            editor.putInt("mAlarmCounter", mAlarmCounter);
            editor.apply();
        }
        mActivity.onBackPressed();
    }

    /**
     * Depending on the user's input of the watering frequency and when was the plant water the last time,
     * a reasonable date for should the plant be watered next is calculated.
     * @param wateringFrequency How often should the plant be watered
     * @param lastWatering When was the plant watered the last time
     * @return A reasonable date for when should the plant be watered next
     */
    private long calculateNextNotificationDate(int wateringFrequency, int lastWatering) {
        // The time of the plant adding, if the plant has been watered many days ago and it should
        // have been watered by now, return now.
        Calendar rightNow = Calendar.getInstance(); //current date and time

        // The plant has been watered some days ago, but it should be watered in the future
        if(lastWatering < wateringFrequency) {
            rightNow.add(Calendar.DAY_OF_WEEK, wateringFrequency - lastWatering); //CORRECT
            //rightNow.add(Calendar.MINUTE, wateringFrequency-lastWatering); //TEST

            // The watered has been watered now, so it should be watered after (watering frequency) days
        } else if (lastWatering == 0) {
            rightNow.add(Calendar.DAY_OF_WEEK, wateringFrequency); //CORRECT
            //rightNow.add(Calendar.MINUTE, wateringFrequency); TEST
        }

        return rightNow.getTimeInMillis();
    }

    /**
     * Validate the user input
     * @return If input is valid
     */
    private boolean inputValidation() {
        boolean valid = true;
        if (TextUtils.isEmpty(mPlantName.getEditText().getText().toString().trim())) {
            mPlantName.setError("Please enter Plant Name");
            mPlantName.requestFocus();
            valid = false;
        }else {
            mPlantName.setError(null);
        }
        if(TextUtils.isEmpty(mPlantSpecie.getEditText().getText().toString().trim())) {
            mPlantSpecie.setError("Please enter Plant Specie");
            mPlantSpecie.requestFocus();
            valid = false;
        }else {
            mPlantSpecie.setError(null);
        }
        if(TextUtils.isEmpty(mLastDays.getEditText().getText().toString().trim())) {
            mLastDays.setError("Please enter Last Watering");
            mLastDays.requestFocus();
            valid = false;
        }else {
            mLastDays.setError(null);
        }
        if(TextUtils.isEmpty(mTempStart.getEditText().getText().toString().trim())) {
            mTempStart.setError("Please enter Temperature");
            mTempStart.requestFocus();
            valid = false;
        }else {
            mTempStart.setError(null);
        }
        if(TextUtils.isEmpty(mTempEnd.getEditText().getText().toString().trim())) {
            mTempEnd.setError("Please enter Temperature");
            mTempEnd.requestFocus();
            valid = false;
        }else {
            mTempEnd.setError(null);
        }

        return valid;
    }
}
