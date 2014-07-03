package co.sveinung.watchtest;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.activity.InsetActivity;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sveinung on 01.07.14.
 */
public class PlayerActivity extends Activity {
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private static final long TIMEOUT_MS = 10000;
    private DismissOverlayView dismissOverlayView;
    private GestureDetector longTouchDetector;
    private MetaView metaView;
    private GoogleApiClient apiClient;
    private Button actionButton;
    private boolean nowPlaying;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Toast.makeText(this, "Created!", Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_player);

        dismissOverlayView = (DismissOverlayView) findViewById(R.id.player_dismiss_overlay);
        dismissOverlayView.setIntroText("Long press to dismiss!");
        dismissOverlayView.showIntroIfNecessary();
        longTouchDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                dismissOverlayView.show();
            }
        });
        metaView = new MetaView(findViewById(R.id.player_root));
        actionButton = (Button) findViewById(R.id.player_action);
        actionButton.setOnClickListener(onActionClickedListener);

        apiClient = new GoogleApiClient.Builder(this, onConnectedListener, onConnectionFailedListener).addApi(Wearable.API).build();
        apiClient.connect();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Toast.makeText(this, "New intent", Toast.LENGTH_LONG).show();
        super.onNewIntent(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return longTouchDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private final GoogleApiClient.ConnectionCallbacks onConnectedListener = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(apiClient, onDataChangedListener);
            Wearable.MessageApi.addListener(apiClient, onMessageListener);

        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private final GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    };

    private DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for (DataEvent event : dataEvents) {
                Log.d(TAG, "onDataChanged event: " + event);
                if (Data.PATH_META.equals(event.getDataItem().getUri().getPath())) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    final String text = dataMapItem.getDataMap().getString(Data.KEY_META_TEXT);
                    Asset asset = dataMapItem.getDataMap().getAsset(Data.KEY_META_ASSET);
                    Bitmap albumArt = loadBitmapFromAsset(asset);
                    metaView.setMeta(text, albumArt);
                }
            }
        }
    };

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                apiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(apiClient, asset).await().getInputStream();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    public MessageApi.MessageListener onMessageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            Log.d(TAG, "onMessageReceived: " + messageEvent);
            if (Message.PATH_STATE.equals(messageEvent.getPath())) {
                byte playingState = messageEvent.getData()[0];
                setNowPlaying(playingState == Message.STATE_PLAYING);
            }
        }
    };

    public void setNowPlaying(final boolean nowPlaying) {
        actionButton.post(new Runnable() {
            @Override
            public void run() {
                if (nowPlaying) {
                    actionButton.setText("Stop");
                }
                else {
                    actionButton.setText("Play");
                }
            }
        });

        this.nowPlaying = nowPlaying;
    }

    private final View.OnClickListener onActionClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            byte payload = nowPlaying ? Message.CONTROL_STOP : Message.CONTROL_PLAY;
            Log.d(TAG, "Send action: " + nowPlaying + " " + payload);
            WearUtils.broadCastMessageAsync(apiClient, Message.PATH_CONTROL, payload);
        }
    };






}
