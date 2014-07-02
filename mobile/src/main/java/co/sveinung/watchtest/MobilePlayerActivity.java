package co.sveinung.watchtest;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by sveinung on 01.07.14.
 */
public class MobilePlayerActivity extends Activity {


    private static final String TAG = MobilePlayerActivity.class.getSimpleName();
    private static final int REQUEST_PICK_PICTURE = 1;
    private GoogleApiClient apiClient;
    private ImageView image;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        image = (ImageView) findViewById(R.id.player_image);
        textView = (TextView) findViewById(R.id.player_text);
        Button changeImageButton = (Button) findViewById(R.id.player_button);
        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), REQUEST_PICK_PICTURE);
            }
        });

        findViewById(R.id.player_button_launch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchWearApp();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensureConnected();
    }

    private void ensureConnected() {
        if (apiClient != null && apiClient.isConnected()) {
        } else {
            apiClient = new GoogleApiClient.Builder(this, onConnectedListener, onConnectionListener).addApi(Wearable.API).build();
            apiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
            Wearable.MessageApi.addListener(apiClient, onMessageListener);
            launchWearApp();
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

    private final GoogleApiClient.OnConnectionFailedListener onConnectionListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG, "Connection failed: " + connectionResult);
            showMessage("Connection failed: " + connectionResult);
            cancelSend();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PICTURE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            sendImage(imageUri);
        }
    }

    private void sendImage(final Uri imageUri) {
        try {
            Bitmap bitmap = getBitmapFromUri(imageUri, 320, 320);
            image.setImageBitmap(bitmap);
            sendImageToWear(bitmap);
        } catch (IOException e) {
            showMessage("Couldn't send image: " + imageUri);
            Log.d(TAG, "Failed to send image.", e);
        }
    }

    private void sendImageToWear(final Bitmap bitmap) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Asset asset = createAssetFromBitmap(bitmap);
                Log.d(TAG, "Changing image to: " + bitmap);
                PutDataMapRequest dataMap = PutDataMapRequest.create(Data.PATH_META);
                dataMap.getDataMap().putString(Data.KEY_META_TEXT, "Rihanna Radio");
                dataMap.getDataMap().putAsset(Data.KEY_META_ASSET, asset);
                PutDataRequest request = dataMap.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi
                        .putDataItem(apiClient, request).await();
                Log.d(TAG, "Send updated metadata with result:" + result);
                return null;
            }
        }.execute();
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int originalSize = bitmap.getByteCount();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private byte[] bitmapToBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        return byteBuffer.array();
    }

    private Bitmap getBitmapFromUri(final Uri uri, final int width, final int height) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getReadOnlyFileDescriptor(uri);
        Bitmap image = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
        Bitmap scaled = Bitmap.createScaledBitmap(image, width, height, false);
        image.recycle();
        parcelFileDescriptor.close();
        return scaled;
    }

    private ParcelFileDescriptor getReadOnlyFileDescriptor(final Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        return parcelFileDescriptor;
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private final MessageApi.MessageListener onMessageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            Log.d(TAG, "Got message: " + messageEvent);
            if (messageEvent.getPath().equals(Message.PATH_CONTROL)) {
                byte playing = messageEvent.getData()[0];
                sendPlayingState(playing == Message.CONTROL_PLAY);
            }
        }
    };

    private void sendPlayingState(final boolean playing) {
        textView.post(new Runnable() {
            @Override
            public void run() {
                textView.setText("Player state: " + (playing ? "playing" : "stopped"));
            }
        });
        WearUtils.broadCastMessageAsync(apiClient, Message.PATH_STATE, (byte) (playing ? 1 : 0));
    }

    private void launchWearApp() {
        if (apiClient != null && apiClient.isConnected()) {
            WearUtils.broadCastMessageAsync(apiClient, Message.PATH_LAUNCH, (byte) 0);
        }
        else {
             ensureConnected();
        }
    }
}
