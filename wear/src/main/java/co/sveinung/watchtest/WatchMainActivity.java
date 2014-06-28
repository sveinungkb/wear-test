package co.sveinung.watchtest;

import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.InsetActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

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
    }

    @Override
    public void onReadyForContent() {
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                Log.d(TAG, "TextView: " + mTextView.getText() + " view=" + mTextView);
                mTextView.setText("Ready!");
            }
        });
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
                    mTextView.setText("Count is: "+ Integer.toString(count));
                }
            }
        };
    }
}
