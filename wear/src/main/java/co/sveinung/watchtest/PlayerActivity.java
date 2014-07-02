package co.sveinung.watchtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.InsetActivity;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

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

import java.util.List;

/**
 * Created by sveinung on 01.07.14.
 */
public class PlayerActivity extends InsetActivity {
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private DismissOverlayView dismissOverlayView;
    private GestureDetector longTouchDetector;
    private MetaView metaView;
    private GoogleApiClient apiClient;
    private Button actionButton;
    private boolean nowPlaying;
    private Handler handler = new Handler();

    @Override
    public void onReadyForContent() {
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
                    Bitmap albumArt = null;
                    if (asset != null) {
                        albumArt = BitmapFactory.decodeStream(Wearable.DataApi.getFdForAsset(apiClient, asset).await().getInputStream());
                    }
                    metaView.setMeta(text, albumArt);
                }
            }
        }
    };

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

    public void setNowPlaying(boolean nowPlaying) {
        if (nowPlaying) {
            actionButton.setText("Stop");
        }
        else {
            actionButton.setText("Play");
        }
        this.nowPlaying = nowPlaying;
    }

    private final View.OnClickListener onActionClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            broadcastMessage(Message.PATH_CONTROL, nowPlaying ? Message.CONTROL_STOP : Message.CONTROL_PLAY);
        }
    };

    private void broadcastMessage(final String action, final byte payload) {
        PendingResult<NodeApi.GetConnectedNodesResult> connectedNodes = Wearable.NodeApi.getConnectedNodes(apiClient);
        List<Node> nodes = connectedNodes.await().getNodes();
        for (Node node : nodes) {
            Log.d(TAG, "Sending message to: " + node);
            Wearable.MessageApi.sendMessage(apiClient, node.getId(), action, new byte[]{payload});
        }
    }


}
