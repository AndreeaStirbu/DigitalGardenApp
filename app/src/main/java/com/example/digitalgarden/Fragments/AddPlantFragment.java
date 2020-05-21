package com.example.digitalgarden.Fragments;
import android.Manifest;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.digitalgarden.DashboardActivity;
import com.example.digitalgarden.Model.Plant;
import com.example.digitalgarden.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Calendar;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Fragment that is adding a Plant object to Firebase.
 */
public class AddPlantFragment extends PhotoFragment implements View.OnClickListener {
    private Plant mPlant;
    private DashboardActivity mActivity;
    private boolean mNotificationsAllowed;

    // Firebase variables
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private DatabaseReference mUsersPlant;

    // UI variables
    private AutoCompleteTextView mSoilDropDown, mLightDropDown;
    private SeekBar mSeekbar;
    private TextView mShowSeekbarProgress;
    private TextInputLayout mPlantName, mPlantSpecie, mLastDays, mTempStart, mTempEnd, mNotes;
    private TextInputEditText mTempStartEditText, mTempEndEditText, mLastDaysEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_add_plant, container, false);

        // Get the parent mActivity
        mActivity = (DashboardActivity) getActivity();

        // Initialise Firebase variables
        mStorageRef = FirebaseStorage.getInstance().getReference("Thumbnails");
        mDatabase = FirebaseDatabase.getInstance().getReference("Plant");
        mUsersPlant = FirebaseDatabase.getInstance().getReference("userPlant");

        // Initialise variable for boolean that checks if notification are on or off
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("NotificationsOnOff", 0);
        mNotificationsAllowed = sharedPreferences.getBoolean("switchValue", true);

        // Initialise Plant object
        mPlant = new Plant();

        // Initialise all the views withing the form
        fetchUI(rootView);

        return  rootView;
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
            case R.id.btnOpenGallery:
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
            case R.id.btnOpenCamera:
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
                break;
            case R.id.btnUpload:
                if(inputValidation()) addPlant();
        }
    }

    /**
     * Initialise all the views in the fragment and make extra configurations where needed.
     * @param rootView The inflated layout of the fragment
     */
    private void fetchUI(View rootView) {
        mPlantName = rootView.findViewById(R.id.plant_name);
        mPlantSpecie = rootView.findViewById(R.id.plant_specie);
        mLastDays = rootView.findViewById(R.id.last_water_inputLayout);
        mTempStart = rootView.findViewById(R.id.temp_start_it);
        mTempEnd = rootView.findViewById(R.id.temp_end_it);

        mLastDaysEditText = rootView.findViewById(R.id.last_water);
        mTempStartEditText = rootView.findViewById(R.id.temperature_start);
        mTempEndEditText =  rootView.findViewById(R.id.temperature_end);
        mLastDaysEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mTempStartEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mTempEndEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout addFormLayout = rootView.findViewById(R.id.add_plant_form);
        addFormLayout.bringToFront();

        mSeekbar = rootView.findViewById(R.id.waterFrequency);
        mShowSeekbarProgress = rootView.findViewById(R.id.showSeekbarProgress);
        mShowSeekbarProgress.setText(String.valueOf(mSeekbar.getProgress()));
        TextView minSeekbar = rootView.findViewById(R.id.minSeekbar);
        minSeekbar.setText(String.valueOf(mSeekbar.getMin()));
        TextView maxSeekbar = rootView.findViewById(R.id.maxSeekbar);
        maxSeekbar.setText(String.valueOf(mSeekbar.getMax()));

        ImageButton btnOpenGallery = rootView.findViewById(R.id.btnOpenGallery);
        ImageButton btnOpenCamera = rootView.findViewById(R.id.btnOpenCamera);
        Button btnUpload = rootView.findViewById(R.id.btnUpload);
        imgView = rootView.findViewById(R.id.imgView);
        mNotes = rootView.findViewById(R.id.notes);
        TextInputEditText notes_editText = rootView.findViewById(R.id.notes_editText);
        notes_editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        btnOpenGallery.setOnClickListener(this);
        btnOpenCamera.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        ArrayAdapter<String> soilAdapter =
                new ArrayAdapter<>(
                        mActivity.getBaseContext(),
                        R.layout.dropdown_menu_popup_item,
                        SOIL_TYPES);

        mSoilDropDown = rootView.findViewById(R.id.soil_moisture);
        mSoilDropDown.setInputType(InputType.TYPE_NULL);
        mSoilDropDown.setAdapter(soilAdapter);

        ArrayAdapter<String> lightAdapter =
                new ArrayAdapter<>(
                        mActivity.getBaseContext(),
                        R.layout.dropdown_menu_popup_item,
                        LIGHT_LEVEL);

        mLightDropDown = rootView.findViewById(R.id.light_level);
        mLightDropDown.setInputType(InputType.TYPE_NULL);
        mLightDropDown.setAdapter(lightAdapter);

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


    // Add a plant
    /**
     * Store Plant image in Firebase Storage and add Plant object to Firebase
     */
    private void addPlant() {
        final StorageReference Ref = mStorageRef.child(System.currentTimeMillis() + "." + getExtension(imgUri));
        Ref.putFile(imgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mPlant.setImageSrc(uri.toString());
                                createDatabaseObject();

                                if(mNotificationsAllowed) {
                                    mActivity.createAlarm(mPlant);
                                }
                                mActivity.onBackPressed();
                            }
                        });
                    }
                });
    }

    /**
     *  A Plant object is initialised with the user input and committed to Firebase
     */
    private void createDatabaseObject() {
        String plantId = mDatabase.push().getKey();

        mPlant.setPlantId(plantId);
        mPlant.setPlantName(mPlantName.getEditText().getText().toString().trim());
        mPlant.setPlantSpecie(mPlantSpecie.getEditText().getText().toString().trim());
        mPlant.setLastWatering(Integer.parseInt(mLastDaysEditText.getText().toString().trim()));
        mPlant.setWateringFrequency(mSeekbar.getProgress());
        mPlant.setSoilType(mSoilDropDown.getText().toString().trim());
        mPlant.setLightLevel(mLightDropDown.getText().toString().trim());
        mPlant.setMinTemp(Integer.parseInt(mTempStartEditText.getText().toString().trim()));
        mPlant.setMaxTemp(Integer.parseInt(mTempEndEditText.getText().toString().trim()));
        mPlant.setNotes(mNotes.getEditText().getText().toString().trim());
        mPlant.setNeedsWater(false);
        mPlant.setNextNotificationDate(calculateNextNotificationDate(mPlant.getWateringFrequency(), mPlant.getLastWatering()));

        if(mNotificationsAllowed) {
            // Retrieve the current alarm counter (code)
            SharedPreferences countPref = mActivity.getSharedPreferences("AlarmCounter", Context.MODE_PRIVATE);
            int alarmCounter = countPref.getInt("alarmCounter", 0);

            mPlant.setNotificationCode(alarmCounter);

            // Increase the counter for the next alarm creation
            alarmCounter++;
            SharedPreferences.Editor editor = countPref.edit();
            editor.putInt("alarmCounter", alarmCounter);
            editor.apply();
        } else {
            // If notifications are turned off, don't bother with the alarm code and set to -1 for easier recognition
            mPlant.setNotificationCode(-1);
        }

        mDatabase.child(plantId).setValue(mPlant);
        String user = FirebaseAuth.getInstance().getUid();
        mUsersPlant.child(user).child(plantId).setValue(true);
        Toast.makeText(getActivity(),"Plant added successfully",Toast.LENGTH_SHORT).show();
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
            //rightNow.add(Calendar.MINUTE, wateringFrequency-lastWatering); //TESTING

        // The watered has been watered now, so it should be watered after (watering frequency) days
        } else if (lastWatering == 0) {
            rightNow.add(Calendar.DAY_OF_WEEK, wateringFrequency); //CORRECT
            //rightNow.add(Calendar.MINUTE, wateringFrequency); //TESTING
        }

        return rightNow.getTimeInMillis();
    }

    /**
     * Validate the user input
     * @return If input is valid
     */
    private boolean inputValidation() {
        boolean valid = true;
        if(imgUri == null || TextUtils.isEmpty(imgUri.toString())) {
            Toast.makeText(mActivity, "Please, select an image!", Toast.LENGTH_LONG).show();
            valid = false;
        }
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
