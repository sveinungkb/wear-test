package co.sveinung.watchtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.wearable.activity.InsetActivity;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by sveinung on 01.07.14.
 */
public class PlayerActivity extends InsetActivity {
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private DismissOverlayView dismissOverlayView;
    private GestureDetector longTouchDetector;
    private MetaView metaView;
    private GoogleApiClient apiClient;

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
        metaView = new MetaView(findViewById(R.id.player_meta));
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
                    byte[] byteArray = dataMapItem.getDataMap().getByteArray(Data.KEY_META_IMAGE);
                    Bitmap albumArt = null;
                    if (byteArray != null && byteArray.length > 0) {
                        albumArt = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    }
                    metaView.setMeta(text, albumArt);
                }
            }
        }
    };
}
