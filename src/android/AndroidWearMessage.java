package cordova.plugin.androidwearmessage;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class AndroidWearMessage extends CordovaPlugin implements MessageClient.OnMessageReceivedListener {
    private String TAG = " [ AndroidWearMessage ] ";

    private static final String CORDOVA_WEAR_MESSAGE_CAPABILITY_NAME = "cordova_wear_message";
    private static final String MESSAGE_PATH = "/cordova_message";
    private String bestNodeId = null;
    private CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("initialize")) {
            Log.d(TAG, "initialize");
            try {
                Collection<String> nodes = getNodes();
                if (!nodes.isEmpty()) {
                    setupTranscription();
                    if (bestNodeId != null) {
                        callbackContext.success("WEAR_APP_CONNECTED");
                    } else {
                        callbackContext.error("WEAR_APP_NOT_REACHABLE");
                    }
                } else {
                    callbackContext.error("NO_PAIRED_WEAR");
                }
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        } else if (action.equals("sendMessage")) {
            Log.d(TAG, "sendMessage");
            try {
                sendMessage(args.get(0).toString());
                callbackContext.success("MESSAGE_SENT");
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        } else if (action.equals("listenMessage")) {
            Log.d(TAG, "listenMessage");
            Wearable.getMessageClient(cordova.getContext()).addListener(this);
            callback = callbackContext;
        } else {
            return false;
        }
        return true;
    }

    private Collection<String> getNodes() throws ExecutionException, InterruptedException {
        HashSet<String> results = new HashSet<String>();
        List<Node> nodes = Tasks.await(Wearable.getNodeClient(cordova.getContext()).getConnectedNodes());
        for (Node node : nodes) {
            results.add(node.getId());
        }
        return results;
    }

    private void setupTranscription() throws ExecutionException, InterruptedException {
        CapabilityInfo capabilityInfo = Tasks.await(Wearable.getCapabilityClient(cordova.getContext()).getCapability(CORDOVA_WEAR_MESSAGE_CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE));
        updateCapability(capabilityInfo);
    }

    private void updateCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        bestNodeId = pickBestNodeId(connectedNodes);
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String nodeId = null;
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            nodeId = node.getId();
        }
        return nodeId;
    }

    private void sendMessage(String message) throws Exception {
        if (bestNodeId != null) {
            Tasks.await(Wearable.getMessageClient(cordova.getContext()).sendMessage(bestNodeId, MESSAGE_PATH, message.getBytes()));
        } else {
            throw new Exception("NO_AVAILABLE_NODE_WITH_CAPABILITY");
        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(MESSAGE_PATH) && callback != null) {
            String message = new String(messageEvent.getData());
            PluginResult result = new PluginResult(PluginResult.Status.OK, message);
            result.setKeepCallback(true);
            callback.sendPluginResult(result);
        }
    }
}
