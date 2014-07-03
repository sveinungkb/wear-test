package co.sveinung.watchtest;

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

public class WatchMainActivity extends InsetActivity {

    private static final String TAG = WatchMainActivity.class.getSimpleName();
    private TextView mTextView;
    private GoogleApiClient apiClient;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = new GoogleApiClient.Builder(this, onConnectedListener, onConnectionListener).addApi(Wearable.API).build();
        apiClient.connect();
        postNotification();
        Toast.makeText(this, "Finishing", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onReadyForContent() {
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setText("Ready!");
        Log.d(TAG, "TextView: " + mTextView.getText() + " view=" + mTextView);
        Log.d(TAG, "Watch ready for content!");
    }

    private final GoogleApiClient.ConnectionCallbacks onConnectedListener = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "Connected, start listening for data!");
            Wearable.DataApi.addListener(apiClient, onDataChangedListener);
        }

        @Override
        public void onConnectionSuspended(int i) {
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener onConnectionListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG, "Connection failed: " + connectionResult);
        }
    };

    public DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            Log.d(TAG, "Data changed: " + dataEvents);
            for (DataEvent event : dataEvents) {
                if (event.getType() == DataEvent.TYPE_DELETED) {
                    Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
                    handler.post(onNewCount(-1));
                } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                    Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());
                    if (event.getDataItem().getUri().getPath().endsWith(Data.PATH_COUNT)) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        int count = dataMapItem.getDataMap().getInt(Data.KEY_COUNT);
                        handler.post(onNewCount(count));
                    }
                }
            }
        }
    };

    private Runnable onNewCount(final int count) {
        return new Runnable() {
            @Override
            public void run() {
                if (mTextView != null) {
                    if (count < 0) {
                        mTextView.setText("Stopped!");
                    }
                    else {
                        mTextView.setText("Count is: " + Integer.toString(count));
                    }
                }
            }
        };
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

    private PendingIntent displayIntent() {
        Intent i = new Intent("co.sveinung.watchtest.LAUNCH");
        return PendingIntent.getBroadcast(this, 0, i, 0);
    }

    private PendingIntent launchIntent() {
        Intent intent = new Intent(this, PlayerActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
