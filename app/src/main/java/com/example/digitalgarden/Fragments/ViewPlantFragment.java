package com.example.digitalgarden.Fragments;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.digitalgarden.DashboardActivity;
import com.example.digitalgarden.Model.Plant;
import com.example.digitalgarden.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * This fragment displays the details of a plant and allows the user to edit/water/delete the plant.
 */
public class ViewPlantFragment extends Fragment implements View.OnClickListener {
    public Plant plant;

    // Firebase variables
    private FirebaseDatabase mDatabase;
    private DatabaseReference mPlantsDb;
    private DatabaseReference mUserPlantsDb;
    private FirebaseAuth mAuth;

    private DashboardActivity mActivity;
    private Boolean mNotificationsAllowed;
    private View mRootView;

    // UI variables
    private ImageView mImgView;
    private TextView mPlantName, mNotes, mWaterDate, mTemperatures, mSoilType, mLightLevel;
    private FloatingActionButton mFabMenu, mFabEdit, mFabDelete, mFabWater;
    private boolean mIsFABOpen = false;
    private Animation mFab_open, mFab_close;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_view_plant, container, false);

        // Get the parent mActivity
        mActivity = (DashboardActivity) getActivity();

        // Initialise Firebase variables
        mDatabase = FirebaseDatabase.getInstance();
        mPlantsDb = mDatabase.getReference().child("Plant");
        mUserPlantsDb = mDatabase.getReference().child("userPlant");
        mAuth = FirebaseAuth.getInstance();

        // Initialise variable for boolean that checks if notification are on or off
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("NotificationsOnOff", 0);
        mNotificationsAllowed = sharedPreferences.getBoolean("switchValue", true);

        fetchUI();

        return mRootView;
    }

    /**
     * {@inheritDoc}
     * @param v The view that has been clicked
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fabMenu :
                if (!mIsFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
                break;
            case R.id.fabEdit:
                editPlant(plant);
                break;
            case R.id.fabDelete:
                deletePlant(plant);
                break;
            case R.id.fabWater:
                waterPlant(plant);
                break;
        }
    }

    //Initialise UI
    /**
     * Initialise all the views in the fragment and make
     * extra configurations where needed.
     */
    private void fetchUI() {
        mImgView = mRootView.findViewById(R.id.img_view);
        mPlantName = mRootView.findViewById(R.id.name_and_specie);
        mNotes = mRootView.findViewById(R.id.notes_view);
        mWaterDate = mRootView.findViewById(R.id.water_date_view);
        mTemperatures = mRootView.findViewById(R.id.temperatures_view);
        mSoilType = mRootView.findViewById(R.id.soil_view);
        mLightLevel = mRootView.findViewById(R.id.light_view);
        mFabMenu = mRootView.findViewById(R.id.fabMenu);
        mFabEdit = mRootView.findViewById(R.id.fabEdit);
        mFabDelete = mRootView.findViewById(R.id.fabDelete);
        mFabWater = mRootView.findViewById(R.id.fabWater);

        // Add animation to fabs
        mFab_open = AnimationUtils.loadAnimation(mActivity.getBaseContext(), R.anim.fab_open);
        mFab_close = AnimationUtils.loadAnimation(mActivity.getBaseContext(),R.anim.fab_close);

        // Add onClick listeners to fabs
        mFabMenu.setOnClickListener(this);
        mFabEdit.setOnClickListener(this);
        mFabDelete.setOnClickListener(this);
        mFabWater.setOnClickListener(this);

        // Initialise UI with data
        initialiseUI();
    }

    /**
     * Populate UI with plant's details
     */
    private void initialiseUI() {
        SpannableString content = new SpannableString(getString(R.string.name_and_specie, plant.getPlantName(), plant.getPlantSpecie()));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        mPlantName.setText(content);
        mNotes.setText(plant.getNotes());

        Picasso.with(mActivity.getBaseContext()).load(plant.getImageSrc())
                .resize(1000, 1000)
                .centerInside()
                .into(mImgView);
        //String firstNotificationDateString = new SimpleDateFormat("EEEE, MMM d").format(plant.getNextNotificationDate());
        String firstNotificationDateString = new SimpleDateFormat("EEEE, MMM d - hh:mm").format(plant.getNextNotificationDate());
        mWaterDate.setText(firstNotificationDateString); //this is not correct
        mTemperatures.setText(plant.getMinTemp() + " - " + plant.getMaxTemp());
        mSoilType.setText(plant.getSoilType());
        mLightLevel.setText(plant.getLightLevel());
    }


    // PLANT OPERATIONS
    /**
     * Opens the Edit Plant Fragment
     * @param plant The plant that will be updated
     */
    private void editPlant(Plant plant) {
        EditPlantFragment editPlantfragment = new EditPlantFragment();
        editPlantfragment.plant = plant;
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.plants_display, editPlantfragment).addToBackStack(null);
        transaction.commit();
    }

    /**
     * Deletes a plant and cancel existing alarms for the plant
     * @param plant The plant that will be deleted
     */
    private void deletePlant(final Plant plant) {
        if(mNotificationsAllowed) {
            mActivity.cancelAlarm(plant);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Are you sure you want to delete " + plant.getPlantName()+"?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String key = plant.getPlantId();

                        // Store the image source before deleting the plant
                        String pictureURI = plant.getImageSrc();

                        // Delete the plant's details from Firebase
                        mPlantsDb = mPlantsDb.child(key);
                        mPlantsDb.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(mActivity.getBaseContext(), "Plant deleted", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Delete the plant entry assigned to the current user
                        mUserPlantsDb = mUserPlantsDb.child(mAuth.getUid()).child(key);
                        mUserPlantsDb.removeValue();

                        // Delete plant picture from Storage
                        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(pictureURI);
                        storageReference.delete();
                        mActivity.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .show();
    }

    /**
     * Waters plant. Calls (water plant) within Dashboard Activity
     * Same functionality as performing notification action "Water".
     * @param plant The plant that will be watered
     */
    private void waterPlant(final Plant plant) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM d");
        if(plant.getNeedsWater()){
            new AlertDialog.Builder(getContext())
                    .setTitle("Are you sure you want to water " + plant.getPlantName()+", now?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Close open notifications
                            NotificationManager notificationManager = (NotificationManager) mActivity.getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(plant.getNotificationCode());

                            // Water the plant
                            mActivity.waterPlant(plant, mNotificationsAllowed);
                            Toast.makeText(mActivity.getBaseContext(), plant.getPlantName() + " has been watered!", Toast.LENGTH_SHORT).show();

                            // Refresh UI
                            initialiseUI();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .show();
        } else {
            new AlertDialog.Builder(getContext())
                    .setTitle(plant.getPlantName() + "is already watered! Next watering day is: " + formatter.format(plant.getNextNotificationDate()))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .show();
        }
    }


    //Floating Action Buttons Animation

    /**
     * Configure Animation for when the main Fab opens
     */
    private void showFABMenu() {
        mIsFABOpen = true;
        mFabMenu.animate().rotation(-180);
        mFabEdit.setVisibility(View.VISIBLE);
        mFabDelete.setVisibility(View.VISIBLE);
        mFabWater.setVisibility(View.VISIBLE);
        mFabEdit.startAnimation(mFab_open);
        mFabDelete.startAnimation(mFab_open);
        mFabWater.startAnimation(mFab_open);
    }

    /**
     * Configure Animation for when the main Fab closes
     */
    private void closeFABMenu() {
        mIsFABOpen = false;
        mFabMenu.animate().rotation(0);
        mFabEdit.startAnimation(mFab_close);
        mFabDelete.startAnimation(mFab_close);
        mFabWater.startAnimation(mFab_close);
    }
}
