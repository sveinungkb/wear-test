package co.sveinung.watchtest;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.activity.InsetActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class WatchMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postNotification();
        Toast.makeText(this, "Finishing", Toast.LENGTH_LONG).show();
        finish();
    }


    private void postNotification() {
        NotificationManagerCompat.from(this).notify(1, createNotification());
    }

    private Notification createNotification() {

        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentText("Test SKB..")
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchIntent())
                .setContentTitle("Test");

        builder.extend(new NotificationCompat.WearableExtender()
                .setDisplayIntent(notificationPendingIntent)
                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_FULL_SCREEN)
        );
        return builder.build();
    }

    private PendingIntent launchIntent() {
        Intent intent = new Intent(this, PlayerActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
