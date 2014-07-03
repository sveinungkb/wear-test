package co.sveinung.watchtest;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by sveinung on 01.07.14.
 */
public class WearListenerService extends WearableListenerService {

    private static final String TAG = WearListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(TAG, "onMessageEvent: " + messageEvent);
        if (messageEvent.getPath().equals(Message.PATH_LAUNCH)) {
            startApp();
        }
    }


    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.d(TAG, "onPeerConnected: " + peer);
    }

    private void startApp() {
        Intent i = new Intent(this, PlayerActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

}
