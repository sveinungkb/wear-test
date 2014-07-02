package co.sveinung.watchtest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


public class MobileMainActivity extends Activity {


    private static final String TAG = MobileMainActivity.class.getSimpleName();
    private GoogleApiClient apiClient;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensureConnected();
    }

    private void ensureConnected() {
        if (apiClient != null && apiClient.isConnected()) {
            updateCount();
        }
        else {
            apiClient = new GoogleApiClient.Builder(this, onConnectedListener, onConnectionListener).addApi(Wearable.API).build();
            apiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sendCount(-1);
        handler.removeCallbacksAndMessages(handler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private final GoogleApiClient.ConnectionCallbacks onConnectedListener = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "Connected, start sharing data.");
            count = 0;

            updateCount();
        }

        @Override
        public void onConnectionSuspended(int i) {
            cancelSend();
        }
    };

    private void cancelSend() {
        handler.removeCallbacksAndMessages(handler);
    }

    private final Handler handler = new Handler();

    private void updateCount() {
        sendCount(count++);
        handler.postAtTime(new Runnable() {
            @Override
            public void run() {
                updateCount();
            }
        }, handler, SystemClock.uptimeMillis() + 1000);
    }

    private void sendCount(final int count) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(Data.PATH_COUNT);
        dataMap.getDataMap().putInt(Data.KEY_COUNT, count);
        PutDataRequest request = dataMap.asPutDataRequest();
        DataApi.DataItemResult result = Wearable.DataApi
                .putDataItem(apiClient, request).await();
        Log.d(TAG, "Updating count to: " + count);
    }

    private final GoogleApiClient.OnConnectionFailedListener onConnectionListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG, "Connection failed: " + connectionResult);
            showMessage("Connection failed: " + connectionResult);
            cancelSend();
        }
    };

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
