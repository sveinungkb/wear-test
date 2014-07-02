package co.sveinung.watchtest;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by sveinung on 01.07.14.
 */
public class WearUtils {
    private static final String TAG = WearUtils.class.getSimpleName();

    public static void broadCastMessageAsync(final GoogleApiClient apiClient, final String pathControl, final byte payload) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                PendingResult<NodeApi.GetConnectedNodesResult> connectedNodes = Wearable.NodeApi.getConnectedNodes(apiClient);
                List<Node> nodes = connectedNodes.await().getNodes();
                for (Node node : nodes) {
                    Log.d(TAG, "Sending message to: " + node);
                    Wearable.MessageApi.sendMessage(apiClient, node.getId(), pathControl, new byte[]{payload});
                }
                return null;
            }
        }.execute();
    }
}
