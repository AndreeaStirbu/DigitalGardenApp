package com.example.digitalgarden;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.digitalgarden.Authentication.SignOptionsActivity;
import com.example.digitalgarden.Fragments.PlantsViewFragment;
import com.example.digitalgarden.Fragments.ProfileFragment;
import com.example.digitalgarden.Model.Plant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static com.example.digitalgarden.AlarmReceiver.ACTION_WATER;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * This is the main activity of the app. This is where I retrieve the data from Firebase and make it available for all the
 * other fragments. Handle Drawer and Notifications functionality.
 */
public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public ArrayList<Plant> plantsList;

    // Firebase variables
    private FirebaseDatabase mDatabase;
    private DatabaseReference mPlantsDb;
    private DatabaseReference mUserPlantsDb;

    // Login variables
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mG_acc;
    private FirebaseAuth mAuth;
    private FirebaseUser mF_acc;

    // UI variables
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private PlantsViewFragment mViewPlantsFragment;

    private boolean mNotificationsAllowed;
    private Set<String> mPlantToWaterIDs = new HashSet<>();
    private int mAlarmCounter = 0;
    private boolean mAfterSignIn = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialise Login variables
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mG_acc = GoogleSignIn.getLastSignedInAccount(this);

        mAuth = FirebaseAuth.getInstance();
        mF_acc = mAuth.getCurrentUser();

        // Initialise database variables
        mDatabase = FirebaseDatabase.getInstance();
        mPlantsDb = mDatabase.getReference().child("Plant");
        mUserPlantsDb = mDatabase.getReference().child("userPlant");

        // Initialise variable for boolean that checks if notification are on or off
        SharedPreferences sharedPreferences = getSharedPreferences("NotificationsOnOff", 0);
        mNotificationsAllowed = sharedPreferences.getBoolean("switchValue", true);

        // Load the fragment containing the list of plants
        mViewPlantsFragment = new PlantsViewFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.load_fragment, mViewPlantsFragment);
        transaction.commit();

        // Retrieve data from Firebase database
        loadData();

        // Initialise the data within the drawer
        setDrawer();

        // Handle Notification action
        Intent intent = getIntent();
        String action = intent.getAction();
        if (ACTION_WATER.equals(action)) {
            handleNotificationAction(intent);
        }

        // Reschedule notifications if the user sign in just now
        mAfterSignIn = intent.getBooleanExtra("afterSignIn",false);
    }

    // Loading data
    /**
     * Retrieve the plants for the logged user from Firebase
     * These plants are added into a list used to display the list of plants.
     * Therefore, this method is called every time an operation (add, edit, delete, water)
     * is performed on a plant.
     * */
    private void loadData() {
        final ArrayList<String> plantsForUser = new ArrayList<>();
        plantsList = new ArrayList<>();
        mUserPlantsDb = mUserPlantsDb.child(mAuth.getUid());

        mUserPlantsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                plantsForUser.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String plantID = dataSnapshot1.getKey();
                    plantsForUser.add(plantID);
                }
                plantsList.clear();
                for (int i = 0; i < plantsForUser.size(); i++) {
                    mPlantsDb.child(plantsForUser.get(i)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot2) {
                            Plant value = dataSnapshot2.getValue(Plant.class);

                            // Add plants that will be displayed in the View Plants fragment
                            addPlantToList(value);

                            if(value != null) {
                                /* When a notification is sent, a new Dashboard Activity intent is created.
                                 * Therefore, the data must be retrieved again from Firebase in order to handle
                                 * the Notification action. Once the data of the plant that needs to be watered
                                 * has loaded, the plant can be watered.
                                 */
                                if(mPlantToWaterIDs.contains(value.getPlantId())) {
                                    mPlantToWaterIDs.remove(value.getPlantId());
                                    waterPlant(value);
                                }

                                /* If the notifications are off, inform the user that the plant needs
                                 * to be watered if it's past "Watering Day" and display the "Needs Water"
                                 * icon
                                 */
                                if(!mNotificationsAllowed && value.getNextNotificationDate() < Calendar.getInstance().getTimeInMillis()) {
                                    mPlantsDb.child(value.getPlantId()).child("needsWater").setValue(true);
                                }
                            }

                            // Once the data is retrieved from Firebase, load the list of plants inside
                            // the View Plants fragment
                            mViewPlantsFragment.setRecycleView();

                            // If the user signed in now, allow it to send notifications by rescheduling alarms for all the plants
                            if(mAfterSignIn && plantsList.size() == plantsForUser.size()) {
                                rescheduleNewAlarmsForAllPlants();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.w("Dashboard Activity", "Failed to load the plant's details.", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Dashboard Activity", "Failed to load the user's plants.", databaseError.toException());
            }
        });
    }

    /**
     * Avoid adding duplicates in the list of plant.
     * E.g. when updating a plant, don't display the old and the new version of the plant,
     * store only the updated version
     * @param plant A Plant object that stores plant data
     */
    private void addPlantToList(Plant plant) {
        boolean exists = false;
        if(plant != null) {
            for (int i = 0; i < plantsList.size(); i++) {
                if (plantsList.get(i).getPlantId().equals(plant.getPlantId())) {
                    plantsList.set(i, plant);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                plantsList.add(plant);
            }
        }
    }


    // Drawer methods
    /**
     * {@inheritDoc}
     * Handle actions for menu items inside the drawer.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.load_fragment, new ProfileFragment()).addToBackStack(null);
                transaction.commit();
                mDrawer.closeDrawers();
                break;
            case R.id.support:
                break;
            case R.id.logout:
                signOut();
                break;
            default:
                return false;
        }
        return false;
    }

    /**
     * Set up the Drawer and handle actions for Notifications switch button.
     */
    private void setDrawer() {
        mDrawer = findViewById(R.id.drawer);
        mNavigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawOpen, R.string.drawClose);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        initialiseDrawerData();
        SwitchCompat drawerSwitch = (SwitchCompat) mNavigationView.getMenu().findItem(R.id.notifications).getActionView();
        drawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save state of Notification switch button in Shared Preferences
                SharedPreferences sharedPreferences = getSharedPreferences("NotificationsOnOff", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("switchValue", isChecked);
                editor.apply();

                // Activate/Deactivate alarms for all plants
                if (!isChecked) {
                    cancelAllScheduledAlarms();
                } else {
                    rescheduleNewAlarmsForAllPlants();
                }
            }
        });
    }

    /**
     * Get the user's details from Google and Firebase accounts.
     * Call initialiseDrawerUI with these details.
     */
    public void initialiseDrawerData() {
        if (mG_acc != null) {
            initialiseDrawerUI(mG_acc.getDisplayName(), mG_acc.getEmail(), mG_acc.getPhotoUrl());
            Menu nav_Menu = mNavigationView.getMenu();
            nav_Menu.findItem(R.id.profile).setVisible(false);
        }

        if (mF_acc != null) {
            for (UserInfo profile : mF_acc.getProviderData()) {
                initialiseDrawerUI(profile.getDisplayName(), profile.getEmail(), profile.getPhotoUrl());
            }
        }
    }

    /**
     *  Populate UI in Drawer header with user's details.
     * @param name The logged user's display name
     * @param email The logged user's email
     * @param photoUrl url giving the location of the user's profile
     */
    private void initialiseDrawerUI(String name, String email, Uri photoUrl) {
        View mDrawerHeaderView = mNavigationView.getHeaderView(0);
        ImageView profile_picture = mDrawerHeaderView.findViewById(R.id.profile_picture);
        TextView profile_name = mDrawerHeaderView.findViewById(R.id.profile_name);
        TextView profile_email = mDrawerHeaderView.findViewById(R.id.profile_email);

        profile_name.setText(name);
        profile_email.setText(email);
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .centerCrop()
                    .dontAnimate()
                    .into(profile_picture);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("NotificationsOnOff", 0);
        boolean notificationsAllowed = sharedPreferences.getBoolean("switchValue", true);
        SwitchCompat drawerSwitch = (SwitchCompat) mNavigationView.getMenu().findItem(R.id.notifications).getActionView();
        drawerSwitch.setChecked(notificationsAllowed);
    }

    /**
     * Sign out from Google/Firebase account when pressing Log Out button in Drawer.
     */
    private void signOut() {
        if(mAuth != null) {
            mAuth.signOut();
            Intent signInOptions = new Intent(DashboardActivity.this, SignOptionsActivity.class);
            DashboardActivity.this.startActivity(signInOptions);
        }

        if(mG_acc != null) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(),"Logged out",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }

        // Cancel all alarms. If the user signs out, he shouldn't receive any other notifications.
        cancelAllScheduledAlarms();
    }


    // Notifications methods
    /**
     *  Create an alarm manager that will trigger/send a notification when
     *  Next Notification Date (Watering date) arrives
     * @param plant A Plant object that stores plant data
     */
    public void createAlarm(Plant plant) {
        // Prepare AlarmReceiver intent
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("PlantName", plant.getPlantName());
        intent.putExtra("PlantId", plant.getPlantId());
        intent.putExtra("AlarmCode", plant.getNotificationCode());

        // Create Pending Intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, plant.getNotificationCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create Alarm Manager
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        /*
         * Create a repeating alarm that will send a notification when the next notification date
         * (watering date) is reached, and if the user does not water the plant, a new notification
         * will be sent every day to get the user's attention.
         */
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                plant.getNextNotificationDate(), //should be get next notification date
                60000*60*24, //should be one day
                pendingIntent);

        Log.i("Alarm: ", "created, id: " + plant.getNotificationCode());
    }

    /**
     * Cancel the alarm manager that schedules alarms for the plant.
     * @param plant A Plant object that stores plant data
     */
    public void cancelAlarm(Plant plant) {
        Intent prevIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, plant.getNotificationCode(), prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(prevPendingIntent);
        Log.i("Alarm: ", "canceled, id: " + plant.getNotificationCode());
    }

    /**
     * Cancel the current alarm and water the plant. (Call waterPlant)
     * @param plant A Plant object that stores plant data
     */
    public void waterPlant(Plant plant) {
        cancelAlarm(plant);
        updateWhenWatered(plant, true);
    }

    /**
     * This is called when the "Water plant" action button withing the "View Plant" fragment is pressed.
     * @param plant A Plant object that stores plant data
     * @param notificationsOn If the notifications are on or off
     */
    public void waterPlant(Plant plant, Boolean notificationsOn) {
        if(notificationsOn) {
            cancelAlarm(plant);
        }
        updateWhenWatered(plant, notificationsOn);
    }

    /**
     * Update the "Needs Water" flag to true so the "water needed" icon is set to visible.
     * Schedule the new alarm.
     * @param plant A Plant object that stores plant data
     * @param notificationsOn If the notifications are on or off
     */
    public void updateWhenWatered(Plant plant, Boolean notificationsOn) {
        Calendar c = Calendar.getInstance();
        //TODO: Change to Day from Minute
        c.add(Calendar.DATE, plant.getWateringFrequency()); //GOOD VERSION
        //c.add(Calendar.MINUTE, plant.getWateringFrequency()); //TESTING
        plant.setNeedsWater(false);
        plant.setNextNotificationDate(c.getTimeInMillis());

        if(notificationsOn) {
            // Get current Alarm Code
            SharedPreferences countPref = getSharedPreferences("AlarmCounter", Context.MODE_PRIVATE);
            mAlarmCounter = countPref.getInt("alarmCounter", 0);
            plant.setNotificationCode(mAlarmCounter);

            // Create new alarm
            createAlarm(plant);

            // Commit changes to Firebase
            mPlantsDb.child(plant.getPlantId()).setValue(plant);

            // Increase Alarm Code Counter
            mAlarmCounter++;
            SharedPreferences.Editor editor = countPref.edit();
            editor.putInt("alarmCounter", mAlarmCounter);
            editor.apply();
        } else {
            // Commit changes to Firebase
            mPlantsDb.child(plant.getPlantId()).setValue(plant);
        }
    }

    /**
     * When the user turns the notifications off, all the existing alarms for each plant should be canceled
     * to prevent sending future notifications.
     */
    private void cancelAllScheduledAlarms(){
        for(int i = 0; i < plantsList.size(); i++) {
            cancelAlarm(plantsList.get(i));
            mPlantsDb.child(plantsList.get(i).getPlantId()).child("notificationCode").setValue(-1);
        }

        // Reset the alarm code counter
        SharedPreferences countPref = getSharedPreferences("AlarmCounter", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = countPref.edit();
        editor.putInt("alarmCounter", 0);
        editor.apply();
    }

    /**
     * When the user turns the notifications back on, alarms are scheduled for all the existing plants so that
     * future notifications will be sent.
     */
    private void rescheduleNewAlarmsForAllPlants(){
        long now = Calendar.getInstance().getTimeInMillis();
        for(int i = 0; i < plantsList.size(); i++) {
            SharedPreferences countPref = getSharedPreferences("AlarmCounter", Context.MODE_PRIVATE);
            mAlarmCounter = countPref.getInt("alarmCounter", 0);
            plantsList.get(i).setNotificationCode(mAlarmCounter);
            if(plantsList.get(i).getNextNotificationDate() < now) {
                plantsList.get(i).setNextNotificationDate(now);
                mPlantsDb.child(plantsList.get(i).getPlantId()).child("nextNotificationDate").setValue(now);
            }
            mPlantsDb.child(plantsList.get(i).getPlantId()).child("notificationCode").setValue(mAlarmCounter);
            createAlarm(plantsList.get(i));
            mAlarmCounter++;
            SharedPreferences.Editor editor = countPref.edit();
            editor.putInt("alarmCounter", mAlarmCounter);
            editor.apply();
        }

        mAfterSignIn = false;
    }

    /**
     * When a notification is sent and the user is executing the "Water" action, a new DashboardActivity intent is created
     * and at this point the plants' details are not loaded into the app.
     * Therefore, I store the plant ID of the plant that requires watering in mPlantToWaterIDs map.
     * Inside loadData() method, when the plant is loaded, I am executing the "Water" action and remove the plant from the map.
     * @param intent A Dashboard Activity intent created by the Alarm Receiver
     */
    private void handleNotificationAction(Intent intent) {
        String plantId = intent.getStringExtra("PlantId");
        int notificationCode = intent.getIntExtra("NotificationCode", 0);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationCode);
        mPlantToWaterIDs.add(plantId);
    }
}