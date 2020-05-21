package com.example.digitalgarden;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Class that sends Notificatinons
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_WATER = "action_water";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the details of the plant
        String plantName = intent.getStringExtra("PlantName");
        String plantId = intent.getStringExtra("PlantId");
        int notificationCode = intent.getIntExtra("AlarmCode", 0);

        // Create Intent for Dashboard Notification for when the user clicks on the Notification
        Intent waterIntent = new Intent(context, DashboardActivity.class);
        waterIntent.putExtra("PlantId", plantId);
        waterIntent.putExtra("NotificationCode", notificationCode);
        waterIntent.setAction(ACTION_WATER);
        PendingIntent waterPI = PendingIntent.getActivity(context, notificationCode, waterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create Notification Manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("WATER_ID",
                    "WATER_CHANNEL",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }

        // Create Notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "WATER_ID")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_water_icon) // notification icon
                .setContentTitle(plantName) // title for notification
                .setContentText("Water the plant!")// message for notification
                .setAutoCancel(true) // clear notification after click
                .addAction(R.drawable.ic_water_icon, "WATER", waterPI);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(notificationCode, mBuilder.build());

        // Set "Needs Water" flag to true to display the icon on the plant card
        // Commit the flag to the Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("Plant").child(plantId);
        myRef.child("needsWater").setValue(true);
    }
}
