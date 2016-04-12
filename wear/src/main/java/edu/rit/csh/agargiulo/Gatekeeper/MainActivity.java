package edu.rit.csh.agargiulo.Gatekeeper;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    private TextView mTextView;
    private Button mSendButton;
    private GoogleApiClient mApiClient;

    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";


    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mApiClient.connect();

    }

    private void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                Log.v("GatekeeperService", "Think we found some nodes!");

                for (Node node : nodes.getNodes()) {
                    Log.v("GatekeeperService", "Sending to node!");
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes()).await();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("GatekeeperWeareService", "Do something here.");
                    }
                });
            }
        }).start();
    }

    @Override
    public void onConnected(Bundle bundle) {
        sendMessage(START_ACTIVITY, "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
            }
        });
        initGoogleApiClient();
        sendMessage(WEAR_MESSAGE_PATH, "YUUUUUUGE");

    }

    public void popLock (View view)
    {
        ((Button) view).setBackgroundColor(Color.parseColor("#FF8080"));
        int doorId;
        switch (view.getId())
        {
            case R.id.door_4:
                doorId = 4;
                break;
            case R.id.door_5:
                doorId = 5;
                break;
            case R.id.door_6:
                doorId = 6;
                break;
            case R.id.door_7:
                doorId = 7;
                break;
            case R.id.door_8:
                doorId = 8;
                break;
            case R.id.door_9:
                doorId = 9;
                break;
            default:
                doorId = -1;
        }
        sendMessage(START_ACTIVITY, String.valueOf(doorId));
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {

        /*
        This method apparently runs in a background thread.
         */

        Log.v("GatekeeperService", "Message received on wear: " + messageEvent.getPath());
    }


    public void onTextClick(View v)
    {

        // do something
    }

    @Override
    public void onStart() {
        super.onStart();
        mApiClient.connect();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }
}
